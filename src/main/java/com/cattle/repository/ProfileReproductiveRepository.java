package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileReproductive;
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
public class ProfileReproductiveRepository {
    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<ProfileReproductive> table;

    public ProfileReproductiveRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        table = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(ProfileReproductive.class));
    }

    public Optional<ProfileReproductive> findById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            ProfileReproductive item = table.getItem(key);
            return Optional.ofNullable(item);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "ProfileReproductive table not found", e);
            return Optional.empty();
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "findById error", ex);
            throw new RepositoryException("Unexpected error finding ProfileReproductive by Id", ex);
        }
    }

    public List<ProfileReproductive> findAll() {
        try {
            List<ProfileReproductive> result = new ArrayList<>();
            table.scan().items().forEach(result::add);
            return result;
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding all ProfileReproductive", ex);
            throw new RepositoryException("Unexpected error finding all ProfileReproductive", ex);
        }
    }

    public Optional<ProfileReproductive> save(ProfileReproductive entity) {
        try {
            table.putItem(entity);
            return Optional.ofNullable(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving ProfileReproductive", ex);
            throw new RepositoryException("Unexpected error saving ProfileReproductive", ex);
        }
    }

    public Optional<ProfileReproductive> update(ProfileReproductive entity) {
        try {
            table.putItem(entity);
            return Optional.of(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error updating ProfileReproductive", ex);
            throw new RepositoryException("Unexpected error updating ProfileReproductive", ex);
        }
    }

    public void deleteById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            table.deleteItem(key);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error deleting ProfileReproductive by Id", ex);
            throw new RepositoryException("Unexpected error deleting ProfileReproductive by Id", ex);
        }
    }
}
