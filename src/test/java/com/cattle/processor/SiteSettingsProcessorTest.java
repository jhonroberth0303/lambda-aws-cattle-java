package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.settings.SiteSettingDTO;
import com.cattle.dtos.settings.SiteSettingUpdateRequestDTO;
import com.cattle.dtos.settings.SiteSettingsResponseDTO;
import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.SiteSettingValueType;
import com.cattle.exceptions.NotFoundException;
import com.cattle.services.SiteSettingService;
import com.cattle.services.SiteSettingsCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("processor")
class SiteSettingsProcessorTest {

    @Mock
    private SiteSettingService siteSettingService;

    @Mock
    private LambdaContext lambdaContext;

    private SiteSettingsProcessor processor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        // Catálogo real desde site-settings.yml (fuente de verdad de las claves).
        SiteSettingsCatalog catalog = new SiteSettingsCatalog(lambdaContext);
        processor = new SiteSettingsProcessor(catalog, siteSettingService, lambdaContext);
    }

    @Test
    void getAll_noStoredValues_returnsDefaultsFromCatalog() {
        when(siteSettingService.findAllCurrent("001")).thenReturn(List.of());

        SiteSettingsResponseDTO response = processor.getAll("001");

        assertTrue(response.getSettings().size() >= 4);
        SiteSettingDTO gestation = response.getSettings().stream()
                .filter(s -> s.getKey().equals("GESTATION_DAYS")).findFirst().orElseThrow();
        assertEquals("DEFAULT", gestation.getSource());
        assertEquals(279.0, gestation.getValue());
    }

    @Test
    void getAll_withStoredValue_overlaysStoredOverDefault() {
        SiteSettingItem stored = SiteSettingItem.builder()
                .settingKey("GESTATION_DAYS").valueType("NUMBER").valueNumber(283.0)
                .version(2).updatedBy("vet").build();
        when(siteSettingService.findAllCurrent("001")).thenReturn(List.of(stored));

        SiteSettingDTO gestation = processor.getAll("001").getSettings().stream()
                .filter(s -> s.getKey().equals("GESTATION_DAYS")).findFirst().orElseThrow();

        assertEquals("STORED", gestation.getSource());
        assertEquals(283.0, gestation.getValue());
        assertEquals(279.0, gestation.getDefaultValue());
        assertEquals(2, gestation.getVersion());
    }

    @Test
    void getByKey_unknownKey_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> processor.getByKey("001", "NOPE"));
    }

    @Test
    void updateByKey_validNumber_upsertsCoercedValue() {
        SiteSettingItem persisted = SiteSettingItem.builder()
                .settingKey("GESTATION_DAYS").valueType("NUMBER").valueNumber(280.0).version(1).build();
        when(siteSettingService.upsertSetting(eq("001"), eq("GESTATION_DAYS"),
                eq(SiteSettingValueType.NUMBER), any(), any(), any())).thenReturn(persisted);

        SiteSettingUpdateRequestDTO request = SiteSettingUpdateRequestDTO.builder()
                .value(280).updatedBy("vet").build();

        SiteSettingDTO result = processor.updateByKey("001", "GESTATION_DAYS", request);

        assertEquals(280.0, result.getValue());
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(siteSettingService).upsertSetting(eq("001"), eq("GESTATION_DAYS"),
                eq(SiteSettingValueType.NUMBER), valueCaptor.capture(), eq("vet"), any());
        assertEquals(280.0, valueCaptor.getValue());
    }

    @Test
    void updateByKey_belowMin_throwsIllegalArgument() {
        SiteSettingUpdateRequestDTO request = SiteSettingUpdateRequestDTO.builder().value(10).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.updateByKey("001", "GESTATION_DAYS", request));
        assertTrue(ex.getMessage().contains("menor"));
    }

    @Test
    void updateByKey_unknownKey_throwsNotFound() {
        SiteSettingUpdateRequestDTO request = SiteSettingUpdateRequestDTO.builder().value(1).build();
        assertThrows(NotFoundException.class, () -> processor.updateByKey("001", "NOPE", request));
    }

    @Test
    void updateByKey_nullValue_throwsIllegalArgument() {
        SiteSettingUpdateRequestDTO request = SiteSettingUpdateRequestDTO.builder().value(null).build();
        assertThrows(IllegalArgumentException.class,
                () -> processor.updateByKey("001", "GESTATION_DAYS", request));
    }

    @Test
    void getByKey_blankSiteId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> processor.getByKey(" ", "GESTATION_DAYS"));
    }

    @Test
    void getByKey_storedValue_returnsStored() {
        when(siteSettingService.findCurrent("001", "MILK_PRICE_PER_LITER"))
                .thenReturn(Optional.of(SiteSettingItem.builder()
                        .settingKey("MILK_PRICE_PER_LITER").valueType("NUMBER").valueNumber(1800.0).version(4).build()));

        SiteSettingDTO dto = processor.getByKey("001", "MILK_PRICE_PER_LITER");

        assertEquals(1800.0, dto.getValue());
        assertEquals("STORED", dto.getSource());
    }
}
