package com.cattle.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para JwtAuthenticationFilter.
 * Valida el filtro de autenticación JWT en requests HTTP.
 */
@Tag("unit")
@Tag("security")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    // ==================== doFilterInternal Tests ====================

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.extractFarmId(validToken)).thenReturn(Optional.of("FARM#001"));
        when(jwtTokenProvider.extractUserId(validToken)).thenReturn(Optional.of("user123"));

        // Act - Llamar directamente al método protegido
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        
        FarmUserPrincipal principal = (FarmUserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        assertEquals("FARM#001", principal.getFarmId());
        assertEquals("user123", principal.getUserId());
    }

    @Test
    void doFilterInternal_noAuthHeader_continuesWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_invalidToken_continuesWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + invalidToken);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_malformedAuthHeader_continuesWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("NotBearer token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_emptyBearerToken_continuesWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_tokenWithMissingFarmId_usesUnknown() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.extractFarmId(validToken)).thenReturn(Optional.empty());
        when(jwtTokenProvider.extractUserId(validToken)).thenReturn(Optional.empty());

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        FarmUserPrincipal principal = (FarmUserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        assertEquals("UNKNOWN", principal.getFarmId());
        assertEquals("UNKNOWN", principal.getUserId());
    }

    // ==================== shouldNotFilter Tests ====================

    @Test
    void shouldNotFilter_actuatorPing_returnsTrue() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/ping");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_swaggerUi_returnsTrue() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_apiDocs_returnsTrue() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_health_returnsTrue() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/health");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_chatHealth_returnsTrue() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/chat/health");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_apiChatMessage_returnsFalse() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/chat/message");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldNotFilter_bovines_returnsFalse() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/bovines");

        // Act
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(result);
    }
}
