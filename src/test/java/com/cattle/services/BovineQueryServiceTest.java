package com.cattle.services;

import com.cattle.dtos.chatbot.BovineContextDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.enums.QueryIntent;
import com.cattle.repository.BovineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class BovineQueryServiceTest {

    @Mock
    private BovineRepository bovineRepository;

    private BovineQueryService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new BovineQueryService(bovineRepository);
    }

    @Test
    void countAllBovines_delegatesToRepository() {
        when(bovineRepository.countByFarmId("farm-001")).thenReturn(12L);

        Long result = service.countAllBovines("farm-001");

        assertEquals(12L, result);
        verify(bovineRepository).countByFarmId("farm-001");
    }

    @Test
    void countByGender_groupsAndIgnoresNullGender() {
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(8).toString()),
                createBovine(2, "female", LocalDate.now().minusMonths(10).toString()),
                createBovine(3, "male", LocalDate.now().minusMonths(12).toString()),
                createBovine(4, null, LocalDate.now().minusMonths(4).toString())
        ));

        Map<String, Long> result = service.countByGender("farm-001");

        assertEquals(2L, result.get("female"));
        assertEquals(1L, result.get("male"));
        assertFalse(result.containsKey(null));
    }

    @Test
    void countPregnantBovines_returnsRepositorySize() {
        when(bovineRepository.findByFarmIdAndStatus("farm-001", "PREGNANT")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(30).toString()),
                createBovine(2, "female", LocalDate.now().minusMonths(36).toString())
        ));

        Long result = service.countPregnantBovines("farm-001");

        assertEquals(2L, result);
    }

    @Test
    void getAgeDistribution_bucketsValidAndInvalidDates() {
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(4).toString()),
                createBovine(2, "female", LocalDate.now().minusMonths(8).toString()),
                createBovine(3, "male", LocalDate.now().minusMonths(18).toString()),
                createBovine(4, "male", LocalDate.now().minusMonths(30).toString()),
                createBovine(5, "male", LocalDate.now().minusMonths(40).toString()),
                createBovine(6, "male", "fecha-invalida")
        ));

        Map<String, Integer> result = service.getAgeDistribution("farm-001");

        assertEquals(2, result.get("0-5 meses"));
        assertEquals(1, result.get("6-11 meses"));
        assertEquals(1, result.get("12-23 meses"));
        assertEquals(1, result.get("24-35 meses"));
        assertEquals(1, result.get("36+ meses"));
    }

    @Test
    void getCalvesForWeaning_filtersAgeWindowAndNullBirthDate() {
        when(bovineRepository.findByFarmIdAndCategory("farm-001", "calf")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(5).toString()),
                createBovine(2, "female", LocalDate.now().minusMonths(8).toString()),
                createBovine(3, "female", LocalDate.now().minusMonths(9).toString()),
                createBovine(4, "female", null)
        ));

        List<BovineContextDTO> result = service.getCalvesForWeaning("farm-001");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getAgeInMonths() >= 5 && dto.getAgeInMonths() <= 8));
    }

    @Test
    void getBovinesNearCalving_mapsRepositoryItems() {
        when(bovineRepository.findByFarmIdAndStatus("farm-001", "PREGNANT")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(30).toString())
        ));

        List<BovineContextDTO> result = service.getBovinesNearCalving("farm-001", 15);

        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().getBovineId());
    }

    @Test
    void getLactatingBovines_mapsRepositoryItems() {
        when(bovineRepository.findByFarmIdAndStatus("farm-001", "LACTATING")).thenReturn(List.of(
                createBovine(2, "female", LocalDate.now().minusMonths(26).toString())
        ));

        List<BovineContextDTO> result = service.getLactatingBovines("farm-001");

        assertEquals(1, result.size());
        assertEquals("2", result.getFirst().getBovineId());
    }

    @Test
    void getAllBovinesDetails_sortsAndHandlesInvalidBornDate() {
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of(
                createBovine(2, "female", "fecha-invalida"),
                createBovine(1, "male", LocalDate.now().minusMonths(15).toString())
        ));

        List<BovineContextDTO> result = service.getAllBovinesDetails("farm-001");

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getBovineId());
        assertNull(result.get(1).getBornDate());
        assertNotNull(result.get(0).getBornDate());
    }

    // ==================== ChatbotContextProvider ====================

    @Test
    void supportedIntents_returnsTheFourBovineIntents() {
        Set<QueryIntent> result = service.supportedIntents();

        assertEquals(Set.of(QueryIntent.COUNT_BOVINES, QueryIntent.COUNT_BY_GENDER,
                QueryIntent.GET_BOVINE_DETAILS, QueryIntent.LIST_ALL_BOVINES), result);
    }

    @Test
    void buildContext_countBovines_translatesGenderLabels() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.COUNT_BOVINES).build();
        when(bovineRepository.countByFarmId("farm-001")).thenReturn(10L);
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(8).toString()),
                createBovine(2, "male", LocalDate.now().minusMonths(12).toString())
        ));

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("TOTAL DE BOVINOS: 10"));
        assertTrue(result.contains("Hembras"));
        assertTrue(result.contains("Machos"));
    }

    @Test
    void buildContext_countByGender_includesTotalGeneral() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.COUNT_BY_GENDER).build();
        when(bovineRepository.countByFarmId("farm-001")).thenReturn(5L);
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of(
                createBovine(1, "female", LocalDate.now().minusMonths(8).toString())
        ));

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("BOVINOS POR GÉNERO"));
        assertTrue(result.contains("Total general: 5"));
    }

    @Test
    void buildContext_getBovineDetails_returnsStubMessage() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.GET_BOVINE_DETAILS).build();
        when(bovineRepository.countByFarmId("farm-001")).thenReturn(1L);
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of());

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("se requiere implementar extracción de ID"));
        assertTrue(result.contains("TOTAL DE BOVINOS"));
    }

    @Test
    void buildContext_listAllBovinesEmpty_returnsEmptyMessage() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.LIST_ALL_BOVINES).build();
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of());

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("No se encontraron bovinos registrados"));
    }

    @Test
    void buildContext_listAllBovines_omitsOptionalFragmentsWhenDataMissing() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.LIST_ALL_BOVINES).build();
        BovineIdentityItem item = new BovineIdentityItem();
        item.setBovineId(1);
        item.setGender(null);
        item.setBornDate(null);
        when(bovineRepository.findAllByFarmId("farm-001")).thenReturn(List.of(item));

        String result = service.buildContext(intent, "farm-001");

        assertTrue(result.contains("Desconocido"));
        assertFalse(result.contains("Nombre:"));
        assertFalse(result.contains("Raza:"));
        assertFalse(result.contains("Edad:"));
    }

    private BovineIdentityItem createBovine(Integer id, String gender, String bornDate) {
        BovineIdentityItem item = new BovineIdentityItem();
        item.setBovineId(id);
        item.setName("Bovine-" + id);
        item.setGender(gender);
        item.setBornDate(bornDate);
        item.setBreed("Holstein");
        item.setFarmId("farm-001");
        return item;
    }
}