package com.cattle.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GlobalExceptionHandler
 * HU-002-pruebas-summary - Aumento de cobertura
 */
@Tag("unit")
@Tag("fast")
@Tag("exceptions")
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleValidationExceptions Tests")
    class HandleValidationExceptionsTests {

        @Test
        @DisplayName("Debe retornar BAD_REQUEST con errores de campo")
        void handleValidationExceptions_returnsFieldErrors() {
            // Arrange
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            
            FieldError fieldError1 = new FieldError("object", "name", "El nombre es requerido");
            FieldError fieldError2 = new FieldError("object", "email", "El email es inválido");
            
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
            assertEquals("Errores de validación", response.getBody().get("error"));
            assertNotNull(response.getBody().get("fieldErrors"));
            
            @SuppressWarnings("unchecked")
            Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
            assertEquals("El nombre es requerido", fieldErrors.get("name"));
            assertEquals("El email es inválido", fieldErrors.get("email"));
        }
    }

    @Nested
    @DisplayName("handleServiceException Tests")
    class HandleServiceExceptionTests {

        @Test
        @DisplayName("Debe retornar INTERNAL_SERVER_ERROR con mensaje")
        void handleServiceException_returnsInternalServerError() {
            // Arrange
            ServiceException ex = new ServiceException("Error de procesamiento");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleServiceException(ex);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
            assertEquals("Error de servicio", response.getBody().get("error"));
            assertEquals("Error de procesamiento", response.getBody().get("message"));
        }
    }

    @Nested
    @DisplayName("handleRepositoryException Tests")
    class HandleRepositoryExceptionTests {

        @Test
        @DisplayName("Debe retornar INTERNAL_SERVER_ERROR sin mensaje detallado")
        void handleRepositoryException_returnsInternalServerError() {
            // Arrange
            RepositoryException ex = new RepositoryException("Error de DB");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleRepositoryException(ex);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
            assertEquals("Error de acceso a datos", response.getBody().get("error"));
            assertEquals("Error interno del servidor", response.getBody().get("message"));
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException Tests")
    class HandleIllegalArgumentExceptionTests {

        @Test
        @DisplayName("Debe retornar BAD_REQUEST con mensaje")
        void handleIllegalArgumentException_returnsBadRequest() {
            // Arrange
            IllegalArgumentException ex = new IllegalArgumentException("ID inválido");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(ex);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
            assertEquals("Argumento inválido", response.getBody().get("error"));
            assertEquals("ID inválido", response.getBody().get("message"));
        }
    }

    @Nested
    @DisplayName("handleNotFoundException Tests")
    class HandleNotFoundExceptionTests {

        @Test
        @DisplayName("Debe retornar NOT_FOUND para NoHandlerFoundException")
        void handleNotFoundException_noHandler_returnsNotFound() throws Exception {
            // Arrange
            NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/unknown", null);

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleNotFoundException(ex);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
            assertEquals("Recurso no encontrado", response.getBody().get("error"));
            assertEquals("La ruta solicitada no existe", response.getBody().get("message"));
        }

        @Test
        @DisplayName("Debe retornar NOT_FOUND para NoResourceFoundException")
        void handleNotFoundException_noResource_returnsNotFound() {
            // Arrange
            NoResourceFoundException ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/static/unknown");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleNotFoundException(ex);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        }
    }

    @Nested
    @DisplayName("handleGenericException Tests")
    class HandleGenericExceptionTests {

        @Test
        @DisplayName("Debe retornar INTERNAL_SERVER_ERROR para excepciones genéricas")
        void handleGenericException_returnsInternalServerError() {
            // Arrange
            Exception ex = new RuntimeException("Error inesperado");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
            assertEquals("Error interno", response.getBody().get("error"));
            assertEquals("Ha ocurrido un error inesperado", response.getBody().get("message"));
        }

        @Test
        @DisplayName("Debe incluir timestamp en la respuesta")
        void handleGenericException_includesTimestamp() {
            // Arrange
            Exception ex = new NullPointerException("null");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

            // Assert
            assertNotNull(response.getBody().get("timestamp"));
        }
    }
}
