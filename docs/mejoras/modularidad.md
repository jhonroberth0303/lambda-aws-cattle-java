# 📦 Análisis de Modularidad y Separación de Dominios

## Executive Summary

El proyecto **cattle-lambda-function** implementa una arquitectura **monolítica modular** con separación de dominios (Bovinos, Ordeño, Potreros, Chatbot) pero **carece de abstracción de dominio explícita** y presenta **acoplamiento implícito** en servicios compartidos. Este análisis propone 3 fases de refactorización para mejorar modularidad manteniendo una Lambda única.

**Recomendación**: Implementar **Fase 1 (P0)** inmediatamente.

---

## 1. Estado Actual

### 1.1 Fortalezas ✅

- Separación clara a nivel de controlador
- Servicios y Repositorios organizados por dominio
- Patrón Processor consistentemente aplicado
- DTOs separados por contexto
- Entidades independientes

### 1.2 Problemas Identificados 🔴

#### Problema 1: ContextBuilderService Acoplado (CRÍTICO)
- **Descripción**: Depende directamente de 3 servicios de dominio
- **Impacto**: Bloquea separación de Chatbot a Lambda dedicada
- **Severidad**: 🔴 ALTA
- **Solución**: Crear interfaz `DomainQueryService`

#### Problema 2: QueryServices sin Interfaz (MEDIO)
- **Descripción**: Cada servicio es independiente sin contrato común
- **Impacto**: Imposible agregar nuevo QueryService sin refactorizar
- **Severidad**: 🟠 MEDIA
- **Solución**: Interfaz común

#### Problema 3: Falta de Facades (MEDIO)
- **Descripción**: Controllers inyectan múltiples dependencias por dominio
- **Impacto**: Mayor complejidad, menor mantenibilidad
- **Severidad**: 🟠 MEDIA
- **Solución**: Crear Facade por dominio

#### Problema 4: DTOs Fragmentados (BAJO)
- Nombres confusos sobre propósito

#### Problema 5: Sin Domain Events (BAJO)
- Dificulta comunicación entre dominios

---

## 2. Recomendaciones de Mejora

### 2.1 Fase 1: Abstracciones (P0) - 2-3 días

**Objetivo**: Reducir acoplamiento mediante abstracciones.

**Tareas**:
1. Crear `abstractions/DomainQueryService.java`
2. Implementar en `BovineQueryService`, `MilkingQueryService`, `PastureQueryService`
3. Refactorizar `ContextBuilderService` para usar Map<Intent, Service>
4. Agregar tests

**Beneficios inmediatos**:
- Reduce acoplamiento Chatbot-Dominios (3 servicios → 1 interfaz)
- Facilita testing unitario (1 mock vs 5 mocks)
- Permite agregar nuevo QueryService sin modificar ContextBuilder

---

### 2.2 Fase 2: Facades por Dominio (P1) - 1 semana

**Objetivo**: Clarificar responsabilidades y facilitar nuevos comportamientos.

**Tareas**:
1. Crear `BovineFacade` que encapsule Service + Processor + QueryService
2. Crear `MilkingFacade` y `PastureFacade` similarmente
3. Refactorizar Controllers para usar Facade (1 inyección vs múltiples)

**Beneficios**:
- Controllers 80% más simples
- Punto único de entrada por dominio
- Facilita agregar comportamiento transversal

---

### 2.3 Fase 3: Separación Física (P2) - 2-3 semanas

**Objetivo**: Preparar base para futura separación en microservicios.

**Cambio de estructura**:
```
ANTES (Por capa):
  controller/, services/, repository/, processor/, dtos/

DESPUÉS (Por dominio):
  domains/
    ├─ bovineIdentityItem/    {controller, service, processor, repository, dto, entity}
    ├─ milkingProd/   {controller, service, processor, repository, dto, entity}
    ├─ pasture/   {controller, service, processor, repository, dto, entity}
    ├─ chatbot/   {controller, service}
    └─ shared/    {config, security, abstractions}
```

**Beneficio**: Cada dominio auto-contenido, fácil de extraer a Lambda separada.

---

## 3. Plan Detallado - Fase 1

### Tarea 1.1: Crear interfaz DomainQueryService

```java
public interface DomainQueryService<T> {
    /**
     * Construye contexto para chatbot
     */
    List<T> buildContext(String farmId);
    
    /**
     * Construye contexto por intención
     */
    List<T> buildContextByIntent(String farmId, QueryIntent intent);
}
```

### Tarea 1.2: Implementar en servicios

```java
@Service
public class BovineQueryService implements DomainQueryService<BovineContextDTO> {
    @Override
    public List<BovineContextDTO> buildContext(String farmId) { ... }
    
    @Override
    public List<BovineContextDTO> buildContextByIntent(String farmId, QueryIntent intent) { ... }
}

// Igual para MilkingQueryService y PastureQueryService
```

### Tarea 1.3: Refactorizar ContextBuilderService

ANTES:
```java
@Service
public class ContextBuilderService {
    @Autowired private BovineQueryService bovineQueryService;
    @Autowired private MilkingQueryService milkingQueryService;
    @Autowired private PastureQueryService pastureQueryService;
    
    public String buildContext(IntentContext intent, String farmId) {
        switch (intent.getIntent()) {
            case COUNT_BOVINES:
                context.append(buildBovineCountContext(farmId));
                break;
            case AGGREGATE_MILKING:
                context.append(buildMilkingContext(farmId));
                break;
            // ...
        }
    }
}
```

DESPUÉS:
```java
@Service
public class ContextBuilderService {
    private Map<QueryIntent, DomainQueryService<?>> queryServices;
    
    public ContextBuilderService(
        BovineQueryService bovineQueryService,
        MilkingQueryService milkingQueryService,
        PastureQueryService pastureQueryService
    ) {
        this.queryServices = Map.of(
            QueryIntent.COUNT_BOVINES, bovineQueryService,
            QueryIntent.AGGREGATE_MILKING, milkingQueryService,
            QueryIntent.PASTURE_STATUS, pastureQueryService
        );
    }
    
    public String buildContext(IntentContext intent, String farmId) {
        DomainQueryService<?> service = queryServices.get(intent.getIntent());
        List<?> contextData = service.buildContextByIntent(farmId, intent.getIntent());
        return formatContext(contextData);
    }
}
```

**Resultado**: 66% menos código en ContextBuilder, agnóstico a implementaciones.

---

## 4. Impacto Estimado

### Fase 1 (Abstracciones)
- Acoplamiento Chatbot: ❌ 3 servicios → ✅ 1 interfaz
- Testabilidad: ❌ 5+ mocks → ✅ 1 mock
- Complejidad ContextBuilder: ❌ ~300 LOC → ✅ ~100 LOC
- Tiempo agregar QueryService: ❌ 2 horas → ✅ 30 min

### Fase 2 (Facades)
- Dependencias por Controller: ❌ 5 → ✅ 1
- LOC por método Controller: ❌ 20-30 → ✅ 5-10
- Facilidad para nuevas features: ❌ Modificar múltiples capas → ✅ Agregar método

### Fase 3 (Separación Física)
- Tiempo extraer dominio a Lambda: ❌ 2-3 semanas → ✅ 2-3 días
- Riesgo regresiones: ❌ Alto → ✅ Mínimo

---

## 5. Checklist de Implementación

### Fase 1
- [ ] Crear `abstractions/DomainQueryService.java`
- [ ] Implementar en `BovineQueryService`
- [ ] Implementar en `MilkingQueryService`
- [ ] Implementar en `PastureQueryService`
- [ ] Refactorizar `ContextBuilderService`
- [ ] Tests unitarios
- [ ] Validar no hay regresiones funcionales

### Fase 2
- [ ] Crear `BovineFacade`
- [ ] Crear `MilkingFacade`
- [ ] Crear `PastureFacade`
- [ ] Refactorizar Controllers
- [ ] Tests de integración

### Fase 3
- [ ] Crear estructura de directorios
- [ ] Mover archivos
- [ ] Actualizar imports
- [ ] Documentar límites de dominio

---

## 6. Riesgos y Mitigaciones

| Riesgo | Mitigation |
|--------|------------|
| Refactor introduce bugs | Aumentar test coverage primero |
| Performance impact | Benchmarking |
| Equipo desconoce abstracciones | Documentación + Code Review |

---

## 7. Próximos Pasos

1. **Aprobación** de arquitecto
2. **Planificación detallada** de Fase 1
3. **Implementación** iterativa (2-3 semanas todas las fases)
4. **Validación** de impacto

---

**Documento**: Análisis de Modularidad  
**Versión**: 1.0  
**Fecha**: Marzo 2026  
**Estado**: Propuesta

