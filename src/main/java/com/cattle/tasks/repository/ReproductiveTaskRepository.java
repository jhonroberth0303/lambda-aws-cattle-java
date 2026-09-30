package com.cattle.tasks.repository;

import com.cattle.config.LambdaContext;
import com.cattle.config.TablesConfig;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.tasks.entity.ReproductiveTaskItem;
import com.cattle.tasks.entity.ServiceFollowUpItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Acceso a la tabla de la agenda ({@code tasks-prod}). Solo primitivas de persistencia.
 * Tareas y seguimiento comparten tabla y partición del bovino; se distinguen por el SK.
 */
@Repository
public class ReproductiveTaskRepository {

    static final String DEFAULT_TABLE_NAME = "tasks-prod";
    /** Mayor que cualquier sufijo "#..." del GSI1SK: incluye todo el día {@code upTo}. */
    private static final String END_OF_DAY_SUFFIX = "#￿";

    private final LambdaContext lambdaContext;
    private final DynamoDbTable<ReproductiveTaskItem> taskTable;
    private final DynamoDbTable<ServiceFollowUpItem> followUpTable;
    private final String tableName;

    public ReproductiveTaskRepository(LambdaContext lambdaContext, DynamoDbEnhancedClient enhancedClient,
                                      TablesConfig tablesConfig) {
        this.lambdaContext = lambdaContext;
        String configured = tablesConfig.getTasks();
        this.tableName = (configured == null || configured.isBlank()) ? DEFAULT_TABLE_NAME : configured;
        this.taskTable = enhancedClient.table(tableName, TableSchema.fromBean(ReproductiveTaskItem.class));
        this.followUpTable = enhancedClient.table(tableName, TableSchema.fromBean(ServiceFollowUpItem.class));
    }

    /** Todas las tareas del bovino, en cualquier estado. */
    public List<ReproductiveTaskItem> findTasksByBovine(String bovineId) {
        String pk = ReproductiveTaskItem.partitionKey(bovineId);
        return execute("querying tasks by bovine", pk, () -> taskTable.query(r -> r.queryConditional(
                        QueryConditional.sortBeginsWith(Key.builder()
                                .partitionValue(pk)
                                .sortValue(ReproductiveTaskItem.SK_PREFIX)
                                .build())))
                .items().stream().toList());
    }

    /**
     * Crea la tarea solo si no existe (G1). Una carrera con otra sincronización del mismo
     * evento no duplica: la segunda escritura falla la condición y se ignora.
     *
     * @return {@code true} si la creó
     */
    public boolean createIfAbsent(ReproductiveTaskItem item) {
        return execute("creating task", item.getPk() + "|" + item.getSk(), () -> {
            try {
                taskTable.putItem(PutItemEnhancedRequest.builder(ReproductiveTaskItem.class)
                        .item(item)
                        .conditionExpression(Expression.builder().expression("attribute_not_exists(pk)").build())
                        .build());
                return true;
            } catch (ConditionalCheckFailedException ex) {
                return false;
            }
        });
    }

    /**
     * Reemplaza la tarea completa solo si su estado guardado sigue siendo {@code expectedStatus}
     * (control optimista, revisión H3): una sincronización concurrente que ya la cambió gana, y
     * esta escritura, calculada sobre una lectura vieja, se descarta en vez de pisarla. Un GSI1
     * nulo la retira del índice de abiertas.
     *
     * @return {@code true} si la guardó; {@code false} si el estado ya había cambiado
     */
    public boolean saveIfStatus(ReproductiveTaskItem item, String expectedStatus) {
        return execute("saving task", item.getPk() + "|" + item.getSk(), () -> {
            try {
                taskTable.putItem(PutItemEnhancedRequest.builder(ReproductiveTaskItem.class)
                        .item(item)
                        .conditionExpression(Expression.builder()
                                .expression("#status = :expected")
                                .putExpressionName("#status", "status")
                                .putExpressionValue(":expected", AttributeValue.fromS(expectedStatus))
                                .build())
                        .build());
                return true;
            } catch (ConditionalCheckFailedException ex) {
                return false;
            }
        });
    }

    /** Tareas abiertas (PENDIENTE/VENCIDA) de la finca con vencimiento hasta {@code upTo}, por fecha ascendente. */
    public List<ReproductiveTaskItem> findOpenTasksByFarm(String farmId, LocalDate upTo) {
        String gsiPk = ReproductiveTaskItem.openIndexKey(farmId);
        return execute("querying open tasks by farm", gsiPk, () -> taskTable.index(ReproductiveTaskItem.GSI1)
                .query(r -> r.queryConditional(QueryConditional.sortLessThanOrEqualTo(Key.builder()
                        .partitionValue(gsiPk)
                        .sortValue(upTo + END_OF_DAY_SUFFIX)
                        .build())))
                .stream()
                .flatMap(page -> page.items().stream())
                .toList());
    }

    public Optional<ServiceFollowUpItem> findFollowUp(String bovineId) {
        String pk = ReproductiveTaskItem.partitionKey(bovineId);
        return execute("reading follow-up", pk, () -> Optional.ofNullable(followUpTable.getItem(Key.builder()
                .partitionValue(pk)
                .sortValue(ServiceFollowUpItem.SORT_KEY)
                .build())));
    }

    public void saveFollowUp(ServiceFollowUpItem item) {
        execute("saving follow-up", item.getPk(), () -> {
            followUpTable.putItem(item);
            return null;
        });
    }

    /** Seguimientos abiertos de la finca, del servicio más antiguo al más reciente. */
    public List<ServiceFollowUpItem> findOpenFollowUps(String farmId) {
        String gsiPk = ServiceFollowUpItem.openIndexKey(farmId);
        return execute("querying open follow-ups", gsiPk, () -> followUpTable.index(ReproductiveTaskItem.GSI1)
                .query(r -> r.queryConditional(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue(gsiPk)
                        .build())))
                .stream()
                .flatMap(page -> page.items().stream())
                .toList());
    }

    private <T> T execute(String action, String key, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (Exception ex) {
            String detail = "DynamoDB error " + action + ". table=" + tableName + ", key=" + key;
            lambdaContext.logException(LogType.REPOSITORY, detail, ex);
            throw new RepositoryException(detail, ex);
        }
    }
}
