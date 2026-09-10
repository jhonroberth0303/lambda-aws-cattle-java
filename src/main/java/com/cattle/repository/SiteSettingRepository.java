package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.SiteSettingItem;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class SiteSettingRepository {

    private static final String TABLE_SITE_SETTINGS = System.getenv("TABLE_SITE_SETTINGS");

    private final LambdaContext lambdaContext;
    private final DynamoDbTable<SiteSettingItem> table;

    public SiteSettingRepository(LambdaContext lambdaContext, final DynamoDbEnhancedClient enhancedClient) {
        this.lambdaContext = lambdaContext;
        this.table = enhancedClient.table(TABLE_SITE_SETTINGS, TableSchema.fromBean(SiteSettingItem.class));
    }

    public Optional<SiteSettingItem> findCurrent(String siteId, String settingKey) {
        try {
            Key key = Key.builder()
                    .partitionValue(SiteSettingItem.buildPk(siteId))
                    .sortValue(SiteSettingItem.buildCurrentSk(settingKey))
                    .build();
            return Optional.ofNullable(table.getItem(key));
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "SiteSetting table not found", e);
            return Optional.empty();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error finding current SiteSetting", ex);
            throw new RepositoryException("Unexpected error finding current SiteSetting", ex);
        }
    }

    /**
     * Todas las settings vigentes de un sitio (ítems {@code SETTING#*#CURRENT}).
     * EP-20260909, Fase 3.
     */
    public List<SiteSettingItem> findAllCurrent(String siteId) {
        try {
            QueryConditional queryConditional = QueryConditional.sortBeginsWith(
                    Key.builder()
                            .partitionValue(SiteSettingItem.buildPk(siteId))
                            .sortValue("SETTING#")
                            .build());
            return StreamSupport.stream(
                            table.query(r -> r.queryConditional(queryConditional)).items().spliterator(), false)
                    .filter(it -> it.getSk() != null && it.getSk().endsWith("#CURRENT"))
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            lambdaContext.logException(LogType.REPOSITORY, "SiteSetting table not found", e);
            return List.of();
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error listing current SiteSettings", ex);
            throw new RepositoryException("Unexpected error listing current SiteSettings", ex);
        }
    }

    public Optional<SiteSettingItem> saveCurrent(SiteSettingItem item) {
        try {
            table.putItem(item);
            return Optional.of(item);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving current SiteSetting", ex);
            throw new RepositoryException("Unexpected error saving current SiteSetting", ex);
        }
    }

    public Optional<SiteSettingItem> saveHistorySnapshot(SiteSettingItem item) {
        try {
            table.putItem(item);
            return Optional.of(item);
        } catch (DynamoDbException ex) {
            lambdaContext.logException(LogType.REPOSITORY, "Error saving SiteSetting history snapshot", ex);
            throw new RepositoryException("Unexpected error saving SiteSetting history snapshot", ex);
        }
    }
}