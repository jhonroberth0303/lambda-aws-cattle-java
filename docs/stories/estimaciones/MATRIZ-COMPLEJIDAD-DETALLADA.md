# 📊 MATRIZ DE COMPLEJIDAD DETALLADA - 25 HU

**Proyecto**: Cattle Pastures
**Fecha**: 2026-01-09
**Versión**: 1.0

---

## 🎯 METODOLOGÍA

Cada historia se evalúa en:
1. **Complejidad Técnica** (1-5): Qué tan difícil es implementar
2. **Testing Requerido** (1-5): Qué tan exhaustivo debe ser testing
3. **Riesgo Técnico** (1-5): Qué puede salir mal
4. **Interdependencias** (0-3): Cuántas historias bloqueadas
5. **Esfuerzo UI/UX** (1-5): Qué tan complejo es UI/frontend
6. **Refactoring Necesario** (0-3): Cuánto código legacy afectado

**Fórmula**: Story Points = (Técnica + Testing + Riesgo + UI) / 2 + Dependencias

**Nota de vigencia documental**: esta matriz conserva estimaciones históricas útiles, pero no todos los componentes marcados con `✓` representan implementación confirmada en el código actual. Para decisiones vigentes, contrastar con `docs/arquitectura/` y con las HUs ya normalizadas de esta carpeta.

---

## 📋 HISTÓRICO DETALLADO (25 HU)

### **HISTORA #001: Backend POST Eventos**

```
Complejidad Técnica:     ███░░ 3/5 (Layered pattern estándar)
Testing Requerido:       ███░░ 3/5 (Unit + integration)
Riesgo Técnico:          ██░░░ 2/5 (Bajo, patrón conocido)
Interdependencias:       ████░ 4/5 (Bloqueador de 6 historias)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 1W1

Componentes previstos en la estimación original:
  - controlador REST de eventos de potreros
  - DTO de eventos
  - aplicación de eventos sobre dominio de pastures
  - validación de eventos
  - integración de persistencia

Riesgos:
  - Timestamp consistency (DynamoDB)
  - User context from JWT

Mitigaciones:
  - Use InstantProvider (testeable)
  - Mock SecurityContext en tests
```

---

### **HISTORA #002: Frontend Botones**

```
Complejidad Técnica:     ███░░ 3/5 (Hooks + API integration)
Testing Requerido:       ███░░ 3/5 (Unit + RTL)
Riesgo Técnico:          ██░░░ 2/5 (Bajo, React estándar)
Interdependencias:       ███░░ 3/5 (Bloqueador de 3 historias)
Esfuerzo UI/UX:          ███░░ 3/5 (Botones + modales)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 1W1

Componentes:
  ✓ OpenPastureModal
  ✓ ClosePastureModal
  ✓ usePastureEvent hook
  ✓ PastureTable updates
  ✓ Error handling

Riesgos:
  - Modal state management
  - Optimistic updates

Mitigaciones:
  - Test modal show/hide
  - Mock API responses
```

---

### **HISTORA #003: DetailPanel Mock**

```
Complejidad Técnica:     ██░░░ 2/5 (Mock data + drawer)
Testing Requerido:       ██░░░ 2/5 (RTL basic)
Riesgo Técnico:          ░░░░░ 1/5 (Muy bajo)
Interdependencias:       ███░░ 3/5 (Bloqueador de 5 historias)
Esfuerzo UI/UX:          ███░░ 3/5 (Tabs + layout)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 2/5 (BAJA)
STORY POINTS: 3
HORAS: 24
SPRINT: 1W1

Componentes:
  ✓ DetailPanel.jsx
  ✓ Mock data (getMockPastureDetail)
  ✓ Tabs (Info, History, Actions)
  ✓ Drawer styling

Riesgos:
  - Mock data accuracy vs real (HU#012)

Mitigaciones:
  - Easy refactor when HU#012 completa
  - Feature flag for toggle
```

---

### **HISTORA #004: Backend PUT Editar**

```
Complejidad Técnica:     ███░░ 3/5 (Optimistic locking + versioning)
Testing Requerido:       ███░░ 3/5 (Unit + 409 conflict tests)
Riesgo Técnico:          ██░░░ 2/5 (Versioning puede ser tricky)
Interdependencias:       ██░░░ 2/5 (Depende HU#001)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 1W2

Componentes:
  ✓ PastureUpdateRequest DTO
  ✓ PUT /pastures/{id} endpoint
  ✓ Optimistic locking validator
  ✓ Version increment logic

Riesgos:
  - Concurrent updates (version mismatch)
  - Partial updates

Mitigaciones:
  - DynamoDB atomic updates
  - Load test concurrent PUTs
  - Clear 409 error messages
```

---

### **HISTORA #005: Backend POST Crear**

```
Complejidad Técnica:     ███░░ 3/5 (TABLE_COUNTERS pattern)
Testing Requerido:       ███░░ 3/5 (Atomic ID generation)
Riesgo Técnico:          ██░░░ 2/5 (DynamoDB counter increment)
Interdependencias:       ██░░░ 2/5 (Depende HU#001)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 1W2

Componentes:
  ✓ CountersRepository
  ✓ PastureIdGenerator
  ✓ POST /pastures endpoint
  ✓ Atomic UpdateItem call

Riesgos:
  - ID collision (race condition)
  - Counter not found

Mitigaciones:
  - Use if_not_exists in UpdateItem
  - Parallel test (1000 concurrent POST)
  - Clear error messages
```

---

### **HISTORA #006: Frontend Modal Bloqueo**

```
Complejidad Técnica:     ███░░ 3/5 (Dual-mode modal)
Testing Requerido:       ███░░ 3/5 (Mode switching, form validation)
Riesgo Técnico:          ██░░░ 2/5 (State complexity)
Interdependencias:       ██░░░ 2/5 (Depende HU#002)
Esfuerzo UI/UX:          ███░░ 3/5 (Modal UX, button states)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 2W1

Componentes:
  ✓ MaintenanceModal
  ✓ ModeSelector (SET/CLEAR buttons)
  ✓ MaintenanceSetForm
  ✓ usePastureEvent integration

Riesgos:
  - Modal state transitions
  - Conflicting form validation

Mitigaciones:
  - Test all mode transitions
  - Keyboard navigation (a11y)
```

---

### **HISTORA #007: Frontend Validaciones**

```
Complejidad Técnica:     ██░░░ 2/5 (Pure functions)
Testing Requerido:       ██░░░ 2/5 (Unit tests only)
Riesgo Técnico:          ░░░░░ 1/5 (Muy bajo)
Interdependencias:       ██░░░ 2/5 (Reutilizable en todas)
Esfuerzo UI/UX:          ██░░░ 2/5 (FormField component)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 2/5 (BAJA)
STORY POINTS: 3
HORAS: 24
SPRINT: 1W2

Componentes:
  ✓ pastureValidators.js (pure functions)
  ✓ useFormValidation hook
  ✓ FormField component
  ✓ Error messages

Riesgos:
  - None (very low risk)

Mitigaciones:
  - Comprehensive unit tests
  - Property-based testing (optional)
```

---

### **HISTORA #008: Backend Tests Engine (STATE MACHINE)**

```
Complejidad Técnica:     ████░ 4/5 (State machine complexity)
Testing Requerido:       █████ 5/5 (Exhaustive parametrized)
Riesgo Técnico:          ███░░ 3/5 (Edge cases en transiciones)
Interdependencias:       ███░░ 3/5 (Depende HU#001, #004, #005)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Tests only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA)
STORY POINTS: 8
HORAS: 64
SPRINT: 2W1

Componentes:
  ✓ PastureStatusEngine tests
  ✓ @ParameterizedTest suite
  ✓ State transition matrix
  ✓ Invalid transition tests
  ✓ Event sequencing tests

Riesgos:
  - Missed edge cases
  - Complex state combos

Mitigaciones:
  - Pair programming
  - Model-based testing approach
  - Matrix validation (N×N states)
```

---

### **HISTORA #009: Backend Tests ETA (MATHEMATICAL)**

```
Complejidad Técnica:     ███░░ 3/5 (Math boundary values)
Testing Requerido:       ███░░ 3/5 (Parametrized + edge cases)
Riesgo Técnico:          ██░░░ 2/5 (Rounding, off-by-one)
Interdependencias:       ██░░░ 2/5 (Depende HU#001)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Tests only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 2W1

Componentes:
  ✓ EtaCalculator tests
  ✓ Boundary value tests (0, -1, max)
  ✓ Growth rate variations
  ✓ Height scenarios

Riesgos:
  - Floating point precision
  - Timezone issues

Mitigaciones:
  - Use Instant (no timezone)
  - Round to integers consistently
```

---

### **HISTORA #010: Frontend Calendario**

```
Complejidad Técnica:     ███░░ 3/5 (Calendar grid logic)
Testing Requerido:       ███░░ 3/5 (Rendering, interactions)
Riesgo Técnico:          ██░░░ 2/5 (Date handling)
Interdependencias:       ██░░░ 2/5 (Depende HU#003)
Esfuerzo UI/UX:          ███░░ 3/5 (Calendar UI, colors)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 2W1

Componentes:
  ✓ CalendarView.jsx
  ✓ CalendarGrid component
  ✓ Color mapping (status → color)
  ✓ Navigation (prev/next month)

Riesgos:
  - Date arithmetic edge cases
  - Performance with many potreros

Mitigaciones:
  - Use date-fns library
  - Virtualization if 100+ potreros
```

---

### **HISTORA #011: Frontend AlertCenter (POLLING)**

```
Complejidad Técnica:     ███░░ 3/5 (Polling + alert state)
Testing Requerido:       ███░░ 3/5 (useEffect, timers)
Riesgo Técnico:          ██░░░ 2/5 (Memory leaks possible)
Interdependencias:       ██░░░ 2/5 (Depende HU#010)
Esfuerzo UI/UX:          ███░░ 3/5 (Alert styling, animations)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 2W2

Componentes:
  ✓ AlertCenter.jsx
  ✓ AlertItem component
  ✓ useAlerts hook (polling)
  ✓ Alert type styles

Riesgos:
  - Memory leaks from intervals
  - Multiple alerts stacking

Mitigaciones:
  - Proper cleanup in useEffect
  - Max 5 alerts shown (queue rest)
  - Test interval cleanup
```

---

### **HISTORA #012: Backend Historial Eventos (PAGINATED QUERY)**

```
Complejidad Técnica:     ████░ 4/5 (Complex pagination + filtering)
Testing Requerido:       ████░ 4/5 (Query edge cases)
Riesgo Técnico:          ███░░ 3/5 (N+1 queries, pagination)
Interdependencias:       ███░░ 3/5 (Bloqueador HU#013, #016, #024)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA)
STORY POINTS: 8
HORAS: 64
SPRINT: 3W1

Componentes previstos en la estimación original:
  - endpoint GET de historial de eventos
  - DTO/filtro de eventos
  - paginación
  - optimización de consulta

Riesgos:
  - Slow queries on large datasets
  - Cursor-based vs offset pagination

Mitigaciones:
  - Index on (pastureId, timestamp)
  - Load test with 10K events
  - Cursor-based (preferred over offset)
```

---

### **HISTORA #013: Backend Auditoría (AOP ASPECT)**

```
Complejidad Técnica:     ███░░ 3/5 (AOP + reflection)
Testing Requerido:       ███░░ 3/5 (Aspect interception)
Riesgo Técnico:          ██░░░ 2/5 (AOP can be fragile)
Interdependencias:       ██░░░ 2/5 (Depende HU#012)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 3W1

Componentes:
  ✓ @Audit annotation
  ✓ AuditingAspect (@Around)
  ✓ AuditLog entity
  ✓ AuditService

Riesgos:
  - Performance overhead (async needed)
  - Reflection complexity

Mitigaciones:
  - Async audit logging
  - Test with and without aspect
```

---

### **HISTORA #014: Frontend EditorPanel (SIMPLE FORM)**

```
Complejidad Técnica:     ██░░░ 2/5 (Reutiliza FormField HU#007)
Testing Requerido:       ██░░░ 2/5 (RTL basic)
Riesgo Técnico:          ░░░░░ 1/5 (Muy bajo)
Interdependencias:       ██░░░ 2/5 (Depende HU#003, #007)
Esfuerzo UI/UX:          ██░░░ 2/5 (Panel layout)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 2/5 (BAJA)
STORY POINTS: 3
HORAS: 24
SPRINT: 3W1

Componentes:
  ✓ EditorPanel.jsx
  ✓ useUpdatePasture hook
  ✓ Form reuse (validateName, etc.)

Riesgos:
  - None (very low risk)

Mitigaciones:
  - Comprehensive RTL tests
```

---

### **HISTORA #015: Frontend react-big-calendar**

```
Complejidad Técnica:     ███░░ 3/5 (3rd-party lib integration)
Testing Requerido:       ███░░ 3/5 (Library usage tests)
Riesgo Técnico:          ██░░░ 2/5 (Library stability)
Interdependencias:       ██░░░ 2/5 (Depende HU#010, #011)
Esfuerzo UI/UX:          ███░░ 3/5 (Multi-view setup)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 3W1

Componentes:
  ✓ BigCalendarView.jsx
  ✓ pasturesToEvents mapper
  ✓ Custom styling
  ✓ Event click handler

Riesgos:
  - Library API changes
  - Performance with many events

Mitigaciones:
  - Pin version in package.json
  - Test with 100+ events
```

---

### **HISTORA #016: Frontend Estadísticas (DASHBOARD + CHARTS)**

```
Complejidad Técnica:     ████░ 4/5 (Recharts + data aggregation)
Testing Requerido:       ████░ 4/5 (Chart rendering, exports)
Riesgo Técnico:          ███░░ 3/5 (Performance, memory)
Interdependencias:       ███░░ 3/5 (Depende HU#012, #013, #015)
Esfuerzo UI/UX:          ████░ 4/5 (Dashboard layout, charts)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA)
STORY POINTS: 8
HORAS: 64
SPRINT: 3W2

Componentes:
  ✓ Dashboard.jsx
  ✓ BarChart, LineChart components
  ✓ KPICard component
  ✓ Export CSV/PDF
  ✓ Date filtering

Riesgos:
  - Slow with large datasets
  - Export format issues

Mitigaciones:
  - Data aggregation in backend
  - Client-side pagination
  - Test export files
```

---

### **HISTORA #017: Backend OpenAPI/Swagger**

```
Complejidad Técnica:     ██░░░ 2/5 (Spring Boot + annotations)
Testing Requerido:       ██░░░ 2/5 (UI access verification)
Riesgo Técnico:          ░░░░░ 1/5 (Muy bajo)
Interdependencias:       ░░░░░ 0/5 (Independiente)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Spring generates UI)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 2/5 (BAJA)
STORY POINTS: 3
HORAS: 24
SPRINT: 3W2

Componentes:
  ✓ springdoc-openapi dependency
  ✓ OpenAPI @Configuration
  ✓ @Operation annotations
  ✓ @ApiResponse annotations

Riesgos:
  - None (very straightforward)

Mitigaciones:
  - Test /swagger-ui.html access
```

---

### **HISTORA #018: Backend SNS/SQS (EVENT-DRIVEN) 🚨**

```
Complejidad Técnica:     ████░ 4/5 (AWS messaging complexity)
Testing Requerido:       ████░ 4/5 (LocalStack, async tests)
Riesgo Técnico:          ████░ 4/5 (AWS SDK, DLQ handling)
Interdependencias:       ██░░░ 2/5 (Depende HU#001)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA) 🚨 HIGH RISK
STORY POINTS: 8
HORAS: 64
SPRINT: 4W1

Componentes previstos en la estimación original:
  - publicador de eventos vía SNS
  - listener/consumidor vía SQS
  - control de idempotencia
  - configuración de DLQ

Riesgos:
  - AWS SDK learning curve ⚠️
  - Async message ordering
  - Dead letter queue handling

Mitigaciones:
  - Spike (2 days pre-sprint)
  - Pair programming
  - LocalStack for testing
  - Feature flag for toggle
```

---

### **HISTORA #019: Backend Multi-tenant 🚨🚨 CRITICAL**

```
Complejidad Técnica:     █████ 5/5 (REFACTOR ARQUITECTURA)
Testing Requerido:       █████ 5/5 (Regression testing)
Riesgo Técnico:          █████ 5/5 (Puede quebrar todo)
Interdependencias:       ████░ 4/5 (Requiere ALL historias anteriores)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ████░ 4/5 (Modificar TODOS repos + controllers)

TOTAL COMPLEJIDAD: 5/5 (MUY ALTA) 🚨🚨 CRITICAL
STORY POINTS: 13 (LARGEST)
HORAS: 104
SPRINT: 4W2

Componentes:
  ✓ TenantContext (ThreadLocal)
  ✓ TenantInterceptor
  ✓ Modify ALL repositories (add filter)
  ✓ Modify ALL controllers (add TenantContext)
  ✓ Update JWT generation

Riesgos:
  - Massive refactor scope ⚠️⚠️
  - Cross-tenant data exposure 🚨
  - Regression bugs en historias anteriores
  - Performance impact

Mitigaciones:
  - Pair programming (BOTH devs)
  - Architect full-time pair
  - Exhaustive testing (95%+ coverage)
  - Feature flag for gradual rollout
  - Staging validation before prod
```

---

### **HISTORA #020: Backend Soft Delete**

```
Complejidad Técnica:     ███░░ 3/5 (Soft delete + filtering)
Testing Requerido:       ███░░ 3/5 (Query inclusion/exclusion)
Riesgo Técnico:          ██░░░ 2/5 (Forgetting to filter)
Interdependencias:       ██░░░ 2/5 (Depende HU#001)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 3W2

Componentes previstos en la estimación original:
  - campos lógicos de baja como `deletedAt` y `deletedBy`
  - servicio de soft delete
  - filtrado de registros retirados
  - endpoint DELETE para potreros

Riesgos:
  - Forgetting soft delete filter
  - Data accumulation over time

Mitigaciones:
  - Repository tests assert filter
  - Warning on GET all (includes deleted)
```

---

### **HISTORA #021: Frontend Export CSV/Excel**

```
Complejidad Técnica:     ███░░ 3/5 (File generation)
Testing Requerido:       ███░░ 3/5 (File format validation)
Riesgo Técnico:          ██░░░ 2/5 (Special characters)
Interdependencias:       ██░░░ 2/5 (Depende HU#010-016)
Esfuerzo UI/UX:          ██░░░ 2/5 (Export buttons)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 4W1

Componentes:
  ✓ csvExporter.js
  ✓ excelExporter.js (XLSX)
  ✓ ExportButton component
  ✓ Filename generation

Riesgos:
  - Unicode characters (ñ, á)
  - Large file memory impact
  - Excel max rows (1M)

Mitigaciones:
  - Test with Spanish chars
  - Paginate if >50K rows
  - Load test memory usage
```

---

### **HISTORA #022: Frontend Dark Mode**

```
Complejidad Técnica:     ███░░ 3/5 (CSS variables + context)
Testing Requerido:       ███░░ 3/5 (Theme switching, persistence)
Riesgo Técnico:          ██░░░ 2/5 (Contrast issues possible)
Interdependencias:       ░░░░░ 0/5 (Independiente)
Esfuerzo UI/UX:          ███░░ 3/5 (Color palette design)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 3/5 (MEDIA)
STORY POINTS: 5
HORAS: 40
SPRINT: 3W1-2

Componentes:
  ✓ ThemeProvider context
  ✓ ThemeToggle button
  ✓ CSS variables
  ✓ localStorage integration

Riesgos:
  - WCAG contrast compliance
  - Incomplete component coverage

Mitigaciones:
  - WCAG AAA validation tool
  - Audit all components
```

---

### **HISTORA #023: Backend Caching Redis ⭐ OPTIONAL**

```
Complejidad Técnica:     ████░ 4/5 (Redis setup + cache strategy)
Testing Requerido:       ████░ 4/5 (Cache hits, invalidation)
Riesgo Técnico:          ███░░ 3/5 (Stale cache, cache miss)
Interdependencias:       ███░░ 3/5 (Opcional, melhora performance)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Backend only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA) ⭐ POST-LAUNCH OK
STORY POINTS: 8
HORAS: 64
SPRINT: 4W1 (POST-LAUNCH)

Componentes previstos en la estimación original:
  - configuración Redis o caché local
  - anotaciones o estrategia de caching
  - invalidación en escrituras
  - TTL configurable

Riesgos:
  - Stale data in cache
  - Cache invalidation bugs

Mitigaciones:
  - Short TTL initially (5 min)
  - Event-driven invalidation
  - Performance testing

Status: ⭐ OPTIONAL - Can launch without this
```

---

### **HISTORA #024: Tests E2E Cypress**

```
Complejidad Técnica:     ████░ 4/5 (E2E test architecture)
Testing Requerido:       █████ 5/5 (Full E2E coverage)
Riesgo Técnico:          ███░░ 3/5 (Flaky tests possible)
Interdependencias:       ████░ 4/5 (Requiere todo terminar)
Esfuerzo UI/UX:          ░░░░░ 0/5 (Tests only)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA)
STORY POINTS: 8
HORAS: 64
SPRINT: 4W2

Componentes:
  ✓ Cypress setup
  ✓ Page Object Model
  ✓ E2E test suite (30+ tests)
  ✓ CI/CD integration

Riesgos:
  - Flaky tests (timing issues)
  - Multi-browser incompatibilities
  - Test data setup complexity

Mitigaciones:
  - Explicit waits (not implicit)
  - Test data fixtures
  - Multi-browser testing (Chrome, FF)
```

---

### **HISTORA #025: Frontend Responsive Mobile**

```
Complejidad Técnica:     ████░ 4/5 (Mobile-first redesign)
Testing Requerido:       ████░ 4/5 (Device testing)
Riesgo Técnico:          ███░░ 3/5 (Layout breakage)
Interdependencias:       ███░░ 3/5 (Depende HU#010-016)
Esfuerzo UI/UX:          █████ 5/5 (Complete redesign mobile)
Refactoring:             ░░░░░ 0/5 (Nueva feature)

TOTAL COMPLEJIDAD: 4/5 (ALTA)
STORY POINTS: 8
HORAS: 64
SPRINT: 4W2

Componentes:
  ✓ Viewport meta tag
  ✓ Tailwind breakpoints
  ✓ Mobile navigation (hamburger)
  ✓ Responsive tables (→ cards)
  ✓ Touch-optimized buttons (44px+)

Riesgos:
  - Layout breakage on some devices
  - Touch interaction UX issues
  - Performance on mobile

Mitigaciones:
  - Test on real devices (not just browser)
  - Lighthouse score >= 90
  - Touch gesture testing
```

---

## 📊 MATRIZ CONSOLIDADA (TODAS 25)

| # | Complejidad | Testing | Riesgo | UI | Deps | Refactor | SP | h |
|---|-------------|---------|--------|----|----|----------|----|----|
| 1 | 3 | 3 | 2 | 0 | 4 | 0 | 5 | 40 |
| 2 | 3 | 3 | 2 | 3 | 3 | 0 | 5 | 40 |
| 3 | 2 | 2 | 1 | 3 | 3 | 0 | 3 | 24 |
| 4 | 3 | 3 | 2 | 0 | 2 | 0 | 5 | 40 |
| 5 | 3 | 3 | 2 | 0 | 2 | 0 | 5 | 40 |
| 6 | 3 | 3 | 2 | 3 | 2 | 0 | 5 | 40 |
| 7 | 2 | 2 | 1 | 2 | 2 | 0 | 3 | 24 |
| 8 | 4 | 5 | 3 | 0 | 3 | 0 | 8 | 64 |
| 9 | 3 | 3 | 2 | 0 | 2 | 0 | 5 | 40 |
| 10 | 3 | 3 | 2 | 3 | 2 | 0 | 5 | 40 |
| 11 | 3 | 3 | 2 | 3 | 2 | 0 | 5 | 40 |
| 12 | 4 | 4 | 3 | 0 | 3 | 0 | 8 | 64 |
| 13 | 3 | 3 | 2 | 0 | 2 | 0 | 5 | 40 |
| 14 | 2 | 2 | 1 | 2 | 2 | 0 | 3 | 24 |
| 15 | 3 | 3 | 2 | 3 | 2 | 0 | 5 | 40 |
| 16 | 4 | 4 | 3 | 4 | 3 | 0 | 8 | 64 |
| 17 | 2 | 2 | 1 | 0 | 0 | 0 | 3 | 24 |
| 18 | 4 | 4 | 4 | 0 | 2 | 0 | 8 | 64 |
| 19 | 5 | 5 | 5 | 0 | 4 | 4 | 13 | 104 |
| 20 | 3 | 3 | 2 | 0 | 2 | 0 | 5 | 40 |
| 21 | 3 | 3 | 2 | 2 | 2 | 0 | 5 | 40 |
| 22 | 3 | 3 | 2 | 3 | 0 | 0 | 5 | 40 |
| 23 | 4 | 4 | 3 | 0 | 3 | 0 | 8 | 64 |
| 24 | 4 | 5 | 3 | 0 | 4 | 0 | 8 | 64 |
| 25 | 4 | 4 | 3 | 5 | 3 | 0 | 8 | 64 |
| **Total** | | | | | | | **130** | **1,040** |

---

## 🎯 CLASIFICACIÓN POR COMPLEJIDAD

### **BAJA (3 HU)**
- HU#003, #007, #017

### **MEDIA (10 HU)**
- HU#001, #002, #004, #005, #006, #009, #010, #011, #013, #020, #021, #022

### **ALTA (11 HU)**
- HU#008, #012, #015, #016, #018, #023, #024, #025

### **MUY ALTA (1 HU)**
- HU#019 (CRITICAL REFACTOR)

---

**Documento generado**: 2026-01-09
**Clasificación**: Para Planning + Risk Assessment
