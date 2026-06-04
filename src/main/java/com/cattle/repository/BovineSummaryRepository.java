package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineSummary;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.*;

/**
 * Repositorio para acceso a los ítems SUMMARY de bovinos en DynamoDB.
 * Utiliza la misma tabla TABLE_BOVINES que las demás entidades de bovinos.
 */
@Repository
public class BovineSummaryRepository {

    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private static final String GSI1_BOVINES = "gsi1";
    private static final String SUMMARY = "SUMMARY";

    private final LambdaContext lambdaContext;
    private final DynamoDbTable<BovineSummary> table;

    public BovineSummaryRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        this.table = enhancedClient.table(TABLE_BOVINES, TableSchema.fromBean(BovineSummary.class));
    }

    /**
     * Obtiene todos los resúmenes de bovinos usando GSI1.
     * @return Lista de BovineSummary
     */
    public List<BovineSummary> findAll() {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Finding all bovine summaries using " + GSI1_BOVINES);
            QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                    .partitionValue(SUMMARY)
                    .build());

            List<BovineSummary> results = new ArrayList<>();
            table.index(GSI1_BOVINES)
                .query(r -> r.queryConditional(queryConditional))
                .forEach(page -> results.addAll(page.items()));

            lambdaContext.logInfo(LogType.REPOSITORY, "findAll summaries: " + results.size() + " records found");
            return results;
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "BovineSummary table/index not found", e);
            return Collections.emptyList();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding summaries", ex);
            throw new RepositoryException("Unexpected error finding summaries", ex);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Unexpected error finding summaries", ex);
            throw new RepositoryException("Unexpected error finding summaries", ex);
        }
    }

    /**
     * Obtiene todos los resúmenes de bovinos con paginación.
     * @param limit Número máximo de registros por página
     * @param lastEvaluatedKey Clave de la última evaluación para paginación
     * @return Página de resultados con items y lastEvaluatedKey
     */
    public Page<BovineSummary> findAllPaginated(int limit, Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> lastEvaluatedKey) {
        try {
            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(SUMMARY).build()
            );

            QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .limit(limit);

            if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
                requestBuilder.exclusiveStartKey(lastEvaluatedKey);
            }

            Iterator<Page<BovineSummary>> iterator = table.index(GSI1_BOVINES)
                .query(requestBuilder.build())
                .iterator();

            if (iterator.hasNext()) {
                Page<BovineSummary> page = iterator.next();
                lambdaContext.logInfo(LogType.REPOSITORY, "findAllPaginated: " + page.items().size() + " records found");
                return page;
            }

            return Page.create(Collections.emptyList());
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding paginated summaries", ex);
            throw new RepositoryException("Unexpected error finding paginated summaries", ex);
        }
    }

    /**
     * Obtiene el resumen de un bovino específico por su ID.
     * @param bovineId ID del bovino (sin prefijo)
     * @return Optional con el BovineSummary o vacío si no existe
     */
    public Optional<BovineSummary> findById(String bovineId) {
        try {
            String pk = "BOVINE#" + bovineId;
            Key key = Key.builder().partitionValue(pk).sortValue(SUMMARY).build();
            BovineSummary item = table.getItem(key);
            return Optional.ofNullable(item);
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "BovineSummary not found for id: " + bovineId, e);
            return Optional.empty();
        } catch (Exception ex) {
            lambdaContext.logException(LogType.REPOSITORY, "findById summary error for id: " + bovineId, ex);
            throw new RepositoryException("Unexpected error finding summary by Id", ex);
        }
    }

    /**
     * Obtiene el resumen de un bovino específico por su ID numérico.
     * @param bovineId ID numérico del bovino
     * @return Optional con el BovineSummary o vacío si no existe
     */
    public Optional<BovineSummary> findById(Integer bovineId) {
        return findById(String.valueOf(bovineId));
    }

    /**
     * Guarda o actualiza el resumen de un bovino.
     * @param entity BovineSummary a guardar
     * @return BovineSummary guardado
     */
    public BovineSummary save(BovineSummary entity) {
        try {
            table.putItem(entity);
            lambdaContext.logInfo(LogType.REPOSITORY, "BovineSummary saved for: " + entity.getPk());
            return entity;
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving BovineSummary", ex);
            throw new RepositoryException("Unexpected error saving BovineSummary", ex);
        }
    }

    /**
     * Guarda múltiples resúmenes de bovinos en batch.
     * @param entities Lista de BovineSummary a guardar
     * @return Número de registros guardados
     */
    public int saveAll(List<BovineSummary> entities) {
        try {
            lambdaContext.logInfo(LogType.REPOSITORY, "Saving batch of BovineSummary: " + entities.size() + " records");
            for (BovineSummary entity : entities) {
                table.putItem(entity);
            }
            lambdaContext.logInfo(LogType.REPOSITORY, "BovineSummary batch saved: " + entities.size() + " records");
            return entities.size();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving BovineSummary batch", ex);
            throw new RepositoryException("Unexpected error saving BovineSummary batch", ex);
        }
    }

    /**
     * Elimina el resumen de un bovino.
     * @param bovineId ID del bovino (sin prefijo)
     */
    public void delete(String bovineId) {
        try {
            String pk = "BOVINE#" + bovineId;
            Key key = Key.builder().partitionValue(pk).sortValue(SUMMARY).build();
            table.deleteItem(key);
            lambdaContext.logInfo(LogType.REPOSITORY, "BovineSummary deleted for: " + pk);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error deleting BovineSummary", ex);
            throw new RepositoryException("Unexpected error deleting BovineSummary", ex);
        }
    }

    /**
     * Elimina el resumen de un bovino por su ID numérico.
     * @param bovineId ID numérico del bovino
     */
    public void delete(Integer bovineId) {
        delete(String.valueOf(bovineId));
    }
}
