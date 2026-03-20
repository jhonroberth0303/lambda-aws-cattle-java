# 🌱 PASTURES-HU#13: Backend: Auditoría de Cambios

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Audit Trail + AOP Interceptor | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend developer**, quiero implementar un sistema de auditoría que registre todos los cambios en potreros, de tal forma que:

1. Se registre automáticamente cada operación (OPEN, CLOSE, CREATE, EDIT, DELETE, MAINTENANCE)
2. Se capture quién realizó el cambio (userId, userName)
3. Se capture cuándo sucedió (timestamp exacto)
4. Se registre qué cambió (valores antes y después)
5. Se pueda consultar el historial completo de cambios
6. Se pueda generar reportes de auditoría
7. Los registros sean inmutables (no se puedan borrar)

Esto habilitará que gerentes auditen operaciones, detecten cambios no autorizados, y cumplan con regulaciones de trazabilidad.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Registrar Evento OPEN Automáticamente
```gherkin
Scenario: Crear registro de auditoría al abrir potrero
  Given usuario USER-123 realiza POST /open en potrero P001
  And timestamp: 2026-01-05 10:30:00
  When se procesa la operación
  Then:
    [ ] Se crea AuditLog automáticamente
    [ ] operation: "OPEN"
    [ ] entityType: "PASTURE"
    [ ] entityId: "P001"
    [ ] userId: "USER-123"
    [ ] timestamp: 2026-01-05 10:30:00
    [ ] Sin necesidad de código manual extra
```

### AC#2: Registrar Cambios (Antes y Después)
```gherkin
Scenario: Capturar valores antes y después
  Given evento CLOSE en potrero con residualHeight=15
  When se registra en auditoría
  Then:
    [ ] beforeValues: { status: "EN_USO", residualHeight: null }
    [ ] afterValues: { status: "EN_DESCANSO", residualHeight: 15 }
    [ ] Diferencias claras para auditores
    [ ] Facilita debugging
```

### AC#3: Información de Usuario Completa
```gherkin
Scenario: Registrar quién realizó cambio
  Given operario "Carlos López" con userId="USER-123" abre potrero
  When se registra
  Then:
    [ ] userId: "USER-123"
    [ ] userName: "Carlos López"
    [ ] farmId: "F001"
    [ ] userRole: "OPERARIO" o "GERENTE"
    [ ] Trazabilidad completa
```

### AC#4: Todas las Operaciones Registradas
```gherkin
Scenario: Auditar todas las operaciones
  Given operaciones: OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR, EDIT, CREATE, DELETE
  When se ejecutan
  Then:
    [ ] Cada una genera AuditLog
    [ ] operation field es correcto
    [ ] Ninguna operación se escapa del registro
```

### AC#5: Timestamp Exacto
```gherkin
Scenario: Registrar timestamp exacto
  When usuario realiza cambio a las 2026-01-05 10:30:45.123
  Then:
    [ ] AuditLog.timestamp: 2026-01-05 10:30:45.123Z
    [ ] UTC/ISO 8601 formato
    [ ] Preciso hasta milisegundos
    [ ] Sin desfase horario
```

### AC#6: Campos Modificados Específicos
```gherkin
Scenario: Mostrar solo campos que cambiaron
  Given EDIT operación que modifica "name" y "description"
  And otros campos sin cambios: "status", "area"
  When se registra
  Then:
    [ ] changedFields: ["name", "description"]
    [ ] Para auditoría clara (qué cambió exactamente)
```

### AC#7: Registro Inmutable
```gherkin
Scenario: Auditoría no se puede modificar
  Given AuditLog creado correctamente
  When usuario intenta UPDATE o DELETE el AuditLog
  Then:
    [ ] Operación rechazada (403)
    [ ] Error: "Registros de auditoría son inmutables"
    [ ] Log de intento de modificación
    [ ] Cumple regulaciones
```

### AC#8: Identificación de IP (Bonus)
```gherkin
Scenario: Capturar IP del cliente
  Given usuario realiza operación desde IP 192.168.1.100
  When se registra
  Then:
    [ ] clientIp: "192.168.1.100"
    [ ] Ayuda a detectar acceso no autorizado
    [ ] Opcional pero valioso para seguridad
```

### AC#9: Contexto de Sesión (Bonus)
```gherkin
Scenario: Vincular a sesión del usuario
  Given sesión S-001 del usuario USER-123
  When realiza operación
  Then:
    [ ] sessionId: "S-001"
    [ ] Facilita rastrear acciones de una sesión
```

### AC#10: Descripción de Cambio Legible
```gherkin
Scenario: Descripción legible de lo que cambió
  Given CLOSE operación en P001
  When se registra
  Then:
    [ ] description: "Potrero P001 cerrado - Altura residual: 15cm"
    [ ] O similar, legible para humanos
    [ ] Facilita reportes sin necesidad de analizar JSON
```

### AC#11: Consultar Auditoría por Entidad
```gherkin
Scenario: Obtener historial de auditoría de una entidad
  Given potrero P001 con 20 cambios registrados
  When GET /farms/F001/audit/pastures/P001
  Then:
    [ ] Retorna todos los 20 registros ordenados por fecha
    [ ] Incluye metadata (total, página)
    [ ] Mismo formato que HU#12
    [ ] Fácil para frontend consumir
```

### AC#12: Filtrar Auditoría por Operación
```gherkin
Scenario: Filtrar cambios por tipo de operación
  When GET /farms/F001/audit?operation=OPEN,CLOSE
  Then:
    [ ] Retorna solo OPEN y CLOSE
    [ ] Útil para analizar operaciones específicas
```

### AC#13: Reporte de Auditoría (Bonus)
```gherkin
Scenario: Generar reporte de cambios
  When GET /farms/F001/audit/report?startDate=2026-01-01&endDate=2026-01-31
  Then:
    [ ] Retorna reporte con estadísticas:
        {
          "totalChanges": 150,
          "changesByOperation": {
            "OPEN": 50,
            "CLOSE": 45,
            "MAINTENANCE_SET": 30,
            "EDIT": 25
          },
          "changesByUser": {
            "USER-123": 80,
            "USER-456": 70
          },
          "topChangedPotreros": [
            { "id": "P001", "changes": 15 },
            { "id": "P002", "changes": 12 }
          ]
        }
    [ ] Útil para gerencia
```

### AC#14: Performance - Auditoría No Bloquea Operación
```gherkin
Scenario: Registro de auditoría no ralentiza operación principal
  When usuario realiza OPEN operación
  Then:
    [ ] Operación tarda < 200ms
    [ ] Auditoría se registra async (no bloquea)
    [ ] Si auditoría falla, operación aún se completa
    [ ] Logging de error de auditoría sin afectar user
```

### AC#15: Cumplimiento Normativo
```gherkin
Scenario: Auditoría cumple regulaciones
  Then:
    [ ] Registros inalterables (immutable)
    [ ] Trazabilidad completa (quién, cuándo, qué)
    [ ] Retención de datos según política
    [ ] GDPR compliant (datos sensibles protegidos)
    [ ] Documentación de retención
```

---

## 📊 **Especificación Técnica**

### Estructura de Auditoría

#### Entity - AuditLog

```java
@Entity
@Table(name = "audit_logs", indexes = {
  @Index(name = "idx_entity_id", columnList = "entity_id"),
  @Index(name = "idx_entity_type", columnList = "entity_type"),
  @Index(name = "idx_timestamp", columnList = "timestamp"),
  @Index(name = "idx_user_id", columnList = "user_id"),
  @Index(name = "idx_operation", columnList = "operation"),
  @Index(name = "idx_farm_id", columnList = "farm_id")
})
public class AuditLog {
  
  @Id
  private String id; // AL-001, AL-002, etc
  
  private String farmId;
  private String entityType; // PASTURE, BOVINE, MILKING, etc
  private String entityId; // P001, B-123, etc
  
  @Enumerated(EnumType.STRING)
  private AuditOperation operation; // OPEN, CLOSE, CREATE, EDIT, DELETE, etc
  
  private LocalDateTime timestamp;
  
  private String userId;
  private String userName;
  private String userRole;
  
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JsonAttributeConverter.class)
  private Map<String, Object> beforeValues;
  
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JsonAttributeConverter.class)
  private Map<String, Object> afterValues;
  
  @ElementCollection
  private Set<String> changedFields;
  
  private String description; // Legible: "Potrero P001 abierto..."
  private String clientIp; // 192.168.1.100
  private String sessionId; // S-001
  
  private LocalDateTime createdAt;
  
  // No permitir modificación
  @PreUpdate
  protected void onUpdate() {
    throw new UnsupportedOperationException("Auditoría no es modificable");
  }
}
```

#### Enum - AuditOperation

```java
public enum AuditOperation {
  OPEN("Abrir potrero"),
  CLOSE("Cerrar potrero"),
  CREATE("Crear entidad"),
  EDIT("Editar entidad"),
  DELETE("Eliminar entidad"),
  MAINTENANCE_SET("Bloquear para mantenimiento"),
  MAINTENANCE_CLEAR("Desbloquear mantenimiento"),
  UNKNOWN("Operación desconocida");
  
  private String description;
}
```

### Servicios

#### AuditService.java

```java
@Service
public class AuditService {
  
  /**
   * Registrar evento de auditoría
   */
  public void audit(AuditContext context) {
    AuditLog log = new AuditLog();
    log.setFarmId(context.getFarmId());
    log.setEntityType(context.getEntityType());
    log.setEntityId(context.getEntityId());
    log.setOperation(context.getOperation());
    log.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
    log.setUserId(context.getUserId());
    log.setUserName(context.getUserName());
    log.setUserRole(context.getUserRole());
    log.setBeforeValues(context.getBeforeValues());
    log.setAfterValues(context.getAfterValues());
    log.setChangedFields(calculateChangedFields(context));
    log.setDescription(generateDescription(context));
    log.setClientIp(context.getClientIp());
    log.setSessionId(context.getSessionId());
    log.setCreatedAt(LocalDateTime.now());
    
    auditRepository.save(log);
  }
  
  private Set<String> calculateChangedFields(AuditContext context) {
    Set<String> changed = new HashSet<>();
    if (context.getBeforeValues() == null || context.getAfterValues() == null) {
      return changed;
    }
    
    for (String key : context.getAfterValues().keySet()) {
      Object before = context.getBeforeValues().get(key);
      Object after = context.getAfterValues().get(key);
      if (!Objects.equals(before, after)) {
        changed.add(key);
      }
    }
    return changed;
  }
  
  private String generateDescription(AuditContext context) {
    return String.format("%s - %s %s",
      context.getEntityType(),
      context.getEntityId(),
      context.getOperation().getDescription()
    );
  }
  
  public Page<AuditLogDTO> getEntityAudit(
    String farmId,
    String entityType,
    String entityId,
    Pageable pageable
  ) {
    return auditRepository
      .findByFarmIdAndEntityTypeAndEntityIdOrderByTimestampDesc(
        farmId, entityType, entityId, pageable
      )
      .map(this::toDTO);
  }
  
  public Page<AuditLogDTO> getFarmAudit(
    String farmId,
    AuditFilter filter,
    Pageable pageable
  ) {
    // Queries dinámicas con filtros
    return auditRepository
      .findByFarmIdAndTimestampBetweenAndOperation(
        farmId, filter.getStartDate(), filter.getEndDate(),
        filter.getOperations(), pageable
      )
      .map(this::toDTO);
  }
  
  public AuditReport generateReport(String farmId, LocalDate startDate, LocalDate endDate) {
    List<AuditLog> logs = auditRepository
      .findByFarmIdAndTimestampBetween(farmId, startDate.atStartOfDay(), endDate.atEndOfDay());
    
    return new AuditReport()
      .setTotalChanges(logs.size())
      .setChangesByOperation(countByOperation(logs))
      .setChangesByUser(countByUser(logs))
      .setTopChangedPotreros(getTopPotreros(logs, 5));
  }
}
```

#### AuditContext.java

```java
public class AuditContext {
  private String farmId;
  private String entityType;
  private String entityId;
  private AuditOperation operation;
  private String userId;
  private String userName;
  private String userRole;
  private Map<String, Object> beforeValues;
  private Map<String, Object> afterValues;
  private String clientIp;
  private String sessionId;
  
  // constructor, getters, setters
}
```

### Interceptor/Aspect

#### AuditAspect.java (Usando Spring AOP)

```java
@Aspect
@Component
public class AuditAspect {
  
  @Around("@annotation(auditLog)")
  public Object auditOperation(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
    String currentUser = getCurrentUser();
    String clientIp = getClientIp();
    String sessionId = getSessionId();
    
    Object[] args = pjp.getArgs();
    String farmId = extractFarmId(args);
    
    // Capturar estado ANTES
    Map<String, Object> beforeValues = captureBeforeState(args);
    
    // Ejecutar operación
    Object result = pjp.proceed();
    
    // Capturar estado DESPUÉS
    Map<String, Object> afterValues = captureAfterState(result);
    
    // Registrar auditoría (asincrónico)
    auditService.auditAsync(
      new AuditContext()
        .setFarmId(farmId)
        .setEntityType(auditLog.entityType())
        .setEntityId(extractEntityId(args))
        .setOperation(auditLog.operation())
        .setUserId(currentUser)
        .setUserName(getCurrentUserName())
        .setUserRole(getCurrentUserRole())
        .setBeforeValues(beforeValues)
        .setAfterValues(afterValues)
        .setClientIp(clientIp)
        .setSessionId(sessionId)
    );
    
    return result;
  }
  
  private Map<String, Object> captureBeforeState(Object[] args) {
    // Convertir argumentos a Map
    if (args.length > 0 && args[0] instanceof Pasture) {
      Pasture p = (Pasture) args[0];
      return Map.of(
        "status", p.getStatus(),
        "residualHeight", p.getResidualHeightCm(),
        "eta", p.getEta()
      );
    }
    return new HashMap<>();
  }
}
```

#### @Audit Annotation

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
  AuditOperation operation();
  String entityType();
}
```

### Uso en Controllers

```java
@PostMapping("/{pastureId}/open")
@Audit(operation = AuditOperation.OPEN, entityType = "PASTURE")
public ResponseEntity<PastureDTO> openPasture(
  @PathVariable String farmId,
  @PathVariable String pastureId,
  @RequestBody OpenPastureRequest request
) {
  // Lógica normal
  Pasture pasture = pastureService.open(farmId, pastureId, request);
  return ResponseEntity.ok(toPastureDTO(pasture));
  
  // Auditoría se registra automáticamente por el @Audit annotation
}
```

### Repository

#### AuditLogRepository.java

```java
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
  
  Page<AuditLog> findByFarmIdAndEntityTypeAndEntityIdOrderByTimestampDesc(
    String farmId, String entityType, String entityId, Pageable pageable
  );
  
  Page<AuditLog> findByFarmIdAndOperationInOrderByTimestampDesc(
    String farmId, Collection<AuditOperation> operations, Pageable pageable
  );
  
  List<AuditLog> findByFarmIdAndTimestampBetweenOrderByTimestampDesc(
    String farmId, LocalDateTime startDate, LocalDateTime endDate
  );
  
  List<AuditLog> findByEntityIdOrderByTimestampDesc(String entityId);
  
  long countByFarmIdAndOperation(String farmId, AuditOperation operation);
  
  long countByUserId(String userId);
}
```

### Controller

#### AuditController.java

```java
@RestController
@RequestMapping("/farms/{farmId}/audit")
public class AuditController {
  
  @GetMapping("/pastures/{pastureId}")
  public ResponseEntity<Page<AuditLogDTO>> getPastureAudit(
    @PathVariable String farmId,
    @PathVariable String pastureId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "50") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
    Page<AuditLogDTO> result = auditService.getEntityAudit(
      farmId, "PASTURE", pastureId, pageable
    );
    return ResponseEntity.ok(result);
  }
  
  @GetMapping
  public ResponseEntity<Page<AuditLogDTO>> getFarmAudit(
    @PathVariable String farmId,
    @RequestParam(required = false) String operation,
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "50") int size
  ) {
    AuditFilter filter = new AuditFilter(operation, startDate, endDate);
    Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
    Page<AuditLogDTO> result = auditService.getFarmAudit(farmId, filter, pageable);
    return ResponseEntity.ok(result);
  }
  
  @GetMapping("/report")
  public ResponseEntity<AuditReport> getAuditReport(
    @PathVariable String farmId,
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate
  ) {
    LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? endDate : LocalDate.now();
    AuditReport report = auditService.generateReport(farmId, start, end);
    return ResponseEntity.ok(report);
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`AuditLog.java`** - Entity
2. **`AuditOperation.java`** - Enum
3. **`AuditLogRepository.java`** - Repository
4. **`AuditService.java`** - Service
5. **`AuditContext.java`** - Context DTO
6. **`AuditAspect.java`** - Spring AOP Aspect
7. **`@Audit.java`** - Annotation
8. **`AuditFilter.java`** - Filter DTO
9. **`AuditLogDTO.java`** - DTO
10. **`AuditReport.java`** - Report DTO
11. **`AuditController.java`** - REST Controller
12. **`AuditControllerTest.java`** - Tests

### Archivos a Modificar

1. **`PastureController.java`** - Agregar @Audit annotations
2. **`PastureService.java`** - Inyectar AuditService (opcional si usas Aspect)
3. **`EventController.java`** - Agregar @Audit annotations
4. **Otros controllers** - Según sea necesario

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear Entity AuditLog
- Tabla con índices (farmId, entityId, timestamp, operation, userId)
- Prevenir modificaciones (onUpdate exception)

### Paso 2: Crear Service AuditService
- Método audit() para registrar cambios
- Métodos para consultar auditoría
- Método para generar reportes

### Paso 3: Crear Aspect/Annotation
- @Audit annotation para métodos
- AuditAspect para capturar antes/después
- Registrar auditoría asincrónico

### Paso 4: Crear Controller
- Endpoints para consultar auditoría
- Endpoint para reporte

### Paso 5: Actualizar Controladores Existentes
- Agregar @Audit annotations a operaciones

### Paso 6: Tests
- Tests unitarios para service
- Tests de integración para controller
- Tests de aspect

---

## 🧪 **Casos de Prueba**

### Test Unitarios

```java
describe('AuditService', () => {
  
  test('registra cambios correctamente', () => {
    AuditContext context = new AuditContext()
      .setOperation(AuditOperation.OPEN)
      .setBeforeValues(Map.of("status", "DISPONIBLE"))
      .setAfterValues(Map.of("status", "EN_USO"));
    
    auditService.audit(context);
    
    AuditLog log = auditRepository.findById("AL-001").get();
    assertEquals(AuditOperation.OPEN, log.getOperation());
    assertEquals(1, log.getChangedFields().size());
  });
  
  test('genera descripción legible', () => {
    AuditContext context = new AuditContext()
      .setEntityType("PASTURE")
      .setEntityId("P001")
      .setOperation(AuditOperation.OPEN);
    
    String desc = auditService.generateDescription(context);
    assertTrue(desc.contains("P001"));
    assertTrue(desc.contains("Abrir"));
  });
});
```

### Test Integración

```java
describe('AuditController', () => {
  
  test('GET /audit/pastures/{id} retorna auditlog', () => {
    mockMvc.perform(get("/farms/F001/audit/pastures/P001"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray());
  });
  
  test('GET /audit/report retorna estadísticas', () => {
    mockMvc.perform(get("/farms/F001/audit/report"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalChanges").isNumber())
      .andExpect(jsonPath("$.changesByOperation").isMap());
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Registrar OPEN Automáticamente
```gherkin
Scenario: Usuario abre potrero, auditoría registra
  Given usuario USER-123 realiza OPEN en P001
  When operación se completa
  Then AuditLog se crea con operation=OPEN, userId=USER-123
```

### Escenario 2: Capturar Cambios
```gherkin
Scenario: Cambios antes/después
  Given CLOSE operación con residualHeight 15cm
  When se audita
  Then beforeValues.residualHeight=null, afterValues.residualHeight=15
```

### Escenario 3: Consultar Auditoría
```gherkin
Scenario: Ver historial de auditoría
  Given potrero P001 con 20 cambios
  When GET /audit/pastures/P001
  Then retorna todos los 20 registros
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- Spring Data JPA
- Spring AOP
- Jackson (JSON)

**Componentes relacionados**:
- HU#12 (Historial de Eventos)
- PastureService
- EventController

---

## 🔧 **Refinamiento Técnico**

### AOP Aspect - Interceptor

```java
@Aspect
@Component
public class AuditingAspect {
  
  @Around("@annotation(Audit)")
  public Object audit(ProceedingJoinPoint pjp) throws Throwable {
    // Capturar estado ANTES
    Object target = pjp.getTarget();
    Object[] args = pjp.getArgs();
    
    // Ejecutar método
    Object result = pjp.proceed();
    
    // Comparar ANTES/DESPUÉS
    Map<String, Object> beforeValues = extractFieldValues(target);
    Map<String, Object> afterValues = extractFieldValues(result);
    
    // Registrar auditoría async (no bloquea)
    auditLogService.saveAsync(
      operation: "UPDATE",
      entity: target.getClass().getSimpleName(),
      beforeValues,
      afterValues,
      user: getCurrentUser()
    );
    
    return result;
  }
}
```

### API Contract

**Endpoint:**
```
GET /farms/{farmId}/audit?entityId=P001&startDate=2026-01-01
Response: [{id, operation, beforeValues, afterValues, user, timestamp}]
```

### Testing Strategy

**Tests Críticos:**
- Auditoría se captura en UPDATE
- Antes/después son diferentes
- Usuario se registra correctamente
- Async no bloquea operación
- Performance: operación + audit < 300ms

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `AuditLog.java` entity con índices
- [ ] `AuditOperation.java` enum
- [ ] `AuditLogRepository.java` con queries
- [ ] `AuditService.java` con lógica completa
- [ ] `AuditContext.java` DTO
- [ ] `AuditAspect.java` con captura antes/después
- [ ] `@Audit.java` annotation
- [ ] `AuditController.java` con endpoints
- [ ] Método audit() funciona
- [ ] Registra OPEN, CLOSE, CREATE, EDIT, DELETE
- [ ] Captura antes/después
- [ ] Captura userId, userName, timestamp
- [ ] Registros inmutables (no modificables)
- [ ] Registra IP (bonus)
- [ ] Registra sessionId (bonus)
- [ ] GET /audit/pastures/{id} funciona
- [ ] GET /audit retorna auditlog por farmId
- [ ] Reporte de auditoría (bonus)
- [ ] Auditoría async (no bloquea operación)
- [ ] Performance: operación + auditoría < 300ms
- [ ] Índices en BD para queries rápidas
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests integración: todos los endpoints
- [ ] Documentación de retención

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Audit Trail Pattern + AOP Interceptor + Immutable Logs

**Justificación:** **Audit Trail**: Registra automáticamente quién, cuándo, qué cambió. **AOP Interceptor**: @Aspect captura cambios sin modificar código. **Immutable Logs**: No se pueden modificar/eliminar. **Before/After State**: Auditoría completa. **Compliance**: Trazabilidad regulatoria. **Queryable**: Consultable por entidad.

**Componentes Afectados:**

- **AuditLog.java (Nuevo):** Entity inmutable. Campos: id, operation, entityType, entityId, userId, userName, timestamp, beforeValues (JSON), afterValues (JSON), clientIp, sessionId. NOT NULL constraints. Índices: entityId, userId, timestamp.

- **AuditingAspect.java (Nuevo):** AOP @Aspect. Anotación @Audit en métodos. Intercepta OPEN, CLOSE, EDIT, DELETE, CREATE. Captura parámetros antes/después. Async (no bloquea).

- **AuditLogRepository.java (Nuevo):** JPA Repository con queries optimizadas. Métodos: findByEntityId, findByUserId, findByDateRange, etc. Usa índices.

- **AuditLogService.java (Nuevo):** Lógica de consultas. Métodos: getEntityHistory, getUserHistory, generateReport. Paginado.

- **AuditLogController.java (Nuevo):** Endpoints GET. Routes: `/audit/entities/{id}`, `/audit/users/{id}`, `/audit/report`. Solo lectura.

- **AuditLogDTO.java (Nuevo):** DTO con beforeValues, afterValues, cambios legibles. Sin datos sensibles.

**Hitos:**
1. AuditLog.java + AuditingAspect.java (core)
2. AuditLogRepository.java (queries)
3. AuditLogService.java (lógica)
4. AuditLogController.java (endpoints)
5. Tests + monitoring

### Validación de Impacto

✅ **Automatic Recording**: Sin código extra
✅ **Immutable**: Cumple regulaciones
✅ **Before/After**: Auditoría completa
✅ **Performance**: Async, no bloquea
✅ **Queryable**: Historial consultable

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-012: Eventos (genera datos)
- ✅ PASTURES-HU-016: Reportes (usa auditoría)
- → PASTURES-HU-013: Auditoría (esta)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Audit Trail + AOP (compliance)

---
- [ ] Code review aprobado
- [ ] CI/CD green
- [ ] Swagger documentado
- [ ] Sin warnings de linting

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
