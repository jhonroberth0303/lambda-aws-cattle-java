package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.processor.BovinesSummaryProcessor;
import com.cattle.services.BovineSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class BovinesSummaryControllerTest {

    @Mock
    private BovinesSummaryProcessor bovinesSummaryProcessor;
    @Mock
    private BovineSummaryService summaryService;
    @Mock
    private LambdaContext lambdaContext;

    private BovinesSummaryController controller;

    @BeforeEach
    void setUp() {
        openMocks(this);
        controller = new BovinesSummaryController(bovinesSummaryProcessor, lambdaContext);
    }

    @Test
    void getAllSummaries_returnsOkWithList() {
        List<BovineSummaryDTO> summaries = List.of(new BovineSummaryDTO(), new BovineSummaryDTO());
        when(bovinesSummaryProcessor.findAll()).thenReturn(summaries);
        ResponseEntity<List<BovineSummaryDTO>> response = controller.getAllSummaries();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAllSummaries_returnsNoContentWhenEmpty() {
        when(bovinesSummaryProcessor.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<List<BovineSummaryDTO>> response = controller.getAllSummaries();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void getSummaryById_returnsOkWhenFound() {
        BovineSummaryDTO dto = new BovineSummaryDTO();
        when(bovinesSummaryProcessor.findById(anyInt())).thenReturn(Optional.of(dto));
        ResponseEntity<BovineSummaryDTO> response = controller.getSummaryById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void getSummaryById_returnsNotFoundWhenMissing() {
        when(bovinesSummaryProcessor.findById(anyInt())).thenReturn(Optional.empty());
        ResponseEntity<BovineSummaryDTO> response = controller.getSummaryById(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getSummaryById_returnsBadRequestWhenInvalid() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.getSummaryById(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getSummaryById(0).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getSummaryById(-1).getStatusCode());
    }

    @Test
    void refreshSummaryById_returnsOkWhenSuccess() {
        BovineSummaryDTO dto = new BovineSummaryDTO();
        when(bovinesSummaryProcessor.refreshSummary(anyInt())).thenReturn(dto);
        ResponseEntity<BovineSummaryDTO> response = controller.refreshSummaryById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void refreshSummaryById_returnsBadRequestWhenInvalid() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.refreshSummaryById(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.refreshSummaryById(0).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.refreshSummaryById(-1).getStatusCode());
    }

    @Test
    void refreshSummaryById_returnsInternalServerErrorOnException() {
        when(bovinesSummaryProcessor.refreshSummary(anyInt())).thenThrow(new RuntimeException("fail"));
        ResponseEntity<BovineSummaryDTO> response = controller.refreshSummaryById(1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void refreshAllSummaries_returnsOkWithCount() {
        when(bovinesSummaryProcessor.refreshAllSummaries()).thenReturn(5);
        ResponseEntity<Map<String, Object>> response = controller.refreshAllSummaries();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().get("count"));
    }

    @Test
    void refreshAllSummaries_returnsInternalServerErrorOnException() {
        when(bovinesSummaryProcessor.refreshAllSummaries()).thenThrow(new RuntimeException("fail"));
        ResponseEntity<Map<String, Object>> response = controller.refreshAllSummaries();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().get("error").toString().contains("Failed"));
    }

    @Test
    void refreshAllCategoriesSummary_returnsOkWithCount() {
        when(bovinesSummaryProcessor.refreshAllCategoriesSummary()).thenReturn(3);
        ResponseEntity<Integer> response = controller.refreshAllCategoriesSummary();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody());
    }
}
