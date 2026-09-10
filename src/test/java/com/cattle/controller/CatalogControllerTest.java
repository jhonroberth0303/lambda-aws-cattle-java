package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.catalog.CatalogDomainDTO;
import com.cattle.dtos.catalog.CatalogResponseDTO;
import com.cattle.exceptions.NotFoundException;
import com.cattle.services.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class CatalogControllerTest {

    @Mock
    private LambdaContext lambdaContext;

    private CatalogController controller;
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        // Servicio real: carga catalog.yml del classpath de test.
        catalogService = new CatalogService(lambdaContext);
        controller = new CatalogController(catalogService, lambdaContext);
    }

    @Test
    void getAllCatalogs_withoutIfNoneMatch_returnsOkWithBodyAndEtag() {
        ResponseEntity<CatalogResponseDTO> response = controller.getAllCatalogs(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDomains()).containsKey("bovine-events");
        assertThat(response.getHeaders().getETag()).isEqualTo(catalogService.getEtag());
        assertThat(response.getHeaders().getCacheControl()).contains("max-age");
    }

    @Test
    void getAllCatalogs_withMatchingIfNoneMatch_returns304WithoutBody() {
        ResponseEntity<CatalogResponseDTO> response = controller.getAllCatalogs(catalogService.getEtag());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(response.getBody()).isNull();
        assertThat(response.getHeaders().getETag()).isEqualTo(catalogService.getEtag());
    }

    @Test
    void getAllCatalogs_withStaleIfNoneMatch_returnsFullBody() {
        ResponseEntity<CatalogResponseDTO> response = controller.getAllCatalogs("\"stale-etag\"");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getCatalogDomain_knownDomain_returnsOk() {
        ResponseEntity<CatalogDomainDTO> response = controller.getCatalogDomain("bovine-events", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDomain()).isEqualTo("bovine-events");
        assertThat(response.getBody().getEntries()).isNotEmpty();
        assertThat(response.getHeaders().getFirst(HttpHeaders.ETAG)).isEqualTo(catalogService.getEtag());
    }

    @Test
    void getCatalogDomain_matchingEtag_returns304() {
        ResponseEntity<CatalogDomainDTO> response =
                controller.getCatalogDomain("bovine-events", catalogService.getEtag());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getCatalogDomain_unknownDomain_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> controller.getCatalogDomain("unknown-domain", null));
    }
}
