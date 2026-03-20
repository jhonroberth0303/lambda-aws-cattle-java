package com.cattle.repository;

import com.cattle.config.LambdaContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Map;

@Repository
public class CounterRepository {
    private static final String TABLE_COUNTERS = System.getenv("TABLE_COUNTERS");
    public static final String ENTITY_NAME_FIELD = "entityName";
    private final LambdaContext lambdaContext;
    private final DynamoDbClient dynamoDbClient;

    public CounterRepository(LambdaContext lambdaContext, DynamoDbEnhancedClient enhancedClient,
                             @Qualifier ("dynamoDbClientBean") DynamoDbClient dynamoDbClient) {
        this.lambdaContext = lambdaContext;
        this.dynamoDbClient = dynamoDbClient;
    }

    public String getNextId(String entityName) {
        Map<String, AttributeValue> key = Map.of(
                ENTITY_NAME_FIELD, AttributeValue.builder().s(entityName).build()
        );

        Map<String, AttributeValue> expressionValues = Map.of(
            ":start", AttributeValue.builder().n("0").build(),
            ":inc", AttributeValue.builder().n("1").build()
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
            .tableName(TABLE_COUNTERS)
            .key(key)
            .updateExpression("SET nextId = if_not_exists(nextId, :start) + :inc")
            .expressionAttributeValues(expressionValues)
            .returnValues(ReturnValue.UPDATED_NEW)
            .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(request);
        String newId = response.attributes().get("nextId").n();

        return newId;
    }


}