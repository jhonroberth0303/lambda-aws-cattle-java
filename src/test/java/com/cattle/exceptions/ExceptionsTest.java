package com.cattle.exceptions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para todas las excepciones del paquete
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Exceptions
 */
@Tag("unit")
@Tag("exceptions")
class ExceptionsTest {

    // ==================== NotFoundException Tests ====================

    @Test
    void notFoundException_withMessage_createsException() {
        // Arrange
        String message = "Bovine not found";

        // Act
        NotFoundException exception = new NotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void notFoundException_withMessageAndCause_createsException() {
        // Arrange
        String message = "Bovine not found";
        Throwable cause = new RuntimeException("Database error");

        // Act
        NotFoundException exception = new NotFoundException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void notFoundException_withCauseOnly_createsException() {
        // Arrange
        Throwable cause = new RuntimeException("Database error");

        // Act
        NotFoundException exception = new NotFoundException(cause);

        // Assert
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("Database error"));
    }

    @Test
    void notFoundException_isRuntimeException() {
        // Act
        NotFoundException exception = new NotFoundException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== ProcessingException Tests ====================

    @Test
    void processingException_withMessage_createsException() {
        // Arrange
        String message = "Processing failed";

        // Act
        ProcessingException exception = new ProcessingException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void processingException_withMessageAndCause_createsException() {
        // Arrange
        String message = "Processing failed";
        Throwable cause = new IllegalArgumentException("Invalid data");

        // Act
        ProcessingException exception = new ProcessingException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void processingException_withCauseOnly_createsException() {
        // Arrange
        Throwable cause = new IllegalStateException("Bad state");

        // Act
        ProcessingException exception = new ProcessingException(cause);

        // Assert
        assertEquals(cause, exception.getCause());
    }

    @Test
    void processingException_isRuntimeException() {
        // Act
        ProcessingException exception = new ProcessingException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== RepositoryException Tests ====================

    @Test
    void repositoryException_withMessage_createsException() {
        // Arrange
        String message = "Database connection failed";

        // Act
        RepositoryException exception = new RepositoryException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void repositoryException_withMessageAndCause_createsException() {
        // Arrange
        String message = "Query failed";
        Throwable cause = new RuntimeException("Connection timeout");

        // Act
        RepositoryException exception = new RepositoryException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void repositoryException_withCauseOnly_createsException() {
        // Arrange
        Throwable cause = new RuntimeException("DynamoDB error");

        // Act
        RepositoryException exception = new RepositoryException(cause);

        // Assert
        assertEquals(cause, exception.getCause());
    }

    @Test
    void repositoryException_isRuntimeException() {
        // Act
        RepositoryException exception = new RepositoryException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== ServiceException Tests ====================

    @Test
    void serviceException_withMessage_createsException() {
        // Arrange
        String message = "Service unavailable";

        // Act
        ServiceException exception = new ServiceException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void serviceException_withMessageAndCause_createsException() {
        // Arrange
        String message = "Service failed";
        Throwable cause = new RuntimeException("External API error");

        // Act
        ServiceException exception = new ServiceException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void serviceException_withCauseOnly_createsException() {
        // Arrange
        Throwable cause = new RuntimeException("Timeout");

        // Act
        ServiceException exception = new ServiceException(cause);

        // Assert
        assertEquals(cause, exception.getCause());
    }

    @Test
    void serviceException_isRuntimeException() {
        // Act
        ServiceException exception = new ServiceException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== Exception Hierarchy Tests ====================

    @Test
    void allExceptions_canBeThrown() {
        // Assert - all can be thrown and caught as RuntimeException
        assertThrows(RuntimeException.class, () -> {
            throw new NotFoundException("test");
        });
        assertThrows(RuntimeException.class, () -> {
            throw new ProcessingException("test");
        });
        assertThrows(RuntimeException.class, () -> {
            throw new RepositoryException("test");
        });
        assertThrows(RuntimeException.class, () -> {
            throw new ServiceException("test");
        });
    }

    @Test
    void allExceptions_canBeCaughtBySpecificType() {
        // NotFoundException
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("not found");
        });

        // ProcessingException
        assertThrows(ProcessingException.class, () -> {
            throw new ProcessingException("processing error");
        });

        // RepositoryException
        assertThrows(RepositoryException.class, () -> {
            throw new RepositoryException("repository error");
        });

        // ServiceException
        assertThrows(ServiceException.class, () -> {
            throw new ServiceException("service error");
        });
    }
}
