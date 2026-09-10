package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.settings.SiteSettingDTO;
import com.cattle.dtos.settings.SiteSettingUpdateRequestDTO;
import com.cattle.dtos.settings.SiteSettingsResponseDTO;
import com.cattle.exceptions.NotFoundException;
import com.cattle.processor.SiteSettingsProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class SiteSettingsControllerTest {

    @Mock
    private SiteSettingsProcessor siteSettingsProcessor;

    @Mock
    private LambdaContext lambdaContext;

    private SiteSettingsController controller;

    @BeforeEach
    void setUp() {
        openMocks(this);
        controller = new SiteSettingsController(siteSettingsProcessor, lambdaContext);
    }

    @Test
    void getAll_returnsOk() {
        SiteSettingsResponseDTO body = SiteSettingsResponseDTO.builder()
                .siteId("001").settings(List.of()).build();
        when(siteSettingsProcessor.getAll("001")).thenReturn(body);

        ResponseEntity<SiteSettingsResponseDTO> response = controller.getAll("001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("001", response.getBody().getSiteId());
        verify(siteSettingsProcessor).getAll("001");
    }

    @Test
    void getByKey_returnsOk() {
        SiteSettingDTO dto = SiteSettingDTO.builder().key("GESTATION_DAYS").value(279.0).build();
        when(siteSettingsProcessor.getByKey("001", "GESTATION_DAYS")).thenReturn(dto);

        ResponseEntity<SiteSettingDTO> response = controller.getByKey("001", "GESTATION_DAYS");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(279.0, response.getBody().getValue());
    }

    @Test
    void getByKey_unknownKey_bubblesNotFound() {
        when(siteSettingsProcessor.getByKey("001", "NOPE"))
                .thenThrow(new NotFoundException("Clave de configuración desconocida: NOPE"));

        assertThrows(NotFoundException.class, () -> controller.getByKey("001", "NOPE"));
    }

    @Test
    void updateByKey_returnsOk() {
        SiteSettingUpdateRequestDTO request = SiteSettingUpdateRequestDTO.builder().value(280).updatedBy("vet").build();
        SiteSettingDTO dto = SiteSettingDTO.builder().key("GESTATION_DAYS").value(280.0).source("STORED").build();
        when(siteSettingsProcessor.updateByKey("001", "GESTATION_DAYS", request)).thenReturn(dto);

        ResponseEntity<SiteSettingDTO> response = controller.updateByKey("001", "GESTATION_DAYS", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(280.0, response.getBody().getValue());
        verify(siteSettingsProcessor).updateByKey("001", "GESTATION_DAYS", request);
    }

    @Test
    void updateByKey_invalidPayload_bubblesIllegalArgument() {
        SiteSettingUpdateRequestDTO request = SiteSettingUpdateRequestDTO.builder().value(10).build();
        when(siteSettingsProcessor.updateByKey("001", "GESTATION_DAYS", request))
                .thenThrow(new IllegalArgumentException("El valor de GESTATION_DAYS no puede ser menor que 150.0"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateByKey("001", "GESTATION_DAYS", request));
    }
}
