package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureEventHistoryItemDTO;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.PastureEventRepository;
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
 * Tests unitarios para PastureEventService.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Service
 */
@Tag("unit")
@Tag("fast")
class PastureEventServiceTest {

    @Mock
    private PastureEventRepository repository;

    @Mock
    private LambdaContext lambdaContext;

    private PastureEventService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new PastureEventService(repository, lambdaContext);
    }

    private PastureEventItem item(String farmId, String eventId) {
        PastureEventItem it = new PastureEventItem();
        it.setFarmId(farmId);
        it.setPastureId("P1");
        it.setEventId(eventId);
        it.setEventType("OPEN");
        it.setEventAt(Instant.parse("2026-02-02T00:00:00Z"));
        it.setCreatedBy("manual-web");
        it.setNotes("obs");
        it.setPayloadJson("{}");
        return it;
    }

    @Test
    void save_delegatesToRepository() {
        PastureEventItem it = item("F1", "e1");
        service.save(it);
        verify(repository).save(it);
    }

    @Test
    void save_repositoryException_wrappedAsServiceException() {
        PastureEventItem it = item("F1", "e1");
        doThrow(new RepositoryException("ddb", new RuntimeException())).when(repository).save(it);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.save(it));
        assertEquals("Repository error saving pasture event", ex.getMessage());
        verify(lambdaContext).logException(eq(LogType.SERVICE), contains("Repository error saving pasture event"), any());
    }

    @Test
    void save_unexpectedException_wrappedAsProcessingException() {
        PastureEventItem it = item("F1", "e1");
        doThrow(new IllegalStateException("boom")).when(repository).save(it);

        ProcessingException ex = assertThrows(ProcessingException.class, () -> service.save(it));
        assertEquals("Unexpected error saving pasture event", ex.getMessage());
    }

    @Test
    void findByPasture_mapsFieldsAndFiltersByFarm() {
        when(repository.findByPasture("P1", 20)).thenReturn(List.of(
                item("F1", "keep"),
                item("OTHER", "drop")));

        List<PastureEventHistoryItemDTO> result = service.findByPasture("F1", "P1", 20);

        assertEquals(1, result.size());
        PastureEventHistoryItemDTO dto = result.get(0);
        assertEquals("keep", dto.getEventId());
        assertEquals("OPEN", dto.getEventType());
        assertEquals("2026-02-02T00:00:00Z", dto.getEventAt());
        assertEquals("manual-web", dto.getCreatedBy());
        assertEquals("obs", dto.getNotes());
        assertEquals("{}", dto.getPayloadJson());
    }

    @Test
    void findByPasture_nullEventAt_mapsToNull() {
        PastureEventItem it = item("F1", "e1");
        it.setEventAt(null);
        when(repository.findByPasture("P1", 10)).thenReturn(List.of(it));

        assertNull(service.findByPasture("F1", "P1", 10).get(0).getEventAt());
    }

    @Test
    void findByPasture_empty_returnsEmptyList() {
        when(repository.findByPasture("P1", 10)).thenReturn(List.of());
        assertTrue(service.findByPasture("F1", "P1", 10).isEmpty());
    }

    @Test
    void findByPasture_repositoryException_wrappedAsServiceException() {
        when(repository.findByPasture(anyString(), anyInt()))
                .thenThrow(new RepositoryException("ddb", new RuntimeException()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.findByPasture("F1", "P1", 10));
        assertEquals("Repository error querying pasture events", ex.getMessage());
    }

    @Test
    void findByPasture_unexpectedException_wrappedAsProcessingException() {
        when(repository.findByPasture(anyString(), anyInt())).thenThrow(new IllegalStateException("boom"));

        ProcessingException ex = assertThrows(ProcessingException.class, () -> service.findByPasture("F1", "P1", 10));
        assertEquals("Unexpected error querying pasture events", ex.getMessage());
    }
}
