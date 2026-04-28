# HU-BEDROCK-003: Seguridad & Hardening del Módulo Chatbot

**ID**: HU-BEDROCK-003  
**Prioridad**: Crítica  
**Sprint original**: S-2 (Enero 2026)  
**Dependencia**: HU-BEDROCK-001  
**Estado actual**: Revisado documentalmente - Abril 2026

## Objetivo

Mantener una definición verificable de los controles de seguridad del módulo chatbot integrado, distinguiendo lo ya implementado de los gaps que siguen abiertos en el código actual.

## Alineación vigente

- El módulo Bedrock está integrado en `lambda-aws-cattle-java`.
- Los endpoints relevantes del chatbot son `/api/chat/message`, `/api/chat/knowledge` y `/api/chat/health`.
- Los endpoints de salud generales del backend están en `/actuator/ping`.

## Evidencia revisada

- `src/main/java/com/cattle/config/SecurityConfig.java`
- `src/main/java/com/cattle/security/JwtAuthenticationFilter.java`
- `src/main/java/com/cattle/security/JwtTokenProvider.java`
- `src/main/java/com/cattle/controller/ChatbotController.java`
- `src/main/java/com/cattle/services/InputValidationService.java`
- `src/main/java/com/cattle/services/RateLimitingService.java`
- `src/main/java/com/cattle/services/AuditLoggingService.java`

## Criterios de aceptación vigentes

### CA-001: Autenticación JWT configurable

Cuando `security.enabled=true`, las rutas protegidas del chatbot deben requerir autenticación JWT válida y permitir extraer `farmId` y `userId` del contexto de seguridad.

### CA-002: Sanitización de input

Las solicitudes del chatbot y de Knowledge Base deben sanitizarse antes de invocar Bedrock o sus servicios asociados.

### CA-003: Rate limiting por finca

Las consultas del chatbot deben limitarse por finca y devolver señales HTTP y de auditoría consistentes cuando se excede el límite.

### CA-004: Auditoría

Las operaciones del chatbot deben dejar trazabilidad suficiente para:

- solicitudes exitosas
- errores de validación
- errores internos
- exceso de rate limiting

### CA-005: Respuesta segura ante error

Los errores deben retornar mensajes genéricos hacia el cliente y reservar el detalle técnico para logs/auditoría.

## Estado observado en el código

### Controles visibles

- `JwtAuthenticationFilter` existe
- `JwtTokenProvider` existe
- `SecurityConfig` existe y puede habilitar o deshabilitar seguridad
- `ChatbotController` usa `SecurityContext`, rate limiting, sanitización y auditoría

### Gap importante detectado

La configuración de seguridad revisada protege de forma explícita `/api/chat/message`, pero la alineación declarativa con `/api/chat/knowledge` no está igual de clara en `SecurityConfig`, aunque el controlador sí depende del contexto autenticado para operar correctamente.

## Riesgos abiertos

- divergencia entre política declarativa de seguridad y comportamiento esperado del endpoint `/api/chat/knowledge`
- dependencia de variables de entorno para activar seguridad y para secreto JWT
- necesidad de mantener sincronizadas la documentación de seguridad y la implementación real del filtro/configuración

## Siguiente paso recomendado

1. Alinear `SecurityConfig` con todos los endpoints Bedrock realmente expuestos.
2. Dejar pruebas explícitas para `/api/chat/message` y `/api/chat/knowledge` en modo seguridad habilitada.
3. Mantener esta HU como referencia de control, no como sustituto de la arquitectura base ni de la implementación.