package com.cattle.containers;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton container de LocalStack para tests de integración con DynamoDB.
 * Inicia automáticamente y crea las tablas necesarias.
 */
public class LocalStackTestContainer {
    
    private static LocalStackContainer container;
    private static DynamoDbClient dynamoDbClient;
    private static final String TABLE_BOVINES = "Bovines";
    private static final String TABLE_FARM_MILKING = "FarmMilking";
    private static final String TABLE_PASTURE = "Pasture";
    
    /**
     * Inicia el container de LocalStack (singleton)
     */
    public static void start() {
        if (container == null || !container.isRunning()) {
            container = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                    .withServices(LocalStackContainer.Service.DYNAMODB);
            container.start();
            
            initializeDynamoDbClient();
            createTables();
        }
    }
    
    /**
     * Detiene el container de LocalStack
     */
    public static void stop() {
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }
    
    /**
     * Obtiene el cliente de DynamoDB configurado para LocalStack
     */
    public static DynamoDbClient getDynamoDbClient() {
        if (dynamoDbClient == null) {
            initializeDynamoDbClient();
        }
        return dynamoDbClient;
    }
    
    /**
     * Obtiene el endpoint de DynamoDB
     */
    public static String getDynamoDbEndpoint() {
        return container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString();
    }
    
    /**
     * Inicializa el cliente de DynamoDB
     */
    private static void initializeDynamoDbClient() {
        dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .region(Region.US_EAST_1)
                .build();
    }
    
    /**
     * Crea las tablas necesarias en DynamoDB
     */
    private static void createTables() {
        createCattleTable();
        createFarmMilkingTable();
        createPastureTable();
    }
    
    /**
     * Crea la tabla Cattle con GSI1
     */
    private static void createCattleTable() {
        try {
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build());
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build());
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("gsi1pk").attributeType(ScalarAttributeType.S).build());
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("gsi1sk").attributeType(ScalarAttributeType.S).build());
            
            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build());
            keySchema.add(KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build());
            
            List<KeySchemaElement> gsi1KeySchema = new ArrayList<>();
            gsi1KeySchema.add(KeySchemaElement.builder().attributeName("gsi1pk").keyType(KeyType.HASH).build());
            gsi1KeySchema.add(KeySchemaElement.builder().attributeName("gsi1sk").keyType(KeyType.RANGE).build());
            
            GlobalSecondaryIndex gsi1 = GlobalSecondaryIndex.builder()
                    .indexName("GSI1")
                    .keySchema(gsi1KeySchema)
                    .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build();
            
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(TABLE_BOVINES)
                    .keySchema(keySchema)
                    .attributeDefinitions(attributeDefinitions)
                    .globalSecondaryIndexes(gsi1)
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build();
            
            dynamoDbClient.createTable(request);
            System.out.println("Created table: " + TABLE_BOVINES);
        } catch (Exception e) {
            System.err.println("Error creating Cattle table: " + e.getMessage());
        }
    }
    
    /**
     * Crea la tabla FarmMilking
     */
    private static void createFarmMilkingTable() {
        try {
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build());
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build());
            
            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build());
            keySchema.add(KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build());
            
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(TABLE_FARM_MILKING)
                    .keySchema(keySchema)
                    .attributeDefinitions(attributeDefinitions)
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build();
            
            dynamoDbClient.createTable(request);
            System.out.println("Created table: " + TABLE_FARM_MILKING);
        } catch (Exception e) {
            System.err.println("Error creating FarmMilking table: " + e.getMessage());
        }
    }
    
    /**
     * Crea la tabla Pasture
     */
    private static void createPastureTable() {
        try {
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build());
            attributeDefinitions.add(AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build());
            
            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build());
            keySchema.add(KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build());
            
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(TABLE_PASTURE)
                    .keySchema(keySchema)
                    .attributeDefinitions(attributeDefinitions)
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build();
            
            dynamoDbClient.createTable(request);
            System.out.println("Created table: " + TABLE_PASTURE);
        } catch (Exception e) {
            System.err.println("Error creating Pasture table: " + e.getMessage());
        }
    }
    
    /**
     * Limpia todas las tablas (útil para tests)
     */
    public static void cleanTables() {
        // Implementar lógica de limpieza si es necesario
    }
}
