package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.MilkPriceSettingDTO;
import com.cattle.dtos.MilkPriceSettingUpdateRequestDTO;
import com.cattle.processor.MilkPriceSettingsProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class MilkPriceSettingsControllerTest {

    @Mock
    private MilkPriceSettingsProcessor milkPriceSettingsProcessor;

    @Mock
    private LambdaContext lambdaContext;

    private MilkPriceSettingsController controller;

    @BeforeEach
    void setUp() {
        openMocks(this);
        controller = new MilkPriceSettingsController(milkPriceSettingsProcessor, lambdaContext);
    }

    @Test
    void getMilkPrice_existingSetting_returnsOk() {
        MilkPriceSettingDTO dto = MilkPriceSettingDTO.builder()
                .siteId("001")
                .milkPricePerLiter(1800.0)
                .updatedAt("2026-04-29T18:45:00Z")
                .updatedBy("jhonroberth")
                .build();

        when(milkPriceSettingsProcessor.getMilkPrice("001")).thenReturn(dto);

        ResponseEntity<MilkPriceSettingDTO> response = controller.getMilkPrice("001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1800.0, response.getBody().getMilkPricePerLiter());
        verify(milkPriceSettingsProcessor).getMilkPrice("001");
    }

    @Test
    void getMilkPrice_missingSetting_returnsOkWithDefaultValue() {
        MilkPriceSettingDTO dto = MilkPriceSettingDTO.builder()
                .siteId("001")
                .milkPricePerLiter(0.0)
                .build();

        when(milkPriceSettingsProcessor.getMilkPrice("001")).thenReturn(dto);

        ResponseEntity<MilkPriceSettingDTO> response = controller.getMilkPrice("001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody().getMilkPricePerLiter());
        verify(milkPriceSettingsProcessor).getMilkPrice("001");
    }

    @Test
    void updateMilkPrice_validPayload_returnsOk() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder()
                .milkPricePerLiter(1900.0)
                .updatedBy("jhonroberth")
                .build();
        MilkPriceSettingDTO dto = MilkPriceSettingDTO.builder()
                .siteId("001")
                .milkPricePerLiter(1900.0)
                .updatedAt("2026-04-29T18:45:00Z")
                .updatedBy("jhonroberth")
                .build();

        when(milkPriceSettingsProcessor.updateMilkPrice("001", request)).thenReturn(dto);

        ResponseEntity<MilkPriceSettingDTO> response = controller.updateMilkPrice("001", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1900.0, response.getBody().getMilkPricePerLiter());
        verify(milkPriceSettingsProcessor, times(1)).updateMilkPrice("001", request);
    }

    @Test
    void updateMilkPrice_invalidPayload_bubblesIllegalArgument() {
        MilkPriceSettingUpdateRequestDTO request = MilkPriceSettingUpdateRequestDTO.builder()
                .milkPricePerLiter(-1.0)
                .build();

        when(milkPriceSettingsProcessor.updateMilkPrice("001", request))
                .thenThrow(new IllegalArgumentException("El valor milkPricePerLiter no puede ser negativo"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.updateMilkPrice("001", request));

        assertTrue(exception.getMessage().contains("no puede ser negativo"));
    }
}