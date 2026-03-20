# 🌱 PASTURES-HU#19: Backend: Soporte Multi-tenant

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Multi-tenant + Tenant Context | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend architect**, quiero implementar soporte multi-tenant para aislar datos por empresa/finca, de tal forma que:

1. Cada usuario pertenece a una o más empresas (tenants)
2. Los datos se aíslen completamente por tenant
3. Las queries se filtren automáticamente por tenant
4. No sea posible acceder a datos de otro tenant
5. Cada tenant tenga su propia configuración
6. Sea escalable horizontalmente (múltiples instancias)
7. Se implemente de forma transparente en el código

Esto habilitará que la plataforma soporte múltiples empresas agrícolas de forma segura y escalable.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Concepto de Tenant
```gherkin
Scenario: Sistema reconoce tenants
  Given sistema Cattle implementado
  When se configura para multi-tenant
  Then:
    [ ] Cada usuario pertenece a tenant (farm/company)
    [ ] Tenant se identifica por ID único (T001, F001, etc)
    [ ] Un usuario puede pertenecer a múltiples tenants
    [ ] Roles pueden variar por tenant (owner, manager, user)
```

### AC#2: Tenant Context
```gherkin
Scenario: Identificar tenant actual
  Given usuario autenticado en tenant T001
  When realiza operación cualquiera
  Then:
    [ ] TenantContext.getCurrentTenant() = "T001"
    [ ] Disponible en toda la petición HTTP
    [ ] Extraído del JWT token
    [ ] Validado en cada request
```

### AC#3: Filtrado Automático de Queries
```gherkin
Scenario: Queries se filtran automáticamente
  Given usuario de tenant T001
  When realiza GET /pastures
  Then:
    [ ] Query SQL añade: WHERE farm_id = 'F001'
    [ ] Sin necesidad de filtro explícito en código
    [ ] Implementado con JPA specs o similar
    [ ] Imposible "olvidar" filtro
```

### AC#4: Prevención de Acceso Cruzado
```gherkin
Scenario: No acceder datos de otro tenant
  Given usuario de T001 intenta acceder P002 de T002
  When realiza GET /pastures/P002
  Then:
    [ ] HTTP 404 Not Found (como si no existiera)
    [ ] No error 403 (que delataría existencia)
    [ ] Se registra intento fallido
    [ ] Usuario no sabe que data existe en otro tenant
```

### AC#5: Multi-tenant en JWT
```gherkin
Scenario: JWT contiene tenant info
  Given usuario autenticado
  When recibe JWT token
  Then payload contiene:
    [ ] userId: "user-id"
    [ ] tenants: ["T001", "T002"] (array de tenants)
    [ ] currentTenant: "T001" (seleccionado)
    [ ] roles: {T001: "OWNER", T002: "USER"}
    [ ] expiresAt: timestamp
```

### AC#6: Cambiar Tenant Activo
```gherkin
Scenario: Usuario puede cambiar entre sus tenants
  Given usuario con acceso a T001 y T002
  When POST /auth/switch-tenant con tenantId=T002
  Then:
    [ ] JWT se actualiza con currentTenant=T002
    [ ] Queries ahora filtran por T002
    [ ] Histórico preservado
    [ ] Sin logout/login
```

### AC#7: Entity Auditable por Tenant
```gherkin
Scenario: Toda entidad sabe su tenant
  Given cualquier entity (Pasture, Event, AuditLog, etc)
  Then debe tener:
    [ ] farmId o tenantId field
    [ ] Indexado para performance
    [ ] NOT NULL constraint
    [ ] Parte de la identidad
    [ ] Parte de soft delete scope
```

### AC#8: Scope en Repositories
```gherkin
Scenario: Repositories filtran automáticamente
  Given PastureRepository
  When usuario llama: findByFarmId(farmId)
  Then:
    [ ] Query se añade: WHERE farm_id = ? AND tenant_id = currentTenant
    [ ] Transparente al llamador
    [ ] Implementado con AOP o similar
    [ ] Sin cambios en firma de método
```

### AC#9: Transacciones por Tenant
```gherkin
Scenario: Transacciones aisladas por tenant
  Given transacción que modifica datos
  When transacción abarca múltiples entities
  Then:
    [ ] Todas las entities pertenecen a mismo tenant
    [ ] Se valida en inicio de transacción
    [ ] Si hay cross-tenant: error
    [ ] Rollback seguro
```

### AC#10: Configuración por Tenant
```gherkin
Scenario: Cada tenant puede tener config diferente
  Given dos tenants: T001 (agrícola), T002 (ganadería)
  Then pueden tener:
    [ ] Diferentes campos personalizados
    [ ] Diferentes roles/permisos
    [ ] Diferentes integraciones (ej: SQS topics)
    [ ] Diferentes feature flags
    [ ] Separado en TenantConfiguration
```

### AC#11: Datos Globales vs Tenant
```gherkin
Scenario: Diferenciar datos globales de tenant
  Given sistema con datos de ambos tipos
  Then:
    [ ] Usuarios: global (un usuario, múltiples tenants)
    [ ] Roles: global pero asignados por tenant
    [ ] Potreros: tenant (aislados)
    [ ] AuditLog: tenant (aislado)
    [ ] Configuración: tenant (separada)
```

### AC#12: Escalabilidad Horizontal
```gherkin
Scenario: Sistema escala con múltiples instancias
  Given N instancias de API
  When tenants distribuidos entre ellas
  Then:
    [ ] Cada instancia accede a BD compartida
    [ ] Tenant context es local por request
    [ ] Sin estado compartido entre instancias
    [ ] Sincronización de cache si existe
```

### AC#13: Performance Optimizado
```gherkin
Scenario: Multi-tenant sin overhead de performance
  Given queries filtradas por tenant
  Then:
    [ ] Índices incluyen tenant_id como primera columna
    [ ] Query plans optimizados
    [ ] Sin N+1 queries
    [ ] Latencia < 100ms más que single-tenant
```

### AC#14: Testing
```gherkin
Scenario: Tests verifica aislamiento
  Given suite de tests
  Then:
    [ ] Test: usuario T001 no ve datos T002
    [ ] Test: query siempre filtra por tenant
    [ ] Test: cambio de tenant funciona
    [ ] Test: cross-tenant error
    [ ] Cobertura >= 85%
```

### AC#15: Documentación
```gherkin
Scenario: Documentar arquitectura multi-tenant
  Given implementación completa
  Then:
    [ ] Diagrama de arquitectura
    [ ] Guía de cómo implementar en nuevas entities
    [ ] Ejemplos de Repository
    [ ] Ejemplos de Controller
    [ ] Troubleshooting guide
```

---

## 📊 **Especificación Técnica**

### Arquitectura

```
┌─────────────────────────────────────────┐
│         HTTP Request (JWT)              │
│  Header: Authorization: Bearer {token}  │
└──────────┬──────────────────────────────┘
           │
           ↓
┌─────────────────────────────────────────┐
│  TenantInterceptor / TenantFilter       │
│  - Extraer tenant del JWT               │
│  - Validar acceso del usuario           │
│  - Establecer TenantContext             │
└──────────┬──────────────────────────────┘
           │
           ↓
┌─────────────────────────────────────────┐
│  Controller / Service / Repository      │
│  - Acceso a TenantContext               │
│  - Queries filtradas automáticamente    │
│  - Validación de acceso                 │
└──────────┬──────────────────────────────┘
           │
           ↓
┌─────────────────────────────────────────┐
│  Base de Datos (Compartida)             │
│  - Todos los tenants                    │
│  - Separados por farm_id/tenant_id      │
└─────────────────────────────────────────┘
```

### TenantContext

```java
@Component
@Scope("request")
public class TenantContext {
    
    private String currentTenant;
    private String userId;
    private List<String> userTenants;
    private Map<String, String> roles; // tenant -> role
    
    public static TenantContext getCurrentContext() {
        return TenantContextHolder.get();
    }
    
    public String getCurrentTenant() {
        return currentTenant;
    }
    
    public void setCurrentTenant(String tenant) {
        if (!userTenants.contains(tenant)) {
            throw new AccessDeniedException("Usuario no tiene acceso a tenant: " + tenant);
        }
        this.currentTenant = tenant;
    }
    
    public boolean hasAccessToTenant(String tenant) {
        return userTenants.contains(tenant);
    }
    
    public String getRoleInTenant(String tenant) {
        return roles.getOrDefault(tenant, "USER");
    }
    
    public boolean isAdmin() {
        return getRoleInTenant(currentTenant).equals("ADMIN");
    }
}
```

### JWT con Tenant

```java
public class JwtTokenProvider {
    
    public String generateToken(UserEntity user) {
        List<String> tenants = userTenantRepository.findTenantsByUserId(user.getId());
        String defaultTenant = tenants.get(0); // o last used
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenants", tenants);
        claims.put("currentTenant", defaultTenant);
        claims.put("roles", buildRolesMap(user));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId())
            .setIssuedAt(new Date())
            .setExpiration(expirationDate)
            .signWith(signingKey)
            .compact();
    }
    
    public TenantInfo extractTenantInfo(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return new TenantInfo(
            (String) claims.get("currentTenant"),
            (List<String>) claims.get("tenants"),
            (Map<String, String>) claims.get("roles")
        );
    }
}
```

### Interceptor

```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final TenantContext tenantContext;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String token = extractToken(request);
        if (token == null) {
            throw new UnauthorizedException("No token provided");
        }
        
        TenantInfo tenantInfo = jwtTokenProvider.extractTenantInfo(token);
        
        // Establecer context
        tenantContext.setCurrentTenant(tenantInfo.getCurrentTenant());
        tenantContext.setUserTenants(tenantInfo.getTenants());
        tenantContext.setRoles(tenantInfo.getRoles());
        
        // También guardar en ThreadLocal para acceso desde beans
        TenantContextHolder.set(tenantContext);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler, Exception ex) {
        TenantContextHolder.clear();
    }
}
```

### Entity Base

```java
@MappedSuperclass
public class TenantEntity {
    
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;
    
    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getCurrentContext().getCurrentTenant();
        }
    }
    
    @PostLoad
    public void postLoad() {
        // Validar que el tenant del usuario coincide con entity
        String currentTenant = TenantContext.getCurrentContext().getCurrentTenant();
        if (!this.tenantId.equals(currentTenant)) {
            throw new AccessDeniedException("Acceso denegado a este recurso");
        }
    }
}
```

### Repository con AOP

```java
@Repository
public interface PastureRepository extends JpaRepository<PastureEntity, String> {
    
    // Estos métodos se interceptan automáticamente
    Page<PastureEntity> findByFarmId(String farmId, Pageable pageable);
    
    Optional<PastureEntity> findById(String id);
}

@Aspect
@Component
public class TenantRepositoryAspect {
    
    @Around("execution(* com.cattle..Repository+.*(..))")
    public Object filterByTenant(ProceedingJoinPoint joinPoint) throws Throwable {
        String tenantId = TenantContext.getCurrentContext().getCurrentTenant();
        
        // Añadir filtro de tenant a la query
        // Interceptar ArgumentResolver o similar
        
        return joinPoint.proceed();
    }
}
```

### Especificación con Tenant

```java
public class PastureSpecification {
    
    public static Specification<PastureEntity> filterByTenant(String tenantId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("tenantId"), tenantId);
    }
    
    public static Specification<PastureEntity> filterByStatus(PastureStatus status) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), status);
    }
    
    public static Specification<PastureEntity> filterByTenantAndCriteria(
        String tenantId, 
        PastureStatus status
    ) {
        return filterByTenant(tenantId).and(filterByStatus(status));
    }
}
```

### Service

```java
@Service
public class PastureService {
    
    private final PastureRepository pastureRepository;
    private final TenantContext tenantContext;
    
    public Page<PastureDTO> listPastures(Pageable pageable) {
        String tenantId = tenantContext.getCurrentTenant();
        
        // Automáticamente filtrado por tenant
        return pastureRepository.findByFarmId(tenantId, pageable)
            .map(this::mapToDTO);
    }
    
    public PastureDTO getPasture(String pastureId) {
        String tenantId = tenantContext.getCurrentTenant();
        
        // Si pertenece a otro tenant, 404
        PastureEntity pasture = pastureRepository.findById(pastureId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new ResourceNotFoundException("Potrero no encontrado"));
        
        return mapToDTO(pasture);
    }
}
```

### Migraciones

```sql
-- Agregar tenant_id a todas las tablas
ALTER TABLE pastures ADD COLUMN tenant_id VARCHAR(36) NOT NULL DEFAULT 'default';
ALTER TABLE pasture_events ADD COLUMN tenant_id VARCHAR(36) NOT NULL DEFAULT 'default';
ALTER TABLE audit_log ADD COLUMN tenant_id VARCHAR(36) NOT NULL DEFAULT 'default';
ALTER TABLE users ADD COLUMN primary_tenant_id VARCHAR(36);

-- Crear índices
CREATE INDEX idx_pastures_tenant ON pastures(tenant_id);
CREATE INDEX idx_pastures_tenant_farm ON pastures(tenant_id, farm_id);
CREATE INDEX idx_pasture_events_tenant ON pasture_events(tenant_id);
CREATE INDEX idx_audit_log_tenant ON audit_log(tenant_id);

-- Crear tabla de relaciones user-tenant
CREATE TABLE user_tenants (
    user_id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, tenant_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (tenant_id) REFERENCES farms(id)
);
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`TenantContext.java`** - Contexto de tenant
2. **`TenantInterceptor.java`** - Interceptor HTTP
3. **`TenantRepositoryAspect.java`** - AOP para repositories
4. **`JwtTokenProvider.java`** - Modificado para tenant
5. **`TenantEntity.java`** - Clase base para entities
6. **`TenantSpecification.java`** - Specs para queries
7. **`TenantControllerTest.java`** - Tests
8. **Migration SQL** - Para agregar columnas

### Archivos a Modificar

1. **Todos los repositories** - Agregar filtros
2. **Todos los controllers** - Usar TenantContext
3. **Todas las entities** - Extender TenantEntity
4. **JWT generation** - Incluir tenant info
5. **WebConfig** - Registrar interceptor

---

## 🔧 **Refinamiento Técnico**

### TenantContext - ThreadLocal

```java
public class TenantContext {
  private static ThreadLocal<String> tenantId = new ThreadLocal<>();
  
  public static void setTenant(String id) {
    tenantId.set(id);
  }
  
  public static String getTenant() {
    return tenantId.get();
  }
  
  public static void clear() {
    tenantId.remove();
  }
}
```

### TenantInterceptor

```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
  
  @Override
  public boolean preHandle(HttpServletRequest req, 
      HttpServletResponse res, Object handler) {
    
    String tenantId = extractTenantFromJWT(req);
    TenantContext.setTenant(tenantId);
    return true;
  }
  
  @Override
  public void afterCompletion(HttpServletRequest req, 
      HttpServletResponse res, Object handler, Exception ex) {
    TenantContext.clear();
  }
}
```

### Repository with Tenant Filter

```java
@Repository
public class PastureRepository {
  
  public List<Pasture> findByFarmId(String farmId) {
    String tenantId = TenantContext.getTenant();
    
    // Query con filtro automático
    return dynamoDb.query(PastureTable)
      .where("farmId", farmId)
      .where("tenantId", tenantId)  // Auto-add
      .execute();
  }
}
```

### Testing Strategy

**Multi-tenant Tests:**
- Tenant 1 no ve datos de Tenant 2
- JWT contiene tenant ID
- Cross-tenant access rechazado (403)

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] TenantContext implementado
- [ ] TenantInterceptor registrado
- [ ] JWT contiene tenant info
- [ ] Queries filtran automáticamente
- [ ] Prevención de acceso cruzado
- [ ] Cambio de tenant funciona
- [ ] Todas las entities tienen tenant_id
- [ ] Índices de BD creados
- [ ] AOP para repositories funciona
- [ ] Validación de transacciones
- [ ] Configuración por tenant
- [ ] Performance validado
- [ ] Tests >= 85%
- [ ] Documentación completa
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

**Patrón Arquitectónico:** Multi-tenant Architecture + Tenant Context + Automatic Filtering

**Justificación:** **Tenant Context**: Identificar tenant actual en cada request. **Automatic Filtering**: Todas queries filtran por tenant automáticamente. **Data Isolation**: Imposible acceder datos otro tenant. **Scalability**: Múltiples tenants sin impacto. **Security**: Prevención cross-tenant integrada. **Transparency**: Sin cambios código negocio.

**Componentes Afectados:**

- **TenantContext.java (Nuevo):** Context holder para tenant actual. ThreadLocal storage. Métodos: `getCurrentTenant()`, `setCurrentTenant(id)`, `clearContext()`.

- **TenantInterceptor.java (Nuevo):** HTTP interceptor. Extrae tenant de JWT token. Pre-request: valida tenant en context. Post-request: limpia context.

- **TenantAspect.java (Nuevo):** AOP @Aspect. Intercepta métodos en repositories. Añade filtro automático `WHERE tenant_id = currentTenant`.

- **TenantFilter.java (Nuevo):** JPA Specification filter. Constructor: `TenantFilter(tenantId)`. Predicate: `root.get("tenantId").equal(tenantId)`.

- **TenantConfiguration.java (Nuevo):** Config por tenant. Bean properties: database, sns_topic, email_domain, etc.

- **TenantEntity.java (Nuevo):** Base class con tenantId. Anotación @MappedSuperclass. Field: `String tenantId` (NOT NULL, indexed).

**Hitos:**
1. TenantContext.java + TenantEntity.java (base)
2. TenantInterceptor.java (extract tenant)
3. TenantFilter.java + TenantAspect.java (filtering)
4. TenantConfiguration.java (config)
5. Tests + migration scripts

### Validación de Impacto

✅ **Data Isolation**: Completamente aislado por tenant
✅ **Automatic Filtering**: Transparente, imposible olvidar
✅ **Security**: Cross-tenant access prevenido
✅ **Scalability**: Múltiples tenants + instancias
✅ **Maintainability**: Cambios solo en TenantContext

### Referencias y Validación

**Historias Relacionadas:**
- → PASTURES-HU-019: Multi-tenant (esta - architecture foundation)
- ← PASTURES-HU-018: SNS/SQS usa tenant context
- ← PASTURES-HU-020: DELETE respeta tenant

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Multi-tenant + automatic filtering (P3 architecture)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
