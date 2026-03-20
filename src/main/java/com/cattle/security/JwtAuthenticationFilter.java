package com.cattle.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filtro de autenticación JWT que intercepta todas las requests HTTP.
 * Extrae y valida el token JWT del header Authorization.
 * Si el token es válido, establece la autenticación en el SecurityContext.
 * 
 * Header esperado: Authorization: Bearer {token}
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extraer token del header
            Optional<String> tokenOpt = getTokenFromRequest(request);
            
            if (tokenOpt.isPresent()) {
                String token = tokenOpt.get();
                
                // Validar token
                if (jwtTokenProvider.validateToken(token)) {
                    // Extraer información del token
                    String farmId = jwtTokenProvider.extractFarmId(token).orElse("UNKNOWN");
                    String userId = jwtTokenProvider.extractUserId(token).orElse("UNKNOWN");
                    
                    // Crear autenticación y establecer en contexto
                    setAuthenticationInContext(farmId, userId);
                    
                    log.debug("JWT authentication successful for farmId: {}, userId: {}", farmId, userId);
                } else {
                    log.warn("Invalid JWT token received from IP: {}", request.getRemoteAddr());
                }
            }
            // Si no hay token, continuar sin autenticar (SecurityConfig decidirá si permitir)
            
        } catch (Exception e) {
            log.error("Error processing JWT authentication: {}", e.getMessage());
            // No lanzar excepción, dejar que SecurityConfig maneje la falta de autenticación
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * Formato esperado: "Bearer {token}"
     * 
     * @param request HttpServletRequest
     * @return Optional con el token si existe y tiene formato válido
     */
    private Optional<String> getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isBlank()) {
                return Optional.of(token);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Establece la autenticación en el SecurityContext.
     * El farmId se almacena como principal para uso posterior en controllers.
     * 
     * @param farmId ID de la finca (principal)
     * @param userId ID del usuario
     */
    private void setAuthenticationInContext(String farmId, String userId) {
        // Crear un objeto de autenticación personalizado
        FarmUserPrincipal principal = new FarmUserPrincipal(farmId, userId);
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Determina si este filtro debe aplicarse a la request.
     * Excluye rutas públicas como /actuator/ping y /swagger-ui.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") 
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.equals("/health")
            || path.equals("/api/chat/health");
    }
}
