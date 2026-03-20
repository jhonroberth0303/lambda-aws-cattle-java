package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Pasture;
import com.cattle.enums.LogType;
import com.cattle.events.EntityPatch;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PastureRepository {
    private static final String TABLE_PASTURE = System.getenv("TABLE_PASTURE");
    private static final String GSI2_SPECIES_ETA = "gsi2-farm-blocked-eta";
    private static final String PASTURE_NOT_EXIST_IN_DYNAMO_DB = "Pasture not exist in DynamoDB";
    private final LambdaContext lambdaContext;
    private final DynamoDbTable<Pasture> table;
    private final DynamoDbClient dynamoDbClient;

    public PastureRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient, final DynamoDbClient dynamoDbClient) {
        this.lambdaContext = lambdaContext;
        this.table = enhancedClient.table(TABLE_PASTURE, TableSchema.fromBean(Pasture.class));
        this.dynamoDbClient = dynamoDbClient;
    }

    public Optional<List<Pasture>> findPastures(String farmId) {
        try {
            List<Pasture> pastures;

            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder()
                    .partitionValue("farm#" + farmId + "#blocked#false")
                    .build()
            );

            Page<Pasture> result = table
                .index(GSI2_SPECIES_ETA)
                .query(r -> r.limit(20).queryConditional(queryConditional))
                .iterator()
                .next();
            pastures = new ArrayList<>(result.items());

            return Optional.of(pastures);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, PASTURE_NOT_EXIST_IN_DYNAMO_DB);
            throw new RepositoryException(PASTURE_NOT_EXIST_IN_DYNAMO_DB, e);
        } catch (DynamoDbException e) {
            lambdaContext.logException(LogType.REPOSITORY, "DynamoDB error: " + e.getMessage());
            throw new RepositoryException("DynamoDB error: " + e.getMessage(), e);
        } catch (Exception e) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error: " + e.getMessage());
            throw new RepositoryException("Unexpected error: " + e.getMessage(), e);
        }
    }


    /** Aplica el patch a un ítem identificado por pk. */
    public void applyPatch(String pk, EntityPatch patch) {
        if (patch == null || patch.isEmpty()) return;

        // 1) Construir UpdateExpression: "SET #f1 = :v1, #f2 = :v2 REMOVE #r1, #r2"
        StringBuilder ue = new StringBuilder();
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, AttributeValue> values = new LinkedHashMap<>();

        // SET
        if (!patch.set().isEmpty()) {
            ue.append("SET ");
            int i = 0;
            for (Map.Entry<String, Object> e : patch.set().entrySet()) {
                String field = e.getKey();
                Object val = e.getValue();
                String nKey = "#n" + i; // nombre
                String vKey = ":v" + i; // valor

                if (i > 0) ue.append(", ");
                ue.append(nKey).append(" = ").append(vKey);

                names.put(nKey, field);
                values.put(vKey, toAttr(val));
                i++;
            }
        }

        // REMOVE
        if (!patch.remove().isEmpty()) {
            if (ue.length() > 0) ue.append(" ");
            ue.append("REMOVE ");
            for (int j = 0; j < patch.remove().size(); j++) {
                String field = patch.remove().get(j);
                String nKey = "#r" + j;
                if (j > 0) ue.append(", ");
                ue.append(nKey);
                names.put(nKey, field);
            }
        }

        // 2) Clave primaria
        Map<String, AttributeValue> key = Map.of("pk", AttributeValue.builder().s(pk).build());

        // 3) Construir request (puedes agregar ConditionExpression si quieres bloqueo optimista)
        UpdateItemRequest req = UpdateItemRequest.builder()
                .tableName(TABLE_PASTURE)
                .key(key)
                .updateExpression(ue.toString())
                .expressionAttributeNames(names.isEmpty() ? null : names)
                .expressionAttributeValues(values.isEmpty() ? null : values)
                .returnValues(ReturnValue.ALL_NEW) // o UPDATED_NEW / NONE
                .build();

        // 4) Ejecutar
        dynamoDbClient.updateItem(req);
    }

    /** Conversión simple Java → AttributeValue. Amplíala si usas listas/mapas anidados. */
    private static AttributeValue toAttr(Object v) {
        if (v == null) return AttributeValue.builder().nul(true).build();
        if (v instanceof String s) return AttributeValue.builder().s(s).build();
        if (v instanceof Number n) return AttributeValue.builder().n(n.toString()).build();
        if (v instanceof Boolean b) return AttributeValue.builder().bool(b).build();
        if (v instanceof List<?> list) {
            List<AttributeValue> l = list.stream().map(PastureRepository::toAttr).collect(Collectors.toList());
            return AttributeValue.builder().l(l).build();
        }
        if (v instanceof Map<?,?> m) {
            Map<String, AttributeValue> mm = new LinkedHashMap<>();
            for (Map.Entry<?,?> e : m.entrySet()) {
                mm.put(String.valueOf(e.getKey()), toAttr(e.getValue()));
            }
            return AttributeValue.builder().m(mm).build();
        }
        return AttributeValue.builder().s(String.valueOf(v)).build();
    }
}
