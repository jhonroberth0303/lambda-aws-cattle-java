package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
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
public class BovineEventRepository {

    private static final String DEFAULT_TABLE_EVENTS = "Events";
    private static final String TABLE_EVENTS = System.getenv("TABLE_EVENTS");

    private final DynamoDbTable<BovineEventItem> table;
    private final LambdaContext lambdaContext;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public BovineEventRepository(LambdaContext lambdaContext, DynamoDbEnhancedClient enhancedClient, ObjectMapper objectMapper) {
        this.lambdaContext = lambdaContext;
        this.tableName = TABLE_EVENTS == null || TABLE_EVENTS.isBlank() ? DEFAULT_TABLE_EVENTS : TABLE_EVENTS;
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(BovineEventItem.class));
        this.objectMapper = objectMapper;
    }

    public void save(BovineEventItem item) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Saving bovine event into table " + tableName + ": " + toJson(item));
            table.putItem(item);
        } catch (ResourceNotFoundException ex) {
            String message = "Events table not found while saving bovine event. table=" + tableName
                    + ", pk=" + item.getPk() + ", sk=" + item.getSk();
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (DynamoDbException ex) {
            String message = "DynamoDB error saving bovine event. table=" + tableName
                    + ", pk=" + item.getPk() + ", sk=" + item.getSk();
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (Exception ex) {
            String message = "Unexpected error saving bovine event. table=" + tableName
                    + ", pk=" + item.getPk() + ", sk=" + item.getSk();
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        }
    }

    public List<BovineEventItem> findByBovine(String bovineId, int limit) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Finding events for bovineId: " + bovineId + " limit: " + limit);
            QueryConditional queryConditional = QueryConditional.sortBeginsWith(
                    Key.builder()
                            .partitionValue("BOVINE#" + bovineId)
                            .sortValue("EVT#")
                            .build()
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
            String message = "Events table not found while querying bovine events. bovineId=" + bovineId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (DynamoDbException ex) {
            String message = "DynamoDB error querying bovine events. bovineId=" + bovineId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        } catch (Exception ex) {
            String message = "Unexpected error querying bovine events. bovineId=" + bovineId;
            lambdaContext.logException(LogType.REPOSITORY, message, ex);
            throw new RepositoryException(message, ex);
        }
    }

    private String toJson(BovineEventItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException ex) {
            return String.valueOf(item);
        }
    }
}
