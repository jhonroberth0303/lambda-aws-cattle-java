package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.services.BovineSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
@Tag("processor")
class BovinesSummaryProcessorTest {

    @Mock
    private BovineSummaryService bovineSummaryService;
    @Mock
    private LambdaContext lambdaContext;

    private BovinesSummaryProcessor processor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        processor = new BovinesSummaryProcessor(bovineSummaryService, lambdaContext);
    }

    @Test
    void refreshAllCategoriesSummary_delegatesToRefreshAllSummaries() {
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(3);

        int result = processor.refreshAllCategoriesSummary();

        assertEquals(3, result);
        verify(bovineSummaryService, times(1)).refreshAllSummaries();
    }

    @Test
    void refreshAllSummaries_delegatesToService() {
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(5);

        int result = processor.refreshAllSummaries();

        assertEquals(5, result);
        verify(bovineSummaryService, times(1)).refreshAllSummaries();
    }

    @Test
    void findAll_delegatesToService() {
        List<BovineSummaryDTO> expected = List.of(BovineSummaryDTO.builder().bovineId(1).build());
        when(bovineSummaryService.findAll()).thenReturn(expected);

        List<BovineSummaryDTO> result = processor.findAll();

        assertSame(expected, result);
        verify(bovineSummaryService, times(1)).findAll();
    }

    @Test
    void findById_delegatesToService() {
        Integer bovineId = 167;
        Optional<BovineSummaryDTO> expected = Optional.of(BovineSummaryDTO.builder().bovineId(bovineId).build());
        when(bovineSummaryService.findById(bovineId)).thenReturn(expected);

        Optional<BovineSummaryDTO> result = processor.findById(bovineId);

        assertSame(expected, result);
        verify(bovineSummaryService, times(1)).findById(bovineId);
    }

    @Test
    void refreshSummary_delegatesToService() {
        Integer bovineId = 42;
        BovineSummaryDTO expected = BovineSummaryDTO.builder().bovineId(bovineId).build();
        when(bovineSummaryService.refreshSummary(bovineId)).thenReturn(expected);

        BovineSummaryDTO result = processor.refreshSummary(bovineId);

        assertSame(expected, result);
        verify(bovineSummaryService, times(1)).refreshSummary(bovineId);
    }

    @Test
    void refreshSummary_propagatesServiceException() {
        Integer bovineId = 42;
        RuntimeException expected = new RuntimeException("service failure");
        when(bovineSummaryService.refreshSummary(bovineId)).thenThrow(expected);

        RuntimeException result = assertThrows(RuntimeException.class, () -> processor.refreshSummary(bovineId));

        assertSame(expected, result);
        verify(bovineSummaryService, times(1)).refreshSummary(bovineId);
    }
}