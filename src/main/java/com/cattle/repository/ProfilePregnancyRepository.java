package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfilePregnancy;
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
public class ProfilePregnancyRepository {
    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<ProfilePregnancy> table;

    public ProfilePregnancyRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        table = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(ProfilePregnancy.class));
    }

    public Optional<ProfilePregnancy> findById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            ProfilePregnancy item = table.getItem(key);
            return Optional.ofNullable(item);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "ProfilePregnancy table not found", e);
            return Optional.empty();
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "findById error", ex);
            throw new RepositoryException("Unexpected error finding ProfilePregnancy by Id", ex);
        }
    }

    public List<ProfilePregnancy> findAll() {
        try {
            List<ProfilePregnancy> result = new ArrayList<>();
            table.scan().items().forEach(result::add);
            return result;
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding all ProfilePregnancy", ex);
            throw new RepositoryException("Unexpected error finding all ProfilePregnancy", ex);
        }
    }

    public Optional<ProfilePregnancy> save(ProfilePregnancy entity) {
        try {
            table.putItem(entity);
            return Optional.ofNullable(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving ProfilePregnancy", ex);
            throw new RepositoryException("Unexpected error saving ProfilePregnancy", ex);
        }
    }

    public Optional<ProfilePregnancy> update(ProfilePregnancy entity) {
        try {
            table.putItem(entity);
            return Optional.of(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error updating ProfilePregnancy", ex);
            throw new RepositoryException("Unexpected error updating ProfilePregnancy", ex);
        }
    }

    public void deleteById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            table.deleteItem(key);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error deleting ProfilePregnancy by Id", ex);
            throw new RepositoryException("Unexpected error deleting ProfilePregnancy by Id", ex);
        }
    }
}
