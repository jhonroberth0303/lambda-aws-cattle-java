# 🔐 HU-BEDROCK-003: Seguridad & Hardening - Análisis y Mitigación

**ID**: HU-BEDROCK-003  
**Prioridad**: 🔴 CRÍTICA  
**Estimación**: 5 puntos  
**Sprint**: S-2 (Enero 2026)  
**Estado**: ✅ Refinado (Developer)  
**Dependencia**: HU-BEDROCK-001 (implementación)  

---

## 📋 Descripción

Como **Arquitecto de Seguridad**, quiero **identificar, documentar y mitigar todas las vulnerabilidades potenciales** en el chatbot para **garantizar que no hay brechas de seguridad críticas** y **cumplir con estándares OWASP Top 10**.

---

## 🎯 Criterios de Aceptación

### CA-001: JWT Token Validation ✅

```gherkin
Scenario: Validar JWT tokens en todas las requests
  Given una request sin Authorization header
  When Lambda recibe la request
  Then rechaza con error 401 (Unauthorized)
  
Scenario: Token expirado debe ser rechazado
  Given un JWT token expirado (exp < ahora)
  When Lambda valida el token
  Then rechaza la request
  And registra intento fallido
```

**Implementación**:
- ✅ JwtAuthenticationFilter implementado
- ✅ Token signature validation
- ✅ Token expiration check
- ✅ FarmID extraction y logging

### CA-002: Input Sanitization ✅

```gherkin
Scenario: Sanitizar input de usuario para prevenir inyecciones
  Given una query con caracteres maliciosos: "'; DROP TABLE --"
  When IntentDetectionService procesa la input
  Then sanitiza la cadena correctamente
  And no ejecuta inyecciones SQL/NoSQL
```

**Vulnerabilidades Analizadas**:
- ✅ SQL Injection (mitigation: parameterized queries)
- ✅ NoSQL Injection (mitigation: input validation)
- ✅ Command Injection (mitigation: no system calls)
- ✅ XSS (mitigation: output encoding)

### CA-003: CORS Policy ✅

```gherkin
Scenario: Validar CORS correctamente configurado
  Given una request desde origen no autorizado
  When Lambda procesa la request
  Then rechaza con error 403 (Forbidden)
  And solo permite orígenes whitelistados
```

**Implementación**:
- ✅ Whitelist de orígenes configurado
- ✅ Métodos HTTP permitidos: GET, POST
- ✅ Headers requeridos validados
- ✅ Credenciales NO enviadas

### CA-004: Rate Limiting ✅

```gherkin
Scenario: Rate limiting por usuario para prevenir DoS
  Given un usuario que hace 100+ queries en 1 hora
  When llega la query 101
  Then rechaza con 429 (Too Many Requests)
  And proporciona Retry-After header
```

**Implementación**:
- ✅ 100 requests/hora por usuario
- ✅ Token bucket algorithm
- ✅ Backoff exponencial
- ✅ Logging de rate limit exceeded

### CA-005: Error Handling sin Exposición de Datos ✅

```gherkin
Scenario: No exponer información técnica en errores
  Given una query que falla en DynamoDB
  When se lanza una excepción
  Then responde con error genérico al usuario
  And no incluye stack trace
  And registra detalle en CloudWatch
```

**Errores Seguros**:
- ✅ Generic error messages
- ✅ 500 error sin details
- ✅ Logging estructurado de errores
- ✅ No se exponen credenciales

### CA-006: Logging & Auditoría ✅

```gherkin
Scenario: Registrar todas las operaciones sensibles
  Given cada query ejecutada
  When se completa la operación
  Then registra: timestamp, userID, intent, duration, result
  And es consultable en CloudWatch
  And se puede auditar en CloudTrail
```

**Implementación**:
- ✅ Logging estructurado JSON
- ✅ CloudWatch integration
- ✅ CloudTrail for AWS API calls
- ✅ No logging de datos sensibles

### CA-007: Data Encryption ✅

```gherkin
Scenario: Encriptar datos en tránsito y en reposo
  Given comunicación con DynamoDB
  When se envía data
  Then utiliza TLS 1.2+ en tránsito
  And DynamoDB usa KMS para encryption at rest
```

### CA-008: Principle of Least Privilege ✅

```gherkin
Scenario: IAM roles con permisos mínimos necesarios
  Given Lambda function IAM role
  When ejecuta operaciones
  Then solo tiene acceso a:
    - DynamoDB table específica
    - Bedrock invoke-model
    - CloudWatch logs (put)
  And no acceso a otros servicios
```

---

## 🚨 Vulnerabilidades Identificadas & Mitigaciones

### 1. **Injection Attacks (SQL/NoSQL)**

**Riesgo**: Alto  
**Descripción**: Inyección de código SQL/NoSQL malicioso  

**Mitigación**:
```java
// ✅ CORRECTO: Parameterized queries
Table table = dynamoDb.getTable("cattle");
Key key = new Key().withPrimaryKey("cattleId", cattleId);
Item item = table.getItem(key);

// ❌ INCORRECTO: No concatenar strings
String query = "SELECT * FROM cattle WHERE id = '" + cattleId + "'";
```

---

### 2. **Broken Authentication**

**Riesgo**: Crítico  
**Descripción**: JWT tokens no validados correctamente  

**Mitigación**:
```java
// ✅ Validar JWT en cada request
@Component
public class JwtAuthenticationFilter implements OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        String token = getTokenFromRequest(request);
        if (!jwtProvider.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Continuar con request
    }
}
```

---

### 3. **Sensitive Data Exposure**

**Riesgo**: Alto  
**Descripción**: Datos sensibles en logs o respuestas  

**Mitigación**:
```java
// ✅ CORRECTO: Logging sin datos sensibles
logger.info("Query executed for farmId: {}", farmId);

// ❌ INCORRECTO: No loguear datos sensibles
logger.info("Full query result: {}", cattleList);
```

---

### 4. **Broken Access Control**

**Riesgo**: Alto  
**Descripción**: Usuarios acceden a datos de otras granjas  

**Mitigación**:
```java
// ✅ CORRECTO: Validar farmId en token
String farmId = jwtProvider.extractFarmId(token);
Query query = new Query()
    .withKeyConditionExpression("farmId = :farmId AND cattleId = :cattleId")
    .addExpressionAttributeValues(":farmId", farmId)
    .addExpressionAttributeValues(":cattleId", cattleId);
```

---

### 5. **Rate Limiting Ausente**

**Riesgo**: Medio (DoS)  
**Descripción**: Usuario puede hacer requests ilimitados  

**Mitigación**:
```java
// ✅ CORRECTO: Rate limiting implementado
@Component
public class RateLimitService {
    private final RateLimiter rateLimiter = 
        RateLimiter.create(0.0278); // ~100/hora
    
    public boolean allowRequest(String userId) {
        return rateLimiter.tryAcquire();
    }
}
```

---

### 6. **Error Handling Inseguro**

**Riesgo**: Medio  
**Descripción**: Stack traces expuestos en respuestas  

**Mitigación**:
```java
// ✅ CORRECTO: Error genérico
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleException(Exception e) {
    logger.error("Internal error", e); // Log detalle
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Internal server error"));
}
```

---

## ✅ Checklist de Seguridad

### Antes del Deploy

- [ ] JWT token validation implementado
- [ ] Input sanitization en todos los endpoints
- [ ] Rate limiting funcional (100 req/hora)
- [ ] CORS correctamente configurado
- [ ] Error handling sin exposición
- [ ] Logging estructurado sin datos sensibles
- [ ] HTTPS/TLS 1.2+ habilitado
- [ ] IAM roles con least privilege
- [ ] Secrets no en código (usar Secrets Manager)
- [ ] DynamoDB encryption at rest habilitada
- [ ] Scan de vulnerabilidades pasó (0 críticas)

### En Producción

- [ ] CloudTrail habilitado
- [ ] CloudWatch alarms configuradas
- [ ] WAF rules en API Gateway
- [ ] VPC security groups restrictivos
- [ ] Backups diarios validados
- [ ] Incident response plan documentado

---

## 📊 Herramientas de Validación

| Herramienta | Uso |
|---|---|
| **SonarQube** | Análisis de código |
| **OWASP ZAP** | Scanning de vulnerabilidades |
| **Dependabot** | Dependency vulnerabilities |
| **AWS Security Hub** | AWS security posture |
| **Burp Suite** | Penetration testing |

---

## 📋 Implementación por Fase

### Fase 1: JWT & Authentication (Día 1-2)

- [ ] JwtProvider implementado
- [ ] JwtAuthenticationFilter creado
- [ ] Token validation en todos los endpoints

### Fase 2: Input Validation & Sanitization (Día 3-4)

- [ ] Input validator creado
- [ ] Sanitization en IntentDetectionService
- [ ] Pruebas de inyección

### Fase 3: Rate Limiting & CORS (Día 5)

- [ ] RateLimiter implementado
- [ ] CORS configuration
- [ ] Tests de límite

### Fase 4: Error Handling & Logging (Día 6)

- [ ] Exception handlers seguros
- [ ] Logging estructurado
- [ ] CloudWatch integration

### Fase 5: Auditoría & Compliance (Día 7)

- [ ] Checklist de seguridad completado
- [ ] Documentación de mitigaciones
- [ ] Scan de vulnerabilidades

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Defense in Depth (Defensa en Profundidad) con Zero Trust Security Model

**Justificación:** Este patrón se seleccionó por proporcionar múltiples capas de seguridad independientes que protegen el sistema incluso si una capa falla. Defense in Depth establece capas de red (API Gateway con throttling y WAF), autenticación (JWT validation obligatoria), autorización (FarmId scoping, role-based access), aplicación (input validation, output encoding, rate limiting), datos (parameterized queries, encryption at rest/transit) y logging (audit trail completo, detección de anomalías). Zero Trust asume que ninguna solicitud es confiable por defecto, validando TODAS las requests incluso internas, autenticando y autorizando explícitamente cada operación, aplicando mínimo privilegio en todos los niveles (IAM, permisos de datos) y manteniendo logging completo para auditoría y forense. Se alinea completamente con OWASP Top 10 2021 abordando las 10 categorías de vulnerabilidades más críticas: A01 Broken Access Control mediante JWT + FarmId scoping, A02 Cryptographic Failures con TLS 1.2+ y DynamoDB encryption, A03 Injection con parameterized queries e input validation, A04 Insecure Design con Zero Trust y least privilege, A05 Security Misconfiguration con CORS restrictivo y error handling seguro, A06 Vulnerable Components con dependency scanning, A07 Identification/Auth Failures con JWT validation robusta, A08 Software/Data Integrity con signed JWTs e immutable logs, A09 Security Logging Failures con structured logging y CloudWatch/CloudTrail, y A10 SSRF con validación de URLs. Es coherente con arquitectura serverless AWS aprovechando IAM roles con least privilege nativos de Lambda, encryption at rest/transit por defecto en servicios AWS, CloudWatch/CloudTrail integrados para observabilidad, y API Gateway como primera línea de defensa.

**Componentes Afectados:**

- **JwtAuthenticationFilter (Nuevo - CRÍTICO):** Filtro de autenticación JWT para todas las requests. Interceptar todas las requests HTTP, extraer y validar JWT token del header Authorization, verificar firma del token, validar expiración, extraer farmId del payload, rechazar requests sin token o con token inválido. Implementar OncePerRequestFilter de Spring, validar formato "Bearer {token}", verificar signature con clave pública, validar claims (exp, iss, sub), extraer farmId y almacenar en SecurityContext, retornar 401 Unauthorized para tokens inválidos/expirados.

- **JwtTokenProvider (Nuevo - CRÍTICO):** Proveedor de validación de tokens JWT. Validar tokens JWT, verificar firma criptográfica, extraer claims (farmId, userId, roles), validar expiración y emisor. Usar biblioteca io.jsonwebtoken:jjwt para parsing, configurar clave pública para validación (NO clave privada en Lambda), validar exp claim < now(), validar iss claim match configuración, método extractFarmId(token) retorna farmId del payload.

- **SecurityConfig (Nuevo - CRÍTICO):** Configuración central de Spring Security. Configuración central de seguridad de Spring Boot, registrar JwtAuthenticationFilter, configurar endpoints públicos vs protegidos, deshabilitar CSRF para API stateless. Anotaciones @Configuration + @EnableWebSecurity, configurar HttpSecurity con filterBefore(JwtAuthenticationFilter), permitir /health sin autenticación, requerir autenticación para /api/chat/**, configurar CORS integration, deshabilitar session management (stateless).

- **InputValidationService (Nuevo - ALTO):** Servicio de sanitización de inputs. Sanitizar input del usuario para prevenir inyecciones, validar caracteres permitidos, detectar patrones maliciosos, normalizar input. Whitelist de caracteres permitidos (letras, números, espacios, puntuación básica), detectar SQL/NoSQL keywords maliciosos, limitar longitud máxima (1000 caracteres), normalizar Unicode, método sanitize(String input) retorna String limpio.

- **RateLimitingService (Nuevo - ALTO):** Servicio de rate limiting por usuario. Implementar rate limiting por usuario, algoritmo token bucket, enforcement de límites, tracking de uso. Usar Guava RateLimiter o implementación custom con cache en memoria (DynamoDB para persistencia opcional), límite: 100 requests/hora por farmId, método allowRequest(String farmId) retorna boolean, registrar exceeded attempts en logs, agregar headers X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset.

- **SecureExceptionHandler (Nuevo - ALTO):** Handler global de excepciones seguro. Manejar excepciones globalmente, prevenir exposición de información técnica, generar errores genéricos al usuario, logging detallado interno. Anotaciones @ControllerAdvice + @ExceptionHandler, capturar Exception.class como fallback, capturar specific exceptions (AuthenticationException, AccessDeniedException, etc.), retornar ErrorResponse genérico sin stack traces, logging completo con log.error() incluyendo stack trace SOLO en logs internos.

- **AuditLoggingService (Nuevo - MEDIO):** Servicio de audit logging estructurado. Logging estructurado de operaciones sensibles, formato JSON para parsing automático, correlación de requests. Logging en formato JSON con campos: timestamp (ISO-8601), farmId, userId, operation, intent, duration, statusCode, errorMessage (si aplica), método logSecurityEvent(SecurityEvent event), integración con CloudWatch Logs, NO loguear datos sensibles (passwords, tokens completos, PII).

- **CorsSecurityConfig (Nuevo - MEDIO):** Configuración CORS restrictiva. Configurar CORS restrictivo con whitelist de orígenes, validar orígenes permitidos, configurar métodos y headers permitidos. Reemplazar CorsConfig actual que permite "*" inseguro, whitelist específica de orígenes: ["https://cattle-front.example.com", "http://localhost:3000"], métodos permitidos: ["GET", "POST"], headers permitidos: ["Authorization", "Content-Type"], credentials: false (no permitir cookies cross-origin).

- **ChatbotController (Modificación Mayor - CRÍTICO):** Controlador REST con seguridad integrada.
  - Nivel de cambio: Mayor
  - Especificaciones: Agregar @PreAuthorize("isAuthenticated()") en endpoints protegidos, extraer farmId de SecurityContext (inyectado por JwtAuthenticationFilter), validar @RequestBody con @Valid, agregar try-catch específico sin exponer detalles técnicos, agregar rate limiting check antes de procesar request. Inyectar RateLimitingService, obtener farmId con Authentication.getPrincipal(), pasar farmId a ChatbotService, retornar 429 Too Many Requests si rate limit excedido, agregar headers de rate limit en response.

- **ChatbotService (Modificación Menor - ALTO):** Servicio de chatbot con input sanitization.
  - Nivel de cambio: Menor
  - Especificaciones: Recibir farmId como parámetro obligatorio (no confiar en datos de request), sanitizar userMessage con InputValidationService antes de procesarlo, validar longitud de mensaje (1000 chars max), agregar audit logging de operaciones. Inyectar InputValidationService y AuditLoggingService, método signature: chat(String farmId, ChatRequestDTO request), validar input antes de construir prompt, loguear evento de chat con auditLoggingService.logSecurityEvent().

- **ChatRequestDTO (Modificación Menor - MEDIO):** DTO con validaciones Bean Validation.
  - Nivel de cambio: Menor
  - Especificaciones: Agregar validaciones Bean Validation (JSR-303), @NotNull y @NotBlank en campos requeridos, @Size para limitar longitud. Anotación @NotBlank(message="User message required") en userMessage, @Size(max=1000) en userMessage, @Pattern para validar formato de conversationId si aplica.

- **CorsConfig (Modificación Mayor - CRÍTICO):** Reemplazo completo por configuración segura.
  - Nivel de cambio: Crítico (reemplazo completo)
  - Problema actual: allowedOrigins("*") es INSEGURO - permite cualquier origen hacer requests
  - Especificaciones: REEMPLAZAR con whitelist específica de orígenes, configurar allowCredentials(false), restringir métodos a GET/POST únicamente. Ver CorsSecurityConfig nuevo componente que reemplaza este.

- **Application (Modificación Menor - BAJO):** Aplicación principal con configuración de seguridad.
  - Nivel de cambio: Menor
  - Especificaciones: Agregar @EnableWebSecurity si no está en SecurityConfig, configurar propiedades de seguridad globales, verificar que componentes de seguridad estén en component scan.

- **IAM Role para Lambda (Configuración - CRÍTICO):** Rol IAM con least privilege. Aplicar principio de least privilege en permisos IAM. Permisos: DynamoDB permitir SOLO Query, GetItem, Scan en tablas específicas (TABLE_CATTLE, TABLE_FARM_MILKING, TABLE_PASTURE); Bedrock permitir SOLO bedrock:InvokeModel para modelo específico (anthropic.claude-3-haiku-20240307-v1:0); CloudWatch permitir logs:CreateLogGroup, logs:CreateLogStream, logs:PutLogEvents; DENEGAR: dynamodb:PutItem, dynamodb:DeleteItem, s3:*, ec2:*, lambda:*.

- **API Gateway Configuration (Configuración - ALTO):** API Gateway con throttling y WAF. Primera línea de defensa, throttling, WAF integration. Configuración: Throttling 1000 requests/second burst y 500 requests/second steady-state; AWS WAF con reglas para SQL Injection, XSS, rate-based blocking; Request validation para Content-Type, tamaño máximo body (100KB); TLS 1.2+ enforcement.

- **DynamoDB Encryption (Configuración - MEDIO):** Encryption at rest. Encryption at rest para proteger datos sensibles. Habilitar AWS KMS encryption para tablas DynamoDB, usar AWS managed key o customer managed key según compliance.

**Hitos de Implementación:**

1. **Fundamentos de Seguridad - Autenticación** - Capa crítica de autenticación: JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig
   - Dependencias: Agregar io.jsonwebtoken:jjwt a build.gradle, spring-boot-starter-security

2. **Validación y Sanitización de Input** - Prevención de inyecciones: InputValidationService, modificaciones en ChatRequestDTO con @Valid annotations
   - Dependencias: JwtAuthenticationFilter del hito 1 para obtener farmId autenticado

3. **Rate Limiting y DoS Protection** - Protección contra abuso: RateLimitingService, modificaciones en ChatbotController para enforcement
   - Dependencias: SecurityConfig del hito 1 para farmId autenticado, Guava rate limiter

4. **CORS Seguro y Configuración de Red** - Control de orígenes: CorsSecurityConfig reemplazando CorsConfig inseguro actual
   - Dependencias: SecurityConfig del hito 1 para integración

5. **Error Handling Seguro** - No exposición de información: SecureExceptionHandler, modificaciones en ChatbotService y ChatbotController
   - Dependencias: Todos los servicios anteriores para capturar sus excepciones

6. **Audit Logging y Observabilidad** - Trazabilidad y forense: AuditLoggingService, integración en ChatbotService y ChatbotController
   - Dependencias: Todos los componentes anteriores para loguear sus operaciones

7. **Configuración de Infraestructura AWS** - Hardening de infraestructura: IAM Role restrictivo, API Gateway throttling/WAF, DynamoDB encryption
   - Dependencias: Aplicación completa funcionando con seguridad a nivel de código

### Validación de Impacto

**Código fuente verificado en cattle-bedrock:**
- ChatbotController NO valida Authorization header - cualquier request sin autenticación es aceptada (vulnerabilidad CRÍTICA)
- NO existe JwtAuthenticationFilter ni SecurityConfig de Spring Security
- ChatbotService.buildPrompt() solo hace replace("\"", "\\\"") - INSUFICIENTE para prevenir injection
- NO valida caracteres maliciosos, SQL/NoSQL injection keywords, NO limita longitud de input (vulnerabilidad ALTA)
- ChatbotController acepta requests ilimitadas, NO existe rate limiting - usuario puede hacer DoS (vulnerabilidad MEDIA)
- CorsConfig.allowedOrigins("*") permite CUALQUIER origen (vulnerabilidad CRÍTICA), métodos incluyen DELETE y PUT innecesarios
- Error handling: ChatbotController catch(Exception e) puede exponer información técnica, ChatbotService throw RuntimeException con mensajes de error técnicos
- Logging básico existe con log.error() pero sin formato JSON estructurado, NO loguea eventos de seguridad, NO incluye metadata de seguridad

**Dependencias verificadas:**
- build.gradle NO incluye spring-boot-starter-security (crítico para agregar)
- build.gradle NO incluye io.jsonwebtoken:jjwt para JWT (crítico para agregar)
- AWS SDK ya configurado: DynamoDB Enhanced Client, Bedrock client, CloudWatch logging vía Log4j
- Mockito y JUnit disponibles para tests de seguridad

**Análisis de vulnerabilidades OWASP Top 10:**
- 🔴 A01 Broken Access Control - CRÍTICO: NO hay autenticación JWT, NO validación de farmId, usuario puede acceder a datos de cualquier farm, brecha de seguridad CRÍTICA con acceso no autorizado total
- 🔴 A02 Cryptographic Failures - MEDIO: TLS en tránsito manejado por API Gateway, DynamoDB encryption at rest por configurar, NO valida tokens JWT
- 🔴 A03 Injection - ALTO: Input NO sanitizado, vulnerable a NoSQL injection en queries DynamoDB, vulnerable a prompt injection en Bedrock
- ⚠️ A04 Insecure Design - MEDIO: NO implementa rate limiting, CORS permite cualquier origen, NO implementa least privilege en código
- 🔴 A05 Security Misconfiguration - CRÍTICO: CORS mal configurado (allowedOrigins="*"), error handling expone información técnica, NO hay SecurityConfig
- ⚠️ A06 Vulnerable Components - BAJO: Dependencias actualizadas pero NO tiene dependency scanning automático
- 🔴 A07 Identification/Authentication Failures - CRÍTICO: NO valida JWT tokens, NO hay autenticación implementada, sistema completamente abierto
- ✅ A08 Software/Data Integrity Failures - BAJO: AWS SDK usa signed requests, logs inmutables en CloudWatch
- ⚠️ A09 Security Logging Failures - MEDIO: Logging básico existe pero NO estructurado, NO loguea eventos de seguridad
- ✅ A10 SSRF - NO APLICA: Sistema no hace requests a URLs de usuario

**Resumen: 4 vulnerabilidades CRÍTICAS, 1 ALTA, 3 MEDIAS, 1 BAJA**

**Hallazgos críticos:**
1. 🔴 NO existe autenticación JWT - sistema completamente abierto, cualquiera puede hacer requests
2. 🔴 CORS configurado inseguramente - allowedOrigins("*") permite ataques CSRF cross-site
3. 🔴 NO existe input validation - vulnerable a injection attacks (NoSQL, prompt injection)
4. ⚠️ NO existe rate limiting - vulnerable a DoS attacks
5. ⚠️ Error handling inseguro - puede exponer información técnica en errores
6. ⚠️ Logging NO estructurado - dificulta auditoría y detección de anomalías
7. 🔴 Spring Security NO está configurado - framework de seguridad ausente
8. ⚠️ IAM role permissions no verificadas - posiblemente demasiado permisivo

**Decisiones arquitectónicas clave:**

**DA-001: JWT como mecanismo de autenticación (no OAuth flow completo)**
- Decisión: Validar JWT tokens generados por sistema externo (Google OAuth en frontend), NO implementar OAuth flow en Lambda
- Justificación: Lambda es stateless, no debe manejar OAuth redirects. Frontend (cattle-front) maneja Google OAuth y obtiene JWT, Lambda SOLO valida signature y claims. Simplifica arquitectura serverless, separa responsabilidades, aprovecha Google como Identity Provider

**DA-002: Farmid scoping para multi-tenancy**
- Decisión: Extraer farmId de JWT payload y usarlo en TODAS las queries DynamoDB para aislamiento de datos
- Justificación: Implementa multi-tenancy a nivel de aplicación, previene broken access control (OWASP A01), cada farm solo ve sus propios datos, simple de implementar y auditar

**DA-003: Input sanitization en capa de servicio (no solo controller)**
- Decisión: Sanitizar input en ChatbotService ANTES de construir prompt, además de validación en controller
- Justificación: Defense in depth con doble validación, protege contra bypass de validación de controller, sanitización específica para Bedrock prompts previene prompt injection, separación de responsabilidades (controller valida formato, service sanitiza contenido)

**DA-004: Rate limiting en memoria con DynamoDB backup opcional**
- Decisión: Implementar rate limiting con RateLimiter en memoria, considerar DynamoDB para persistencia cross-instances
- Justificación: In-memory es más rápido (< 1ms vs ~50ms DynamoDB), Lambda stateless permite reset entre cold starts (aceptable), DynamoDB opcional para enforcement estricto cross-instances si es crítico, balance performance vs precisión

**DA-005: CORS whitelist restrictiva en lugar de wildcard**
- Decisión: Whitelist específica de orígenes permitidos, NO allowedOrigins("*")
- Justificación: Previene CSRF attacks, controla qué frontends pueden consumir API, cumple security best practices, fácil de mantener con lista de producción/staging/dev

**DA-006: Error handling con mensajes genéricos públicos + logging detallado privado**
- Decisión: Retornar errores genéricos sin stack traces al cliente, loguear detalles completos en CloudWatch
- Justificación: Previene information disclosure (OWASP A05), facilita debugging con logs completos internos, balance entre seguridad y troubleshooting, usuarios no necesitan detalles técnicos

**DA-007: Audit logging estructurado JSON para SIEM integration**
- Decisión: Logging en formato JSON con campos estándar para parsing automático
- Justificación: Permite integración con CloudWatch Insights queries, facilita detección de anomalías, preparado para SIEM/monitoring tools futuros, auditoría y compliance, correlación de eventos

**DA-008: Least privilege IAM con deny explícito de operaciones destructivas**
- Decisión: IAM role con permisos mínimos + deny explícito de PutItem/DeleteItem en DynamoDB
- Justificación: Previene data tampering accidental o malicioso, cumple least privilege principle, defense in depth (código + infraestructura), auditable en CloudTrail

### Referencias y Validación

**Documentación consultada:**
- ChatbotController.java - Verificado: NO valida autenticación, expone endpoints sin protección
- ChatbotService.java - Verificado: NO sanitiza input, logging básico sin estructura de seguridad
- CorsConfig.java - Verificado: Configuración insegura con allowedOrigins("*")
- build.gradle - Verificado: NO incluye spring-boot-starter-security ni jjwt
- ARQUITECTURA-ECOSISTEMA-CATTLE.md - Menciona Google OAuth en frontend, confirma necesidad de validación JWT en Lambda
- OWASP Top 10 2021 - Framework de referencia para análisis de vulnerabilidades

**Historias relacionadas:**
- Historia HU-BEDROCK-001 (Implementación): Define componentes a asegurar (ChatbotController, ChatbotService, repositories), especifica CA-006 (JWT) y CA-007 (Rate Limiting)
- Historia HU-BEDROCK-002 (Testing): Debe incluir 12 tests de seguridad para validar implementaciones (JWTValidationTest, InputSanitizationTest, RateLimitingSecurityTest)
- Historia HU-BEDROCK-004 (Documentación): Debe documentar configuración de seguridad, troubleshooting de errores de autenticación, guía de security best practices

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-16 | **Enfoque:** Exploratorio

---

## 🔧 Refinamiento Técnico (Developer)

### Plan de Implementación Detallado

**Estimación Total**: 5 puntos (30-32 horas efectivas, ~4-5 días laborales)

### HITO 1: Fundamentos de Seguridad - Autenticación (10h - Días 1-2)

**Objetivo**: Implementar autenticación JWT completa

**Tareas Técnicas**:

**T1.1: Agregar dependencias de seguridad a build.gradle (0.5h)**
- **Archivo a modificar**: `build.gradle`
- **Dependencias a agregar**:
  ```gradle
  implementation 'org.springframework.boot:spring-boot-starter-security:3.4.5'
  implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
  runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
  runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
  implementation 'com.google.guava:guava:33.0.0-jre'
  ```
- **Verificación**: Build exitoso con nuevas dependencias
- **Dependencias**: Ninguna
- **Estimación**: 0.5 horas

**T1.2: Crear JwtTokenProvider (3h)**
- **Archivo a crear**: `src/main/java/com/cattle/security/JwtTokenProvider.java`
- **Métodos a implementar**:
  ```java
  @Component
  public class JwtTokenProvider {
      @Value("${jwt.secret}")
      private String jwtSecret;
      
      @Value("${jwt.issuer}")
      private String jwtIssuer;
      
      public boolean validateToken(String token);
      public Claims extractClaims(String token);
      public String extractFarmId(String token);
      public String extractUserId(String token);
      public boolean isTokenExpired(String token);
      private Key getSigningKey();
  }
  ```
- **Lógica**: Usar JJWT library para parsing, validar signature con clave pública, validar expiration (exp claim < now()), validar issuer
- **Verificación**: Token válido es aceptado, token inválido/expirado es rechazado
- **Dependencias**: T1.1 (jjwt dependency)
- **Estimación**: 3 horas

**T1.3: Crear JwtAuthenticationFilter (4h)**
- **Archivo a crear**: `src/main/java/com/cattle/security/JwtAuthenticationFilter.java`
- **Métodos a implementar**:
  ```java
  @Component
  public class JwtAuthenticationFilter extends OncePerRequestFilter {
      @Autowired
      private JwtTokenProvider jwtTokenProvider;
      
      @Override
      protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain);
      private String getTokenFromRequest(HttpServletRequest request);
      private void setAuthenticationInContext(String farmId, String userId);
  }
  ```
- **Lógica**: Interceptar todas las requests, extraer token de Authorization header ("Bearer {token}"), validar con JwtTokenProvider, si válido: extraer farmId y userId, almacenar en SecurityContext; si inválido: retornar 401 Unauthorized
- **Verificación**: Requests con token válido pasan, sin token o inválido retorna 401
- **Dependencias**: T1.2 (JwtTokenProvider)
- **Estimación**: 4 horas

**T1.4: Crear SecurityConfig (2.5h)**
- **Archivo a crear**: `src/main/java/com/cattle/config/SecurityConfig.java`
- **Configuración**:
  ```java
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig {
      @Autowired
      private JwtAuthenticationFilter jwtAuthenticationFilter;
      
      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http);
      @Bean
      public AuthenticationManager authenticationManager();
  }
  ```
- **Lógica**: Deshabilitar CSRF (stateless API), permitir /health sin autenticación, requerir autenticación para /api/chat/**, registrar JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter, configurar session management como STATELESS
- **Verificación**: /health accesible sin token, /api/chat/message requiere token
- **Dependencias**: T1.3 (JwtAuthenticationFilter)
- **Estimación**: 2.5 horas

---

### HITO 2: Validación y Sanitización de Input (6h - Día 3)

**Objetivo**: Prevenir injection attacks

**Tareas Técnicas**:

**T2.1: Crear InputValidationService (3h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/InputValidationService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class InputValidationService {
      private static final int MAX_LENGTH = 1000;
      private static final Pattern ALLOWED_CHARS = Pattern.compile("[a-zA-Z0-9\\s.,¿?¡!áéíóúñÁÉÍÓÚÑ-]*");
      private static final List<String> SQL_KEYWORDS = Arrays.asList("DROP", "DELETE", "INSERT", "UPDATE", "SELECT", "--", ";");
      
      public String sanitize(String input);
      private boolean containsMaliciousPatterns(String input);
      private String removeSpecialChars(String input);
      public void validateLength(String input);
  }
  ```
- **Lógica**: Validar longitud ≤1000, whitelist de caracteres (letras, números, espacios, puntuación básica), detectar SQL/NoSQL keywords maliciosos, normalizar Unicode, remover caracteres peligrosos
- **Verificación**: Input malicioso es sanitizado, input válido pasa sin cambios
- **Dependencias**: Ninguna
- **Estimación**: 3 horas

**T2.2: Agregar validaciones a ChatRequestDTO (1h)**
- **Archivo a modificar**: `src/main/java/com/cattle/dtos/ChatRequestDTO.java`
- **Validaciones a agregar**:
  ```java
  @Data
  public class ChatRequestDTO {
      @NotBlank(message = "User message is required")
      @Size(max = 1000, message = "Message too long")
      private String userMessage;
      
      @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid conversation ID format")
      private String conversationId;
  }
  ```
- **Verificación**: Validaciones de Bean Validation funcionan
- **Dependencias**: Ninguna (Bean Validation ya disponible)
- **Estimación**: 1 hora

**T2.3: Integrar sanitization en ChatbotService (2h)**
- **Archivo a modificar**: `src/main/java/com/cattle/services/ChatbotService.java`
- **Cambios**:
  - Inyectar InputValidationService
  - En método chat(), antes de procesar: `String sanitizedMessage = inputValidationService.sanitize(request.getUserMessage());`
  - Validar longitud con `inputValidationService.validateLength(sanitizedMessage);`
  - Usar sanitizedMessage en lugar de userMessage original
- **Verificación**: Input es sanitizado antes de procesamiento
- **Dependencias**: T2.1 (InputValidationService)
- **Estimación**: 2 horas

---

### HITO 3: Rate Limiting y DoS Protection (5h - Día 4)

**Objetivo**: Proteger contra ataques DoS

**Tareas Técnicas**:

**T3.1: Crear RateLimitingService (3h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/RateLimitingService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class RateLimitingService {
      private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
      private static final double REQUESTS_PER_HOUR = 100.0;
      private static final double PERMITS_PER_SECOND = REQUESTS_PER_HOUR / 3600.0;
      
      public boolean allowRequest(String farmId);
      public RateLimitInfo getRateLimitInfo(String farmId);
      private RateLimiter getOrCreateLimiter(String farmId);
  }
  ```
- **Lógica**: Usar Guava RateLimiter con token bucket algorithm, 100 requests/hora = ~0.0278 permits/segundo, mantener map de limiters por farmId, método allowRequest() retorna true/false
- **Verificación**: 100 requests permitidos, request 101 rechazado
- **Dependencias**: T1.1 (Guava dependency)
- **Estimación**: 3 horas

**T3.2: Integrar rate limiting en ChatbotController (2h)**
- **Archivo a modificar**: `src/main/java/com/cattle/controller/ChatbotController.java`
- **Cambios**:
  ```java
  @PostMapping("/message")
  public ResponseEntity<ChatResponseDTO> sendMessage(
      @RequestHeader("Authorization") String authHeader,
      @RequestBody @Valid ChatRequestDTO request) {
      
      String farmId = getFarmIdFromSecurityContext();
      
      // Rate limiting check
      if (!rateLimitingService.allowRequest(farmId)) {
          return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
              .header("X-RateLimit-Limit", "100")
              .header("X-RateLimit-Remaining", "0")
              .header("Retry-After", "3600")
              .body(new ChatResponseDTO("Rate limit exceeded"));
      }
      
      ChatResponseDTO response = chatbotService.chat(farmId, request);
      
      // Agregar headers de rate limit en response exitoso
      RateLimitInfo info = rateLimitingService.getRateLimitInfo(farmId);
      return ResponseEntity.ok()
          .header("X-RateLimit-Limit", "100")
          .header("X-RateLimit-Remaining", String.valueOf(info.getRemaining()))
          .body(response);
  }
  ```
- **Verificación**: Request 101 retorna 429, headers de rate limit presentes
- **Dependencias**: T3.1 (RateLimitingService), T1.3 (SecurityContext con farmId)
- **Estimación**: 2 horas

---

### HITO 4: CORS Seguro y Configuración de Red (2h - Día 4 PM)

**Objetivo**: Configurar CORS restrictivo

**Tareas Técnicas**:

**T4.1: Reemplazar CorsConfig por CorsSecurityConfig (2h)**
- **Archivo a eliminar**: `src/main/java/com/cattle/config/CorsConfig.java`
- **Archivo a crear**: `src/main/java/com/cattle/config/CorsSecurityConfig.java`
- **Configuración**:
  ```java
  @Configuration
  public class CorsSecurityConfig {
      @Bean
      public WebMvcConfigurer corsConfigurer() {
          return new WebMvcConfigurer() {
              @Override
              public void addCorsMappings(CorsRegistry registry) {
                  registry.addMapping("/api/**")
                      .allowedOrigins(
                          "https://cattle-front.example.com",
                          "http://localhost:3000",
                          "http://localhost:5173"
                      )
                      .allowedMethods("GET", "POST")
                      .allowedHeaders("Authorization", "Content-Type")
                      .allowCredentials(false)
                      .maxAge(3600);
              }
          };
      }
  }
  ```
- **Cambios críticos**: Reemplazar allowedOrigins("*") por whitelist específica, restringir métodos a GET/POST, no permitir credentials
- **Verificación**: Origen no whitelistado es rechazado
- **Dependencias**: Ninguna
- **Estimación**: 2 horas

---

### HITO 5: Error Handling Seguro (4h - Día 5 AM)

**Objetivo**: No exponer información técnica

**Tareas Técnicas**:

**T5.1: Crear SecureExceptionHandler (2.5h)**
- **Archivo a crear**: `src/main/java/com/cattle/exceptions/SecureExceptionHandler.java`
- **Handlers a implementar**:
  ```java
  @ControllerAdvice
  public class SecureExceptionHandler {
      @ExceptionHandler(AuthenticationException.class)
      public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e);
      
      @ExceptionHandler(AccessDeniedException.class)
      public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e);
      
      @ExceptionHandler(MethodArgumentNotValidException.class)
      public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e);
      
      @ExceptionHandler(Exception.class)
      public ResponseEntity<ErrorResponse> handleGenericException(Exception e);
      
      private ErrorResponse buildErrorResponse(String message, HttpStatus status);
  }
  ```
- **Lógica**: Capturar excepciones globalmente, loguear detalles con log.error() incluyendo stack trace, retornar ErrorResponse genérico al cliente sin stack trace
- **Verificación**: Errores retornan mensajes genéricos, logs internos tienen detalles completos
- **Dependencias**: Ninguna
- **Estimación**: 2.5 horas

**T5.2: Crear clase ErrorResponse (0.5h)**
- **Archivo a crear**: `src/main/java/com/cattle/dtos/ErrorResponse.java`
- **Campos**:
  ```java
  @Data
  @AllArgsConstructor
  public class ErrorResponse {
      private String message;
      private int status;
      private LocalDateTime timestamp;
      private String path;
  }
  ```
- **Verificación**: DTO compilable
- **Dependencias**: Ninguna
- **Estimación**: 0.5 horas

**T5.3: Refactorizar error handling en ChatbotController (1h)**
- **Archivo a modificar**: `src/main/java/com/cattle/controller/ChatbotController.java`
- **Cambios**: Remover try-catch actual que expone detalles técnicos, dejar que SecureExceptionHandler maneje excepciones
- **Verificación**: Excepciones manejadas por handler global
- **Dependencias**: T5.1 (SecureExceptionHandler)
- **Estimación**: 1 hora

---

### HITO 6: Audit Logging y Observabilidad (3h - Día 5 PM)

**Objetivo**: Trazabilidad completa y forense

**Tareas Técnicas**:

**T6.1: Crear AuditLoggingService (2h)**
- **Archivo a crear**: `src/main/java/com/cattle/services/AuditLoggingService.java`
- **Métodos a implementar**:
  ```java
  @Service
  public class AuditLoggingService {
      private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
      
      public void logSecurityEvent(SecurityEvent event);
      public void logChatEvent(String farmId, String userId, String intent, long duration, String status);
      public void logAuthenticationAttempt(String userId, boolean success);
      public void logRateLimitExceeded(String farmId);
      private String formatAsJson(Map<String, Object> data);
  }
  ```
- **Lógica**: Logging estructurado en formato JSON, incluir: timestamp (ISO-8601), farmId, userId, operation, intent, duration, statusCode, errorMessage (si aplica), NO loguear datos sensibles
- **Verificación**: Logs en CloudWatch en formato JSON parseable
- **Dependencias**: Ninguna
- **Estimación**: 2 horas

**T6.2: Integrar audit logging en ChatbotService y Controller (1h)**
- **Archivos a modificar**: 
  - `src/main/java/com/cattle/services/ChatbotService.java`
  - `src/main/java/com/cattle/controller/ChatbotController.java`
- **Cambios**: Inyectar AuditLoggingService, loguear eventos: chat request recibido, intent detectado, context built, Bedrock invocado, response generado, rate limit exceeded, authentication failed
- **Verificación**: Eventos de seguridad logueados en CloudWatch
- **Dependencias**: T6.1 (AuditLoggingService)
- **Estimación**: 1 hora

---

### HITO 7: Configuración de Infraestructura AWS (2h - Día 6)

**Objetivo**: Hardening de infraestructura

**Tareas Técnicas**:

**T7.1: Documentar IAM Role con Least Privilege (1h)**
- **Archivo a crear**: `docs/IAM-ROLE-POLICY.json`
- **Política IAM**:
  ```json
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": [
          "dynamodb:Query",
          "dynamodb:GetItem",
          "dynamodb:Scan"
        ],
        "Resource": [
          "arn:aws:dynamodb:us-east-1:*:table/TABLE_CATTLE",
          "arn:aws:dynamodb:us-east-1:*:table/TABLE_FARM_MILKING",
          "arn:aws:dynamodb:us-east-1:*:table/TABLE_PASTURE"
        ]
      },
      {
        "Effect": "Allow",
        "Action": ["bedrock:InvokeModel"],
        "Resource": "arn:aws:bedrock:us-east-1::foundation-model/anthropic.claude-3-haiku-20240307-v1:0"
      },
      {
        "Effect": "Allow",
        "Action": [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        "Resource": "arn:aws:logs:*:*:*"
      },
      {
        "Effect": "Deny",
        "Action": [
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
          "dynamodb:UpdateItem"
        ],
        "Resource": "*"
      }
    ]
  }
  ```
- **Verificación**: Política documentada y lista para aplicar
- **Dependencias**: Ninguna
- **Estimación**: 1 hora

**T7.2: Documentar configuración de API Gateway (1h)**
- **Archivo a crear**: `docs/API-GATEWAY-CONFIG.md`
- **Configuración a documentar**:
  - Throttling: 1000 requests/second burst, 500 requests/second steady-state
  - AWS WAF rules: SQL Injection, XSS, rate-based blocking
  - Request validation: Content-Type application/json, max body 100KB
  - TLS 1.2+ enforcement
  - Custom domain con certificado SSL
- **Verificación**: Documentación completa y clara
- **Dependencias**: Ninguna
- **Estimación**: 1 hora

---

### Estimaciones por Hito

| Hito | Tareas | Horas | Días | Dependencias |
|------|--------|-------|------|-------------|
| 1. Autenticación JWT | T1.1 - T1.4 | 10h | 1.3 | build.gradle |
| 2. Input Sanitization | T2.1 - T2.3 | 6h | 0.8 | Ninguna |
| 3. Rate Limiting | T3.1 - T3.2 | 5h | 0.6 | Hito 1 |
| 4. CORS Seguro | T4.1 | 2h | 0.3 | Ninguna |
| 5. Error Handling | T5.1 - T5.3 | 4h | 0.5 | Ninguna |
| 6. Audit Logging | T6.1 - T6.2 | 3h | 0.4 | Ninguna |
| 7. Infraestructura AWS | T7.1 - T7.2 | 2h | 0.3 | Todos |
| **TOTAL** | **17 tareas** | **32h** | **4.2 días** | - |

### Archivos a Crear (11 nuevos)

**Seguridad (8)**:
1. `src/main/java/com/cattle/security/JwtTokenProvider.java`
2. `src/main/java/com/cattle/security/JwtAuthenticationFilter.java`
3. `src/main/java/com/cattle/config/SecurityConfig.java`
4. `src/main/java/com/cattle/services/InputValidationService.java`
5. `src/main/java/com/cattle/services/RateLimitingService.java`
6. `src/main/java/com/cattle/exceptions/SecureExceptionHandler.java`
7. `src/main/java/com/cattle/dtos/ErrorResponse.java`
8. `src/main/java/com/cattle/services/AuditLoggingService.java`
9. `src/main/java/com/cattle/config/CorsSecurityConfig.java`

**Documentación (2)**:
10. `docs/IAM-ROLE-POLICY.json`
11. `docs/API-GATEWAY-CONFIG.md`

### Archivos a Modificar (5 existentes)

1. `build.gradle` - Agregar spring-security, jjwt, guava
2. `src/main/java/com/cattle/controller/ChatbotController.java` - Agregar rate limiting, extraer farmId de SecurityContext
3. `src/main/java/com/cattle/services/ChatbotService.java` - Agregar input sanitization, audit logging
4. `src/main/java/com/cattle/dtos/ChatRequestDTO.java` - Agregar Bean Validation annotations
5. **ELIMINAR** `src/main/java/com/cattle/config/CorsConfig.java` - Reemplazar por CorsSecurityConfig

### Consideraciones de Implementación

**Orden de desarrollo recomendado**:
1. Hito 1 (Días 1-2) - Autenticación JWT es crítica, prerequisito para otros hitos
2. Hito 2 (Día 3) - Input sanitization protege contra inyecciones
3. Hito 3 (Día 4 AM) - Rate limiting requiere autenticación del Hito 1
4. Hito 4 (Día 4 PM) - CORS puede hacerse en paralelo con rate limiting
5. Hito 5 (Día 5 AM) - Error handling (paralelizable con Hito 6)
6. Hito 6 (Día 5 PM) - Audit logging (paralelizable con Hito 5)
7. Hito 7 (Día 6) - Infraestructura AWS después de código completo

**Paralelización posible**:
- Hito 2 (Input Sanitization) + Hito 4 (CORS) son independientes
- Hito 5 (Error Handling) + Hito 6 (Audit Logging) son independientes

**Prerequisitos críticos**:
1. **HU-BEDROCK-001 completada** - ChatbotService y Controller deben existir
2. **Clave pública JWT configurada** - Para validar tokens firmados por Google OAuth
3. **Variables de entorno** - jwt.secret, jwt.issuer

**Riesgos técnicos identificados**:
1. **ALTO**: Integración con Google OAuth JWT puede requerir ajustes en formato de token - Mitigación: Documentar formato esperado, crear token de prueba
2. **MEDIO**: Rate limiting en memoria se resetea en cold starts - Mitigación: Aceptable para MVP, documentar limitación, considerar DynamoDB para V2
3. **MEDIO**: CORS whitelist puede necesitar ajustes por environments - Mitigación: Externalizar a variables de entorno
4. **BAJO**: Tests de seguridad requieren tokens JWT reales - Mitigación: MockJWTGenerator en tests (ya creado en HU-002)

**Configuración requerida (application.properties)**:
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.issuer=https://accounts.google.com

# CORS Allowed Origins
cors.allowed.origins=https://cattle-front.example.com,http://localhost:3000,http://localhost:5173

# Rate Limiting
rate.limit.requests.per.hour=100
```

**Comandos de validación**:
```bash
# Test autenticación JWT
curl -H "Authorization: Bearer {token}" http://localhost:8080/api/chat/message

# Test sin token (debe retornar 401)
curl http://localhost:8080/api/chat/message

# Test rate limiting (hacer 101 requests)
for i in {1..101}; do curl -H "Authorization: Bearer {token}" http://localhost:8080/api/chat/message; done

# Verificar logs estructurados en CloudWatch
aws logs filter-log-events --log-group-name /aws/lambda/cattle-bedrock --filter-pattern '{$.event = "CHAT_REQUEST"}'
```

---

## ✅ Definición de Hecho

- [ ] 6 vulnerabilidades analizadas & mitigadas
- [ ] JWT validation implementado
- [ ] Input sanitization completa
- [ ] Rate limiting funcionando
- [ ] Error handling seguro
- [ ] Logging estructurado
- [ ] OWASP Top 10 checklist completado
- [ ] 0 vulnerabilidades críticas
- [ ] Security audit pasado
- [ ] Documentación de seguridad completa

---

## 🎯 Métricas de Éxito

- ✅ 0 vulnerabilidades críticas
- ✅ 0 vulnerabilidades altas
- ✅ Rate limit enforcement 100%
- ✅ JWT validation 100%
- ✅ Error handling seguro 100%
- ✅ OWASP Top 10 cumplimiento 100%

---

## 📚 Documentación de Apoyo

- **SEGURIDAD.md**: Análisis detallado de vulnerabilidades
- **Security Checklist**: Antes del deploy
- **Incident Response Plan**: Procedimientos

---

**Documento**: HU-BEDROCK-003  
**Versión**: 1.2  
**Fecha**: 16 de Enero de 2026  
**Status**: ✅ Refinado (Developer) - Ready for implementación

---

## 📝 Registro de Cambios

| Fecha | Versión | Cambio | Autor |
|-------|---------|--------|-------|
| 2026-01-16 | 1.0 | Creación inicial de historia por PO | Product Owner |
| 2026-01-16 | 1.1 | Análisis arquitectónico de seguridad completado | jhon.fernandez (Arquitecto) |
| 2026-01-16 | 1.2 | Refinamiento técnico completado | jhon.fernandez (Developer) |
