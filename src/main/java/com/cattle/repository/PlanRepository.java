package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Plan;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PlanRepository {
    private static final String TABLE_PLAN = System.getenv("TABLE_PLAN");
    private static final String PLAN_NOT_EXIST_IN_DYNAMO_DB = "Plan not exist in DynamoDB";
    private static final String GSI1_FARM_ID = "gsi1";
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<Plan> table;

    public PlanRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        this.table = enhancedClient.table(TABLE_PLAN, TableSchema.fromBean(Plan.class));
    }

    public Optional<List<Plan>> findPlans(String farmId) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Finding plans for farmId: " + farmId);
            List<Plan> plans;

            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue("farm#"+farmId)
                    .build()
            );

            Page<Plan> result = table
                    .query(r -> r.limit(20).queryConditional(queryConditional))
                    .iterator()
                    .next();
            plans = new ArrayList<>(result.items());

            return Optional.of(plans);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, PLAN_NOT_EXIST_IN_DYNAMO_DB);
            throw new RepositoryException(PLAN_NOT_EXIST_IN_DYNAMO_DB, e);
        } catch (DynamoDbException e) {
            lambdaContext.logException(LogType.REPOSITORY, "DynamoDB error: " + e.getMessage());
            throw new RepositoryException("DynamoDB error: " + e.getMessage(), e);
        } catch (Exception e) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error: " + e.getMessage());
            throw new RepositoryException("Unexpected error: " + e.getMessage(), e);
        }

    }

}
