package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.catalog.CatalogDomainDTO;
import com.cattle.dtos.catalog.CatalogEntryDTO;
import com.cattle.dtos.catalog.CatalogResponseDTO;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.EventType;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;
import com.cattle.enums.profiles.BovineCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class CatalogServiceTest {

    @Mock
    private LambdaContext lambdaContext;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        catalogService = new CatalogService(lambdaContext);
    }

    @Test
    void load_exposesEveryDeclaredDomain() {
        CatalogResponseDTO catalog = catalogService.getCatalog();

        assertThat(catalog.getVersion()).isNotBlank();
        assertThat(catalog.getDomains()).containsKeys(
                "bovine-events", "pasture-events", "pasture-statuses",
                "pasture-substatuses", "bovine-categories", "pasture-species", "notification-types");
    }

    @Test
    void load_assignsSequentialOrderAndKeepsLabels() {
        List<CatalogEntryDTO> events = catalogService.getCatalog().getDomains().get("bovine-events");

        assertThat(events).hasSize(BovineEventType.values().length);
        for (int i = 0; i < events.size(); i++) {
            assertThat(events.get(i).getOrder()).isEqualTo(i);
            assertThat(events.get(i).getLabel()).isNotBlank();
            assertThat(events.get(i).getCode()).isNotBlank();
        }
    }

    @Test
    void load_enumBackedDomainsMatchTheirJavaEnumExactly() {
        assertCodesMatchEnum("bovine-events", BovineEventType.class);
        assertCodesMatchEnum("pasture-events", EventType.class);
        assertCodesMatchEnum("pasture-statuses", PastureStatus.class);
        assertCodesMatchEnum("pasture-substatuses", PastureSubstatus.class);
        assertCodesMatchEnum("bovine-categories", BovineCategory.class);
    }

    @Test
    void getDomain_returnsDomainOrNull() {
        CatalogDomainDTO domain = catalogService.getDomain("pasture-species");
        assertThat(domain).isNotNull();
        assertThat(domain.getDomain()).isEqualTo("pasture-species");
        assertThat(domain.getEntries()).extracting(CatalogEntryDTO::getCode)
                .containsExactly("KIKUYO", "RYEGRASS", "CUBA22");
        assertThat(domain.getVersion()).isEqualTo(catalogService.getCatalog().getVersion());

        assertNull(catalogService.getDomain("does-not-exist"));
    }

    @Test
    void etag_isQuotedStableAndContentDerived() {
        String etag = catalogService.getEtag();
        assertThat(etag).startsWith("\"").endsWith("\"");
        assertThat(etag).contains(catalogService.getCatalog().getVersion());
        assertThat(etag).contains(catalogService.getCatalog().getHash());

        // Una segunda carga del mismo contenido produce el mismo hash/etag.
        CatalogService other = new CatalogService(lambdaContext);
        assertThat(other.getEtag()).isEqualTo(etag);
    }

    @Test
    void notificationTypes_carryIconAndTone() {
        CatalogEntryDTO milking = catalogService.getDomain("notification-types").getEntries().get(0);
        assertThat(milking.getCode()).isEqualTo("MILKING_AM_REMINDER");
        assertThat(milking.getIcon()).isNotBlank();
        assertThat(milking.getTone()).isEqualTo("warn");
    }

    private void assertCodesMatchEnum(String domain, Class<? extends Enum<?>> enumClass) {
        Set<String> codes = catalogService.getCatalog().getDomains().get(domain).stream()
                .map(CatalogEntryDTO::getCode)
                .collect(Collectors.toSet());
        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertThat(codes).as("catalog.yml domain '%s' vs enum %s", domain, enumClass.getSimpleName())
                .isEqualTo(enumNames);
    }
}
