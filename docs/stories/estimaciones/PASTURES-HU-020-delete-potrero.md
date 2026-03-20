# 🌱 PASTURES-HU#20: Backend: DELETE Potrero (Soft Delete)

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Soft Delete + Audit Trail | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend architect**, quiero implementar DELETE de potreros con soft delete, de tal forma que:

1. Los potreros se marquen como REMOVED (no se eliminen físicamente)
2. Los datos históricos se preserven para auditoría
3. Los potreros REMOVED no aparezcan en listados normales
4. Se registre quién y cuándo eliminó cada potrero
5. Sea posible restaurar potreros eliminados (opcionalmente)
6. Los endpoints de DELETE sean protegidos (solo administradores)
7. Se publique evento de eliminación en SNS (HU#16)

Esto habilitará que se mantenga la integridad histórica y auditoría, mientras se "elimina" potreros de forma segura.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Endpoint DELETE
```gherkin
Scenario: Eliminar un potrero
  Given usuario administrador autenticado
  And potrero P001 existe en estado DISPONIBLE
  When realiza DELETE /farms/F001/pastures/P001
  Then:
    [ ] HTTP 204 No Content (exitoso)
    [ ] Potrero no se elimina de BD
    [ ] Status del potrero cambia a REMOVED
    [ ] Campo deletedAt se llena con timestamp
    [ ] Campo deletedBy se llena con userId
    [ ] Sin errores
```

### AC#2: Soft Delete en BD
```gherkin
Scenario: Potrero se marca como eliminado lógicamente
  Given potrero con status DISPONIBLE
  When se ejecuta DELETE
  Then en BD:
    [ ] Registro NO se borra
    [ ] Campo: status = 'REMOVED'
    [ ] Campo: deletedAt = '2026-01-09T15:30:00Z'
    [ ] Campo: deletedBy = 'admin-user-id'
    [ ] Otros campos se conservan (nombre, area, etc)
    [ ] Timestamps: createdAt, updatedAt sin cambios
```

### AC#3: Filtrar REMOVED del GET
```gherkin
Scenario: Potreros eliminados no aparecen en listados
  Given 10 potreros, 3 de ellos con status REMOVED
  When usuario realiza GET /pastures
  Then:
    [ ] Retorna solo 7 potreros (sin REMOVED)
    [ ] Campo status != 'REMOVED'
    [ ] Sin filtro explícito
    [ ] Query SQL: WHERE status != 'REMOVED'
```

### AC#4: Incluir REMOVED en Query
```gherkin
Scenario: Opción para incluir potreros eliminados
  Given 10 potreros, 3 REMOVED
  When realiza GET /pastures?includeDeleted=true
  Then:
    [ ] Retorna los 10 potreros
    [ ] Incluye los 3 REMOVED
    [ ] Parámetro opcional
    [ ] Default: false
```

### AC#5: Verificar Autorización
```gherkin
Scenario: Solo administradores pueden eliminar
  Given usuario regular (no admin)
  When intenta DELETE /pastures/P001
  Then:
    [ ] HTTP 403 Forbidden
    [ ] Mensaje: "No tienes permisos para eliminar"
    [ ] Potrero NO se elimina
    [ ] Se registra intento en logs
```

### AC#6: Validar Estado del Potrero
```gherkin
Scenario: No se puede eliminar potrero en ciertos estados
  Given potrero en estado EN_USO
  When usuario intenta DELETE
  Then:
    [ ] HTTP 409 Conflict
    [ ] Mensaje: "No puedes eliminar un potrero en uso"
    [ ] Estados permitidos para DELETE: DISPONIBLE, EN_DESCANSO, MANTENIMIENTO
    [ ] Estados NO permitidos: EN_USO, SOLD
```

### AC#7: Evento SNS de Eliminación
```gherkin
Scenario: Se publica evento cuando se elimina
  Given potrero a eliminar
  When DELETE se ejecuta exitosamente
  Then:
    [ ] Evento se publica en SNS topic
    [ ] Evento type: PASTURE_DELETED
    [ ] Contiene: farmId, pastureId, deletedBy, deletedAt
    [ ] No bloquea respuesta HTTP
    [ ] Se registra en logs
```

### AC#8: Auditoría de Eliminación
```gherkin
Scenario: Registrar eliminación en audit log
  Given DELETE /pastures/P001 exitoso
  When se consulta historial de auditoría
  Then:
    [ ] Nuevo registro en AuditLog
    [ ] operation: DELETE
    [ ] entityId: P001
    [ ] userId: user que eliminó
    [ ] timestamp: ahora
    [ ] beforeState: estado anterior
    [ ] afterState: status=REMOVED
    [ ] visible en GET /audit?entityId=P001
```

### AC#9: Restaurar Potrero (Bonus)
```gherkin
Scenario: Administrador puede restaurar potrero
  Given potrero con status REMOVED
  When realiza PATCH /pastures/P001/restore
  Then:
    [ ] HTTP 200 OK
    [ ] Potrero restaurado a estado anterior (ej: DISPONIBLE)
    [ ] deletedAt y deletedBy se limpian (NULL)
    [ ] Nuevo evento: PASTURE_RESTORED
    [ ] Registrado en auditoría
```

### AC#10: Cascada de Eliminación
```gherkin
Scenario: Validar integridad referencial
  Given potrero P001 con eventos históricos
  When se elimina potrero
  Then:
    [ ] Eventos históricos se conservan (soft delete)
    [ ] Registros en AuditLog se conservan
    [ ] Referencias en otras tablas se preservan
    [ ] No se usa ON DELETE CASCADE
    [ ] Integridad de datos garantizada
```

### AC#11: Performance
```gherkin
Scenario: DELETE es rápido
  Given potrero existe
  When DELETE /pastures/P001
  Then:
    [ ] Respuesta en < 100ms
    [ ] Sin bloqueos en BD
    [ ] No afecta otros potreros
    [ ] Índices optimizados en status
```

### AC#12: Documentación OpenAPI
```gherkin
Scenario: DELETE documentado en Swagger
  Given OpenAPI spec
  When usuario abre /swagger-ui.html
  Then:
    [ ] DELETE /pastures/{pastureId} visible
    [ ] Descripción: "Eliminar potrero (soft delete)"
    [ ] @ApiResponse 204: exitoso
    [ ] @ApiResponse 403: sin permisos
    [ ] @ApiResponse 404: no encontrado
    [ ] @ApiResponse 409: no puede eliminarse
    [ ] Example documentado
```

### AC#13: Testing
```gherkin
Scenario: Tests completos para DELETE
  Given código implementado
  Then:
    [ ] Test: DELETE exitoso
    [ ] Test: filtrado de REMOVED
    [ ] Test: validación de permisos
    [ ] Test: validación de estado
    [ ] Test: evento SNS publicado
    [ ] Test: auditoría registrada
    [ ] Test: restauración (si aplica)
    [ ] Cobertura >= 85%
```

### AC#14: Configuración de Soft Delete
```gherkin
Scenario: Soft delete configurable
  Given application.properties
  When se especifica:
    [ ] app.delete-mode=SOFT (default)
    [ ] app.delete-mode=HARD (alternativa)
    [ ] app.restore-enabled=true (permitir restaurar)
  Then:
    [ ] Comportamiento cambia según config
    [ ] Logs indican modo actual
    [ ] CI/CD usa SOFT en todos lados
```

### AC#15: Historial Preservado
```gherkin
Scenario: Ver historial completo de potrero
  Given potrero creado, usado, editado, eliminado
  When GET /pastures/P001/history
  Then:
    [ ] Mostrar todas las operaciones: CREATE, UPDATE, DELETE
    [ ] Incluir potrero REMOVED
    [ ] Timestamps y usuarios de cada cambio
    [ ] Permitir análisis completo del ciclo de vida
```

---

## 📊 **Especificación Técnica**

### Modelo de Datos

#### PastureEntity (cambios)
```java
@Entity
@Table(name = "pastures")
public class PastureEntity {
    
    @Id
    private String id;
    
    // ... otros campos ...
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PastureStatus status; // DISPONIBLE, EN_USO, EN_DESCANSO, MANTENIMIENTO, REMOVED
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // NULL si no está eliminado
    
    @Column(name = "deleted_by")
    private String deletedBy; // userId de quién eliminó
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Método helper
    public boolean isDeleted() {
        return status == PastureStatus.REMOVED;
    }
}
```

### Enumeración de Estados

```java
public enum PastureStatus {
    DISPONIBLE("Disponible"),
    EN_USO("En uso"),
    EN_DESCANSO("En descanso"),
    MANTENIMIENTO("Mantenimiento"),
    SOLD("Vendido"),
    REMOVED("Eliminado");
    
    private final String label;
    
    PastureStatus(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public boolean canBeDeleted() {
        return this == DISPONIBLE || this == EN_DESCANSO || this == MANTENIMIENTO;
    }
}
```

### Controller

#### PastureController.java
```java
@RestController
@RequestMapping("/farms/{farmId}/pastures")
public class PastureController {
    
    private final PastureService pastureService;
    private final AuthService authService;
    
    @DeleteMapping("/{pastureId}")
    @Operation(summary = "Eliminar potrero", description = "Soft delete de potrero")
    @ApiResponse(responseCode = "204", description = "Eliminado exitosamente")
    @ApiResponse(responseCode = "403", description = "Sin permisos")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @ApiResponse(responseCode = "409", description = "No puede eliminarse en este estado")
    public ResponseEntity<Void> deletePasture(
        @PathVariable String farmId,
        @PathVariable String pastureId,
        HttpServletRequest request
    ) {
        // Verificar autorización (admin)
        String userId = authService.getCurrentUserId();
        if (!authService.isAdmin(userId)) {
            throw new AccessDeniedException("No tienes permisos para eliminar");
        }
        
        // Ejecutar soft delete
        pastureService.deletePasture(farmId, pastureId, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @Operation(summary = "Listar potreros")
    public ResponseEntity<Page<PastureDTO>> listPastures(
        @PathVariable String farmId,
        @RequestParam(defaultValue = "false") boolean includeDeleted,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        Page<PastureDTO> pastures = pastureService.listPastures(
            farmId, 
            includeDeleted, 
            PageRequest.of(page, size)
        );
        return ResponseEntity.ok(pastures);
    }
    
    @PatchMapping("/{pastureId}/restore")
    @Operation(summary = "Restaurar potrero eliminado")
    @ApiResponse(responseCode = "200", description = "Restaurado exitosamente")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @ApiResponse(responseCode = "409", description = "No está eliminado")
    public ResponseEntity<PastureDTO> restorePasture(
        @PathVariable String farmId,
        @PathVariable String pastureId,
        HttpServletRequest request
    ) {
        String userId = authService.getCurrentUserId();
        if (!authService.isAdmin(userId)) {
            throw new AccessDeniedException("No tienes permisos");
        }
        
        PastureDTO restored = pastureService.restorePasture(farmId, pastureId, userId);
        return ResponseEntity.ok(restored);
    }
}
```

### Service

#### PastureService.java
```java
@Service
@Slf4j
public class PastureService {
    
    private final PastureRepository pastureRepository;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;
    
    public void deletePasture(String farmId, String pastureId, String userId) {
        // Obtener potrero
        PastureEntity pasture = pastureRepository
            .findById(pastureId)
            .orElseThrow(() -> new ResourceNotFoundException("Potrero no encontrado"));
        
        // Validar que pertenece a la finca
        if (!pasture.getFarmId().equals(farmId)) {
            throw new AccessDeniedException("Acceso denegado");
        }
        
        // Validar que puede ser eliminado
        if (!pasture.getStatus().canBeDeleted()) {
            throw new InvalidStateException(
                "No puedes eliminar un potrero en estado " + pasture.getStatus()
            );
        }
        
        // Capturar estado anterior
        PastureDTO beforeState = mapToDTO(pasture);
        
        // Soft delete
        pasture.setStatus(PastureStatus.REMOVED);
        pasture.setDeletedAt(LocalDateTime.now());
        pasture.setDeletedBy(userId);
        pasture.setUpdatedAt(LocalDateTime.now());
        
        pastureRepository.save(pasture);
        
        // Registrar en auditoría
        auditService.logDeletion(
            farmId,
            pastureId,
            userId,
            beforeState
        );
        
        // Publicar evento SNS
        eventPublisher.publishPastureEvent(new PastureEvent(
            operation = EventOperation.DELETE,
            entityId = pastureId,
            userId = userId,
            timestamp = LocalDateTime.now()
        ));
        
        log.info("Potrero {} eliminado por {}", pastureId, userId);
    }
    
    public Page<PastureDTO> listPastures(
        String farmId,
        boolean includeDeleted,
        Pageable pageable
    ) {
        if (includeDeleted) {
            return pastureRepository
                .findByFarmId(farmId, pageable)
                .map(this::mapToDTO);
        } else {
            return pastureRepository
                .findByFarmIdAndStatusNot(farmId, PastureStatus.REMOVED, pageable)
                .map(this::mapToDTO);
        }
    }
    
    public PastureDTO restorePasture(String farmId, String pastureId, String userId) {
        PastureEntity pasture = pastureRepository
            .findById(pastureId)
            .orElseThrow(() -> new ResourceNotFoundException("Potrero no encontrado"));
        
        if (!pasture.getFarmId().equals(farmId)) {
            throw new AccessDeniedException("Acceso denegado");
        }
        
        if (pasture.getStatus() != PastureStatus.REMOVED) {
            throw new InvalidStateException("El potrero no está eliminado");
        }
        
        // Capturar estado anterior
        PastureDTO beforeState = mapToDTO(pasture);
        
        // Restaurar a estado anterior (o DISPONIBLE por defecto)
        pasture.setStatus(PastureStatus.DISPONIBLE);
        pasture.setDeletedAt(null);
        pasture.setDeletedBy(null);
        pasture.setUpdatedAt(LocalDateTime.now());
        
        pastureRepository.save(pasture);
        
        // Registrar en auditoría
        auditService.logRestoration(farmId, pastureId, userId, beforeState);
        
        // Publicar evento
        eventPublisher.publishPastureEvent(new PastureEvent(
            operation = EventOperation.RESTORE,
            entityId = pastureId,
            userId = userId,
            timestamp = LocalDateTime.now()
        ));
        
        log.info("Potrero {} restaurado por {}", pastureId, userId);
        
        return mapToDTO(pasture);
    }
}
```

### Repository

#### PastureRepository.java
```java
@Repository
public interface PastureRepository extends JpaRepository<PastureEntity, String> {
    
    // Listar sin eliminados
    Page<PastureEntity> findByFarmIdAndStatusNot(
        String farmId,
        PastureStatus status,
        Pageable pageable
    );
    
    // Listar todos
    Page<PastureEntity> findByFarmId(String farmId, Pageable pageable);
    
    // Por ID (incluir eliminados)
    Optional<PastureEntity> findById(String id);
    
    // Query personalizada
    @Query("SELECT p FROM PastureEntity p WHERE p.farmId = :farmId AND p.status = 'REMOVED'")
    List<PastureEntity> findDeletedByFarmId(@Param("farmId") String farmId);
}
```

### Testing

#### PastureControllerDeleteTest.java
```java
@SpringBootTest
@AutoConfigureMockMvc
class PastureControllerDeleteTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PastureService pastureService;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void shouldDeletePastureSuccessfully() throws Exception {
        // Arrange
        when(authService.getCurrentUserId()).thenReturn("admin-id");
        when(authService.isAdmin("admin-id")).thenReturn(true);
        
        // Act
        mockMvc.perform(delete("/farms/F001/pastures/P001")
                .header("Authorization", "Bearer token"))
            // Assert
            .andExpect(status().isNoContent());
        
        verify(pastureService).deletePasture("F001", "P001", "admin-id");
    }
    
    @Test
    void shouldReturnForbiddenIfNotAdmin() throws Exception {
        // Arrange
        when(authService.getCurrentUserId()).thenReturn("user-id");
        when(authService.isAdmin("user-id")).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(delete("/farms/F001/pastures/P001")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldExcludeDeletedFromList() throws Exception {
        // Arrange
        Page<PastureDTO> page = new PageImpl<>(
            List.of(new PastureDTO("P001", "DISPONIBLE", ...))
        );
        when(pastureService.listPastures("F001", false, any()))
            .thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/farms/F001/pastures"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));
    }
    
    @Test
    void shouldIncludeDeletedIfRequested() throws Exception {
        // Arrange
        Page<PastureDTO> page = new PageImpl<>(
            List.of(
                new PastureDTO("P001", "DISPONIBLE", ...),
                new PastureDTO("P002", "REMOVED", ...)
            )
        );
        when(pastureService.listPastures("F001", true, any()))
            .thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/farms/F001/pastures?includeDeleted=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`PastureControllerDeleteTest.java`** - Tests DELETE

### Archivos a Modificar

1. **`PastureEntity.java`** - Agregar deletedAt, deletedBy
2. **`PastureStatus.java`** - Enum actualizado
3. **`PastureController.java`** - Agregar DELETE y PATCH endpoints
4. **`PastureService.java`** - Lógica de soft delete
5. **`PastureRepository.java`** - Queries sin REMOVED
6. **Migration SQL** - Agregar columnas en BD

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Actualizar BD
- Migration: agregar deleted_at, deleted_by

### Paso 2: Actualizar Entity
- Agregar campos deletedAt, deletedBy
- Agregar método isDeleted()

### Paso 3: Actualizar Status Enum
- Agregar REMOVED
- Agregar canBeDeleted()

### Paso 4: Implementar Service
- deletePasture() con validaciones
- restorePasture() (opcional)
- Actualizar queries para excluir REMOVED

### Paso 5: Agregar Controller Endpoints
- DELETE /pastures/{id}
- PATCH /pastures/{id}/restore
- GET con parámetro includeDeleted

### Paso 6: Testing
- Tests unitarios de service
- Tests integración de controller

---

## 🔧 **Refinamiento Técnico**

### Soft Delete Implementation

```java
@Entity
@Table(name = "PASTURE")
public class Pasture {
  @Id
  private String id;
  
  // Existing fields...
  
  @Column(name = "deleted_at")
  private Instant deletedAt;
  
  @Column(name = "deleted_by")
  private String deletedBy;
  
  public void softDelete(String user) {
    this.deletedAt = Instant.now();
    this.deletedBy = user;
  }
  
  public void restore() {
    this.deletedAt = null;
    this.deletedBy = null;
  }
  
  public boolean isDeleted() {
    return deletedAt != null;
  }
}
```

### Repository Query - Auto-Filter

```java
@Repository
public class PastureRepository {
  
  public List<Pasture> findActive(String farmId) {
    // Excluye eliminados automáticamente
    return findByFarmIdAndDeletedAtNull(farmId);
  }
  
  public List<Pasture> findIncludingDeleted(String farmId, 
      boolean includeDeleted) {
    if (includeDeleted) {
      return findByFarmId(farmId);
    }
    return findActive(farmId);
  }
}
```

### Controller Endpoints

```java
@DeleteMapping("/{pastureId}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> deletePasture(@PathVariable String pastureId) {
  pastureService.softDelete(pastureId, getCurrentUser());
  return ResponseEntity.noContent().build();
}

@GetMapping
public List<PastureDTO> list(
  @RequestParam(required = false) boolean includeDeleted) {
  return pastureService.findAll(includeDeleted);
}
```

### Testing Strategy

**Tests Críticos:**
- DELETE marca como REMOVED (soft delete)
- GET no incluye REMOVED por default
- GET con includeDeleted=true muestra todos
- Datos no se pierden físicamente

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Migration SQL ejecutada
- [ ] Entity actualizada con campos
- [ ] Enum Status con REMOVED
- [ ] Controller DELETE endpoint
- [ ] Controller PATCH restore endpoint
- [ ] Service deletePasture() funciona
- [ ] Service restorePasture() funciona (si aplica)
- [ ] Filtrado de REMOVED en GET
- [ ] Autorización verificada (admin)
- [ ] Validación de estado
- [ ] Evento SNS publicado
- [ ] Auditoría registrada
- [ ] Tests unitarios >= 85%
- [ ] Tests integración pasando
- [ ] OpenAPI documentado
- [ ] Code review aprobado
- [ ] CI/CD green

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Soft Delete Pattern + Audit Trail + Event Publishing

**Justificación:** **Soft Delete**: Marcar REMOVED en lugar de borrar. **Audit Trail**: Registrar quién/cuándo eliminó. **Data Recovery**: Posibilidad restauración. **Historical Integrity**: Preservar historial completo. **Event Publishing**: Notificar cambio SNS. **Authorization**: Solo admins pueden eliminar.

**Componentes Afectados:**

- **PastureDeleteController.java (Nuevo):** Endpoint DELETE. Route: `DELETE /farms/{farmId}/pastures/{pastureId}`. Autenticación + autorización. Response: HTTP 204 No Content. Delega a service.

- **PastureDeleteService.java (Nuevo):** Lógica soft delete. Métodos: `deletePasture(pastureId) → void`. Validar estado, marcar REMOVED, timestamp, userId. Publicar evento SNS.

- **DeletionAuditLog.java (Nuevo):** Entity para auditoría. Campos: id, pastureId, tenantId, deletedBy, deletedAt, reason. Query para historial.

- **SoftDeleteRepository.java (Nuevo):** Repository con filtrado. Método base: `findAll()` retorna solo NO REMOVED. Método: `findAllIncludingDeleted()` para admin.

- **RestoreProcessor.java (Nuevo):** Restauración (opcional). Método: `restorePasture(pastureId)`. Validar autorización, revert status, publish event.

- **DeleteEventPublisher.java (Nuevo):** Publica evento eliminación. Evento: PASTURE_DELETED. SNS topic. Async (no bloquea).

**Hitos:**
1. PastureDeleteService.java (core logic)
2. SoftDeleteRepository.java (query filtering)
3. DeletionAuditLog.java (auditing)
4. PastureDeleteController.java (HTTP endpoint)
5. DeleteEventPublisher.java (notifications)

### Validación de Impacto

✅ **Data Preservation**: Historial completo guardado
✅ **Audit Trail**: Quién/cuándo eliminó registrado
✅ **Soft Delete**: Query filtering automático
✅ **Event Publishing**: Notificaciones a través SNS
✅ **Recovery**: Restauración posible

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-018: SNS/SQS (publica evento)
- → PASTURES-HU-020: Soft Delete (esta - safe deletion)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Soft delete + audit trail (P3 data integrity)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
