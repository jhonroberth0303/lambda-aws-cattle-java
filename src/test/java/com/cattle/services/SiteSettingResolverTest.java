package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.SiteSettingItem;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
@DisplayName("SiteSettingResolver Tests")
class SiteSettingResolverTest {

    @Mock private SiteSettingService siteSettingService;
    @Mock private LambdaContext lambdaContext;

    private SiteSettingResolver resolver;

    @BeforeEach
    void setUp() {
        openMocks(this);
        resolver = new SiteSettingResolver(new SiteSettingsCatalog(lambdaContext), siteSettingService);
    }

    @Test
    @DisplayName("Sin valores almacenados devuelve los defaults del catálogo, incluidos los reproductivos")
    void withoutStored_returnsCatalogDefaults() {
        when(siteSettingService.findAllCurrent("001")).thenReturn(List.of());

        Map<String, Object> values = resolver.resolveAll("001");

        assertThat(ReproductiveTaskSettings.from(values)).isEqualTo(ReproductiveTaskSettings.defaults());
    }

    @Test
    @DisplayName("Un valor almacenado gana sobre el default (CA12)")
    void storedValue_overridesDefault() {
        SiteSettingItem stored = SiteSettingItem.builder()
                .settingKey("GESTATION_DAYS").valueType("NUMBER").valueNumber(285.0).build();
        when(siteSettingService.findAllCurrent("001")).thenReturn(List.of(stored));

        ReproductiveTaskSettings settings = ReproductiveTaskSettings.from(resolver.resolveAll("001"));

        assertThat(settings.gestationDays()).isEqualTo(285);
        assertThat(settings.pregnancyCheckDays()).isEqualTo(40);
    }
}
