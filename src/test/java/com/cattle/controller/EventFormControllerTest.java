package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.forms.EventFormSchemasDTO;
import com.cattle.exceptions.NotFoundException;
import com.cattle.services.EventFormCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class EventFormControllerTest {

    @Mock
    private LambdaContext lambdaContext;

    private EventFormController controller;
    private EventFormCatalog catalog;

    @BeforeEach
    void setUp() {
        openMocks(this);
        catalog = new EventFormCatalog(lambdaContext);
        controller = new EventFormController(catalog, lambdaContext);
    }

    @Test
    void getDomainSchema_knownDomain_returnsOkWithEtagAndCache() {
        ResponseEntity<EventFormSchemasDTO> response = controller.getDomainSchema("bovine-events", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDomain()).isEqualTo("bovine-events");
        assertThat(response.getBody().getSchemas()).containsKey("PESAJE");
        assertThat(response.getHeaders().getETag()).isEqualTo(catalog.getEtag());
        assertThat(response.getHeaders().getCacheControl()).contains("max-age");
    }

    @Test
    void getDomainSchema_matchingEtag_returns304() {
        ResponseEntity<EventFormSchemasDTO> response =
                controller.getDomainSchema("bovine-events", catalog.getEtag());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getDomainSchema_staleEtag_returnsFullBody() {
        ResponseEntity<EventFormSchemasDTO> response =
                controller.getDomainSchema("bovine-events", "\"stale\"");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getDomainSchema_domainWithoutSchemas_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> controller.getDomainSchema("pasture-events", null));
    }
}
