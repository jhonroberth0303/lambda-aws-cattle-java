package com.cattle.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Proveedor de validación y extracción de tokens JWT.
 * Valida signature, expiración y claims del token.
 * 
 * Configuración requerida en application.properties:
 * - jwt.secret: Clave secreta para validar firma (min 256 bits)
 * - jwt.issuer: Emisor esperado del token (ej: https://accounts.google.com)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_FARM_ID = "farmId";
    private static final String CLAIM_USER_ID = "sub";
    private static final String CLAIM_EMAIL = "email";

    @Value("${jwt.secret:cattle-secret-key-for-jwt-validation-minimum-256-bits-required}")
    private String jwtSecret;

    @Value("${jwt.issuer:cattle-app}")
    private String jwtIssuer;

    @Value("${jwt.validate-issuer:false}")
    private boolean validateIssuer;

    /**
     * Valida un token JWT completo.
     * Verifica: formato, signature, expiración.
     * 
     * @param token Token JWT sin prefijo "Bearer "
     * @return true si el token es válido
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Token is null or blank");
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);
            
            // Validar expiración
            if (isTokenExpired(claims)) {
                log.warn("Token has expired");
                return false;
            }

            // Validar issuer si está configurado
            if (validateIssuer && !isValidIssuer(claims)) {
                log.warn("Invalid token issuer: {}", claims.getIssuer());
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el farmId del token JWT.
     * El farmId puede estar en el claim 'farmId' o inferirse del 'sub'.
     * 
     * @param token Token JWT válido
     * @return Optional con farmId si existe
     */
    public Optional<String> extractFarmId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Primero intentar claim directo 'farmId'
            String farmId = claims.get(CLAIM_FARM_ID, String.class);
            if (farmId != null && !farmId.isBlank()) {
                return Optional.of(farmId);
            }
            
            // Fallback: usar subject como farmId (para desarrollo)
            String subject = claims.getSubject();
            if (subject != null && !subject.isBlank()) {
                // Si el subject es un email, extraer parte antes del @
                if (subject.contains("@")) {
                    return Optional.of("FARM#" + subject.split("@")[0]);
                }
                return Optional.of("FARM#" + subject);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error extracting farmId: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extrae el userId (subject) del token JWT.
     * 
     * @param token Token JWT válido
     * @return Optional con userId si existe
     */
    public Optional<String> extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            log.error("Error extracting userId: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extrae el email del token JWT.
     * 
     * @param token Token JWT válido
     * @return Optional con email si existe
     */
    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return Optional.ofNullable(claims.get(CLAIM_EMAIL, String.class));
        } catch (Exception e) {
            log.error("Error extracting email: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verifica si el token ha expirado.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return isTokenExpired(claims);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extrae todos los claims del token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma para validación.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Verifica si el token ha expirado basándose en claims.
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            // Sin expiración, considerar válido (configurable)
            return false;
        }
        return expiration.before(new Date());
    }

    /**
     * Valida que el issuer del token coincida con el esperado.
     */
    private boolean isValidIssuer(Claims claims) {
        String issuer = claims.getIssuer();
        return jwtIssuer.equals(issuer);
    }
}
