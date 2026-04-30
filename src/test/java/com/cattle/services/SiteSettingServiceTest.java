package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.SiteSettingItem;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.SiteSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class SiteSettingServiceTest {

    @Mock
    private SiteSettingRepository siteSettingRepository;

    @Mock
    private LambdaContext lambdaContext;

    private SiteSettingService siteSettingService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        siteSettingService = new SiteSettingService(siteSettingRepository, lambdaContext);
    }

    @Test
    void findCurrent_existingSetting_returnsItem() {
        SiteSettingItem current = SiteSettingItem.builder()
                .siteId("001")
                .settingKey("MILK_PRICE_PER_LITER")
                .valueType("NUMBER")
                .valueNumber(1800.0)
                .version(1)
                .build();

        when(siteSettingRepository.findCurrent("001", "MILK_PRICE_PER_LITER")).thenReturn(Optional.of(current));

        Optional<SiteSettingItem> result = siteSettingService.findCurrent("001", "MILK_PRICE_PER_LITER");

        assertTrue(result.isPresent());
        assertEquals(1800.0, result.get().getValueNumber());
        verify(siteSettingRepository).findCurrent("001", "MILK_PRICE_PER_LITER");
    }

    @Test
    void findCurrent_repositoryFailure_throwsServiceException() {
        when(siteSettingRepository.findCurrent("001", "MILK_PRICE_PER_LITER"))
                .thenThrow(new RepositoryException("ddb failure"));

        assertThrows(ServiceException.class,
                () -> siteSettingService.findCurrent("001", "MILK_PRICE_PER_LITER"));

        verify(siteSettingRepository).findCurrent("001", "MILK_PRICE_PER_LITER");
    }

    @Test
    void upsertNumberSetting_withoutExisting_createsCurrentAndHistoryVersionOne() {
        when(siteSettingRepository.findCurrent("001", "MILK_PRICE_PER_LITER")).thenReturn(Optional.empty());
        when(siteSettingRepository.saveCurrent(any(SiteSettingItem.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
        when(siteSettingRepository.saveHistorySnapshot(any(SiteSettingItem.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        SiteSettingItem result = siteSettingService.upsertNumberSetting(
                "001",
                "MILK_PRICE_PER_LITER",
                1800.0,
                "jhonroberth",
                "Actualizacion manual"
        );

        assertEquals(1, result.getVersion());
        assertEquals(1800.0, result.getValueNumber());
        assertEquals("SITE#001", result.getPk());
        assertEquals("SETTING#MILK_PRICE_PER_LITER#CURRENT", result.getSk());

        ArgumentCaptor<SiteSettingItem> currentCaptor = ArgumentCaptor.forClass(SiteSettingItem.class);
        ArgumentCaptor<SiteSettingItem> historyCaptor = ArgumentCaptor.forClass(SiteSettingItem.class);

        verify(siteSettingRepository).saveCurrent(currentCaptor.capture());
        verify(siteSettingRepository).saveHistorySnapshot(historyCaptor.capture());

        assertEquals("SETTING#MILK_PRICE_PER_LITER#CURRENT", currentCaptor.getValue().getSk());
        assertTrue(historyCaptor.getValue().getSk().startsWith("SETTING#MILK_PRICE_PER_LITER#HISTORY#"));
        assertEquals(1, historyCaptor.getValue().getVersion());
    }

    @Test
    void upsertNumberSetting_withExisting_incrementsVersionAndPreservesCreatedAt() {
        SiteSettingItem existing = SiteSettingItem.builder()
                .pk("SITE#001")
                .sk("SETTING#MILK_PRICE_PER_LITER#CURRENT")
                .siteId("001")
                .settingKey("MILK_PRICE_PER_LITER")
                .valueType("NUMBER")
                .valueNumber(1700.0)
                .version(2)
                .createdAt("2026-04-28T10:00:00Z")
                .updatedAt("2026-04-28T12:00:00Z")
                .updatedBy("old-user")
                .build();

        when(siteSettingRepository.findCurrent("001", "MILK_PRICE_PER_LITER")).thenReturn(Optional.of(existing));
        when(siteSettingRepository.saveCurrent(any(SiteSettingItem.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
        when(siteSettingRepository.saveHistorySnapshot(any(SiteSettingItem.class))).thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        SiteSettingItem result = siteSettingService.upsertNumberSetting(
                "001",
                "MILK_PRICE_PER_LITER",
                1900.0,
                "new-user",
                "Actualizacion manual"
        );

        assertEquals(3, result.getVersion());
        assertEquals("2026-04-28T10:00:00Z", result.getCreatedAt());
        assertEquals(1900.0, result.getValueNumber());
        assertEquals("new-user", result.getUpdatedBy());
    }
}