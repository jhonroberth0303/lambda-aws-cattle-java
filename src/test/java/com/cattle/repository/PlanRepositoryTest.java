package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Plan;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
 * Tests unitarios para PlanRepository
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class PlanRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<Plan> table;

    @Mock
    private DynamoDbIndex<Plan> gsi1Index;

    @Mock
    private PageIterable<Plan> pageIterable;

    @Mock
    private Page<Plan> page;

    private PlanRepository planRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        planRepository = new PlanRepository(lambdaContext, enhancedClient);
    }

    // ==================== findPlans Tests ====================

    @Test
    void findPlans_withPlans_returnsList() {
        // Arrange
        String farmId = "farm-001";
        List<Plan> plans = createPlanList(farmId, 3);
        
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(plans);

        // Act
        Optional<List<Plan>> result = planRepository.findPlans(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
    }

    @Test
    void findPlans_emptyResult_returnsEmptyList() {
        // Arrange
        String farmId = "farm-empty";
        
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(new ArrayList<>());

        // Act
        Optional<List<Plan>> result = planRepository.findPlans(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void findPlans_resourceNotFoundException_throwsRepositoryException() {
        // Arrange
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Table not found").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> planRepository.findPlans(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void findPlans_dynamoDbException_throwsRepositoryException() {
        // Arrange
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> planRepository.findPlans(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void findPlans_unexpectedException_throwsRepositoryException() {
        // Arrange
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> planRepository.findPlans(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString());
    }

    // ==================== Helper Methods ====================

    private Plan createPlan(String farmId, String species) {
        return Plan.builder()
                .pk("PLAN#" + farmId + "#" + species)
                .farmId(farmId)
                .species(species)
                .planType("rotation")
                .rules(Plan.Rules.builder()
                        .restDaysMin(30)
                        .entryHeightCm(20)
                        .exitResidualCm(5)
                        .build())
                .build();
    }

    private List<Plan> createPlanList(String farmId, int count) {
        List<Plan> list = new ArrayList<>();
        String[] species = {"kikuyo", "rye_grass", "brachiaria"};
        for (int i = 0; i < count && i < species.length; i++) {
            list.add(createPlan(farmId, species[i]));
        }
        return list;
    }
}
