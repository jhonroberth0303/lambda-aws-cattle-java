
package com.cattle.repository;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.BovineSummary;
import com.cattle.utils.TestDataBuilder;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.commons.MessageDTO;
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
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovineRepository
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 * 
 * Cobertura objetivo: Repository 6% → 60%
 * Tests: 15
 */
@Tag("unit")
@Tag("repository")
class BovineIdentityItemRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<BovineIdentityItem> table;

    @Mock
    private DynamoDbTable<BovineSummary> summaryTable;

    @Mock
    private DynamoDbIndex<BovineIdentityItem> gsi1Index;

    @Mock
    private PageIterable<BovineIdentityItem> pageIterable;

    @Mock
    private Page<BovineIdentityItem> page;

    @Mock
    private SdkIterable<BovineIdentityItem> sdkIterable;

    @Mock
    private PageIterable<BovineIdentityItem> scanPageIterable;

    private BovineRepository bovineRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class)))
                .thenReturn((DynamoDbTable) table, (DynamoDbTable) summaryTable);
        bovineRepository = new BovineRepository(lambdaContext, enhancedClient);
    }

    // ==================== findAll Tests ====================

    @Test
    void findAll_withBovines_returnsList() {
        // Arrange
        List<BovineIdentityItem> bovineIdentityItems = createBovineList(5);
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(bovineIdentityItems);

        // Act
        Optional<List<BovineIdentityItem>> result = bovineRepository.findAll();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().size());
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), contains("findAll"));
    }

    @Test
    void findAll_emptyResult_returnsEmpty() {
        // Arrange
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(new ArrayList<>());

        // Act
        Optional<List<BovineIdentityItem>> result = bovineRepository.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_dynamoDbException_throwsRepositoryException() {
        // Arrange
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.findAll());
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString(), any());
    }

    // ==================== findById Tests ====================

    @Test
    void findById_existingId_returnsBovine() {
        Integer bovineId = 123;

        when(table.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(inv -> List.of(createBovine(bovineId)).iterator()).when(sdkIterable).iterator();
        doAnswer(invocation -> {
            java.util.function.Consumer<BovineIdentityItem> consumer = invocation.getArgument(0);
            consumer.accept(createBovine(bovineId));
            return null;
        }).when(sdkIterable).forEach(any());

        Optional<BovineIdentityItem> result = bovineRepository.findById(bovineId);

        assertTrue(result.isPresent());
        assertEquals(bovineId, result.get().getBovineId());
        verify(table, times(1)).query(any(java.util.function.Consumer.class));
    }

    @Test
    void findById_withoutResults_returnsEmptyOptional() {
        Integer bovineId = 123;

        when(table.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        doAnswer(inv -> List.<BovineIdentityItem>of().iterator()).when(sdkIterable).iterator();
        doAnswer(invocation -> null).when(sdkIterable).forEach(any());

        Optional<BovineIdentityItem> result = bovineRepository.findById(bovineId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_resourceNotFound_returnsEmptyOptional() {
        Integer bovineId = 123;
        when(table.query(any(java.util.function.Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing table").build());

        Optional<BovineIdentityItem> result = bovineRepository.findById(bovineId);

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Bovines table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findById_dynamoDbException_throwsRepositoryException() {
        // Arrange
        Integer bovineId = 123;
        when(table.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.findById(bovineId));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString(), any());
    }

    // ==================== findByGender Tests ====================

    @Test
    void findByGender_withMatches_returnsList() {
        List<BovineIdentityItem> bovineIdentityItems = createBovineListWithGender("farm-001", "female", 4);

        when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(scanPageIterable);
        when(scanPageIterable.items()).thenReturn(sdkIterable);
        doAnswer(inv -> bovineIdentityItems.iterator()).when(sdkIterable).iterator();

        List<BovineIdentityItem> result = bovineRepository.findByGender("female");

        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(b -> "female".equals(b.getGender())));
        verify(table).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    void findByGender_withoutResults_returnsEmptyList() {
        when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(scanPageIterable);
        when(scanPageIterable.items()).thenReturn(sdkIterable);
        doAnswer(inv -> List.<BovineIdentityItem>of().iterator()).when(sdkIterable).iterator();

        List<BovineIdentityItem> result = bovineRepository.findByGender("male");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByGender_dynamoDbException_throwsRepositoryException() {
        when(table.scan(any(ScanEnhancedRequest.class)))
                .thenThrow(DynamoDbException.builder().message("scan error").build());

        assertThrows(RepositoryException.class, () -> bovineRepository.findByGender("female"));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Unexpected error querying bovines by gender"), any(DynamoDbException.class));
    }

    // ==================== save Tests ====================

    @Test
    void save_validBovine_returnsOptionalWithBovine() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = createBovine(1);
        doNothing().when(table).putItem(any(BovineIdentityItem.class));

        // Act
        Optional<BovineIdentityItem> result = bovineRepository.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bovineIdentityItem.getBovineId(), result.get().getBovineId());
        verify(table, times(1)).putItem(bovineIdentityItem);
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = createBovine(1);
        doThrow(DynamoDbException.builder().message("Save error").build())
                .when(table).putItem(any(BovineIdentityItem.class));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.save(bovineIdentityItem));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString(), any());
    }

    // ==================== update Tests ====================

    @Test
    void update_validBovine_returnsUpdatedBovine() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = createBovine(1);
        bovineIdentityItem.setName("Updated Name");
        doNothing().when(table).putItem(any(BovineIdentityItem.class));

        // Act
        Optional<BovineIdentityItem> result = bovineRepository.update(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        verify(table, times(1)).putItem(bovineIdentityItem);
    }

    @Test
    void update_dynamoDbException_throwsRepositoryException() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = createBovine(1);
        doThrow(DynamoDbException.builder().message("Update error").build())
                .when(table).putItem(any(BovineIdentityItem.class));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.update(bovineIdentityItem));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString(), any());
    }

    // ==================== deleteById Tests ====================

    @Test
    void deleteById_existingId_returnsSuccessMessage() {
        // Arrange
        Long bovineId = 123L;
        BovineIdentityItem bovineIdentityItem = createBovine(bovineId.intValue());
        when(table.getItem(any(Key.class))).thenReturn(bovineIdentityItem);

        // Act
        MessageDTO result = bovineRepository.deleteById(bovineId);

        // Assert
        assertNotNull(result);
        assertEquals("200", result.getStatus());
        assertTrue(result.getMessage().contains("successfully"));
    }

    @Test
    void deleteById_nonExistingId_returnsNotExistMessage() {
        // Arrange
        Long bovineId = 999L;
        when(table.getItem(any(Key.class))).thenReturn(null);

        // Act
        MessageDTO result = bovineRepository.deleteById(bovineId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getMessage().contains("not exist"));
    }

    @Test
    void deleteById_exception_throwsRepositoryException() {
        // Arrange
        Long bovineId = 123L;
        when(table.getItem(any(Key.class)))
                .thenThrow(DynamoDbException.builder().message("Delete error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.deleteById(bovineId));
    }

    // ==================== countByFarmId Tests ====================

    @Test
    void countByFarmId_withBovines_returnsCount() {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = createBovineListForFarm(farmId, 10);
        
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(bovineIdentityItems);

        // Act
        Long result = bovineRepository.countByFarmId(farmId);

        // Assert
        assertEquals(10L, result);
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), contains("countByFarmId"));
    }

    @Test
    void countByFarmId_exception_throwsRepositoryException() {
        // Arrange
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("Count error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.countByFarmId(farmId));
    }

    // ==================== findByFarmIdAndCategory Tests ====================

    @Test
    void findByFarmIdAndCategory_withMatches_returnsList() {
        // Arrange
        String farmId = "farm-001";
        String category = "cow";
        List<BovineIdentityItem> bovineIdentityItems = createBovineListForFarm(farmId, 6);
        
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(bovineIdentityItems);
        mockSummaryCategory(1, "cow");
        mockSummaryCategory(2, "cow");
        mockSummaryCategory(3, "bull");
        mockSummaryCategory(4, "heifer");
        mockSummaryCategory(5, null);
        mockSummaryCategory(6, "cow");

        // Act
        List<BovineIdentityItem> result = bovineRepository.findByFarmIdAndCategory(farmId, category);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().map(BovineIdentityItem::getBovineId).allMatch(id -> List.of(1, 2, 6).contains(id)));
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), contains("findByFarmIdAndCategory"));
    }
    @Test
    void findByFarmIdAndStatus_withMatches_returnsFilteredList() {
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = createBovineListForFarm(farmId, 4);

        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenAnswer(invocation -> List.of(page).iterator());
        when(page.items()).thenReturn(bovineIdentityItems);
        mockSummaryStatus(1, true, false, "PREGNANT", "PREGNANT");
        mockSummaryStatus(2, false, true, "OPEN", "LACTATING");
        mockSummaryStatus(3, false, false, "OPEN", "DRY");
        mockSummaryStatus(4, true, true, "PRE_PARTO", "LACTATING");

        List<BovineIdentityItem> pregnant = bovineRepository.findByFarmIdAndStatus(farmId, "PREGNANT");
        List<BovineIdentityItem> lactating = bovineRepository.findByFarmIdAndStatus(farmId, "LACTATING");

        assertEquals(2, pregnant.size());
        assertEquals(2, lactating.size());
    }



    @Test
    void findByFarmIdAndCategory_exception_throwsRepositoryException() {
        // Arrange
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.findByFarmIdAndCategory("farm", "cow"));
    }

    // ==================== findByFarmIdAndGender Tests ====================

    @Test
    void findByFarmIdAndGender_withMatches_returnsList() {
        // Arrange
        String farmId = "farm-001";
        String gender = "female";
        List<BovineIdentityItem> bovineIdentityItems = createBovineListWithGender(farmId, gender, 8);
        
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(bovineIdentityItems);

        // Act
        List<BovineIdentityItem> result = bovineRepository.findByFarmIdAndGender(farmId, gender);

        // Assert
        assertEquals(8, result.size());
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), contains("findByFarmIdAndGender"));
    }

    @Test
    void findByFarmIdAndGender_exception_throwsRepositoryException() {
        // Arrange
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(new RuntimeException("Error"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.findByFarmIdAndGender("farm", "female"));
    }

    // ==================== findByFarmIdAndStatus Tests ====================


    @Test
    void findByFarmIdAndStatus_exception_throwsRepositoryException() {
        // Arrange
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(new RuntimeException("Error"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.findByFarmIdAndStatus("farm", "LACTATING"));
    }

    // ==================== findAllByFarmId Tests ====================

    @Test
    void findAllByFarmId_withBovines_returnsList() {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = createBovineListForFarm(farmId, 15);
        
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(bovineIdentityItems);

        // Act
        List<BovineIdentityItem> result = bovineRepository.findAllByFarmId(farmId);

        // Assert
        assertEquals(15, result.size());
        verify(lambdaContext, times(1)).logInfo(eq(LogType.REPOSITORY), contains("findAllByFarmId"));
    }

    @Test
    void findAllByFarmId_exception_throwsRepositoryException() {
        // Arrange
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(new RuntimeException("Error"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> bovineRepository.findAllByFarmId("farm"));
    }

    // ==================== Helper Methods ====================

    private BovineIdentityItem createBovine(Integer id) {
        return TestDataBuilder.createBovine(id, "farm-001", "cow", "female");
    }

    private List<BovineIdentityItem> createBovineList(int count) {
        return TestDataBuilder.createBovineList("farm-001", count);
    }

    private List<BovineIdentityItem> createBovineListForFarm(String farmId, int count) {
        return TestDataBuilder.createBovineList(farmId, count);
    }

    private List<BovineIdentityItem> createBovineListWithCategory(String farmId, String category, int count) {
        return TestDataBuilder.createBovineList(farmId, count);
    }

        private void mockSummaryCategory(Integer bovineId, String category) {
        BovineSummary summary = new BovineSummary();
        summary.setCategory(category);
            when(summaryTable.getItem(argThat((Key key) ->
                key != null
                    && ("BOVINE#" + bovineId).equals(key.partitionKeyValue().s())
                && key.sortKeyValue().map(value -> "SUMMARY".equals(value.s())).orElse(false))))
            .thenReturn(summary);
        }

        private void mockSummaryStatus(Integer bovineId, boolean isPregnant, boolean isLactating,
                       String reproductiveState, String lactationStatus) {
        BovineSummary summary = new BovineSummary();
        summary.setIsPregnant(isPregnant);
        summary.setIsLactating(isLactating);
        summary.setReproductiveState(reproductiveState);
        summary.setLactationStatus(lactationStatus);
        when(summaryTable.getItem(argThat((Key key) ->
            key != null
                && ("BOVINE#" + bovineId).equals(key.partitionKeyValue().s())
                && key.sortKeyValue().map(value -> "SUMMARY".equals(value.s())).orElse(false))))
            .thenReturn(summary);
        }

    private List<BovineIdentityItem> createBovineListWithGender(String farmId, String gender, int count) {
        List<BovineIdentityItem> bovineIdentityItems = TestDataBuilder.createBovineList(farmId, count);
        bovineIdentityItems.forEach(b -> b.setGender(gender));
        return bovineIdentityItems;
    }

    // Método eliminado: createBovineListWithStatus (ya no se usa status en Bovine)
}
