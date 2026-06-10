package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class PastureStatisticsRepository {
    private static final String DEFAULT_TABLE_EVENTS = "Events";
    private static final String TABLE_EVENTS = System.getenv("TABLE_EVENTS");
    private static final int MAX_EVENTS_PER_QUERY = 500;

    private final DynamoDbTable<PastureEventItem> table;
    private final LambdaContext lambdaContext;
    private final String tableName;

    public PastureStatisticsRepository(LambdaContext lambdaContext, DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        this.tableName = TABLE_EVENTS == null || TABLE_EVENTS.isBlank() ? DEFAULT_TABLE_EVENTS : TABLE_EVENTS;
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(PastureEventItem.class));
    }

    public List<PastureEventItem> findByPastureInRange(String farmId, String pastureId, LocalDate from, LocalDate to) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY,
                    "Querying events for pastureId=" + pastureId + " from=" + from + " to=" + to);

            Key lower = Key.builder()
                    .partitionValue("PASTURE#" + pastureId)
                    .sortValue("EVT#" + from.toString())
                    .build();
            Key upper = Key.builder()
                    .partitionValue("PASTURE#" + pastureId)
                    .sortValue("EVT#" + to.plusDays(1).toString())
                    .build();

            QueryConditional condition = QueryConditional.sortBetween(lower, upper);

            return StreamSupport.stream(
                            table.query(r -> r
                                    .queryConditional(condition)
                                    .scanIndexForward(true))
                                    .items()
                                    .spliterator(), false)
                    .filter(item -> farmId.equals(item.getFarmId()))
                    .limit(MAX_EVENTS_PER_QUERY)
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException ex) {
            String message = "Events table not found while querying statistics. pastureId=" + pastureId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (DynamoDbException ex) {
            String message = "DynamoDB error querying statistics. pastureId=" + pastureId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (Exception ex) {
            String message = "Unexpected error querying statistics. pastureId=" + pastureId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        }
    }
}
