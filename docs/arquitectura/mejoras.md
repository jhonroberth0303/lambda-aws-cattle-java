# Análisis y Mejoras del Proyecto cattle-lambda-function

**Fecha de análisis:** 2026-02-03  
**Versión:** 1.0  
**Autor:** Agente Developer - Método Ceiba

---

## Resumen Ejecutivo

El proyecto **cattle-lambda-function** es una aplicación AWS Lambda con Spring Boot 3.4.5 que sigue una arquitectura bien estructurada (Controller → Processor → Service → Repository). La cobertura de tests es del **79% en instrucciones** y **69% en branches**, lo cual es una buena base pero con oportunidades de mejora.

---

## 1. Código No Usado / Duplicado

### 1.1 DTO Duplicado: MessageDTO (ALTA)

| Archivo | Descripción |
|---------|-------------|
| `dtos/MessageDTO.java` | Record con solo `content` - **SIN USOS** |
| `dtos/commons/MessageDTO.java` | Clase con `message` y `status` - Usado en controllers |

**Acción:** Eliminar `dtos/MessageDTO.java` (record sin usos).

```java
// ELIMINAR: src/main/java/com/cattle/dtos/MessageDTO.java
package com.cattle.dtos;
public record MessageDTO(String content) {}
```

### 1.2 Excepción NotFoundException (MEDIA)

| Archivo | Estado |
|---------|--------|
| `exceptions/NotFoundException.java` | Solo tiene tests, no se usa en código principal |

**Acción:** Agregar handler en `GlobalExceptionHandler.java` o eliminar si no se planea usar.

```java
// AGREGAR en GlobalExceptionHandler.java:
@ExceptionHandler(NotFoundException.class)
public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now().toString());
    response.put("status", HttpStatus.NOT_FOUND.value());
    response.put("error", "Recurso no encontrado");
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
}
```

---

## 2. Inyección de Dependencias

### 2.1 Uso de @Autowired (Field Injection) (ALTA)

Se encontraron **11 clases** usando `@Autowired` en campos, lo cual dificulta el testing y no es la práctica recomendada.

| Archivo | Línea |
|---------|-------|
| `config/SecurityConfig.java` | 24 |
| `services/AuditLoggingService.java` | 22 |
| `services/BovineQueryService.java` | 25 |
| `services/knowledge/KnowledgeBaseService.java` | 31 |
| `services/PastureQueryService.java` | 25 |
| `services/MilkingQueryService.java` | 25 |
| `services/ContextBuilderService.java` | 24, 27, 30 |
| `security/JwtAuthenticationFilter.java` | 33 |
| `controller/MilkingController.java` | 24 |

**Acción:** Migrar a **constructor injection** usando `@RequiredArgsConstructor` de Lombok.

```java
// ANTES (incorrecto):
@Service
public class BovineQueryService {
    @Autowired
    private BovineRepository bovineRepository;
}

// DESPUÉS (correcto):
@Service
@RequiredArgsConstructor
public class BovineQueryService {
    private final BovineRepository bovineRepository;
}
```

---

## 3. Configuración Hardcodeada

### 3.1 Región AWS (MEDIA)

La región `Region.US_EAST_1` está hardcodeada en 4 archivos:

| Archivo | Línea |
|---------|-------|
| `config/BedrockConfig.java` | 18 |
| `config/RepositoryConfig.java` | 15 |
| `config/BedrockAgentConfig.java` | 20 |
| (test) `containers/LocalStackTestContainer.java` | 78 |

**Acción:** Externalizar usando `application.properties`:

```properties
# application.properties
aws.region=${AWS_REGION:us-east-1}
```

```java
// AwsConfig.java
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsConfig {
    private String region = "us-east-1";
    
    public Region getRegion() {
        return Region.of(region);
    }
}
```

### 3.2 System.getenv() Disperso (MEDIA)

Se encontraron **17+ ocurrencias** de `System.getenv()` en repositorios y servicios:

| Variable | Archivos afectados |
|----------|-------------------|
| `TABLE_BOVINES` | 6 repositorios |
| `TABLE_PASTURE` | 1 repositorio |
| `TABLE_FARM_MILKING` | 1 repositorio |
| `TABLE_COUNTERS` | 1 repositorio |
| `TABLE_PLAN` | 1 repositorio |
| `APP_TIMEZONE` | 2 servicios |
| `BEDROCK_KB_ID` | 2 archivos |

**Acción:** Centralizar en clase `@ConfigurationProperties`:

```java
@Component
@ConfigurationProperties(prefix = "tables")
@Getter @Setter
public class TablesConfig {
    private String bovineIdentityItems;
    private String pasture;
    private String milkingProd;
    private String counters;
    private String plan;
}
```

### 3.3 Logs DEBUG en Producción (ALTA)

```java
// KnowledgeBaseService.java línea 37 y 45
lambdaContext.logInfo(LogType.SERVICE, "[DEBUG] Valor real de BEDROCK_KB_ID en Lambda: " + System.getenv("BEDROCK_KB_ID"));
```

**Acción:** Eliminar logs de debug que exponen variables de entorno.

---

## 4. TODOs Pendientes

### 4.1 Evento de Castración (MEDIA)

```java
// LifecycleRecalculationService.java línea 79
// TODO: isCastrated should come from EVENT#CASTRATION when events are implemented
boolean isCastrated = false;
```

**Acción:** Implementar consulta a eventos de castración o documentar como feature pendiente.

---

## 5. Mejoras de Cobertura de Tests

### 5.1 Brechas de Cobertura

| Paquete | Cobertura Actual | Meta |
|---------|------------------|------|
| `repository` | ~49% | 80%+ |
| `mapper` | ~52% | 80%+ |
| `rules` | 74% (0% branches) | 85%+ |
| `services/knowledge` | ~60% | 80%+ |

### 5.2 Tests Faltantes Prioritarios

1. **BovineSummaryRepository** - tests de integración con DynamoDB
2. **BovineCategoryRulesConfig** - test de carga de YAML
3. **KnowledgeBaseService** - tests con mocks de Bedrock
4. **Mappers** - casos edge (nulls, listas vacías)

---

## 6. Estructura de Paquetes

### 6.1 Organización Actual (Correcta ✅)

```
com.cattle
├── config/          # Configuraciones Spring
├── controller/      # REST endpoints
├── dtos/           # Data Transfer Objects
│   ├── chatbot/    # DTOs específicos del chatbot
│   ├── commons/    # DTOs comunes
│   └── knowledge/  # DTOs del knowledge base
├── entities/       # Entidades DynamoDB
├── enums/          # Enumeraciones
├── exceptions/     # Excepciones personalizadas
├── mapper/         # MapStruct mappers
├── processor/      # Capa de procesamiento
├── repository/     # Acceso a datos
├── rules/          # Reglas de negocio configurables
├── security/       # Seguridad JWT
└── services/       # Lógica de negocio
    └── knowledge/  # Servicios del Knowledge Base
```

### 6.2 Mejoras Sugeridas

| Mejora | Prioridad |
|--------|-----------|
| Mover `BovineCategoryRulesService` de `services/` a `rules/` | BAJA |
| Crear paquete `services/batch/` para servicios de procesamiento batch | BAJA |

---

## 7. Resumen de Acciones

### Prioridad ALTA

| # | Acción | Esfuerzo | Estado |
|---|--------|----------|--------|
| 1 | Eliminar `dtos/MessageDTO.java` (record sin uso) | 5 min | ✅ COMPLETADO |
| 2 | Eliminar logs DEBUG en `KnowledgeBaseService.java` | 5 min | ✅ COMPLETADO |
| 3 | Migrar 11 clases de `@Autowired` a constructor injection | 1 hora | ⏳ PENDIENTE |

### Prioridad MEDIA

| # | Acción | Esfuerzo | Estado |
|---|--------|----------|--------|
| 4 | Agregar handler para `NotFoundException` en `GlobalExceptionHandler` | 15 min | ✅ COMPLETADO |
| 5 | Centralizar `System.getenv()` en `TablesConfig` | 2 horas | ⏳ PENDIENTE |
| 6 | Externalizar región AWS a configuración | 30 min | ⏳ PENDIENTE |
| 7 | Aumentar cobertura de tests a 85%+ | 4-6 horas | ⏳ PENDIENTE |

### Prioridad BAJA

| # | Acción | Esfuerzo | Estado |
|---|--------|----------|--------|
| 8 | Reorganizar paquete `rules/` | 30 min | ⏳ PENDIENTE |
| 9 | Implementar evento de castración (TODO pendiente) | 2-4 horas | ⏳ PENDIENTE |
| 10 | Consolidar versiones de JUnit en BOM | 30 min | ⏳ PENDIENTE |

---

## 8. Métricas de Calidad Actuales

| Métrica | Valor Actual | Meta |
|---------|--------------|------|
| Cobertura de código | 79% | 85%+ |
| Cobertura de branches | 69% | 80%+ |
| Tests totales | 622 | - |
| Tests fallidos | 0 | 0 |
| Warnings de compilación | 4 | 0 |

---

## 9. Próximos Pasos Recomendados

1. **Sprint actual:** Ejecutar acciones de prioridad ALTA (< 2 horas)
2. **Siguiente sprint:** Ejecutar acciones de prioridad MEDIA
3. **Backlog:** Planificar acciones de prioridad BAJA

---

*Documento generado por Método Ceiba - Developer Agent*
