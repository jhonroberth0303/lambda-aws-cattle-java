package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLactancy;
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

import java.util.*;

@Repository
public class ProfileLactancyRepository {
    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private static final String GSI1 = "GSI1";
    private static final String LACT_PREFIX = "LACT#";
    private static final String LACT_FARM_PREFIX = "LACT#FARM#";
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<ProfileLactancy> table;

    public ProfileLactancyRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        table = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(ProfileLactancy.class));
    }

    public Optional<ProfileLactancy> findById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            ProfileLactancy item = table.getItem(key);
            return Optional.ofNullable(item);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "ProfileLactancy table not found", e);
            return Optional.empty();
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "findById error", ex);
            throw new RepositoryException("Unexpected error finding ProfileLactancy by Id", ex);
        }
    }

    public Optional<List<ProfileLactancy>> findAll() {
        try {
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(LACT_PREFIX).build());

            Page<ProfileLactancy> result = table
                    .index(GSI1) // apuntamos al índice
                    .query(r -> r.limit(15).queryConditional(queryConditional)).iterator().next();

            List<ProfileLactancy> items = new ArrayList<>(result.items());

            if(items.isEmpty()){
                return Optional.empty();
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findAll: " + items.size() + " records found in table: " + TABLE_BOVINES);
            return Optional.of(items);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding lactancies", ex);
            throw new RepositoryException("Unexpected error finding lactancies", ex);
        }
    }

    public Optional<ProfileLactancy> save(ProfileLactancy entity) {
        try {
            table.putItem(entity);
            return Optional.ofNullable(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving ProfileLactancy", ex);
            throw new RepositoryException("Unexpected error saving ProfileLactancy", ex);
        }
    }

    public Optional<ProfileLactancy> update(ProfileLactancy entity) {
        try {
            table.putItem(entity);
            return Optional.of(entity);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error updating ProfileLactancy", ex);
            throw new RepositoryException("Unexpected error updating ProfileLactancy", ex);
        }
    }

    public void deleteById(String pk, String sk) {
        try {
            Key key = Key.builder().partitionValue(pk).sortValue(sk).build();
            table.deleteItem(key);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error deleting ProfileLactancy by Id", ex);
            throw new RepositoryException("Unexpected error deleting ProfileLactancy by Id", ex);
        }
    }

    /**
     * Obtiene todas las lactancias (OPEN y CLOSE) usando el GSI1.
     * Combina los resultados de ambos estados.
     */
    public Optional<List<ProfileLactancy>> findAllLactations(String siteId) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Finding all lactations (OPEN and CLOSE)");
            List<ProfileLactancy> allItems = new ArrayList<>();

            QueryConditional openQuery = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(LACT_FARM_PREFIX + siteId).build());
            table.index(GSI1)
                    .query(r -> r.queryConditional(openQuery))
                    .forEach(page -> allItems.addAll(page.items()));

            if (allItems.isEmpty()) {
                return Optional.empty();
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findAllLactations: " + allItems.size() + " records found");
            return Optional.of(allItems);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding all lactations", ex);
            throw new RepositoryException("Unexpected error finding all lactations", ex);
        }
    }

    public Optional<List<ProfileLactancy>> findAllLactationsByBovine(String pk) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Finding all lactations for bovine with PK: " + pk);
            QueryConditional queryConditional = QueryConditional.sortBeginsWith(
                    Key.builder().partitionValue(pk).sortValue(LACT_PREFIX).build());

            List<ProfileLactancy> items = new ArrayList<>();
            table.query(r -> r.queryConditional(queryConditional))
                    .forEach(page -> items.addAll(page.items()));

            if (items.isEmpty()) {
                return Optional.empty();
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findAllLactationsByBovine: " + items.size() + " lactations for " + pk);
            return Optional.of(items);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding lactations by bovine", ex);
            throw new RepositoryException("Unexpected error finding lactations by bovine", ex);
        }
    }
}
