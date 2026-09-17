package com.cattle.services;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.dtos.chatbot.MilkingContextDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.enums.QueryIntent;
import com.cattle.repository.MilkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class MilkingQueryServiceTest {

    @Mock
    private MilkingRepository milkingRepository;

    private MilkingQueryService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new MilkingQueryService(milkingRepository);
    }

    @Test
    void getMonthlyAverageProduction_returnsZeroWhenEmpty() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        Double result = service.getMonthlyAverageProduction("farm-001");

        assertEquals(0.0, result);
    }

        @Test
        void getMonthlyAverageProduction_returnsZeroWhenOptionalHasEmptyList() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.of(List.of()));

                Double result = service.getMonthlyAverageProduction("farm-001");

                assertEquals(0.0, result);
        }

    @Test
    void getMonthlyAverageProduction_ignoresNullLitersInSumButDividesByRecords() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of(
                        createMilking(1, "2026-04-10", "AM", 10.0),
                        createMilking(1, "2026-04-11", "PM", null),
                        createMilking(2, "2026-04-12", "AM", 20.0)
                )));

        Double result = service.getMonthlyAverageProduction("farm-001");

        assertEquals(10.0, result);
    }

    @Test
    void getWeeklyAverageProduction_returnsZeroWhenListEmpty() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of()));

        Double result = service.getWeeklyAverageProduction("farm-001");

        assertEquals(0.0, result);
    }

        @Test
        void getWeeklyAverageProduction_returnsZeroWhenOptionalIsEmpty() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.empty());

                Double result = service.getWeeklyAverageProduction("farm-001");

                assertEquals(0.0, result);
        }

        @Test
        void getWeeklyAverageProduction_averagesOnlyNonNullLitersOverRecordCount() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.of(List.of(
                                                createMilking(1, "2026-04-10", "AM", 6.0),
                                                createMilking(1, "2026-04-11", "PM", null),
                                                createMilking(2, "2026-04-12", "AM", 9.0)
                                )));

                Double result = service.getWeeklyAverageProduction("farm-001");

                assertEquals(5.0, result);
        }

        @Test
        void getTopProducerBovine_returnsNullWhenNoRecords() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.empty());

                MilkingContextDTO result = service.getTopProducerBovine("farm-001");

                assertNull(result);
        }

    @Test
    void getTopProducerBovine_returnsMostRecentRecordForTopBovine() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of(
                        createMilking(1, "2026-04-01", "AM", 10.0),
                        createMilking(2, "2026-04-02", "AM", 15.0),
                        createMilking(2, "2026-04-03", "PM", 20.0)
                )));

        MilkingContextDTO result = service.getTopProducerBovine("farm-001");

        assertNotNull(result);
        assertEquals("2", result.getBovineId());
        assertEquals("Bovino 2", result.getBovineName());
        assertEquals(20.0, result.getLitersMilked());
        assertEquals(18, result.getMilkingTime().getHour());
    }

    @Test
    void getTopProducerBovine_returnsNullWhenGroupedDataIsEmpty() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of(
                        createMilking(null, "2026-04-01", "AM", 10.0),
                        createMilking(1, "2026-04-02", "AM", null)
                )));

        MilkingContextDTO result = service.getTopProducerBovine("farm-001");

        assertNull(result);
    }

        @Test
        void getTopProducerBovine_handlesUnknownShiftAndNullDate() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.of(List.of(
                                                createMilking(5, null, "MID", 13.0)
                                )));

                MilkingContextDTO result = service.getTopProducerBovine("farm-001");

                assertNotNull(result);
                assertNull(result.getMilkingDate());
                assertNull(result.getMilkingTime());
                assertEquals("MID", result.getShift());
        }

    @Test
    void getProductionByShift_returnsAggregatedLiters() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of(
                        createMilking(1, "2026-04-01", "AM", 10.0),
                        createMilking(2, "2026-04-01", "AM", 5.0),
                        createMilking(3, "2026-04-01", "PM", 8.0),
                        createMilking(4, "2026-04-01", null, 9.0)
                )));

        Map<String, Double> result = service.getProductionByShift("farm-001");

        assertEquals(15.0, result.get("AM"));
        assertEquals(8.0, result.get("PM"));
    }

        @Test
        void getProductionByShift_returnsEmptyMapWhenNoData() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.empty());

                Map<String, Double> result = service.getProductionByShift("farm-001");

                assertTrue(result.isEmpty());
        }

    @Test
    void getRecentMilkings_returnsSortedDtosAndHandlesInvalidDate() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of(
                        createMilking(1, "2026-04-01", "AM", 10.0),
                        createMilking(2, "fecha-invalida", "PM", 12.0),
                        createMilking(3, "2026-04-03", "PM", 8.0)
                )));

        List<MilkingContextDTO> result = service.getRecentMilkings("farm-001", 7);

        assertEquals(3, result.size());
        assertEquals("3", result.get(0).getBovineId());
        assertNull(result.get(2).getMilkingDate());
        assertEquals(6, result.get(1).getMilkingTime().getHour());
    }

    @Test
    void getRecentMilkings_returnsEmptyListWhenRepositoryHasNoData() {
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        List<MilkingContextDTO> result = service.getRecentMilkings("farm-001", 3);

        assertTrue(result.isEmpty());
    }

        @Test
        void getRecentMilkings_returnsEmptyListWhenRepositoryReturnsEmptyList() {
                when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                                .thenReturn(Optional.of(List.of()));

                List<MilkingContextDTO> result = service.getRecentMilkings("farm-001", 3);

                assertTrue(result.isEmpty());
        }

    // ==================== ChatbotContextProvider ====================

    @Test
    void supportedIntents_returnsAggregateMilking() {
        Set<QueryIntent> result = service.supportedIntents();

        assertEquals(Set.of(QueryIntent.AGGREGATE_MILKING), result);
    }

    @Test
    void buildContext_aggregateMilking_includesShiftAndTopProducer() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.AGGREGATE_MILKING).build();
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(List.of(
                        createMilking(10, "2026-04-01", "AM", 30.0),
                        createMilking(10, "2026-04-02", "PM", 14.2)
                )));

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("PRODUCCIÓN DE LECHE"));
        assertTrue(result.contains("Bovino 10"));
        assertTrue(result.contains("AM"));
    }

    @Test
    void buildContext_aggregateMilking_withoutTopProducer_skipsSection() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.AGGREGATE_MILKING).build();
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("PRODUCCIÓN DE LECHE"));
        assertFalse(result.contains("Top productor"));
    }

    private MilkingRecord createMilking(Integer bovineId, String date, String shift, Double liters) {
        return MilkingRecord.builder()
                .pk("FARM#farm-001")
                .sk("MILKING#" + date + "#" + shift)
                .bovineId(bovineId)
                .date(date)
                .shift(shift)
                .liters(liters)
                .build();
    }
}