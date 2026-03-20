package com.cattle.config;

import com.cattle.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security.
 * Define qué endpoints requieren autenticación y cuáles son públicos.
 * Configura JWT como mecanismo de autenticación.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        if (!securityEnabled) {
            // En modo desarrollo, permitir todo sin autenticación
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }

        return http
                // Deshabilitar CSRF (API stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configuración de sesiones (stateless para JWT)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configuración de autorización por endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers(
                                "/actuator/**",
                                "/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/chat/health"
                        ).permitAll()
                        
                        // Endpoints de bovinos - requieren autenticación
                        .requestMatchers("/bovines/**").authenticated()
                        
                        // Endpoints de ordeño - requieren autenticación
                        .requestMatchers("/milking/**").authenticated()
                        
                        // Endpoints de potreros - requieren autenticación
                        .requestMatchers("/farms/**").authenticated()
                        
                        // Endpoint del chatbot - requiere autenticación
                        .requestMatchers("/api/chat/message").authenticated()
                        
                        // Cualquier otro endpoint - requiere autenticación
                        .anyRequest().authenticated()
                )
                
                // Agregar filtro JWT antes del filtro de autenticación por username/password
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }
}
