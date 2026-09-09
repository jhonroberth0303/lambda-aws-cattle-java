package com.cattle.notifications.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.notifications.entity.NotificationItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Acceso a la tabla {@code Notifications}. Solo primitivas de persistencia; las reglas
 * de negocio viven en los servicios.
 * <pre>
 * PK = FARM#&lt;farmId&gt;
 * SK = NOTIF#&lt;notificationId&gt;   notificación
 * SK = DEDUPE#&lt;dedupeKey&gt;       centinela de deduplicación (con TTL propio)
 * </pre>
 */
@Repository
public class NotificationRepository {

    static final String DEFAULT_TABLE_NAME = "Notifications";
    private static final String DEDUPE_SK_PREFIX = "DEDUPE#";

    private final LambdaContext lambdaContext;
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbTable<NotificationItem> table;
    private final String tableName;

    public NotificationRepository(LambdaContext lambdaContext,
                                  DynamoDbEnhancedClient enhancedClient,
                                  @Qualifier("dynamoDbClientBean") DynamoDbClient dynamoDbClient) {
        this.lambdaContext = lambdaContext;
        this.dynamoDbClient = dynamoDbClient;
        String configured = System.getenv("TABLE_NOTIFICATIONS");
        this.tableName = (configured == null || configured.isBlank()) ? DEFAULT_TABLE_NAME : configured;
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(NotificationItem.class));
    }

    public void save(NotificationItem item) {
        try {
            table.putItem(item);
        } catch (ResourceNotFoundException ex) {
            throw error("Notifications table not found while saving notification", ex, item.getPk(), item.getSk());
        } catch (DynamoDbException ex) {
            throw error("DynamoDB error saving notification", ex, item.getPk(), item.getSk());
        } catch (Exception ex) {
            throw error("Unexpected error saving notification", ex, item.getPk(), item.getSk());
        }
    }

    /**
     * Reclama la clave de deduplicación para la finca escribiendo un centinela condicional.
     *
     * @return {@code true} si la reclamó (primera vez); {@code false} si ya existía
     */
    public boolean tryClaimDedupe(String farmId, String dedupeKey, long ttlEpochSeconds) {
        String pk = NotificationItem.partitionKey(farmId);
        String sk = DEDUPE_SK_PREFIX + dedupeKey;
        Map<String, AttributeValue> item = Map.of(
                "pk", AttributeValue.fromS(pk),
                "sk", AttributeValue.fromS(sk),
                "dedupeKey", AttributeValue.fromS(dedupeKey),
                "createdAt", AttributeValue.fromS(Instant.now().toString()),
                "ttl", AttributeValue.fromN(Long.toString(ttlEpochSeconds))
        );
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(pk)")
                .build();
        try {
            dynamoDbClient.putItem(request);
            return true;
        } catch (ConditionalCheckFailedException ex) {
            return false;
        } catch (DynamoDbException ex) {
            throw error("DynamoDB error claiming dedupe key", ex, pk, sk);
        } catch (Exception ex) {
            throw error("Unexpected error claiming dedupe key", ex, pk, sk);
        }
    }

    public List<NotificationItem> findByFarm(String farmId, int limit) {
        return query(farmId, limit, null);
    }

    public List<NotificationItem> findUnreadByFarm(String farmId, int limit) {
        Expression onlyUnread = Expression.builder()
                .expression("#status = :unread")
                .putExpressionName("#status", "status")
                .putExpressionValue(":unread", AttributeValue.fromS("UNREAD"))
                .build();
        return query(farmId, limit, onlyUnread);
    }

    public Optional<NotificationItem> findById(String farmId, String notificationId) {
        String pk = NotificationItem.partitionKey(farmId);
        String sk = NotificationItem.sortKey(notificationId);
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            return Optional.ofNullable(table.getItem(r -> r.key(key)));
        } catch (ResourceNotFoundException ex) {
            throw error("Notifications table not found while reading notification", ex, pk, sk);
        } catch (DynamoDbException ex) {
            throw error("DynamoDB error reading notification", ex, pk, sk);
        } catch (Exception ex) {
            throw error("Unexpected error reading notification", ex, pk, sk);
        }
    }

    private List<NotificationItem> query(String farmId, int limit, Expression filter) {
        String pk = NotificationItem.partitionKey(farmId);
        try {
            QueryConditional conditional = QueryConditional.sortBeginsWith(Key.builder()
                    .partitionValue(pk)
                    .sortValue(NotificationItem.SK_PREFIX)
                    .build());
            QueryEnhancedRequest.Builder request = QueryEnhancedRequest.builder()
                    .queryConditional(conditional)
                    .scanIndexForward(false)
                    .limit(limit);
            if (filter != null) {
                request.filterExpression(filter);
            }
            return StreamSupport.stream(table.query(request.build()).items().spliterator(), false)
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException ex) {
            throw error("Notifications table not found while querying notifications", ex, pk, NotificationItem.SK_PREFIX);
        } catch (DynamoDbException ex) {
            throw error("DynamoDB error querying notifications", ex, pk, NotificationItem.SK_PREFIX);
        } catch (Exception ex) {
            throw error("Unexpected error querying notifications", ex, pk, NotificationItem.SK_PREFIX);
        }
    }

    private RepositoryException error(String message, Exception cause, String pk, String sk) {
        String detail = message + ". table=" + tableName + ", pk=" + pk + ", sk=" + sk;
        lambdaContext.logException(LogType.REPOSITORY, detail, cause);
        return new RepositoryException(detail, cause);
    }
}
