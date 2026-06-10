package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.exceptions.RepositoryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class PastureEventRepository {
    private static final String DEFAULT_TABLE_EVENTS = "Events";
    private static final String TABLE_EVENTS = System.getenv("TABLE_EVENTS");

    private final DynamoDbTable<PastureEventItem> table;
    private final LambdaContext lambdaContext;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public PastureEventRepository(LambdaContext lambdaContext, DynamoDbEnhancedClient enhancedClient, ObjectMapper objectMapper) {
        this.lambdaContext = lambdaContext;
        this.tableName = TABLE_EVENTS == null || TABLE_EVENTS.isBlank() ? DEFAULT_TABLE_EVENTS : TABLE_EVENTS;
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(PastureEventItem.class));
        this.objectMapper = objectMapper;
    }

    public void save(PastureEventItem item) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Saving pasture event into table " + tableName + ": " + toJson(item));
            table.putItem(item);
        } catch (ResourceNotFoundException ex) {
            String message = "Events table not found while saving pasture event. table=" + tableName
                    + ", pk=" + item.getPk() + ", sk=" + item.getSk();
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (DynamoDbException ex) {
            String message = "DynamoDB error saving pasture event. table=" + tableName
                    + ", pk=" + item.getPk() + ", sk=" + item.getSk();
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (Exception ex) {
            String message = "Unexpected error saving pasture event. table=" + tableName
                    + ", pk=" + item.getPk() + ", sk=" + item.getSk();
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        }
    }

    public List<PastureEventItem> findByPasture(String pastureId, int limit) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Finding events for pastureId: " + pastureId + " limit: " + limit);
            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue("PASTURE#" + pastureId).build()
            );
            return StreamSupport.stream(
                            table.query(r -> r
                                    .limit(limit)
                                    .queryConditional(queryConditional)
                                    .scanIndexForward(false))
                                    .items()
                                    .spliterator(), false)
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException ex) {
            String message = "Events table not found while querying pasture events. pastureId=" + pastureId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (DynamoDbException ex) {
            String message = "DynamoDB error querying pasture events. pastureId=" + pastureId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (Exception ex) {
            String message = "Unexpected error querying pasture events. pastureId=" + pastureId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        }
    }

    private String toJson(PastureEventItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException ex) {
            return String.valueOf(item);
        }
    }

}