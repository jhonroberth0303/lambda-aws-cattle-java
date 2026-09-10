package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para SiteSettingRepository.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class SiteSettingRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<SiteSettingItem> table;

    private SiteSettingRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new SiteSettingRepository(lambdaContext, enhancedClient);
    }

    private SiteSettingItem item() {
        return SiteSettingItem.builder().siteId("001").settingKey("MILK_PRICE_PER_LITER").valueNumber(1800.0).build();
    }

    // ==================== findCurrent ====================

    @Test
    void findCurrent_itemExists_returnsIt() {
        SiteSettingItem stored = item();
        when(table.getItem(any(Key.class))).thenReturn(stored);

        Optional<SiteSettingItem> result = repository.findCurrent("001", "MILK_PRICE_PER_LITER");

        assertTrue(result.isPresent());
        assertSame(stored, result.get());
    }

    @Test
    void findCurrent_itemMissing_returnsEmpty() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        assertTrue(repository.findCurrent("001", "MILK_PRICE_PER_LITER").isEmpty());
    }

    @Test
    void findCurrent_resourceNotFound_returnsEmptyAndLogs() {
        when(table.getItem(any(Key.class))).thenThrow(ResourceNotFoundException.builder().message("m").build());

        Optional<SiteSettingItem> result = repository.findCurrent("001", "MILK_PRICE_PER_LITER");

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("SiteSetting table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findCurrent_dynamoDbException_throwsRepositoryException() {
        when(table.getItem(any(Key.class))).thenThrow(DynamoDbException.builder().message("m").build());

        RepositoryException ex = assertThrows(RepositoryException.class,
                () -> repository.findCurrent("001", "MILK_PRICE_PER_LITER"));
        assertEquals("Unexpected error finding current SiteSetting", ex.getMessage());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding current SiteSetting"), any(DynamoDbException.class));
    }

    // ==================== saveCurrent ====================

    @Test
    void saveCurrent_ok_returnsSameItem() {
        SiteSettingItem it = item();

        Optional<SiteSettingItem> result = repository.saveCurrent(it);

        assertSame(it, result.orElseThrow());
        verify(table).putItem(it);
    }

    @Test
    void saveCurrent_dynamoDbException_throwsRepositoryException() {
        SiteSettingItem it = item();
        doThrow(DynamoDbException.builder().message("m").build()).when(table).putItem(it);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.saveCurrent(it));
        assertEquals("Unexpected error saving current SiteSetting", ex.getMessage());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving current SiteSetting"), any(DynamoDbException.class));
    }

    // ==================== saveHistorySnapshot ====================

    @Test
    void saveHistorySnapshot_ok_returnsSameItem() {
        SiteSettingItem it = item();

        assertSame(it, repository.saveHistorySnapshot(it).orElseThrow());
        verify(table).putItem(it);
    }

    @Test
    void saveHistorySnapshot_dynamoDbException_throwsRepositoryException() {
        SiteSettingItem it = item();
        doThrow(DynamoDbException.builder().message("m").build()).when(table).putItem(it);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.saveHistorySnapshot(it));
        assertEquals("Unexpected error saving SiteSetting history snapshot", ex.getMessage());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving SiteSetting history snapshot"), any(DynamoDbException.class));
    }

    @Test
    void keyHelpers_produceExpectedShape() {
        assertEquals("SITE#001", SiteSettingItem.buildPk("001"));
        assertEquals("SETTING#MILK_PRICE_PER_LITER#CURRENT", SiteSettingItem.buildCurrentSk("MILK_PRICE_PER_LITER"));
        assertFalse(SiteSettingItem.buildHistorySk("K", "2026-01-01").isBlank());
    }
}
