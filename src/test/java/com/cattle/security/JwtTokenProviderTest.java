package com.cattle.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtTokenProvider.
 * Valida la lógica de validación y extracción de tokens JWT.
 */
@Tag("unit")
@Tag("security")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    
    private static final String TEST_SECRET = "cattle-test-secret-key-minimum-256-bits-32-chars!";
    private static final String TEST_ISSUER = "cattle-test";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtIssuer", TEST_ISSUER);
        ReflectionTestUtils.setField(jwtTokenProvider, "validateIssuer", false);
    }

    // ==================== validateToken Tests ====================

    @Test
    void validateToken_validToken_returnsTrue() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", 3600000);

        // Act
        boolean result = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", -1000); // Expirado

        // Act
        boolean result = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        // Act
        boolean result = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        // Act
        boolean result = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_blankToken_returnsFalse() {
        // Act
        boolean result = jwtTokenProvider.validateToken("   ");

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        // Act
        boolean result = jwtTokenProvider.validateToken("not.a.valid.jwt.token");

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_invalidSignature_returnsFalse() {
        // Arrange - Token firmado con otra clave
        String differentSecret = "different-secret-key-minimum-256-bits-32-chars!";
        SecretKey wrongKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        
        String token = Jwts.builder()
                .subject("user123")
                .claim("farmId", "FARM#001")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(wrongKey)
                .compact();

        // Act
        boolean result = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(result);
    }

    // ==================== extractFarmId Tests ====================

    @Test
    void extractFarmId_validTokenWithFarmIdClaim_returnsFarmId() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", 3600000);

        // Act
        Optional<String> result = jwtTokenProvider.extractFarmId(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("FARM#001", result.get());
    }

    @Test
    void extractFarmId_validTokenWithoutFarmId_usesSubjectAsFallback() {
        // Arrange
        String token = createTokenWithoutFarmId("user123");

        // Act
        Optional<String> result = jwtTokenProvider.extractFarmId(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("FARM#user123", result.get());
    }

    @Test
    void extractFarmId_validTokenWithEmailSubject_extractsUsername() {
        // Arrange
        String token = createTokenWithoutFarmId("john.doe@example.com");

        // Act
        Optional<String> result = jwtTokenProvider.extractFarmId(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("FARM#john.doe", result.get());
    }

    @Test
    void extractFarmId_invalidToken_returnsEmpty() {
        // Act
        Optional<String> result = jwtTokenProvider.extractFarmId("invalid.token");

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== extractUserId Tests ====================

    @Test
    void extractUserId_validToken_returnsUserId() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", 3600000);

        // Act
        Optional<String> result = jwtTokenProvider.extractUserId(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("user123", result.get());
    }

    @Test
    void extractUserId_invalidToken_returnsEmpty() {
        // Act
        Optional<String> result = jwtTokenProvider.extractUserId("invalid.token");

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== extractEmail Tests ====================

    @Test
    void extractEmail_validTokenWithEmail_returnsEmail() {
        // Arrange
        String token = createTokenWithEmail("user123", "FARM#001", "john@example.com");

        // Act
        Optional<String> result = jwtTokenProvider.extractEmail(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get());
    }

    @Test
    void extractEmail_validTokenWithoutEmail_returnsEmpty() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", 3600000);

        // Act
        Optional<String> result = jwtTokenProvider.extractEmail(token);

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== isTokenExpired Tests ====================

    @Test
    void isTokenExpired_validToken_returnsFalse() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", 3600000);

        // Act
        boolean result = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void isTokenExpired_expiredToken_returnsTrue() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", -1000);

        // Act
        boolean result = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void isTokenExpired_invalidToken_returnsTrue() {
        // Act
        boolean result = jwtTokenProvider.isTokenExpired("invalid.token");

        // Assert
        assertTrue(result);
    }

    // ==================== extractAllClaims Tests ====================

    @Test
    void extractAllClaims_validToken_returnsClaims() {
        // Arrange
        String token = createValidToken("user123", "FARM#001", 3600000);

        // Act
        Claims claims = jwtTokenProvider.extractAllClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("user123", claims.getSubject());
        assertEquals("FARM#001", claims.get("farmId", String.class));
    }

    // ==================== Helper Methods ====================

    private String createValidToken(String subject, String farmId, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(subject)
                .claim("farmId", farmId)
                .issuer(TEST_ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    private String createTokenWithoutFarmId(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(subject)
                .issuer(TEST_ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    private String createTokenWithEmail(String subject, String farmId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(subject)
                .claim("farmId", farmId)
                .claim("email", email)
                .issuer(TEST_ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}
