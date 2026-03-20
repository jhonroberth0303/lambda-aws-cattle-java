package com.cattle.config;

import com.cattle.enums.LogType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para LambdaContext
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Config
 * 
 * Nota: LambdaContext usa LambdaRuntime.getLogger() que es estático
 * y no puede ser mockeado fácilmente. Los tests verifican que no
 * se lanzan excepciones durante la ejecución.
 */
@Tag("unit")
@Tag("config")
class LambdaContextTest {

    // ==================== logInfo Tests ====================

    @Test
    void logInfo_withTypeAndMessage_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert - verificar que no lanza excepción
        assertDoesNotThrow(() -> lambdaContext.logInfo(LogType.REPOSITORY, "Test message"));
    }

    @Test
    void logInfo_withServiceType_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logInfo(LogType.SERVICE, "Service operation"));
    }

    @Test
    void logInfo_withControllerType_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logInfo(LogType.CONTROLLER, "Controller log"));
    }

    @Test
    void logInfo_withProcessorType_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logInfo(LogType.PROCESSOR, "Processor log"));
    }

    // ==================== logException (message only) Tests ====================

    @Test
    void logException_withMessageOnly_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.REPOSITORY, "Error occurred"));
    }

    @Test
    void logException_messageOnly_allTypes_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.SERVICE, "Service error"));
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.CONTROLLER, "Controller error"));
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.PROCESSOR, "Processor error"));
    }

    // ==================== logException (with Exception) Tests ====================

    @Test
    void logException_withException_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        Exception testException = new RuntimeException("Test exception");
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.PROCESSOR, "Processing failed", testException));
    }

    @Test
    void logException_withNullException_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.CONTROLLER, "Error without exception", null));
    }

    @Test
    void logException_withNestedExceptionStackTrace_doesNotThrow() {
        // Arrange
        LambdaContext lambdaContext = new LambdaContext();
        Exception cause = new IllegalArgumentException("Root cause");
        Exception testException = new RuntimeException("Wrapper exception", cause);
        
        // Act & Assert
        assertDoesNotThrow(() -> lambdaContext.logException(LogType.SERVICE, "Nested exception", testException));
    }

    // ==================== Instance Creation Tests ====================

    @Test
    void lambdaContext_canBeInstantiated() {
        // Act
        LambdaContext lambdaContext = new LambdaContext();
        
        // Assert
        assertNotNull(lambdaContext);
    }
}
