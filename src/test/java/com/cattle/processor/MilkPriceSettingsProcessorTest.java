package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.MilkPriceSettingDTO;
import com.cattle.dtos.MilkPriceSettingUpdateRequestDTO;
import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.LogType;
import com.cattle.services.SiteSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para MilkPriceSettingsProcessor.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Processor
 */
@Tag("unit")
@Tag("fast")
@Tag("processor")
class MilkPriceSettingsProcessorTest {

    @Mock
    private SiteSettingService siteSettingService;

    @Mock
    private LambdaContext lambdaContext;

    private MilkPriceSettingsProcessor processor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        processor = new MilkPriceSettingsProcessor(siteSettingService, lambdaContext);
    }

    // ==================== getMilkPrice ====================

    @Test
    void getMilkPrice_existingSetting_mapsAllFields() {
        SiteSettingItem stored = SiteSettingItem.builder()
                .siteId("001")
                .valueNumber(1850.0)
                .updatedAt("2026-05-01T10:00:00Z")
                .updatedBy("jhon")
                .build();
        when(siteSettingService.findCurrent("001", MilkPriceSettingsProcessor.MILK_PRICE_PER_LITER))
                .thenReturn(Optional.of(stored));

        MilkPriceSettingDTO result = processor.getMilkPrice("001");

        assertEquals("001", result.getSiteId());
        assertEquals(1850.0, result.getMilkPricePerLiter());
        assertEquals("2026-05-01T10:00:00Z", result.getUpdatedAt());
        assertEquals("jhon", result.getUpdatedBy());
    }

    @Test
    void getMilkPrice_noSetting_returnsZeroDefault() {
        when(siteSettingService.findCurrent("001", MilkPriceSettingsProcessor.MILK_PRICE_PER_LITER))
                .thenReturn(Optional.empty());

        MilkPriceSettingDTO result = processor.getMilkPrice("001");

        assertEquals("001", result.getSiteId());
        assertEquals(0.0, result.getMilkPricePerLiter());
        assertNull(result.getUpdatedAt());
        assertNull(result.getUpdatedBy());
    }

    @Test
    void getMilkPrice_blankSiteId_throwsAndSkipsService() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.getMilkPrice("  "));

        assertEquals("El siteId es obligatorio", ex.getMessage());
        verify(siteSettingService, never()).findCurrent(anyString(), anyString());
    }

    @Test
    void getMilkPrice_nullSiteId_throws() {
        assertThrows(IllegalArgumentException.class, () -> processor.getMilkPrice(null));
    }

    // ==================== updateMilkPrice ====================

    @Test
    void updateMilkPrice_validRequest_persistsAndReturnsMappedDto() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder()
                .milkPricePerLiter(2000.0)
                .updatedBy("maria")
                .build();
        SiteSettingItem persisted = SiteSettingItem.builder()
                .siteId("001")
                .valueNumber(2000.0)
                .updatedAt("2026-05-02T08:00:00Z")
                .updatedBy("maria")
                .build();
        when(siteSettingService.upsertNumberSetting(
                eq("001"),
                eq(MilkPriceSettingsProcessor.MILK_PRICE_PER_LITER),
                eq(2000.0),
                eq("maria"),
                anyString()))
                .thenReturn(persisted);

        MilkPriceSettingDTO result = processor.updateMilkPrice("001", request);

        assertEquals(2000.0, result.getMilkPricePerLiter());
        assertEquals("maria", result.getUpdatedBy());
        assertEquals("2026-05-02T08:00:00Z", result.getUpdatedAt());

        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(siteSettingService).upsertNumberSetting(eq("001"), anyString(), eq(2000.0), eq("maria"), reasonCaptor.capture());
        assertEquals("Actualizacion manual desde endpoint milk-price", reasonCaptor.getValue());
    }

    @Test
    void updateMilkPrice_zeroValue_isAllowed() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder()
                .milkPricePerLiter(0.0)
                .build();
        when(siteSettingService.upsertNumberSetting(anyString(), anyString(), eq(0.0), any(), anyString()))
                .thenReturn(SiteSettingItem.builder().siteId("001").valueNumber(0.0).build());

        MilkPriceSettingDTO result = processor.updateMilkPrice("001", request);

        assertEquals(0.0, result.getMilkPricePerLiter());
    }

    @Test
    void updateMilkPrice_nullRequest_throwsAndSkipsService() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.updateMilkPrice("001", null));

        assertEquals("El payload de actualización es obligatorio", ex.getMessage());
        verify(siteSettingService, never()).upsertNumberSetting(any(), any(), any(), any(), any());
    }

    @Test
    void updateMilkPrice_nullValue_throws() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder().build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.updateMilkPrice("001", request));

        assertEquals("El valor milkPricePerLiter es obligatorio", ex.getMessage());
    }

    @Test
    void updateMilkPrice_negativeValue_throws() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder()
                .milkPricePerLiter(-1.0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.updateMilkPrice("001", request));

        assertEquals("El valor milkPricePerLiter no puede ser negativo", ex.getMessage());
        verify(siteSettingService, never()).upsertNumberSetting(any(), any(), any(), any(), any());
    }

    @Test
    void updateMilkPrice_blankSiteId_throwsBeforeValidatingRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.updateMilkPrice("", MilkPriceSettingUpdateRequestDTO.builder().milkPricePerLiter(1.0).build()));
    }

    @Test
    void updateMilkPrice_logsProcessorInfo() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder()
                .milkPricePerLiter(10.0)
                .build();
        when(siteSettingService.upsertNumberSetting(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(SiteSettingItem.builder().siteId("001").valueNumber(10.0).build());

        processor.updateMilkPrice("001", request);

        verify(lambdaContext).logInfo(eq(LogType.PROCESSOR), anyString());
    }
}
