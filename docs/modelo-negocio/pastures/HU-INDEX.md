# 📋 Índice de Historias de Usuario - Módulo Pastures

**Fecha**: 2026-01-09 | **Estado**: 🔴 CRÍTICO - 1 HU completa, 24 pendientes

---

## 📊 Estado General

```
Total HUs: 25
Completas: 1
Pendientes: 24
Estimación Total: 76-112 horas (10-16 días de desarrollo)

Distribución:
├─ P0 (Crítico): 3 HUs    [████ ]   12-17h
├─ P1 (Alto):    6 HUs    [████████████] 24-35h
├─ P2 (Medio):   7 HUs    [████████████] 25-35h
└─ P3 (Bajo):    9 HUs    [███████] 15-25h
```

---

## 🔴 CRÍTICO (P0) - 3 HUs

| # | Título | Estado | Horas | Enlace |
|---|--------|--------|-------|--------|
| 1 | **Backend: POST Eventos OPEN/CLOSE/MAINTENANCE** | ✅ ESCRITA | 4-6h | [HU-PASTURES-001](./HU-PASTURES-001-backend-post-eventos.md) |
| 2 | **Frontend: Conectar Botones Abrir/Cerrar** | 🔲 Pendiente | 2-3h | (por escribir) |
| 3 | **Frontend: DetailPanel Funcional** | 🔲 Pendiente | 6-8h | (por escribir) |

---

## 🟠 ALTO (P1) - 6 HUs

| # | Título | Estado | Horas | Enlace |
|---|--------|--------|-------|--------|
| 4 | **Backend: PUT para Editar Potrero** | 🔲 Pendiente | 3-4h | (por escribir) |
| 5 | **Backend: POST para Crear Potrero** | 🔲 Pendiente | 4-5h | (por escribir) |
| 6 | **Frontend: Modal de Bloqueo/Mantenimiento** | 🔲 Pendiente | 2-3h | (por escribir) |
| 7 | **Frontend: Validaciones en Formularios** | 🔲 Pendiente | 2-3h | (por escribir) |
| 8 | **Backend: Tests Unitarios PastureStatusEngine** | 🔲 Pendiente | 4-5h | (por escribir) |
| 9 | **Backend: Tests para EtaCalculator** | 🔲 Pendiente | 2-3h | (por escribir) |

---

## 🟡 MEDIO (P2) - 7 HUs

| # | Título | Estado | Horas | Enlace |
|---|--------|--------|-------|--------|
| 10 | **Frontend: Calendario Funcional** | 🔲 Pendiente | 5-6h | (por escribir) |
| 11 | **Frontend: AlertCenter con Datos Reales** | 🔲 Pendiente | 4-5h | (por escribir) |
| 12 | **Backend: GET Historial de Eventos** | 🔲 Pendiente | 3-4h | (por escribir) |
| 13 | **Backend: Auditoría de Cambios** | 🔲 Pendiente | 4-5h | (por escribir) |
| 14 | **Frontend: EditorPanel** | 🔲 Pendiente | 2-3h | (por escribir) |
| 15 | **Frontend: Integrar react-calendar** | 🔲 Pendiente | 3-4h | (por escribir) |
| 18 | **Frontend: Estadísticas y Reportes** | 🔲 Pendiente | 5-6h | (por escribir) |

---

## 🟢 BAJO (P3) - 9 HUs

| # | Título | Estado | Horas | Enlace |
|---|--------|--------|-------|--------|
| 16 | **Docs: OpenAPI/Swagger** | 🔲 Pendiente | 2-3h | (por escribir) |
| 17 | **Backend: SNS/SQS Integration** | 🔲 Pendiente | 4-5h | (por escribir) |
| 19 | **Backend: DELETE Potrero** | 🔲 Pendiente | 1-2h | (por escribir) |
| 20 | **Frontend: Exportar CSV/Excel** | 🔲 Pendiente | 1-2h | (por escribir) |
| 21 | **Backend: Soporte Multi-tenant** | 🔲 Pendiente | 2-3h | (por escribir) |
| 22 | **Frontend: Modo Oscuro** | 🔲 Pendiente | 1-2h | (por escribir) |
| 23 | **Tests E2E (Cypress/Playwright)** | 🔲 Pendiente | 5-6h | (por escribir) |
| 24 | **Backend: Caching (Redis/Local)** | 🔲 Pendiente | 3-4h | (por escribir) |
| 25 | **Frontend: Responsive Design Móvil** | 🔲 Pendiente | 2-3h | (por escribir) |

---

## 📈 Roadmap por Fase

### Fase 1: MVP Básico (Semanas 1-2)
**Objetivo**: Completar flujos CRUD y eventos

```
SEMANA 1
└─ Day 1-2
   ├─ HU#1 ✅ Backend: POST Eventos (COMPLETADA)
   └─ HU#2 🔲 Frontend: Conectar botones
       
└─ Day 3-4
   ├─ HU#4 🔲 Backend: PUT Editar potrero
   └─ HU#5 🔲 Backend: POST Crear potrero
   
└─ Day 5
   ├─ HU#6 🔲 Frontend: Modal Bloqueo
   └─ HU#7 🔲 Frontend: Validaciones

SEMANA 2
└─ Day 6-10
   ├─ HU#3 🔲 Frontend: DetailPanel funcional
   └─ Tests manuales en staging

└─ Day 11-14
   └─ Deploy a staging + PO acceptance
```

**Hito**: Dashboard completamente funcional CRUD + eventos

---

### Fase 2: Robustez y Testing (Semanas 3-4)
**Objetivo**: Tests y manejo de errores robusto

```
SEMANA 3
└─ HU#8 🔲 Backend: Tests PastureStatusEngine
└─ HU#9 🔲 Backend: Tests EtaCalculator
└─ HU#12 🔲 Backend: GET Historial eventos
└─ HU#13 🔲 Backend: Auditoría cambios

SEMANA 4
└─ Manejo de errores mejorado (todas las capas)
└─ Integración con CI/CD
└─ Documentación técnica
```

**Hito**: Código testeado, trazable, con coverage >= 80%

---

### Fase 3: UX y Features Avanzadas (Semanas 5-6)
**Objetivo**: Mejora de UX y features nice-to-have

```
SEMANA 5
└─ HU#10 🔲 Frontend: Calendario funcional
└─ HU#15 🔲 Frontend: react-calendar
└─ HU#11 🔲 Frontend: AlertCenter

SEMANA 6
└─ HU#14 🔲 Frontend: EditorPanel
└─ HU#16 🔲 Docs: OpenAPI/Swagger
└─ Testing UX en staging
```

**Hito**: Dashboard con UX mejorada, intuitivo

---

### Fase 4: Escala y Optimización (Semanas 7+)
**Objetivo**: Performance, notificaciones, multi-tenant

```
SEMANA 7-8
└─ HU#17 🔲 Backend: SNS/SQS
└─ HU#24 🔲 Backend: Caching
└─ HU#21 🔲 Backend: Multi-tenant

SEMANA 9-10
└─ HU#18 🔲 Frontend: Reportes
└─ HU#23 🔲 Tests E2E

SEMANA 11+
└─ HU#19 🔲 Backend: DELETE
└─ HU#20 🔲 Frontend: CSV export
└─ HU#22 🔲 Frontend: Dark mode
└─ HU#25 🔲 Frontend: Mobile responsive
```

**Hito**: Sistema production-ready con escalabilidad

---

## 🔗 Dependencias Entre HUs

```
                        ┌─ HU#1 ✅ ─┐
                        │ POST EVENTOS│
                        └──────┬──────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
        ┌───────▼────┐  ┌──────▼─────┐  ┌────▼──────┐
        │ HU#2        │  │ HU#4       │  │ HU#5      │
        │ Conectar    │  │ PUT Editar │  │ POST Crear│
        │ botones     │  │            │  │           │
        └───────┬────┘  └──────┬─────┘  └────┬──────┘
                │              │              │
                └──────────────┼──────────────┘
                               │
                    ┌──────────▼──────────┐
                    │ HU#3 DetailPanel    │
                    │ Funcional           │
                    └──────────┬──────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
    ┌───▼────┐         ┌───────▼────┐        ┌──────▼────┐
    │ HU#6   │         │ HU#7       │        │ HU#8, HU#9│
    │ Modal  │         │ Validaciones       │ Tests      │
    │ Bloqueo│         │            │        │            │
    └────────┘         └────────────┘        └────────────┘
```

---

## 💡 Notas Importantes

### Sobre HU#1 (YA COMPLETADA)
- ✅ Especificación técnica detallada
- ✅ DTOs definidas
- ✅ Casos de prueba BDD
- ✅ Código pseudocódigo de implementación
- ✅ Criterios de aceptación claros

**Próximo paso**: Desarrollador comienza a implementar basado en esta HU

### Sobre HU#2-25 (A ESCRIBIR)
Cada HU debe tener:
1. **Descripción clara** de qué, por qué, para quién
2. **Criterios de aceptación** (AC) en formato Gherkin (Given-When-Then)
3. **Especificación técnica** (DTOs, endpoints, cambios de código)
4. **Casos de prueba** (unitarios, integración, BDD)
5. **Dependencias** claras
6. **Definición de Completado** específica

### Template para Nuevas HUs

```markdown
# 🌱 PASTURES-HU#{n}: {Título}

## 📝 Descripción de la Historia
Como {rol}, quiero {acción}, para {beneficio}

## 🎯 Criterios de Aceptación
### AC#{n}: {Descripción}
\`\`\`gherkin
Scenario: {Título}
  Given {estado inicial}
  When {acción}
  Then {resultado esperado}
\`\`\`

## 📊 Especificación Técnica
- DTOs
- Endpoints
- Cambios de código

## 🧪 Casos de Prueba
- Unitarios
- Integración
- BDD

## ✅ Definición de Completado
- [ ] Código implementado
- [ ] Tests >= 80% coverage
- [ ] Code review aprobado
- [ ] Documentación actualizada
- [ ] CI/CD green
```

---

## 📞 Contacto para Preguntas

**Por cada HU**, revisar:
- [Pastures Overview](./pastures-overview.md) - Visión técnica general
- [Events Architecture](./events-architecture.md) - Patrones de eventos
- [Architecture Index](../architecture/index.md) - Sistema general

**Para especificaciones de datos**:
- [Analysis Table Design](../analysis-table-design.md)

**Para flujos visuales**:
- [Flujo Dashboard Potreros](../architecture/flujo-dashboard-potreros.md)

---

**Generado**: 2026-01-09 | **Versión**: 1.0
