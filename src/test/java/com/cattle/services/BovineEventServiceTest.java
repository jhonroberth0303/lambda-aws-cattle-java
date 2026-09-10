package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineEventHistoryItemDTO;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.BovineEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovineEventService.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Service
 */
@Tag("unit")
@Tag("fast")
class BovineEventServiceTest {

    @Mock
    private BovineEventRepository repository;

    @Mock
    private LambdaContext lambdaContext;

    private BovineEventService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new BovineEventService(repository, lambdaContext);
    }

    private BovineEventItem item(String farmId, String eventId) {
        BovineEventItem it = new BovineEventItem();
        it.setFarmId(farmId);
        it.setBovineId("B1");
        it.setEventId(eventId);
        it.setEventType("SEGUIMIENTO");
        it.setEventAt(Instant.parse("2026-01-01T00:00:00Z"));
        it.setCreatedBy("manual-web");
        it.setNotes("nota");
        it.setPayloadJson("{\"notes\":\"nota\"}");
        return it;
    }

    // ==================== save ====================

    @Test
    void save_delegatesToRepository() {
        BovineEventItem it = item("F1", "e1");
        service.save(it);
        verify(repository).save(it);
    }

    @Test
    void save_repositoryException_wrappedAsServiceException() {
        BovineEventItem it = item("F1", "e1");
        doThrow(new RepositoryException("ddb", new RuntimeException())).when(repository).save(it);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.save(it));
        assertEquals("Repository error saving bovine event", ex.getMessage());
        verify(lambdaContext).logException(eq(LogType.SERVICE), contains("Repository error saving bovine event"), any());
    }

    @Test
    void save_unexpectedException_wrappedAsProcessingException() {
        BovineEventItem it = item("F1", "e1");
        doThrow(new IllegalStateException("boom")).when(repository).save(it);

        ProcessingException ex = assertThrows(ProcessingException.class, () -> service.save(it));
        assertEquals("Unexpected error saving bovine event", ex.getMessage());
    }

    // ==================== findByBovine ====================

    @Test
    void findByBovine_mapsFieldsAndFiltersByFarm() {
        when(repository.findByBovine("B1", 20)).thenReturn(List.of(
                item("F1", "keep"),
                item("OTHER", "drop"),
                item("F1", "keep2")));

        List<BovineEventHistoryItemDTO> result = service.findByBovine("F1", "B1", 20);

        assertEquals(2, result.size());
        BovineEventHistoryItemDTO first = result.get(0);
        assertEquals("keep", first.getEventId());
        assertEquals("SEGUIMIENTO", first.getEventType());
        assertEquals("2026-01-01T00:00:00Z", first.getEventAt());
        assertEquals("manual-web", first.getCreatedBy());
        assertEquals("nota", first.getNotes());
        assertEquals("{\"notes\":\"nota\"}", first.getPayloadJson());
    }

    @Test
    void findByBovine_nullEventAt_mapsToNullString() {
        BovineEventItem noDate = item("F1", "e1");
        noDate.setEventAt(null);
        when(repository.findByBovine("B1", 10)).thenReturn(List.of(noDate));

        assertNull(service.findByBovine("F1", "B1", 10).get(0).getEventAt());
    }

    @Test
    void findByBovine_emptyRepository_returnsEmptyList() {
        when(repository.findByBovine("B1", 10)).thenReturn(List.of());
        assertTrue(service.findByBovine("F1", "B1", 10).isEmpty());
    }

    @Test
    void findByBovine_repositoryException_wrappedAsServiceException() {
        when(repository.findByBovine(anyString(), anyInt()))
                .thenThrow(new RepositoryException("ddb", new RuntimeException()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.findByBovine("F1", "B1", 10));
        assertEquals("Repository error querying bovine events", ex.getMessage());
    }

    @Test
    void findByBovine_unexpectedException_wrappedAsProcessingException() {
        when(repository.findByBovine(anyString(), anyInt())).thenThrow(new IllegalStateException("boom"));

        ProcessingException ex = assertThrows(ProcessingException.class, () -> service.findByBovine("F1", "B1", 10));
        assertEquals("Unexpected error querying bovine events", ex.getMessage());
    }
}
