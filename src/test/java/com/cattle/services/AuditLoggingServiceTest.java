package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuditLoggingService.
 * Valida el logging estructurado de eventos de seguridad.
 */
@Tag("unit")
@Tag("security")
@ExtendWith(MockitoExtension.class)
class AuditLoggingServiceTest {

    @Mock
    private LambdaContext lambdaContext;

    @InjectMocks
    private AuditLoggingService auditLoggingService;

    // ==================== logChatEvent Tests ====================

    @Test
    void logChatEvent_validParams_logsJsonEvent() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logChatEvent("FARM#001", "user123", "QUERY_CATTLE", 150, "SUCCESS");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"CHAT_REQUEST\""));
        assertTrue(logMessage.contains("\"farmId\":\"FARM#001\""));
        assertTrue(logMessage.contains("\"userId\":\"user123\""));
        assertTrue(logMessage.contains("\"intent\":\"QUERY_CATTLE\""));
        assertTrue(logMessage.contains("\"durationMs\":150"));
        assertTrue(logMessage.contains("\"status\":\"SUCCESS\""));
    }

    @Test
    void logChatEvent_nullValues_sanitizesAsNull() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logChatEvent(null, null, null, 0, "ERROR");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"farmId\":\"null\""));
        assertTrue(logMessage.contains("\"userId\":\"null\""));
    }

    // ==================== logAuthenticationAttempt Tests ====================

    @Test
    void logAuthenticationAttempt_successfulAuth_logsEvent() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logAuthenticationAttempt("user123", true, null);

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"AUTHENTICATION\""));
        assertTrue(logMessage.contains("\"userId\":\"user123\""));
        assertTrue(logMessage.contains("\"success\":true"));
    }

    @Test
    void logAuthenticationAttempt_failedAuth_logsWithReason() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logAuthenticationAttempt("user123", false, "Invalid token");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"success\":false"));
        assertTrue(logMessage.contains("\"failureReason\":\"Invalid token\""));
    }

    // ==================== logRateLimitExceeded Tests ====================

    @Test
    void logRateLimitExceeded_logsEvent() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logRateLimitExceeded("FARM#001", 100);

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"RATE_LIMIT_EXCEEDED\""));
        assertTrue(logMessage.contains("\"farmId\":\"FARM#001\""));
        assertTrue(logMessage.contains("\"limit\":100"));
        assertTrue(logMessage.contains("\"severity\":\"WARNING\""));
    }

    // ==================== logSecurityEvent Tests ====================

    @Test
    void logSecurityEvent_customEvent_logsDetails() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logSecurityEvent("CUSTOM_EVENT", "FARM#001", "Some details");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"CUSTOM_EVENT\""));
        assertTrue(logMessage.contains("\"details\":\"Some details\""));
    }

    // ==================== logSecurityError Tests ====================

    @Test
    void logSecurityError_logsWithErrorSeverity() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logSecurityError("SECURITY_VIOLATION", "FARM#001", "Unauthorized access attempt");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"SECURITY_VIOLATION\""));
        assertTrue(logMessage.contains("\"error\":\"Unauthorized access attempt\""));
        assertTrue(logMessage.contains("\"severity\":\"ERROR\""));
    }

    // ==================== logMaliciousInputDetected Tests ====================

    @Test
    void logMaliciousInputDetected_truncatesLongInput() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        String longInput = "a".repeat(100);

        // Act
        auditLoggingService.logMaliciousInputDetected("FARM#001", longInput, "SQL_INJECTION");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"MALICIOUS_INPUT_DETECTED\""));
        assertTrue(logMessage.contains("\"detectedPattern\":\"SQL_INJECTION\""));
        // El inputPreview debe estar truncado a 50 caracteres
        assertTrue(logMessage.contains("\"inputPreview\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    void logMaliciousInputDetected_shortInput_notTruncated() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logMaliciousInputDetected("FARM#001", "DROP TABLE", "SQL_INJECTION");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"inputPreview\":\"DROP TABLE\""));
    }

    // ==================== logAccessControlViolation Tests ====================

    @Test
    void logAccessControlViolation_logsCriticalSeverity() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logAccessControlViolation("FARM#001", "FARM#002", "/bovines/123");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"eventType\":\"ACCESS_CONTROL_VIOLATION\""));
        assertTrue(logMessage.contains("\"requestingFarmId\":\"FARM#001\""));
        assertTrue(logMessage.contains("\"targetFarmId\":\"FARM#002\""));
        assertTrue(logMessage.contains("\"resource\":\"/bovines/123\""));
        assertTrue(logMessage.contains("\"severity\":\"CRITICAL\""));
    }

    // ==================== JSON Format Tests ====================

    @Test
    void logEvent_containsTimestamp() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logSecurityEvent("TEST", "FARM#001", "test");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"timestamp\":"));
    }

    @Test
    void logEvent_containsServiceName() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logSecurityEvent("TEST", "FARM#001", "test");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\"service\":\"cattle-chatbot\""));
    }

    @Test
    void logEvent_escapesSpecialCharacters() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        auditLoggingService.logSecurityEvent("TEST", "FARM#001", "Details with \"quotes\" and \nnewlines");

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        assertTrue(logMessage.contains("\\\"quotes\\\""));
        assertTrue(logMessage.contains("\\n"));
    }

    // ==================== Sanitization Tests ====================

    @Test
    void logEvent_truncatesVeryLongValues() {
        // Arrange
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        String veryLongDetails = "x".repeat(500);

        // Act
        auditLoggingService.logSecurityEvent("TEST", "FARM#001", veryLongDetails);

        // Assert
        verify(lambdaContext).logInfo(eq(LogType.SECURITY), messageCaptor.capture());
        
        String logMessage = messageCaptor.getValue();
        // Los detalles deben estar truncados a 200 caracteres + "..."
        assertTrue(logMessage.contains("..."));
    }
}
