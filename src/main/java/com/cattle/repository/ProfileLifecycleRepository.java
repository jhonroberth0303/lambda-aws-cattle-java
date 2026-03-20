package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.*;

@Repository
public class ProfileLifecycleRepository {
    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<ProfileLifecycle> table;

    public ProfileLifecycleRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        table = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(ProfileLifecycle.class));
    }

    public Optional<ProfileLifecycle> findById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            ProfileLifecycle item = table.getItem(key);
            return Optional.ofNullable(item);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "ProfileLifecycle table not found", e);
            return Optional.empty();
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "findById error", ex);
            throw new RepositoryException("Unexpected error finding ProfileLifecycle by Id", ex);
        }
    }

    public List<ProfileLifecycle> findAll() {
        try {
            List<ProfileLifecycle> result = new ArrayList<>();
            table.scan().items().forEach(result::add);
            return result;
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding all ProfileLifecycle", ex);
            throw new RepositoryException("Unexpected error finding all ProfileLifecycle", ex);
        }
    }

    public Optional<ProfileLifecycle> save(ProfileLifecycle entity) {
        try {
            table.putItem(entity);
            return Optional.ofNullable(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving ProfileLifecycle", ex);
            throw new RepositoryException("Unexpected error saving ProfileLifecycle", ex);
        }
    }

    public Optional<ProfileLifecycle> update(ProfileLifecycle entity) {
        try {
            table.putItem(entity);
            return Optional.of(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error updating ProfileLifecycle", ex);
            throw new RepositoryException("Unexpected error updating ProfileLifecycle", ex);
        }
    }

    public void deleteById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            table.deleteItem(key);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error deleting ProfileLifecycle by Id", ex);
            throw new RepositoryException("Unexpected error deleting ProfileLifecycle by Id", ex);
        }
    }
}

