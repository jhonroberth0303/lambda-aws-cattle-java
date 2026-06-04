package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.MilkingRecord;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MilkingRepository {

    private static final String TABLE_FARM_MILKING = System.getenv("TABLE_FARM_MILKING");
    private static final String GSI1 = "gsi1";
    public static final String MILKING_NOT_EXIST_IN_DYNAMO_DB = "Milking not exist in DynamoDB";
    public static final String STATUS_SUCCESS = "200";
    public static final String MILKING_WAS_DELETE_SUCCESSFULLY = "Milking was delete successfully: ";
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<MilkingRecord> table;

    public MilkingRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        table = enhancedClient.table(TABLE_FARM_MILKING, TableSchema.fromBean(MilkingRecord.class));
    }

    public Optional<MilkingRecord> save(MilkingRecord milkingRecord) throws RepositoryException {
        try {
            table.putItem(milkingRecord);
            return Optional.ofNullable(milkingRecord);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error saving farmMilking", ex);
            throw new RepositoryException("Unexpected error saving farmMilking", ex);
        }
    }

    public Optional<List<MilkingRecord>> getMilkingByPk(String pk) throws RepositoryException {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "getAllMilking: Searching records with PK: " + pk);
            List<MilkingRecord> list = new ArrayList<>();

            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(pk).build());

            for (MilkingRecord record : table.query(r -> r.queryConditional(queryConditional)).items()) {
                list.add(record);
            }

            lambdaContext.logInfo(LogType.REPOSITORY, "getAllMilking: " + list.size() + " records found in table: " + TABLE_FARM_MILKING);
            return Optional.of(list);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "FarmMilking table not found", e);
            return Optional.empty();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, " Unexpected error finding farmMilking By Pk", ex);
            throw new RepositoryException("Unexpected error finding farmMilking By Pk", ex);
        }
    }

    public Optional<MilkingRecord> getMilkingByPkAndSk(String pk, String sk) throws RepositoryException {
        try {
            Key key = Key.builder()
                    .partitionValue(pk)
                    .sortValue(sk)
                    .build();

            MilkingRecord milkingRecord = table.getItem(r -> r.key(key));

            lambdaContext.logInfo(LogType.REPOSITORY, "getMilkingByPkAndSk: Record : " + pk + "-" + sk + " found in table" + TABLE_FARM_MILKING);
            return Optional.of(milkingRecord);
        }  catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error getting farmMilking", ex);
            throw new RepositoryException("Unexpected error getting farmMilking", ex);
        }
    }

    public Optional<List<MilkingRecord>> getMilkingBetweenDates(String pk, String skInit, String skEnd) throws RepositoryException {
        try {
            List<MilkingRecord> milkingRecord = new ArrayList<>();
            table.query(r -> r.queryConditional(
                    QueryConditional.sortBetween(
                            Key.builder().partitionValue(pk).sortValue(skInit).build(),
                            Key.builder().partitionValue(pk).sortValue(skEnd).build()
                    )
            )).items().forEach(milkingRecord::add);

            lambdaContext.logInfo(LogType.REPOSITORY, "getMilkingBetweenDates: " + milkingRecord.size() + " records found in table: " + TABLE_FARM_MILKING);
            return Optional.of(milkingRecord);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "FarmMilking table not found", e);
            return Optional.empty();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error getMilkingBetweenDates", ex);
            throw new RepositoryException("Unexpected error finding getMilkingBetweenDates", ex);
        }
    }

    public Optional<List<MilkingRecord>> getMilkingByBovineAndLactation(Integer bovineId, String lactationNumber) {
        try {
            String gsi1pk = "BOVINE#" + bovineId + "#LACT#" + lactationNumber;

            lambdaContext.logInfo(LogType.REPOSITORY, "getMilkingByBovineAndLactation: Searching with gsi1pk: " + gsi1pk);

            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(gsi1pk).build());

            List<MilkingRecord> records = new ArrayList<>();
            table.index(GSI1)
                    .query(r -> r.queryConditional(queryConditional))
                    .forEach(page -> records.addAll(page.items()));

            lambdaContext.logInfo(LogType.REPOSITORY, "getMilkingByBovineAndLactation: " + records.size() + " records found");
            return Optional.of(records);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "gsi1 index not found", e);
            return Optional.empty();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error getMilkingByBovineAndLactation", ex);
            throw new RepositoryException("Unexpected error finding milking by bovine and lactation", ex);
        }
    }

    public Optional<List<MilkingRecord>> findAllScan() {
        try {
            List<MilkingRecord> milkingRecords = new ArrayList<>();
            table.scan().items().forEach(milkingRecords::add);
            lambdaContext.logInfo(LogType.REPOSITORY, "findAllScan: " + milkingRecords.size() + " records found in table: " + TABLE_FARM_MILKING);
            return Optional.of(milkingRecords);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "FarmMilking table not found", e);
            return Optional.empty();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error finding all farmMilking", ex);
            throw new RepositoryException("Unexpected error finding all farmMilking", ex);
        }
    }
}
