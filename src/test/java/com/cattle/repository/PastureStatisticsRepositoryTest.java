package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para PastureStatisticsRepository.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class PastureStatisticsRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<PastureEventItem> table;

    @Mock
    private PageIterable<PastureEventItem> pageIterable;

    private PastureStatisticsRepository repository;

    private final LocalDate from = LocalDate.parse("2026-01-01");
    private final LocalDate to = LocalDate.parse("2026-01-31");

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new PastureStatisticsRepository(lambdaContext, enhancedClient);
    }

    private PastureEventItem event(String farmId) {
        PastureEventItem item = new PastureEventItem();
        item.setFarmId(farmId);
        item.setEventType("OPEN");
        return item;
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(List<PastureEventItem> items) {
        when(table.query(any(Consumer.class))).thenReturn(pageIterable);
        SdkIterable<PastureEventItem> iterable = items::iterator;
        when(pageIterable.items()).thenReturn(iterable);
    }

    @Test
    void findByPastureInRange_filtersByFarmId() {
        stubQuery(List.of(event("F001"), event("F002"), event("F001"), event("OTHER")));

        List<PastureEventItem> result = repository.findByPastureInRange("F001", "P1", from, to);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> "F001".equals(e.getFarmId())));
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), contains("pastureId=P1"));
    }

    @Test
    void findByPastureInRange_noMatches_returnsEmpty() {
        stubQuery(List.of(event("F999")));

        assertTrue(repository.findByPastureInRange("F001", "P1", from, to).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPastureInRange_resourceNotFound_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(ResourceNotFoundException.builder().message("m").build());

        RepositoryException ex = assertThrows(RepositoryException.class,
                () -> repository.findByPastureInRange("F001", "P1", from, to));
        assertTrue(ex.getMessage().contains("Events table not found while querying statistics"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPastureInRange_dynamoDbException_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(DynamoDbException.builder().message("m").build());

        assertThrows(RepositoryException.class, () -> repository.findByPastureInRange("F001", "P1", from, to));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("DynamoDB error querying statistics"), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPastureInRange_unexpectedException_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(new IllegalStateException("m"));

        RepositoryException ex = assertThrows(RepositoryException.class,
                () -> repository.findByPastureInRange("F001", "P1", from, to));
        assertTrue(ex.getMessage().contains("Unexpected error querying statistics"));
    }
}
