# 🌱 Módulo Pastures (Potreros) - Índice de Documentación

**Última actualización**: 2026-01-08

Bienvenido a la documentación del módulo **Pastures** (Potreros). Esta carpeta contiene análisis detallado, guías de implementación, y roadmap de tareas.

## 📚 Documentos

### 1. [pastures-overview.md](pastures-overview.md)
**Descripción General y Técnica del Módulo**

Documento principal que cubre:
- 📋 Descripción y características.
- 🗄️ Modelado de datos DynamoDB (TABLE_PASTURE, TABLE_PLAN).
- 🔄 Máquina de estados y motor de rotación.
- 🧮 Fórmula de ETA y lógica automática.
- 📌 **NUEVO**: Arquitectura de Eventos (sealed interface PastureEvent vs generic Event.java).
- 🎨 Arquitectura Frontend (componentes, hooks, constantes).
- 🔌 Arquitectura Backend (controller, service, processor, repository).
- 📊 Patrones de acceso a DynamoDB.
- 🔄 Flujos de caso de uso (consultar, filtrar, abrir, bloquear).
- 🧪 Tests (unitarios, integración).
- 📝 Configuración y endpoints.
- 🚀 Comandos útiles (AWS CLI).

**Público**: Arquitectos, Desarrolladores.

**Longitud**: ~600 líneas (actualizado con eventos).

---

### 3. [components-frontend.md](components-frontend.md)
**Documentación Detallada de Componentes Frontend**

Documentación profunda con código fuente de cada componente:
- 🎨 Estructura del módulo Paddock.
- 📍 Ubicación y responsabilidad de cada componente.
- 📋 Props, estado local, y flujos.
- 📊 Código fuente completo de cada componente.
- 🔄 Cómo conectar eventos y acciones.
- TO-DOs: Qué falta implementar en cada componente.
- 🧩 Componentes auxiliares reutilizables.
- 🔧 Hooks personalizados (useFilteredPastures).

**Público**: Desarrolladores Frontend.

**Longitud**: ~600 líneas.

---

### 5. [implementation-guide.md](implementation-guide.md)
**Guía Paso a Paso: Cómo Implementar Tareas P0**

Guía detallada con código fuente para implementar las 3 tareas críticas:
- 🔴 **P0#1**: Endpoint POST de eventos (backend)
  - Crear DTOs y controller
  - Implementar conversión a PastureEvent
  - Testing unitario
- 🔴 **P0#2**: Conectar botones en frontend
  - Handlers para OPEN/CLOSE
  - Mostrar loading/error
  - Refetch de datos
- 🔴 **P0#3**: DetailPanel funcional
  - Drawer lateral
  - Acciones rápidas
  - Formulario de bloqueo
- 🧪 Testing & validación manual
- ⏱️ Estimación por tarea

**Público**: Desarrolladores implementando features.

**Longitud**: ~800 líneas.

---

## 📚 Documentos Anteriores
**Roadmap de Tareas Faltantes**

Documento detallado de tareas pendientes:
- 🎯 Resumen ejecutivo (qué está hecho, qué falta).
- 📊 25 tareas clasificadas por prioridad (P0, P1, P2, P3).
- 🔴 Crítico (P0): 3 tareas (eventos, conexiones, detalle panel).
- 🟠 Alto (P1): 6 tareas (edición, creación, tests, validaciones).
- 🟡 Medio (P2): 8 tareas (calendario, alertas, historial, auditoría).
- 🟢 Bajo (P3): 8 tareas (reportes, CSV, E2E, caching, etc.).
- 📈 Roadmap recomendado en 4 fases (8+ semanas).
- 🔗 Dependencias entre tareas.
- 📊 Estimación global (76-112 horas).

**Público**: Product Managers, Tech Leads, Developers.

**Longitud**: ~400 líneas.

---

### 3. [events-architecture.md](events-architecture.md)
**Arquitectura de Eventos: Sealed Interface Pattern**

Documento complementario que explica en detalle el sistema de eventos usado en Pastures:
- 🎯 ¿Qué es una sealed interface?
- 📚 Arquitectura de PastureEvent (sealed interface + records).
- 📌 4 tipos de eventos: OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent.
- 🔀 Pattern matching y switch expression (Java 17+).
- 🔌 Implementación en PastureStatusEngine.
- 🔄 Comparación detallada: PastureEvent vs Event.java (diferencias y cuándo usar cada uno).
- 💡 Ejemplos prácticos (abrir, cerrar, bloquear, liberar).
- 🎓 Aprendizajes clave sobre type-safety e inmutabilidad.

**Público**: Desarrolladores backend (Java), arquitectos.

**Longitud**: ~600 líneas.

**Nota Importante**: Responde a la pregunta "¿Se tuvo en cuenta Event.java y EventBuilder.java en el análisis?" con una comparación clara entre ambos sistemas.

---

## 🗺️ Estructura del Proyecto

```
cattle/
├── cattle-front/
│   └── src/components/Paddock/
│       ├── page/                   # Dashboard principal
│       │   ├── PaddockPage.jsx
│       │   ├── listKpiCards.jsx
│       │   └── *.css
│       ├── pastureTable/           # Tabla de potreros
│       │   ├── pastureTable.jsx
│       │   └── pastureTable.css
│       ├── detailPanel/            # Panel detallado (TO-DO)
│       ├── drawer/                 # Componente drawer reutilizable
│       ├── hooks/                  # Custom hooks
│       │   └── padockHooks.js
│       ├── paddockConstants/       # Constantes (especies, estados)
│       ├── rotationSemaphore/      # Semáforo visual
│       ├── calendarMini/           # Mini calendario (TO-DO)
│       ├── alertCenter/            # Centro de alertas (TO-DO)
│       ├── StatusChip/             # Chip de estado
│       ├── section/                # Componente Section
│       ├── kpiCard/                # Tarjeta KPI
│       └── mocks/                  # Datos mock
│
├── cattle-lambda-function/
│   └── src/main/java/com/cattle/
│       ├── controller/
│       │   ├── PasturesController.java
│       │   └── PastureEventController.java (TO-DO)
│       ├── processor/
│       │   ├── PastureProcessor.java
│       │   └── RotationPlanProcessor.java
│       ├── services/
│       │   ├── PastureService.java
│       │   └── PlanService.java
│       ├── repository/
│       │   ├── PastureRepository.java
│       │   └── PlanRepository.java
│       ├── entities/
│       │   ├── Pasture.java
│       │   └── Plan.java
│       ├── dtos/
│       │   ├── PastureDTO.java
│       │   ├── RotationSemaphoreItemDTO.java
│       │   └── PastureEventDTO.java (TO-DO)
│       ├── enums/
│       │   ├── PastureStatus.java
│       │   └── PastureSubstatus.java
│       ├── events/
│       │   └── PastureEvent.java
│       ├── utils/
│       │   ├── PastureStatusEngine.java
│       │   └── EtaCalculator.java
│       └── mapper/
│           └── PasturesMapper.java
│
└── docs/pastures/
    ├── index.md                  # Este archivo
    ├── pastures-overview.md      # Documentación técnica
    ├── tasks-pending.md          # Tareas faltantes
    └── (TO-DO) api-spec.yaml     # OpenAPI spec
```

---

## 🚀 Quick Start

### Para Desarrolladores Backend

1. Lee [pastures-overview.md](pastures-overview.md) secciones:
   - 🗄️ Modelo de Datos
   - 🔌 Backend - Arquitectura
   - 🧮 Motor de Rotación

2. Revisa tareas **P0** en [tasks-pending.md](tasks-pending.md):
   - Endpoint POST eventos
   - Tests unitarios

3. Comienza por tarea #1 (Endpoint POST para eventos).

### Para Desarrolladores Frontend

1. Lee [pastures-overview.md](pastures-overview.md) secciones:
   - 🎨 Frontend - Arquitectura
   - Componentes Principales
   - Custom Hooks

2. Revisa tareas **P0** en [tasks-pending.md](tasks-pending.md):
   - Conectar botones Abrir/Cerrar
   - DetailPanel funcional

3. Comienza por tarea #2 (Conectar botones).

### Para Testers/QA

1. Lee [pastures-overview.md](pastures-overview.md) sección:
   - 🧪 Tests

2. Revisa tareas **P1** en [tasks-pending.md](tasks-pending.md):
   - Tests unitarios
   - Tests E2E

---

## 📊 Estado de Implementación

| Área | % | Notas |
|------|---|-------|
| Modelo de Datos | 100% | ✅ Completo |
| Motor de Rotación | 100% | ✅ PastureStatusEngine, EtaCalculator |
| Backend (lectura) | 100% | ✅ GET /farms/{farmId}/pastures |
| Backend (escritura) | 0% | ❌ Falta POST eventos, PUT, POST crear |
| Frontend (lectura) | 80% | 🟡 Dashboard, tabla, filtros (sin calendario) |
| Frontend (acciones) | 0% | ❌ Botones sin conectar |
| Tests | 30% | 🟡 StatusEngine test parcial, falta EtaCalc |
| Documentación | 100% | ✅ Completa en esta carpeta |

---

## 🎯 Objetivos por Fase

### Fase 1: MVP (Funcionalidad Básica)
- ✅ Listar potreros con filtros y KPIs.
- ✅ Ver detalles de potrero.
- ✅ Abrir/Cerrar potrero (eventos).
- ✅ Editar atributos básicos.

**Timeline**: 2-3 semanas.

### Fase 2: Robustez
- ✅ Tests unitarios e integración.
- ✅ Manejo de errores.
- ✅ Auditoría de cambios.

**Timeline**: 1-2 semanas.

### Fase 3: UX Mejorada
- ✅ Calendario funcional.
- ✅ Alertas en tiempo real.
- ✅ Crear nuevos potreros.

**Timeline**: 1-2 semanas.

### Fase 4: Producción
- ✅ Caching, performance.
- ✅ Multi-tenant.
- ✅ Reportes.

**Timeline**: 2+ semanas.

---

## 📞 Preguntas Frecuentes (FAQ)

**P: ¿Dónde está definida la máquina de estados?**
R: En [PastureStatusEngine.java](../../cattle-lambda-function/src/main/java/com/cattle/utils/PastureStatusEngine.java) y documentada en [pastures-overview.md](pastures-overview.md#-máquina-de-estados).

---

**P: ¿Cómo se calcula el ETA?**
R: Fórmula en [pastures-overview.md](pastures-overview.md#-motor-de-rotación-pasturestatusengine), implementada en [EtaCalculator.java](../../cattle-lambda-function/src/main/java/com/cattle/utils/EtaCalculator.java).

---

**P: ¿Qué falta por hacer?**
R: Revisa [docs/stories/P0/INDEX.md](../stories/P0/INDEX.md) para lista de 25 HUs priorizadas.

---

**P: ¿Cuál es la prioridad de desarrollo?**
R: Fase 1 → 2 → 3 → 4. Cada fase tiene 3-4 HUs críticas. Ver roadmap en [stories/P0/INDEX.md](../stories/P0/INDEX.md#-roadmap-p0-fase-1---mvp-básico).

---

**P: ¿Cómo conecto nuevos endpoints?**
R: El endpoint GET ya existe. Para POST eventos ver [HU#1: Backend POST Eventos](../stories/P0/PASTURES-HU-001-post-eventos.md) ✅

---

## 📚 **Historias de Usuario**

**Ubicación**: `docs/stories/` (organizado por prioridad)

**HU Completadas** (1/25):
- ✅ [HU#1: Backend POST Eventos OPEN/CLOSE/MAINTENANCE](../stories/P0/PASTURES-HU-001-post-eventos.md)

**Índices por Prioridad**:
- 🔴 [P0 - CRÍTICO (3 HUs)](../stories/P0/INDEX.md) - MVP Básico
- 🟠 [P1 - ALTO (6 HUs)](../stories/P1/INDEX.md) - Robustez + Testing
- 🟡 [P2 - MEDIO (7 HUs)](../stories/P2/INDEX.md) - UX + Features
- 🟢 [P3 - BAJO (9 HUs)](../stories/P3/INDEX.md) - Escala + Performance

**Estructura completa**: [docs/stories/README.md](../stories/README.md)

---

## 🔗 Referencias Externas

- [Análisis de Modelado DynamoDB](../analysis-table-design.md)
- [Arquitectura General del Proyecto](../architecture/index.md)
- [Flujo de Dashboard Potreros](../architecture/flujo-dashboard-potreros.md)
- [Diagrama de Estados](../../documentation/state-diagram.puml)
- [Diagrama de Actividad](../../documentation/pastures-activity.puml)

---

## 📝 Convenciones

### Nomenclatura de Claves DynamoDB

- `pk`: Partition Key (ej. `PASTURE#P001`)
- `gsi1pk`, `gsi1sk`: Global Secondary Index 1
- `gsi2pk`, `gsi2sk`: Global Secondary Index 2

### Estados de Potrero

- `EN_DESCANSO`: Recuperándose, no disponible.
- `DISPONIBLE`: Listo para abrir (ETA <= 0).
- `EN_USO`: Siendo usado por rebaño.
- `MANTENIMIENTO`: Bloqueado (fertilización, reparación, etc.).

### Eventos

- `OPEN`: Transición a EN_USO.
- `CLOSE`: Transición a EN_DESCANSO.
- `MAINTENANCE_SET`: Bloquear con fecha.
- `MAINTENANCE_CLEAR`: Liberar bloqueo.

---

## 📈 Métricas de Éxito

- ✅ Dashboard carga en < 2 segundos.
- ✅ Latencia de evento (OPEN/CLOSE) < 500ms.
- ✅ Cobertura de tests > 80%.
- ✅ Disponibilidad del API > 99.9%.
- ✅ Usuarios pueden crear/editar potreros sin errores.

---

**Generado**: 2026-01-08 | **Versión**: 1.0
