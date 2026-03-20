# 🌱 PASTURES-HU#17: Backend API: Documentación OpenAPI/Swagger

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - API Documentation + Auto-generation | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer o usuario de API**, quiero tener documentación interactiva de todos los endpoints, de tal forma que:

1. Todos los endpoints estén documentados en OpenAPI 3.0
2. Se genere automáticamente con herramientas Spring (springdoc-openapi)
3. Swagger UI sea accesible en /swagger-ui.html
4. Se incluyan ejemplos de request/response
5. Se documente cada parámetro y validación
6. Se incluyan códigos de error y mensajes
7. La documentación sea sincrónica con el código

Esto habilitará que desarrolladores comprendan la API sin necesidad de código manual, y que frontend consuma correctamente los endpoints.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Setup de SpringDoc OpenAPI
```gherkin
Scenario: Instalar y configurar springdoc-openapi
  Given proyecto cattle-lambda-function sin documentación
  When se agrega: org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.x
  Then:
    [ ] Dependency se instala sin conflictos
    [ ] Swagger UI accesible en /swagger-ui.html
    [ ] OpenAPI JSON en /v3/api-docs
    [ ] Sin errores en startup
```

### AC#2: Documentar GET /pastures
```gherkin
Scenario: Documentar endpoint de lectura
  Given endpoint GET /farms/{farmId}/pastures
  When se documenta con @Operation, @Parameters
  Then:
    [ ] Descripción clara: "Obtener todos los potreros"
    [ ] Parámetros documentados:
        * farmId: String, "ID de la finca", required
        * status: String, "Filtrar por estado (DISPONIBLE, EN_USO, etc)", optional
        * page: Integer, "Número de página (0-indexed)", default: 0
        * size: Integer, "Tamaño de página", default: 50
    [ ] Response 200:
        {
          "content": [...],
          "totalElements": 100,
          "totalPages": 2
        }
    [ ] Response 400: Bad Request
    [ ] Response 403: Forbidden
    [ ] Response 404: Not Found
```

### AC#3: Documentar POST /pastures
```gherkin
Scenario: Documentar endpoint de creación
  Given endpoint POST /farms/{farmId}/pastures
  When se documenta
  Then:
    [ ] Descripción: "Crear nuevo potrero"
    [ ] Request body documentado:
        {
          "name": "String, required, 3-100 chars",
          "description": "String, optional",
          "areHa": "Double, required, > 0",
          "animalLoad": "Integer, optional, >= 0"
        }
    [ ] Response 201: Potrero creado
    [ ] Response 400: Validación fallida
    [ ] Ejemplo completo de request/response
```

### AC#4: Documentar PUT /pastures/{id}
```gherkin
Scenario: Documentar endpoint de edición
  Given endpoint PUT /farms/{farmId}/pastures/{pastureId}
  When se documenta
  Then:
    [ ] Descripción: "Editar potrero existente"
    [ ] Path parameters: farmId, pastureId
    [ ] Request body: mismo que POST
    [ ] Response 200: Potrero actualizado
    [ ] Response 404: No encontrado
    [ ] Response 409: Conflicto de versión (si aplica)
```

### AC#5: Documentar Endpoints de Eventos
```gherkin
Scenario: Documentar historial de eventos
  Given endpoint GET /farms/{farmId}/pastures/{pastureId}/events
  When se documenta (HU#12)
  Then:
    [ ] Query parameters documentados:
        * type: Enum[OPEN, CLOSE, MAINTENANCE_SET]
        * startDate: ISO 8601 date
        * endDate: ISO 8601 date
        * page: Integer
        * size: Integer
    [ ] Response con paginación
    [ ] Ejemplo de respuesta con 3 eventos
```

### AC#6: Documentar Endpoints de Auditoría
```gherkin
Scenario: Documentar auditoría
  Given endpoint GET /farms/{farmId}/audit
  When se documenta (HU#13)
  Then:
    [ ] Explicar qué es auditoría
    [ ] Parámetros de filtro documentados
    [ ] Response con structure completo
    [ ] Campos before/after explicados
```

### AC#7: Modelos y Esquemas
```gherkin
Scenario: Documentar modelos de datos
  Given DTOs: PastureDTO, AuditLogDTO, PastureEventDTO
  When se documentan con @Schema
  Then:
    [ ] Cada campo tiene @Schema(description)
    [ ] Tipos correctamente mapeados
    [ ] Validaciones visibles (@NotNull, @NotBlank, etc)
    [ ] Ejemplos en @Schema(example)
    [ ] Enum values listados
```

### AC#8: Errores Documentados
```gherkin
Scenario: Documentar respuestas de error
  Given cualquier endpoint
  When se documentan errores
  Then:
    [ ] 400 Bad Request: error de validación
    [ ] 401 Unauthorized: sin autenticación
    [ ] 403 Forbidden: sin autorización
    [ ] 404 Not Found: recurso no existe
    [ ] 409 Conflict: conflicto (ej: versión)
    [ ] 500 Internal Server Error: error del servidor
    [ ] Cada error tiene estructura:
        {
          "error": "ERROR_CODE",
          "message": "Descripción amigable",
          "timestamp": "ISO 8601",
          "path": "/api/endpoint"
        }
```

### AC#9: Tags y Categorización
```gherkin
Scenario: Agrupar endpoints por categoría
  Given múltiples endpoints
  When se organizan con @Tag
  Then:
    [ ] Categoría: Pastures
        * POST /pastures
        * GET /pastures
        * PUT /pastures/{id}
        * DELETE /pastures/{id}
    [ ] Categoría: Events
        * GET /pastures/{id}/events
        * GET /pastures/{id}/events/{eventId}
    [ ] Categoría: Audit
        * GET /audit
        * GET /audit/report
    [ ] Categoría: Auth (si existe)
        * POST /login
        * POST /logout
```

### AC#10: Ejemplos Realistas
```gherkin
Scenario: Proporcionar ejemplos completos
  Given documentación de endpoint
  When se incluyen ejemplos
  Then:
    [ ] Request JSON con datos válidos
    [ ] Response JSON exitosa (200)
    [ ] Response error (400)
    [ ] Valores reales (no placeholders vacios)
    [ ] Datos coherentes y lógicos
```

### AC#11: Autenticación Documentada
```gherkin
Scenario: Documentar esquema de autenticación
  Given API con autorización
  When se documenta autenticación
  Then:
    [ ] Tipo: Bearer token (JWT)
    [ ] Header: Authorization: Bearer {token}
    [ ] Ejemplo de token
    [ ] Cómo obtener token (login)
    [ ] Scopes/permisos necesarios
```

### AC#12: Versionado de API
```gherkin
Scenario: Documentar versión de API
  Given OpenAPI info
  When se documenta
  Then:
    [ ] Version: 1.0.0
    [ ] API title: "Cattle Farm Management API"
    [ ] Description clara
    [ ] Contact info (support email)
    [ ] License info si aplica
    [ ] URL del servidor: http://localhost:8080/api
```

### AC#13: Swagger UI Funcional
```gherkin
Scenario: Interface web de Swagger
  Given documentación OpenAPI generada
  When usuario accede /swagger-ui.html
  Then:
    [ ] Página carga correctamente
    [ ] Todos los endpoints visibles
    [ ] Se puede expandir cada endpoint
    [ ] Parámetros editables (Try it out)
    [ ] Ejecutar requests desde el navegador
    [ ] Ver responses en tiempo real
    [ ] Descargar OpenAPI JSON/YAML
```

### AC#14: Documentación en Markdown
```gherkin
Scenario: Exportar documentación a Markdown
  Given OpenAPI spec
  When se convierte a Markdown (bonus)
  Then:
    [ ] Generación automática con herramienta
    [ ] Archivo: api-documentation.md
    [ ] Incluye todos los endpoints
    [ ] Ejemplos embebidos
    [ ] Tabla de contenidos
    [ ] Fácil de leer en GitHub/Confluence
```

### AC#15: Mantener Sincronización
```gherkin
Scenario: Documentación se actualiza automáticamente
  Given cambio en código (nuevo parámetro, etc)
  When se ejecuta compilación
  Then:
    [ ] OpenAPI spec se regenera automáticamente
    [ ] Swagger UI refleja cambios
    [ ] No se necesita documentación manual extra
    [ ] CI/CD valida que doc esté sincronizada
```

---

## 📊 **Especificación Técnica**

### 1. Instalación de Dependencias

#### pom.xml
```xml
<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- Opcional: Generador de documentación -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-maven-plugin</artifactId>
    <version>1.4</version>
</dependency>
```

#### application.properties
```properties
# Swagger UI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.display-operation-id=false
springdoc.swagger-ui.display-request-duration=true

# API Info
springdoc.api-docs.title=Cattle Farm Management API
springdoc.api-docs.description=API para gestión de rotación de potreros
springdoc.api-docs.version=1.0.0
springdoc.api-docs.terms-of-service=https://example.com/terms
springdoc.api-docs.contact.name=API Support
springdoc.api-docs.contact.email=support@example.com
springdoc.api-docs.contact.url=https://example.com/support
springdoc.api-docs.license.name=Apache 2.0
springdoc.api-docs.license.url=https://www.apache.org/licenses/LICENSE-2.0.html
```

### 2. Configuración Global de OpenAPI

#### OpenAPIConfiguration.java
```java
@Configuration
public class OpenAPIConfiguration {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cattle Farm Management API")
                .version("1.0.0")
                .description("API para gestionar rotación de potreros, ganado y operaciones")
                .contact(new Contact()
                    .name("Tech Support")
                    .email("support@cattle.farm")
                    .url("https://cattle.farm/support"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080/api")
                    .description("Desarrollo"),
                new Server()
                    .url("https://api.cattle.farm/api")
                    .description("Producción")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token")));
    }
}
```

### 3. Documentación de Controllers

#### PastureController.java
```java
@RestController
@RequestMapping("/farms/{farmId}/pastures")
@Tag(name = "Pastures", description = "Operaciones sobre potreros")
public class PastureController {
    
    @GetMapping
    @Operation(
        summary = "Obtener todos los potreros",
        description = "Retorna lista paginada de potreros de una finca"
    )
    @Parameters({
        @Parameter(name = "farmId", description = "ID de la finca", required = true),
        @Parameter(name = "status", description = "Filtrar por estado (DISPONIBLE, EN_USO, EN_DESCANSO, MANTENIMIENTO)", required = false),
        @Parameter(name = "page", description = "Número de página (0-indexed)", example = "0"),
        @Parameter(name = "size", description = "Tamaño de página", example = "50")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de potreros",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = PasturesPage.class))),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso denegado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Finca no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<PastureDTO>> getPastures(
        @PathVariable String farmId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        // Implementation
    }
    
    @PostMapping
    @Operation(
        summary = "Crear nuevo potrero",
        description = "Crea un nuevo potrero en la finca"
    )
    @ApiResponse(responseCode = "201", description = "Potrero creado exitosamente",
        content = @Content(schema = @Schema(implementation = PastureDTO.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PastureDTO> createPasture(
        @PathVariable String farmId,
        @Valid @RequestBody CreatePastureRequest request
    ) {
        // Implementation
    }
    
    @PutMapping("/{pastureId}")
    @Operation(
        summary = "Editar potrero",
        description = "Actualiza información de un potrero existente"
    )
    @ApiResponse(responseCode = "200", description = "Potrero actualizado",
        content = @Content(schema = @Schema(implementation = PastureDTO.class)))
    @ApiResponse(responseCode = "404", description = "Potrero no encontrado")
    public ResponseEntity<PastureDTO> updatePasture(
        @PathVariable String farmId,
        @PathVariable String pastureId,
        @Valid @RequestBody UpdatePastureRequest request
    ) {
        // Implementation
    }
}
```

### 4. Documentación de DTOs

#### PastureDTO.java
```java
@Schema(
    name = "PastureDTO",
    description = "Datos de un potrero"
)
@Data
public class PastureDTO {
    
    @Schema(
        description = "ID único del potrero",
        example = "P001"
    )
    private String id;
    
    @Schema(
        description = "Nombre del potrero",
        example = "Potrero Norte",
        minLength = 3,
        maxLength = 100
    )
    private String name;
    
    @Schema(
        description = "Descripción del potrero",
        example = "Pasto de fescue, bien drenado",
        maxLength = 500
    )
    private String description;
    
    @Schema(
        description = "Área en hectáreas",
        example = "5.5",
        minimum = "0.1"
    )
    private Double areHa;
    
    @Schema(
        description = "Carga de animales actual",
        example = "20",
        minimum = "0"
    )
    private Integer animalLoad;
    
    @Schema(
        description = "Estado actual",
        example = "DISPONIBLE",
        allowableValues = {"DISPONIBLE", "EN_USO", "EN_DESCANSO", "MANTENIMIENTO", "SOLD", "REMOVED"}
    )
    private String status;
    
    @Schema(
        description = "Días hasta disponible",
        example = "15",
        minimum = "0"
    )
    private Integer eta;
    
    @Schema(
        description = "Timestamp de creación (ISO 8601)",
        example = "2026-01-09T10:30:00Z"
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Última edición",
        example = "2026-01-09T14:45:00Z"
    )
    private LocalDateTime lastModifiedAt;
}
```

#### CreatePastureRequest.java
```java
@Schema(
    name = "CreatePastureRequest",
    description = "Datos para crear nuevo potrero"
)
@Data
@Valid
public class CreatePastureRequest {
    
    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100)
    @Schema(
        description = "Nombre del potrero",
        example = "Potrero Norte",
        minLength = 3,
        maxLength = 100
    )
    private String name;
    
    @Schema(
        description = "Descripción",
        example = "Descripción del potrero"
    )
    private String description;
    
    @NotNull(message = "Área es requerida")
    @DecimalMin(value = "0.1")
    @Schema(
        description = "Área en hectáreas",
        example = "5.5",
        minimum = "0.1"
    )
    private Double areHa;
    
    @Min(value = 0)
    @Schema(
        description = "Carga de animales",
        example = "20",
        minimum = "0"
    )
    private Integer animalLoad;
}
```

### 5. Documentación de Errores

#### ErrorResponse.java
```java
@Schema(
    name = "ErrorResponse",
    description = "Respuesta de error estándar"
)
@Data
public class ErrorResponse {
    
    @Schema(
        description = "Código de error",
        example = "VALIDATION_ERROR"
    )
    private String error;
    
    @Schema(
        description = "Mensaje de error",
        example = "El nombre es requerido"
    )
    private String message;
    
    @Schema(
        description = "Timestamp del error",
        example = "2026-01-09T10:30:00Z"
    )
    private LocalDateTime timestamp;
    
    @Schema(
        description = "Path del request",
        example = "/api/farms/F001/pastures"
    )
    private String path;
    
    @Schema(
        description = "Detalles de validación (si aplica)"
    )
    private Map<String, String> validationErrors;
}
```

### 6. Acceso a Documentación

```
Swagger UI (interactivo):
  http://localhost:8080/swagger-ui.html

OpenAPI JSON:
  http://localhost:8080/v3/api-docs

OpenAPI YAML:
  http://localhost:8080/v3/api-docs.yaml

Redoc (alternativa):
  http://localhost:8080/v3/api-docs/redoc.html (requiere plugin)
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`OpenAPIConfiguration.java`** - Configuración global
2. **`ErrorResponse.java`** - Esquema de errores
3. **`api-documentation.md`** - Documentación en Markdown (bonus)

### Archivos a Modificar

1. **`pom.xml`** - Agregar dependencias
2. **`application.properties`** - Configurar Swagger
3. **Todos los Controllers** - Agregar @Operation, @ApiResponse
4. **Todos los DTOs** - Agregar @Schema
5. **Todos los Requests** - Agregar @Schema

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Agregar Dependencia
- Editar pom.xml
- springdoc-openapi-starter-webmvc-ui

### Paso 2: Configurar Global
- OpenAPIConfiguration.java
- application.properties

### Paso 3: Documentar Controllers
- @Tag, @Operation, @ApiResponse
- Todos los endpoints

### Paso 4: Documentar DTOs
- @Schema en todas las clases
- Ejemplos, validaciones, descripción

### Paso 5: Probar Swagger UI
- Verificar acceso en /swagger-ui.html
- Probar "Try it out"

### Paso 6: Documentación Adicional
- Markdown export (bonus)
- Guía de consumo para frontend

---

## 🔧 **Refinamiento Técnico**

### Maven Dependency

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.0.2</version>
</dependency>
```

### OpenAPI Configuration

```java
@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("Cattle Pastures API")
        .version("1.0.0")
        .description("API para gestión de potreros")
        .contact(new Contact().name("Team").email("team@farm.com")))
      .externalDocs(new ExternalDocumentation()
        .description("Documentación adicional")
        .url("https://docs.farm.com"));
  }
}
```

### Controller Annotation

```java
@RestController
@RequestMapping("/pastures")
public class PastureController {
  
  @PostMapping
  @Operation(summary = "Crear potrero", 
    description = "Crea nuevo potrero con ID único")
  @ApiResponse(responseCode = "201", description = "Potrero creado")
  @ApiResponse(responseCode = "400", description = "Datos inválidos")
  public PastureDTO createPasture(@RequestBody CreatePastureRequest req) {
    // ...
  }
}
```

### Endpoints Swagger UI

```
GET /v3/api-docs - OpenAPI JSON
GET /swagger-ui.html - Swagger UI
GET /swagger-ui.html?urls.primaryName=Pastures - Grouped
```

### Testing Strategy

**Access Swagger:**
- http://localhost:8080/swagger-ui.html
- Test "Try it out" en cada endpoint
- Validate response codes

---

## ✅ **Definición de Completado**

Para marcar esta documentación como **DONE**:

- [ ] springdoc-openapi instalado
- [ ] Swagger UI accesible en /swagger-ui.html
- [ ] OpenAPI JSON generado en /v3/api-docs
- [ ] Todos los Controllers documentados con @Operation
- [ ] Todos los endpoints tienen @ApiResponse
- [ ] Todos los DTOs tienen @Schema
- [ ] Parámetros documentados con @Parameter
- [ ] Ejemplos realistas en @Schema(example)
- [ ] Errores documentados (400, 403, 404, 500)
- [ ] Autenticación documentada (Bearer JWT)
- [ ] Tags organizan endpoints por categoría
- [ ] Validaciones visibles
- [ ] "Try it out" funciona en Swagger UI
- [ ] Documentación sincrónica con código
- [ ] CI/CD valida spec
- [ ] Markdown export (bonus)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** API Documentation Pattern + springdoc-openapi + Auto-generation

**Justificación:** **OpenAPI 3.0**: Industry standard. **Automatic Generation**: Desde código Java. **Interactive Swagger UI**: Prueba endpoints. **Example Documentation**: Request/response ejemplos. **Error Documentation**: Todos códigos HTTP. **Self-updating**: Sincronizado código.

**Componentes Afectados:**

- **springdoc-openapi dependency:** `org.springdoc:springdoc-openapi-starter-webmvc-ui`. Genera automáticamente.

- **PastureOpenApiConfig.java (Nuevo):** Configuración custom. Bean: OpenAPI. Define info (title, version, description), contact, license.

- **@Operation annotations:** En todos los métodos Controller. Descripciones claras de qué hace endpoint.

- **@Schema annotations:** En todos DTOs. Describe fields, ejemplos, validaciones.

- **ErrorResponse.java (Nuevo):** Modelo estándar para errores. Fields: error (code), message, timestamp. Usado en @ApiResponse.

- **OpenApiExamples.java (Nuevo):** Ejemplos centralizados. Request/response ejemplos para cada endpoint.

**Hitos:**
1. PastureOpenApiConfig.java (setup)
2. @Operation annotations en Controllers
3. @Schema annotations en DTOs
4. ErrorResponse.java (error model)
5. OpenApiExamples.java (ejemplos)

### Validación de Impacto

✅ **Auto-generation**: Sincronizado con código
✅ **Interactive Swagger UI**: Prueba endpoints
✅ **Complete Documentation**: Todos endpoints
✅ **Error Documentation**: Códigos HTTP claros
✅ **Examples**: Request/response realistas

### Notas Técnicas

**Swagger UI Access:**
- URL: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs
- YAML: http://localhost:8080/v3/api-docs.yaml

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-001-012: Todos endpoints
- ✅ PASTURES-HU-013: Auditoría (documentada)
- → PASTURES-HU-017: OpenAPI (esta)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** API documentation auto-generation (developer experience)

---
- [ ] Redoc alternativo (bonus)
- [ ] Guía de consumo escrita
- [ ] Code review aprobado
- [ ] Tests de documentación pasando

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
