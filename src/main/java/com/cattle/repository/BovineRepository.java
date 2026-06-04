package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.commons.MessageDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.BovineSummary;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.*;

@Repository
public class BovineRepository {

    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private static final String BOVINE_NOT_EXIST_IN_DYNAMO_DB = "Bovine not exist in DynamoDB";
    private static final String STATUS_SUCCESS = "200";
    private static final String BOVINE_WAS_DELETE_SUCCESSFULLY = "Bovine was delete successfully: ";
    private static final String IDENTITY = "IDENTITY";
    private static final String GSI1_BOVINES = "gsi1";
    private static final String PREFIX_BOVINE = "BOVINE#";
    private static final String SUMMARY = "SUMMARY";
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<BovineIdentityItem> table;
    private final DynamoDbTable<BovineSummary> summaryTable;

    public BovineRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        table = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(BovineIdentityItem.class));
        summaryTable = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(BovineSummary.class));
    }

    public Optional<List<BovineIdentityItem>> findAll() {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Received request to find all bovines");
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(IDENTITY)
                    .build());

            Page<BovineIdentityItem> result = table
                    .index(GSI1_BOVINES) // apuntamos al índice
                    .query(r -> r.limit(15).queryConditional(queryConditional))
                    .iterator().next();

            List<BovineIdentityItem> bovineIdentityItems = new ArrayList<>(result.items());

            if(bovineIdentityItems.isEmpty()){
                return Optional.empty();
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findAll: " + bovineIdentityItems.size() + " records found in table: " + TABLE_BOVINES);
            return Optional.of(bovineIdentityItems);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding bovines", ex);
            throw new RepositoryException("Unexpected error finding bovines", ex);
        }
    }


    public Optional<BovineIdentityItem> findById(Integer id) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Received request to find bovine with ID: " + id);
            List<BovineIdentityItem> list = new ArrayList<>();
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(PREFIX_BOVINE + id)
                    .sortValue(IDENTITY)
                    .build());

            table.query(r -> r.queryConditional(queryConditional)).items().forEach(list::add);

            if (list.isEmpty()) {
                return Optional.empty();
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findById: " + list.getFirst().getBovineId() + " in table: " + TABLE_BOVINES);
            return Optional.ofNullable(list.getFirst());

        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "Bovines table not found", e);
            return Optional.empty();
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "findById bovines error", ex);
            throw new RepositoryException("Unexpected error finding bovines by Id", ex);
        }
    }

    public Optional<BovineIdentityItem> save(BovineIdentityItem bovineIdentityItem) throws RepositoryException {
        try {
            table.putItem(bovineIdentityItem);
            return Optional.ofNullable(bovineIdentityItem);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error saving bovines", ex);
            throw new RepositoryException("Unexpected error saving bovines", ex);
        }
    }

    public Optional<BovineIdentityItem> update(BovineIdentityItem bovineIdentityItem) throws RepositoryException {
        try {
            table.putItem(bovineIdentityItem);
            return Optional.of(bovineIdentityItem);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error updating bovines by Id", ex);
            throw new RepositoryException("Unexpected error updating bovines by Id", ex);
        }
    }

    public MessageDTO deleteById(Long id) {
        try {
//          var key = Key.builder().partitionValue(id).build();
//          getTable().deleteItem(r -> r.key(key));
            BovineIdentityItem bovineIdentityItem = table.getItem(Key.builder().partitionValue(id).build());
            if (bovineIdentityItem == null) {
                return new MessageDTO(BOVINE_NOT_EXIST_IN_DYNAMO_DB, STATUS_SUCCESS);
            }
            table.deleteItem(Key.builder().partitionValue(id).build());
            return new MessageDTO(BOVINE_WAS_DELETE_SUCCESSFULLY + id, STATUS_SUCCESS);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error deleting bovines by Id", ex);
            throw new RepositoryException("Unexpected error deleting bovines by Id", ex);
        }
    }

    public List<BovineIdentityItem> findByGender(String gender) throws RepositoryException {

        try {
            List<BovineIdentityItem> result = new ArrayList<>();
            AttributeValue attVal = AttributeValue.builder().s(gender).build();
            // Get only items in the Employee table that match the date
            Map<String, AttributeValue> myMap = new HashMap<>();
            myMap.put(":val1", attVal);

            Map<String, String> myExMap = new HashMap<>();
            myExMap.put("#gender", "gender");
            Expression expression = Expression.builder()
                    .expressionValues(myMap)
                    .expressionNames(myExMap)
                    .expression("#gender = :val1")
                    .build();

            ScanEnhancedRequest enhancedRequest = ScanEnhancedRequest.builder()
                    .filterExpression(expression)
                    .limit(15)
                    .build();

            Iterator<BovineIdentityItem> bovinesIterator = table.scan(enhancedRequest).items().iterator();

            while (bovinesIterator.hasNext()) {
                BovineIdentityItem bovineIdentityItem = bovinesIterator.next();
                result.add(bovineIdentityItem);
            }

            return result;

        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error querying bovines by gender", ex);
            throw new RepositoryException("Unexpected error querying bovines by gender", ex);
        }
    }
    
    // ====== MÉTODOS ADICIONALES PARA CHATBOT ======

    /**
     * Cuenta todos los bovinos de una finca usando GSI1
     */
    public Long countByFarmId(String farmId) throws RepositoryException {
        try {

            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(IDENTITY)
                    .build());

            long count = 0;
            var iterator = table.index(GSI1_BOVINES).query(r -> r.queryConditional(queryConditional)).iterator();

            while (iterator.hasNext()) {
                Page<BovineIdentityItem> page = iterator.next();
                count += page.items().stream()
                        .filter(b -> farmId != null && farmId.equals(b.getFarmId()))
                        .count();
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "countByFarmId: " + count + " bovines found for farmId: " + farmId);
            return count;
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error counting bovines by farmId", ex);
            throw new RepositoryException("Unexpected error counting bovines by farmId", ex);
        }
    }

    /**
     * Encuentra bovinos por finca y categoría
     */
    public List<BovineIdentityItem> findByFarmIdAndCategory(String farmId, String category) throws RepositoryException {
        try {
            List<BovineIdentityItem> result = new ArrayList<>();
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(IDENTITY)
                    .build());

            var iterator = table.index(GSI1_BOVINES)
                    .query(r -> r.queryConditional(queryConditional))
                    .iterator();

            while (iterator.hasNext()) {
                Page<BovineIdentityItem> page = iterator.next();
                page.items().stream()
                        .filter(b -> matchesFarmId(b, farmId) && matchesCategory(b, category))
                        .forEach(result::add);
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findByFarmIdAndCategory: " + result.size() + " bovines found");
            return result;
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding bovines by farmId and category", ex);
            throw new RepositoryException("Unexpected error finding bovines by farmId and category", ex);
        }
    }

    /**
     * Encuentra bovinos por finca y género
     */
    public List<BovineIdentityItem> findByFarmIdAndGender(String farmId, String gender) throws RepositoryException {
        try {
            List<BovineIdentityItem> result = new ArrayList<>();
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(IDENTITY)
                    .build());

            var iterator = table.index(GSI1_BOVINES).query(r -> r.queryConditional(queryConditional)).iterator();

            while (iterator.hasNext()) {
                Page<BovineIdentityItem> page = iterator.next();
                page.items().stream()
                        .filter(b -> farmId != null && farmId.equals(b.getFarmId()) &&
                                   gender != null && gender.equals(b.getGender()))
                        .forEach(result::add);
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findByFarmIdAndGender: " + result.size() + " bovines found");
            return result;
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding bovines by farmId and gender", ex);
            throw new RepositoryException("Unexpected error finding bovines by farmId and gender", ex);
        }
    }

    /**
     * Encuentra bovinos por finca y estado (status)
     */
    public List<BovineIdentityItem> findByFarmIdAndStatus(String farmId, String status) throws RepositoryException {
        try {
            List<BovineIdentityItem> result = new ArrayList<>();
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(IDENTITY)
                    .build());

            var iterator = table.index(GSI1_BOVINES).query(r -> r.queryConditional(queryConditional)).iterator();

            while (iterator.hasNext()) {
                Page<BovineIdentityItem> page = iterator.next();
                page.items().stream()
                        .filter(b -> matchesFarmId(b, farmId) && matchesStatus(b, status))
                        .forEach(result::add);
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findByFarmIdAndStatus: " + result.size() + " bovines found");
            return result;
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding bovines by farmId and status", ex);
            throw new RepositoryException("Unexpected error finding bovines by farmId and status", ex);
        }
    }

    /**
     * Encuentra todos los bovinos de una finca
     */
    public List<BovineIdentityItem> findAllByFarmId(String farmId) throws RepositoryException {
        try {
            List<BovineIdentityItem> result = new ArrayList<>();
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(IDENTITY)
                    .build());

            var iterator = table.index(GSI1_BOVINES).query(r -> r.queryConditional(queryConditional)).iterator();

            while (iterator.hasNext()) {
                Page<BovineIdentityItem> page = iterator.next();
                page.items().stream()
                        .filter(b -> farmId != null && farmId.equals(b.getFarmId()))
                        .forEach(result::add);
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "findAllByFarmId: " + result.size() + " bovines found for farmId: " + farmId);
            return result;
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding all bovines by farmId", ex);
            throw new RepositoryException("Unexpected error finding all bovines by farmId", ex);
        }
    }

    private boolean matchesFarmId(BovineIdentityItem bovine, String farmId) {
        return farmId != null && farmId.equals(bovine.getFarmId());
    }

    private boolean matchesCategory(BovineIdentityItem bovine, String category) {
        if (category == null) {
            return false;
        }

        return loadSummary(bovine.getBovineId())
                .map(BovineSummary::getCategory)
                .filter(summaryCategory -> summaryCategory.equalsIgnoreCase(category))
                .isPresent();
    }

    private boolean matchesStatus(BovineIdentityItem bovine, String requestedStatus) {
        if (requestedStatus == null) {
            return false;
        }

        return loadSummary(bovine.getBovineId())
                .map(summary -> matchesStatus(summary, requestedStatus))
                .orElse(false);
    }

    private boolean matchesStatus(BovineSummary summary, String requestedStatus) {
        if (equalsIgnoreCase(summary.getStatus(), requestedStatus)
                || equalsIgnoreCase(summary.getReproductiveState(), requestedStatus)
                || equalsIgnoreCase(summary.getProductiveState(), requestedStatus)
                || equalsIgnoreCase(summary.getLactationStatus(), requestedStatus)
                || equalsIgnoreCase(summary.getPregnancyStatus(), requestedStatus)) {
            return true;
        }

        if ("PREGNANT".equalsIgnoreCase(requestedStatus)) {
            return Boolean.TRUE.equals(summary.getIsPregnant());
        }

        if ("LACTATING".equalsIgnoreCase(requestedStatus)) {
            return Boolean.TRUE.equals(summary.getIsLactating());
        }

        return false;
    }

    private boolean equalsIgnoreCase(String value, String requestedStatus) {
        return value != null && value.equalsIgnoreCase(requestedStatus);
    }

    private Optional<BovineSummary> loadSummary(Integer bovineId) {
        if (bovineId == null) {
            return Optional.empty();
        }

        Key summaryKey = Key.builder()
                .partitionValue(PREFIX_BOVINE + bovineId)
                .sortValue(SUMMARY)
                .build();
        return Optional.ofNullable(summaryTable.getItem(summaryKey));
    }
}
