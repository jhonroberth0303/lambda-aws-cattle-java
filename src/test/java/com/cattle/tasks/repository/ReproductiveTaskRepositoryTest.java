package com.cattle.tasks.repository;

import com.cattle.config.LambdaContext;
import com.cattle.config.TablesConfig;
import com.cattle.exceptions.RepositoryException;
import com.cattle.tasks.entity.ReproductiveTaskItem;
import com.cattle.tasks.entity.ServiceFollowUpItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("repository")
@DisplayName("ReproductiveTaskRepository Tests")
class ReproductiveTaskRepositoryTest {

    @Mock private LambdaContext lambdaContext;
    @Mock private DynamoDbEnhancedClient enhancedClient;
    @Mock private DynamoDbTable<ReproductiveTaskItem> taskTable;
    @Mock private DynamoDbTable<ServiceFollowUpItem> followUpTable;
    @Mock private DynamoDbIndex<ReproductiveTaskItem> taskIndex;
    @Mock private DynamoDbIndex<ServiceFollowUpItem> followUpIndex;
    @Mock private PageIterable<ReproductiveTaskItem> taskPages;

    private ReproductiveTaskRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        openMocks(this);
        // El constructor crea primero la tabla de tareas y luego la de seguimiento.
        when(enhancedClient.table(any(), any(TableSchema.class)))
                .thenReturn((DynamoDbTable) taskTable, (DynamoDbTable) followUpTable);
        when(taskTable.index(ReproductiveTaskItem.GSI1)).thenReturn(taskIndex);
        when(followUpTable.index(ReproductiveTaskItem.GSI1)).thenReturn(followUpIndex);
        repository = new ReproductiveTaskRepository(lambdaContext, enhancedClient, new TablesConfig());
    }

    private static ReproductiveTaskItem task(String sk) {
        ReproductiveTaskItem item = new ReproductiveTaskItem();
        item.setPk("BOVINE#7");
        item.setSk(sk);
        return item;
    }

    @Test
    @DisplayName("Usa tasks-prod cuando TABLE_TASKS no está configurada (una vista por entidad)")
    void defaultTableName_isTasksProd() {
        verify(enhancedClient, times(2)).table(eq("tasks-prod"), any(TableSchema.class));
    }

    @Test
    @DisplayName("createIfAbsent escribe con condición attribute_not_exists(pk)")
    @SuppressWarnings("unchecked")
    void createIfAbsent_usesConditionalPut() {
        ReproductiveTaskItem item = task("TASK#R1_DIAGNOSIS#S1");

        assertThat(repository.createIfAbsent(item)).isTrue();

        ArgumentCaptor<PutItemEnhancedRequest<ReproductiveTaskItem>> request =
                ArgumentCaptor.forClass(PutItemEnhancedRequest.class);
        verify(taskTable).putItem(request.capture());
        assertThat(request.getValue().item()).isSameAs(item);
        assertThat(request.getValue().conditionExpression().expression()).isEqualTo("attribute_not_exists(pk)");
    }

    @Test
    @DisplayName("createIfAbsent devuelve false si la tarea ya existe (G1)")
    @SuppressWarnings("unchecked")
    void createIfAbsent_existing_returnsFalse() {
        doThrow(ConditionalCheckFailedException.builder().message("exists").build())
                .when(taskTable).putItem(any(PutItemEnhancedRequest.class));

        assertThat(repository.createIfAbsent(task("TASK#R1_DIAGNOSIS#S1"))).isFalse();
    }

    @Test
    @DisplayName("H3: saveIfStatus condiciona el reemplazo al estado leído")
    @SuppressWarnings("unchecked")
    void saveIfStatus_usesOptimisticCondition() {
        ReproductiveTaskItem item = task("TASK#R1_DIAGNOSIS#S1");

        assertThat(repository.saveIfStatus(item, "PENDIENTE")).isTrue();

        ArgumentCaptor<PutItemEnhancedRequest<ReproductiveTaskItem>> request =
                ArgumentCaptor.forClass(PutItemEnhancedRequest.class);
        verify(taskTable).putItem(request.capture());
        assertThat(request.getValue().item()).isSameAs(item);
        assertThat(request.getValue().conditionExpression().expression()).isEqualTo("#status = :expected");
        assertThat(request.getValue().conditionExpression().expressionNames()).containsEntry("#status", "status");
        assertThat(request.getValue().conditionExpression().expressionValues().get(":expected").s())
                .isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("H3: saveIfStatus devuelve false si el estado ya cambió")
    @SuppressWarnings("unchecked")
    void saveIfStatus_conflict_returnsFalse() {
        doThrow(ConditionalCheckFailedException.builder().message("changed").build())
                .when(taskTable).putItem(any(PutItemEnhancedRequest.class));

        assertThat(repository.saveIfStatus(task("TASK#R1_DIAGNOSIS#S1"), "PENDIENTE")).isFalse();
    }

    @Test
    @DisplayName("Un error de DynamoDB se envuelve en RepositoryException con contexto")
    @SuppressWarnings("unchecked")
    void dynamoError_isWrapped() {
        ReproductiveTaskItem item = task("TASK#R1_DIAGNOSIS#S1");
        doThrow(DynamoDbException.builder().message("boom").build())
                .when(taskTable).putItem(any(PutItemEnhancedRequest.class));

        assertThatThrownBy(() -> repository.saveIfStatus(item, "PENDIENTE"))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining("saving task")
                .hasMessageContaining("table=tasks-prod");
    }

    @Test
    @DisplayName("findTasksByBovine devuelve los ítems de la consulta por partición")
    @SuppressWarnings("unchecked")
    void findTasksByBovine_returnsItems() {
        ReproductiveTaskItem item = task("TASK#R1_DIAGNOSIS#S1");
        when(taskTable.query(any(Consumer.class))).thenReturn(taskPages);
        SdkIterable<ReproductiveTaskItem> items = () -> List.of(item).iterator();
        when(taskPages.items()).thenReturn(items);

        assertThat(repository.findTasksByBovine("7")).containsExactly(item);
    }

    @Test
    @DisplayName("findOpenTasksByFarm aplana las páginas del índice de abiertas")
    @SuppressWarnings("unchecked")
    void findOpenTasksByFarm_flattensPages() {
        ReproductiveTaskItem a = task("TASK#A");
        ReproductiveTaskItem b = task("TASK#B");
        SdkIterable<Page<ReproductiveTaskItem>> pages =
                () -> List.of(Page.create(List.of(a)), Page.create(List.of(b))).iterator();
        when(taskIndex.query(any(Consumer.class))).thenReturn(pages);

        assertThat(repository.findOpenTasksByFarm("F001", LocalDate.parse("2026-10-05"))).containsExactly(a, b);
    }

    @Test
    @DisplayName("findFollowUp lee el ítem FOLLOWUP#SERVICE del bovino")
    void findFollowUp_readsByKey() {
        ServiceFollowUpItem item = new ServiceFollowUpItem();
        when(followUpTable.getItem(any(Key.class))).thenReturn(item);

        assertThat(repository.findFollowUp("7")).containsSame(item);
        ArgumentCaptor<Key> key = ArgumentCaptor.forClass(Key.class);
        verify(followUpTable).getItem(key.capture());
        assertThat(key.getValue().partitionKeyValue().s()).isEqualTo("BOVINE#7");
        assertThat(key.getValue().sortKeyValue()).get().extracting(v -> v.s()).isEqualTo("FOLLOWUP#SERVICE");
    }

    @Test
    @DisplayName("findOpenFollowUps aplana las páginas del índice")
    @SuppressWarnings("unchecked")
    void findOpenFollowUps_flattensPages() {
        ServiceFollowUpItem item = new ServiceFollowUpItem();
        SdkIterable<Page<ServiceFollowUpItem>> pages = () -> List.of(Page.create(List.of(item))).iterator();
        when(followUpIndex.query(any(Consumer.class))).thenReturn(pages);

        assertThat(repository.findOpenFollowUps("F001")).containsExactly(item);
    }

    @Test
    @DisplayName("El índice disperso: una tarea cerrada no tiene claves GSI1")
    void closedTask_leavesOpenIndex() {
        ReproductiveTaskItem item = task("TASK#R1_DIAGNOSIS#S1");
        item.setFarmId("F001");
        item.setBovineId("7");
        item.setDueDate("2026-11-10");
        item.setRuleCode("R1_DIAGNOSIS");
        item.applyOpenIndex(true);
        assertThat(item.getGsi1pk()).isEqualTo("FARM#F001#OPEN");

        item.applyOpenIndex(false);
        assertThat(item.getGsi1pk()).isNull();
        assertThat(item.getGsi1sk()).isNull();
    }
}
