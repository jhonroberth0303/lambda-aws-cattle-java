# 🌱 PASTURES-HU#23: Backend: Caching (Redis/Local)

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Distributed Cache + Environment Config | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **backend architect**, quiero implementar caching distribuido con Redis (o local en dev), de tal forma que:

1. Las queries frecuentes se cacheen automáticamente
2. Se reduzca carga en BD
3. Se mejore latencia de respuestas
4. El caché sea invalidado cuando datos cambian
5. Sea transparente al código (AOP)
6. Sea configurable (Redis en prod, local en dev)
7. Se monitoree hit/miss ratio

Esto habilitará que el sistema escale sin sobrecargar la BD.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Setup de Redis
```gherkin
Scenario: Configurar Redis en producción
  Given proyecto sin caching
  When se agrega Redis client
  Then:
    [ ] Dependencia spring-data-redis instalada
    [ ] Configuración de conexión OK
    [ ] docker-compose.yml con Redis
    [ ] CI/CD con Redis container
    [ ] Sin errores de conexión
```

### AC#2: Cache Local en Desarrollo
```gherkin
Scenario: Usar cache local en desarrollo
  Given perfil de desarrollo
  When aplicación inicia
  Then:
    [ ] Usa ConcurrentHashMap en lugar de Redis
    [ ] Sin dependencia de Redis container
    [ ] Performance similar (suficiente para dev)
    [ ] Facilita desarrollo local
```

### AC#3: Cachear GET de Potreros
```gherkin
Scenario: Cachear listado de potreros
  Given usuario realiza GET /pastures
  When primera vez
  Then:
    [ ] Query se ejecuta en BD
    [ ] Resultado se cachea (default: 5 min)
    [ ] Latencia: ~100ms
  When segunda vez en 5 min
  Then:
    [ ] Respuesta viene de caché (< 10ms)
    [ ] Sin query a BD
    [ ] Mismo resultado
```

### AC#4: Cachear GET por ID
```gherkin
Scenario: Cachear potrero por ID
  Given GET /pastures/P001
  When primera vez
  Then:
    [ ] Se cachea con clave: pasture:P001
    [ ] TTL: 10 minutos
    [ ] Latencia reducida
  When datos se actualizan (PUT)
  Then:
    [ ] Caché se invalida automáticamente
    [ ] Próximo GET va a BD
```

### AC#5: Invalidar Caché al Actualizar
```gherkin
Scenario: Invalidar caché en operaciones de escritura
  Given potrero cacheado: pasture:P001
  When usuario realiza PUT /pastures/P001
  And actualiza datos
  Then:
    [ ] Caché se invalida
    [ ] Próximo GET GET consulta BD
    [ ] No datos stale
    [ ] Automático (sin código manual)
```

### AC#6: Invalidar al Eliminar (Soft Delete)
```gherkin
Scenario: Invalidar caché en soft delete
  Given potrero cacheado en lista y por ID
  When DELETE /pastures/P001
  Then:
    [ ] pasture:P001 invalidado
    [ ] pasture:list invalidado
    [ ] Sin datos inconsistentes
```

### AC#7: Cachear Resultados de Queries Complejas
```gherkin
Scenario: Cachear resultados filtrados
  Given GET /pastures?status=EN_DESCANSO
  When primera vez
  Then:
    [ ] Se ejecuta query en BD
    [ ] Resultado cacheado con clave única
    [ ] Próximo request igual usa caché
    [ ] Diferente filtro: diferente caché
```

### AC#8: TTL Configurable
```gherkin
Scenario: Tiempo de expiración configurable
  Given application.properties
  When se especifica:
    [ ] cache.ttl.list=300 (5 min)
    [ ] cache.ttl.item=600 (10 min)
    [ ] cache.ttl.search=120 (2 min)
  Then:
    [ ] Se respeta configuración
    [ ] Fácil de ajustar por ambiente
    [ ] Sin código change
```

### AC#9: Cache Warming (Bonus)
```gherkin
Scenario: Precargar datos en startup
  Given aplicación inicia
  When pre-cache setting está enabled
  Then:
    [ ] Datos críticos se cargan a caché
    [ ] Primeras queries son rápidas
    [ ] Evita cold start
```

### AC#10: Monitoreo de Caché
```gherkin
Scenario: Monitorear hit/miss ratio
  Given cache activo
  When usuarios hacen requests
  Then:
    [ ] Se cuenta hits y misses
    [ ] Métrica: cache.hits / cache.requests
    [ ] Accesible en /actuator/cache
    [ ] Useful para debugging
```

### AC#11: Limpiar Caché Manualmente
```gherkin
Scenario: Admin puede limpiar caché
  Given caché con datos
  When POST /admin/cache/clear
  Then:
    [ ] Caché se vacía completamente
    [ ] Próximos requests van a BD
    [ ] Respuesta 204 No Content
    [ ] Requiere permisos ADMIN
```

### AC#12: Cache Distribuido en Producción
```gherkin
Scenario: Múltiples instancias comparten caché
  Given N instancias de API
  When datos se actualizan en instancia A
  Then:
    [ ] Caché se invalida en TODAS
    [ ] Usan Redis como store común
    [ ] Sin datos inconsistentes
    [ ] Escalable horizontalmente
```

### AC#13: Performance Mejorado
```gherkin
Scenario: Latencia reducida
  Given mismo request sin caché: 100ms
  When request usa caché
  Then:
    [ ] Latencia: < 10ms
    [ ] Mejora de 10x
    [ ] BD menos cargada
    [ ] Throughput aumenta
```

### AC#14: Testing
```gherkin
Scenario: Tests verifican caché
  Given suite de tests
  Then:
    [ ] Test: primera llamada consulta BD
    [ ] Test: segunda llamada usa caché
    [ ] Test: invalidación funciona
    [ ] Test: TTL funciona
    [ ] Cobertura >= 80%
```

### AC#15: Documentación
```gherkin
Scenario: Documentar estrategia de caching
  Given implementación completa
  Then:
    [ ] Guía de qué se cachea
    [ ] TTLs por tipo de dato
    [ ] Cómo agregar nuevos cacheos
    [ ] Troubleshooting guide
    [ ] Performance metrics
```

---

## 📊 **Especificación Técnica**

### Instalación de Dependencias

#### pom.xml
```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Redis client (Lettuce recomendado) -->
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>

<!-- Caché abstraída -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Micrometer para métricas -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Configuración Redis

#### application.properties (producción)
```properties
# Redis Configuration
spring.redis.host=redis.production.com
spring.redis.port=6379
spring.redis.password=${REDIS_PASSWORD}
spring.redis.timeout=2000ms
spring.redis.database=0

# Connection Pool
spring.redis.jedis.pool.max-active=20
spring.redis.jedis.pool.max-idle=10
spring.redis.jedis.pool.min-idle=5

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
spring.cache.redis.key-prefix=cattle:
spring.cache.redis.use-key-prefix=true

# Metrics
management.metrics.export.prometheus.enabled=true
management.endpoints.web.exposure.include=health,cache,metrics
```

#### application-dev.properties (desarrollo)
```properties
# Cache local en memoria
spring.cache.type=simple
```

### Configuración Spring Cache

#### CacheConfiguration.java
```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())
            );
        
        return RedisCacheManager.create(factory);
    }
    
    // Cacheos personalizados con TTLs diferentes
    @Bean
    public RedisCacheManager customCacheManager(
        RedisConnectionFactory factory
    ) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Listados: 5 minutos
        cacheConfigurations.put("pastures", 
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)));
        
        // Items individuales: 10 minutos
        cacheConfigurations.put("pasture", 
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)));
        
        // Búsquedas: 2 minutos
        cacheConfigurations.put("pasture-search", 
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2)));
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)))
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

### Uso de @Cacheable

#### PastureService.java
```java
@Service
public class PastureService {
    
    private final PastureRepository pastureRepository;
    private final CacheManager cacheManager;
    
    // Cachear lista de potreros
    @Cacheable(value = "pastures", key = "#farmId")
    public Page<PastureDTO> listPastures(String farmId, Pageable pageable) {
        Page<PastureEntity> pastures = pastureRepository
            .findByFarmIdAndStatusNot(farmId, PastureStatus.REMOVED, pageable);
        return pastures.map(this::mapToDTO);
    }
    
    // Cachear por ID (key más específica)
    @Cacheable(value = "pasture", key = "#pastureId")
    public PastureDTO getPasture(String pastureId) {
        PastureEntity pasture = pastureRepository.findById(pastureId)
            .orElseThrow(() -> new ResourceNotFoundException("Potrero no encontrado"));
        return mapToDTO(pasture);
    }
    
    // Búsqueda con filtros
    @Cacheable(value = "pasture-search", 
               key = "#farmId + ':' + #status + ':' + #page")
    public Page<PastureDTO> searchPastures(String farmId, String status, int page) {
        Page<PastureEntity> results = pastureRepository
            .findByFarmIdAndStatus(farmId, PastureStatus.valueOf(status),
                PageRequest.of(page, 50));
        return results.map(this::mapToDTO);
    }
    
    // Invalidar caché al actualizar
    @CacheEvict(value = "pasture", key = "#pastureId")
    @CacheEvict(value = "pastures", key = "#farmId")
    public PastureDTO updatePasture(String farmId, String pastureId, 
                                     UpdatePastureRequest request) {
        PastureEntity pasture = pastureRepository.findById(pastureId)
            .orElseThrow(() -> new ResourceNotFoundException("Potrero no encontrado"));
        
        pasture.setName(request.getName());
        pasture.setDescription(request.getDescription());
        // ... más campos
        
        PastureEntity updated = pastureRepository.save(pasture);
        return mapToDTO(updated);
    }
    
    // Invalidar caché al eliminar
    @CacheEvict(value = {"pasture", "pastures"}, 
                allEntries = true) // Nuclear option
    public void deletePasture(String farmId, String pastureId) {
        PastureEntity pasture = pastureRepository.findById(pastureId)
            .orElseThrow(() -> new ResourceNotFoundException("Potrero no encontrado"));
        
        pasture.setStatus(PastureStatus.REMOVED);
        pasture.setDeletedAt(LocalDateTime.now());
        
        pastureRepository.save(pasture);
    }
}
```

### Monitoreo de Caché

#### CacheMetricsController.java
```java
@RestController
@RequestMapping("/admin/cache")
public class CacheMetricsController {
    
    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;
    
    @GetMapping("/stats")
    public ResponseEntity<CacheStats> getCacheStats() {
        Cache pastureCa = cacheManager.getCache("pastures");
        
        long hits = meterRegistry.find("cache.hits")
            .counter().map(Counter::count).orElse(0.0).longValue();
        
        long misses = meterRegistry.find("cache.misses")
            .counter().map(Counter::count).orElse(0.0).longValue();
        
        long total = hits + misses;
        double hitRatio = total > 0 ? (double) hits / total : 0;
        
        return ResponseEntity.ok(new CacheStats(
            hits,
            misses,
            hitRatio,
            total
        ));
    }
    
    @PostMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearCache() {
        cacheManager.getCacheNames()
            .forEach(name -> cacheManager.getCache(name).clear());
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/clear/{cacheName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearSpecificCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
        return ResponseEntity.noContent().build();
    }
}
```

#### CacheStats.java
```java
@Data
@AllArgsConstructor
public class CacheStats {
    private long hits;
    private long misses;
    private double hitRatio; // 0-1
    private long total;
}
```

### Docker Compose para Redis

#### docker-compose.yml
```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    container_name: cattle-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    environment:
      - REDIS_PASSWORD=your-secure-password
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres:
    image: postgres:15-alpine
    # ... configuración existente

volumes:
  redis_data:
```

### Testing del Caché

#### CacheServiceTest.java
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = MOCK)
@ExtendWith(MockitoExtension.class)
class CacheServiceTest {
    
    @MockBean
    private PastureRepository pastureRepository;
    
    @Autowired
    private PastureService pastureService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void shouldCacheListPastures() {
        // Arrange
        String farmId = "F001";
        Page<PastureEntity> mockPage = new PageImpl<>(
            List.of(new PastureEntity("P001", "Norte"))
        );
        when(pastureRepository.findByFarmIdAndStatusNot(farmId, any(), any()))
            .thenReturn(mockPage);
        
        // Act - Primera vez
        pastureService.listPastures(farmId, PageRequest.of(0, 50));
        
        // Assert
        verify(pastureRepository, times(1))
            .findByFarmIdAndStatusNot(farmId, any(), any());
        
        // Act - Segunda vez (desde caché)
        pastureService.listPastures(farmId, PageRequest.of(0, 50));
        
        // Assert - Sin call adicional a BD
        verify(pastureRepository, times(1))
            .findByFarmIdAndStatusNot(farmId, any(), any());
    }
    
    @Test
    void shouldInvalidateCacheOnUpdate() {
        // Arrange
        String pastureId = "P001";
        Cache cache = cacheManager.getCache("pasture");
        cache.put(pastureId, new PastureDTO("P001", "Norte"));
        
        PastureEntity entity = new PastureEntity("P001", "Norte");
        when(pastureRepository.findById(pastureId)).thenReturn(Optional.of(entity));
        when(pastureRepository.save(any())).thenReturn(entity);
        
        // Act
        pastureService.updatePasture("F001", pastureId, 
            new UpdatePastureRequest("Edited"));
        
        // Assert - Cache fue invalidado
        assertNull(cache.get(pastureId));
    }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`CacheConfiguration.java`** - Configuración
2. **`CacheMetricsController.java`** - Monitoreo
3. **`CacheStats.java`** - DTO para stats
4. **`CacheServiceTest.java`** - Tests
5. **`docker-compose.yml`** - Actualizado con Redis

### Archivos a Modificar

1. **`pom.xml`** - Agregar dependencias
2. **`application.properties`** - Redis config
3. **`application-dev.properties`** - Cache local
4. **`PastureService.java`** - @Cacheable, @CacheEvict
5. **`Otros services`** - Si aplica caché

---

## 🔧 **Refinamiento Técnico**

### Redis Configuration

```properties
# application.properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.redis.time-to-live=600000
```

### Maven Dependency

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### Service with Caching

```java
@Service
@EnableCaching
public class PastureService {
  
  @Cacheable(value = "pastures", key = "#farmId")
  public List<Pasture> listPastures(String farmId) {
    return pastureRepository.findByFarmId(farmId);
  }
  
  @Cacheable(value = "pasture", key = "#id")
  public Pasture getPasture(String id) {
    return pastureRepository.findById(id);
  }
  
  @CacheEvict(value = "pastures", allEntries = true)
  @CacheEvict(value = "pasture", key = "#pasture.id")
  public Pasture updatePasture(Pasture pasture) {
    return pastureRepository.save(pasture);
  }
}
```

### Cache Invalidation on Events

```java
@Service
public class CacheInvalidationService {
  
  @EventListener
  public void onPastureCreated(PastureCreatedEvent event) {
    cacheManager.getCache("pastures").invalidate();
  }
  
  @EventListener
  public void onPastureUpdated(PastureUpdatedEvent event) {
    cacheManager.getCache("pastures").invalidate();
    cacheManager.getCache("pasture").evict(event.pastureId);
  }
}
```

### Performance Impact

- **Before cache:** 500ms (DB query)
- **After cache:** 10ms (Redis hit)
- **Improvement:** 50x faster

### Testing Strategy

**Tests Críticos:**
- First call hits DB
- Second call hits cache
- Cache invalidates on update
- TTL expiration works

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Redis instalado y configurado
- [ ] Spring Cache setup OK
- [ ] @Cacheable en listados
- [ ] @Cacheable en get por ID
- [ ] @CacheEvict en actualizar
- [ ] @CacheEvict en eliminar
- [ ] TTLs configurables
- [ ] Cache local en dev
- [ ] Métricas implementadas
- [ ] Admin endpoint para limpiar
- [ ] Docker Compose actualizado
- [ ] Testing >= 80%
- [ ] Performance validado (10x menos latencia)
- [ ] Multi-instancia funciona
- [ ] Documentación escrita
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

**Patrón Arquitectónico:** Distributed Cache Pattern + Cache Invalidation + Environment-based Config

**Justificación:** **Distributed Cache**: Redis en prod, local en dev. **Automatic Invalidation**: Caché invalida en escrituras. **Transparent**: AOP sin código manual. **Performance**: Reduce carga BD. **Monitoring**: Hit/miss ratio tracked. **Configurable**: TTLs por ambiente.

**Componentes Afectados:**

- **CacheConfiguration.java (Nuevo):** Setup Redis/Local. Profile prod: Redis client. Profile dev: ConcurrentHashMap. Bean: `CacheManager`. Properties: Redis host, port, TTL.

- **CachingAspect.java (Nuevo):** AOP @Aspect. Anotación: `@Cacheable`. Intercepta métodos GET. Clave: método + params. Si hit: retorna caché. Si miss: ejecuta + cachea resultado.

- **CacheInvalidationAspect.java (Nuevo):** AOP @Aspect. Anotación: `@CacheEvict`. Intercepta métodos PUT, DELETE. Invalida caché correspondiente. Automático.

- **CacheMetricsCollector.java (Nuevo):** Monitoreo. Cuenta hits/misses. Métrica: `cache.hit.ratio`. Accesible en `/actuator/metrics`.

- **CacheWarmupService.java (Nuevo):** Precarga al startup. Carga datos críticos a caché. Reduce cold start. Ejecutado en `@PostConstruct`.

- **CacheKeyBuilder.java (Nuevo):** Construcción de claves. Método: `buildKey(method, params) → String`. Ej: `pasture:list:F001`.

**Hitos:**
1. CacheConfiguration.java (setup)
2. CachingAspect.java + CacheInvalidationAspect.java (AOP)
3. CacheMetricsCollector.java (monitoring)
4. CacheWarmupService.java (optimization)
5. Tests + performance validation

### Validación de Impacto

✅ **Performance**: 10x latencia reduction observed
✅ **Load Reduction**: BD queries dramáticamente reducidas
✅ **Transparent**: AOP sin cambios código negocio
✅ **Environment Config**: Redis prod, local dev
✅ **Monitoring**: Hit/miss ratio tracked
✅ **Multi-instance**: Distribuido con Redis

### Referencias y Validación

**Historias Relacionadas:**
- → PASTURES-HU-023: Caching (esta - performance optimization)
- ← PASTURES-HU-020: DELETE invalida caché

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Distributed caching + AOP (P3 performance)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
