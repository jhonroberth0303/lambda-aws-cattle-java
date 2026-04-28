package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.MilkingRecord;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para MilkingRepository
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class MilkingRecordRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<MilkingRecord> table;

    @Mock
    private PageIterable<MilkingRecord> pageIterable;

    @Mock
    private SdkIterable<MilkingRecord> sdkIterable;

    @Mock
    private Page<MilkingRecord> page;

    @Mock
    private DynamoDbIndex<MilkingRecord> index;

    @Mock
    private SdkIterable<Page<MilkingRecord>> pageResults;

    private MilkingRepository milkingRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        milkingRepository = new MilkingRepository(lambdaContext, enhancedClient);
    }

    // ==================== save Tests ====================

    @Test
    void save_validMilking_returnsOptionalWithMilking() {
        // Arrange
        MilkingRecord milkingRecord = createFarmMilking(123, "2025-01-15", "AM");
        doNothing().when(table).putItem(any(MilkingRecord.class));

        // Act
        Optional<MilkingRecord> result = milkingRepository.save(milkingRecord);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(123, result.get().getBovineId());
        verify(table, times(1)).putItem(milkingRecord);
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        // Arrange
        MilkingRecord milkingRecord = createFarmMilking(123, "2025-01-15", "AM");
        doThrow(DynamoDbException.builder().message("Save error").build())
                .when(table).putItem(any(MilkingRecord.class));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> milkingRepository.save(milkingRecord));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString(), any());
    }

    // ==================== getMilkingByPk Tests ====================

    @Test
    void getMilkingByPk_existingPk_returnsList() {
        // Arrange
        String pk = "BOVINE#123";
        List<MilkingRecord> milkingRecords = createFarmMilkingList(123, 5);
        
        when(table.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(inv -> milkingRecords.iterator()).when(sdkIterable).iterator();

        // Act
        Optional<List<MilkingRecord>> result = milkingRepository.getMilkingByPk(pk);

        // Assert
        assertTrue(result.isPresent());
        verify(lambdaContext, atLeast(1)).logInfo(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void getMilkingByPk_dynamoDbException_throwsRepositoryException() {
        // Arrange
        String pk = "BOVINE#123";
        when(table.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("Query error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> milkingRepository.getMilkingByPk(pk));
    }

    // ==================== getMilkingByPkAndSk Tests ====================

    @Test
    void getMilkingByPkAndSk_existingRecord_returnsMilking() {
        // Arrange
        String pk = "BOVINE#123";
        String sk = "MILK#2025-01-15#AM";
        MilkingRecord milkingRecord = createFarmMilking(123, "2025-01-15", "AM");
        
        when(table.getItem(any(java.util.function.Consumer.class))).thenReturn(milkingRecord);

        // Act
        Optional<MilkingRecord> result = milkingRepository.getMilkingByPkAndSk(pk, sk);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(123, result.get().getBovineId());
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void getMilkingByPkAndSk_dynamoDbException_throwsRepositoryException() {
        // Arrange
        String pk = "BOVINE#123";
        String sk = "MILK#2025-01-15#AM";
        when(table.getItem(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("Get error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> milkingRepository.getMilkingByPkAndSk(pk, sk));
    }

    // ==================== getMilkingBetweenDates Tests ====================

    @Test
    void getMilkingBetweenDates_validRange_returnsList() {
        // Arrange
        String pk = "BOVINE#123";
        String skInit = "MILK#2025-01-01#AM";
        String skEnd = "MILK#2025-01-31#PM";
        List<MilkingRecord> milkingRecords = createFarmMilkingList(123, 10);
        
        when(table.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(inv -> milkingRecords.iterator()).when(sdkIterable).iterator();

        // Act
        Optional<List<MilkingRecord>> result = milkingRepository.getMilkingBetweenDates(pk, skInit, skEnd);

        // Assert
        assertTrue(result.isPresent());
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void getMilkingBetweenDates_dynamoDbException_throwsRepositoryException() {
        // Arrange
        String pk = "BOVINE#123";
        String skInit = "MILK#2025-01-01#AM";
        String skEnd = "MILK#2025-01-31#PM";
        when(table.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("Query error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> milkingRepository.getMilkingBetweenDates(pk, skInit, skEnd));
    }

    // ==================== getMilkingByBovineAndLactation Tests ====================

    @Test
    void getMilkingByBovineAndLactation_validQuery_returnsList() {
        Integer bovineId = 123;
        String lactationNumber = "002";
        List<MilkingRecord> milkingRecords = List.of(
                createFarmMilking(bovineId, "2025-01-15", "AM"),
                createFarmMilking(bovineId, "2025-01-15", "PM")
        );
        SdkIterable<Page<MilkingRecord>> gsiResults = () -> List.of(page).iterator();

        when(table.index(anyString())).thenReturn(index);
        when(index.query(any(java.util.function.Consumer.class))).thenReturn(gsiResults);
        when(page.items()).thenReturn(milkingRecords);

        Optional<List<MilkingRecord>> result = milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(table).index("GSI2-bovine-lactation-index");
        verify(index).query(any(java.util.function.Consumer.class));
        verify(lambdaContext, atLeastOnce()).logInfo(eq(LogType.REPOSITORY), contains("GSI2PK"));
    }

    @Test
    void getMilkingByBovineAndLactation_missingIndex_returnsEmpty() {
        Integer bovineId = 123;
        String lactationNumber = "002";

        when(table.index(anyString())).thenReturn(index);
        when(index.query(any(java.util.function.Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Index not found").build());

        Optional<List<MilkingRecord>> result = milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber);

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("index not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void getMilkingByBovineAndLactation_dynamoDbException_throwsRepositoryException() {
        Integer bovineId = 123;
        String lactationNumber = "002";

        when(table.index(anyString())).thenReturn(index);
        when(index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("GSI query error").build());

        assertThrows(RepositoryException.class,
                () -> milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("Unexpected error getMilkingByBovineAndLactation"), any(DynamoDbException.class));
    }

    // ==================== Helper Methods ====================

    private MilkingRecord createFarmMilking(Integer bovineId, String date, String shift) {
        return MilkingRecord.builder()
                .PK("BOVINE#" + bovineId)
                .SK("MILK#" + date + "#" + shift)
                .bovineId(bovineId)
                .date(date)
                .shift(shift)
                .liters(12.5)
                .status("completo")
                .observations("Normal milking")
                .recordedBy("user-001")
                .build();
    }

    private List<MilkingRecord> createFarmMilkingList(Integer bovineId, int count) {
        List<MilkingRecord> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String date = String.format("2025-01-%02d", i);
            list.add(createFarmMilking(bovineId, date, i % 2 == 0 ? "AM" : "PM"));
        }
        return list;
    }
}
