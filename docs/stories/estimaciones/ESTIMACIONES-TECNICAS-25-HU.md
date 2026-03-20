# 📊 ESTIMACIONES TÉCNICAS - 25 HISTORIAS DE USUARIO

**Proyecto**: Cattle Pastures (Gestión Rotacional de Potreros)
**Fecha**: 2026-01-09
**Versión**: 1.0
**Equipo**: Backend (2) + Frontend (2) + QA (1)

---

## 📋 RESUMEN EJECUTIVO

| Métrica | Valor |
|---------|-------|
| **Total Story Points** | 130 SP |
| **Total Horas Estimadas** | 520 horas |
| **Sprints Recomendados** | 4 sprints de 2 semanas |
| **Velocidad Equipo** | 30 SP / sprint (referencial) |
| **Ruta Crítica** | HU#001 → HU#002 → HU#003 → HU#012 → HU#016 |

---

## 🎯 DISTRIBUCIÓN POR PRIORIDAD

### **P0 - MVP (3 HU) - Sprint 1: Week 1-2**

| HU | Título | SP | Horas | Complejidad | Riesgo | Bloqueadores |
|----|--------|----|----|-----------|--------|--------------|
| #001 | Backend POST Eventos | 5 | 40 | Media | Bajo | Ninguno |
| #002 | Frontend Botones | 5 | 40 | Media | Bajo | HU#001 |
| #003 | DetailPanel Mock | 3 | 24 | Baja | Bajo | HU#002 |
| | **SUBTOTAL P0** | **13 SP** | **104h** | | | |

**Notas P0:**
- Historias independientes pero secuenciales
- Mock en HU#003 acelera HU#012
- Sin dependencias externas
- **Entregable**: MVP básico (crear, ver, abrir/cerrar potreros)

---

### **P1 - Core Rotation (6 HU) - Sprint 1: Week 3-4 + Sprint 2: Week 1-2**

| HU | Título | SP | Horas | Complejidad | Riesgo | Bloqueadores |
|----|--------|----|----|-----------|--------|--------------|
| #004 | Backend PUT Editar | 5 | 40 | Media | Bajo | HU#001 |
| #005 | Backend POST Crear | 5 | 40 | Media | Bajo | HU#001 |
| #006 | Frontend Modal Bloqueo | 5 | 40 | Media | Medio | HU#002 |
| #007 | Frontend Validaciones | 3 | 24 | Baja | Bajo | HU#002 |
| #008 | Backend Tests Engine | 8 | 64 | Alta | Medio | HU#001, HU#004, HU#005 |
| #009 | Backend Tests ETA | 5 | 40 | Media | Bajo | HU#001 |
| | **SUBTOTAL P1** | **31 SP** | **248h** | | | |

**Notas P1:**
- HU#008 es crítica (state machine testing)
- HU#007 reutilizable en todas las historias
- Testing robusto = reducción de bugs futuro
- **Entregable**: Sistema rotacional funcional con tests 85%+

---

### **P2 - Dashboard & Visualization (7 HU) - Sprint 2: Week 3-4 + Sprint 3: Week 1-2**

| HU | Título | SP | Horas | Complejidad | Riesgo | Bloqueadores |
|----|--------|----|----|-----------|--------|--------------|
| #010 | Frontend Calendario | 5 | 40 | Media | Bajo | HU#003 |
| #011 | Frontend AlertCenter | 5 | 40 | Media | Bajo | HU#010 |
| #012 | Backend Historial Eventos | 8 | 64 | Alta | Medio | HU#001, HU#003 |
| #013 | Backend Auditoría | 5 | 40 | Media | Bajo | HU#012 |
| #014 | Frontend EditorPanel | 3 | 24 | Baja | Bajo | HU#003, HU#007 |
| #015 | Frontend react-big-calendar | 5 | 40 | Media | Bajo | HU#010, HU#011 |
| #016 | Frontend Estadísticas | 8 | 64 | Alta | Medio | HU#012, HU#013, HU#015 |
| | **SUBTOTAL P2** | **39 SP** | **312h** | | | |

**Notas P2:**
- HU#012 + HU#016 son complejas (high value)
- HU#014 rápida (reutiliza FormField de HU#007)
- Dashboard profesional = negociación con cliente
- **Entregable**: Dashboard completo con analytics

---

### **P3 - Infrastructure & Optimization (9 HU) - Sprint 3: Week 3-4 + Sprint 4**

| HU | Título | SP | Horas | Complejidad | Riesgo | Bloqueadores |
|----|--------|----|----|-----------|--------|--------------|
| #017 | Backend OpenAPI/Swagger | 3 | 24 | Baja | Bajo | Ninguno |
| #018 | Backend SNS/SQS | 8 | 64 | Alta | Alto | HU#001 |
| #019 | Backend Multi-tenant | 13 | 104 | Muy Alta | Alto | HU#001-009 (refactor) |
| #020 | Backend Soft Delete | 5 | 40 | Media | Bajo | HU#001 |
| #021 | Frontend Exportar CSV/Excel | 5 | 40 | Media | Bajo | HU#010-016 |
| #022 | Frontend Dark Mode | 5 | 40 | Media | Bajo | Ninguno |
| #023 | Backend Caching Redis | 8 | 64 | Alta | Medio | HU#001-016 (optional) |
| #024 | Tests E2E Cypress | 8 | 64 | Alta | Medio | Todos los anteriores |
| #025 | Frontend Responsive Mobile | 8 | 64 | Alta | Medio | HU#010-016 |
| | **SUBTOTAL P3** | **63 SP** | **504h** | | | |

**Notas P3:**
- **HU#019 es MUY COMPLEJA** (refactor de toda arquitectura)
- HU#018 riesgo alto (AWS SQS/SNS)
- HU#023 optional (mejora performance, no MVP)
- HU#024 crítica para producción
- **Entregable**: Sistema production-ready, escalable, auditable

---

## 📈 TIMELINE Y SPRINTS

### **Sprint 1 (Semana 1-2): MVP + Init Core**
```
Lunes-Viernes (Week 1): P0 (13 SP)
- HU#001: Backend POST Eventos (5 SP) - 2 devs backend
- HU#002: Frontend Botones (5 SP) - 2 devs frontend
- HU#003: DetailPanel Mock (3 SP) - 1 dev frontend

Lunes-Viernes (Week 2): Start P1 (15 SP)
- HU#004: Backend PUT Editar (5 SP) - 1 dev backend
- HU#005: Backend POST Crear (5 SP) - 1 dev backend
- HU#007: Frontend Validaciones (3 SP) - 1 dev frontend
- HU#006: Modal Bloqueo (init) - 1 dev frontend

Velocidad Sprint 1: 28 SP ✅ (Meta: 26-30 SP)
```

### **Sprint 2 (Semana 3-4): Core + Start Dashboard**
```
Lunes-Viernes (Week 3): Finish P1 + Start P2 (17 SP)
- HU#006: Modal Bloqueo (finish) - 1 dev frontend
- HU#008: Backend Tests Engine (8 SP) - 1 dev backend
- HU#009: Backend Tests ETA (5 SP) - 1 dev backend
- HU#010: Frontend Calendario (init) - 1 dev frontend

Lunes-Viernes (Week 4): Dashboard (14 SP)
- HU#010: Frontend Calendario (finish) (5 SP)
- HU#011: Frontend AlertCenter (5 SP) - 2 devs frontend
- HU#014: Frontend EditorPanel (3 SP) - 1 dev frontend
- HU#012: Backend Historial (init) - 1 dev backend

Velocidad Sprint 2: 31 SP ✅ (Meta: 26-30 SP)
```

### **Sprint 3 (Semana 5-6): Dashboard Finish + Init Infra**
```
Lunes-Viernes (Week 5): Dashboard + Audit (16 SP)
- HU#012: Backend Historial (finish) (8 SP) - 1 dev backend
- HU#013: Backend Auditoría (5 SP) - 1 dev backend
- HU#015: react-big-calendar (5 SP) - 1 dev frontend
- HU#022: Dark Mode (init) - 1 dev frontend

Lunes-Viernes (Week 6): Analytics + Infra Init (18 SP)
- HU#016: Estadísticas (8 SP) - 2 devs frontend
- HU#017: OpenAPI/Swagger (3 SP) - 1 dev backend
- HU#020: Soft Delete (5 SP) - 1 dev backend
- HU#022: Dark Mode (finish) (2 SP)

Velocidad Sprint 3: 34 SP (incluye HU#022 small)
```

### **Sprint 4 (Semana 7-8): Infrastructure + Testing**
```
Lunes-Viernes (Week 7): Events + Export + Cache (21 SP)
- HU#018: SNS/SQS (8 SP) - 1 dev backend (riesgo alto)
- HU#021: Export CSV/Excel (5 SP) - 1 dev frontend
- HU#023: Redis Caching (8 SP) - 1 dev backend (optional)

Lunes-Viernes (Week 8): Multi-tenant + E2E + Responsive (29 SP)
- HU#019: Multi-tenant (13 SP) - 2 devs backend (refactor crítica)
- HU#024: E2E Tests (8 SP) - 1 dev QA
- HU#025: Responsive Mobile (8 SP) - 2 devs frontend

Velocidad Sprint 4: 50 SP (alto, pero posible con refactor paralelo)
```

---

## 📊 GRÁFICO DE ESTIMACIONES

```
Sprint 1: |████████████████ 28 SP|
Sprint 2: |██████████████████ 31 SP|
Sprint 3: |████████████████████ 34 SP|
Sprint 4: |██████████████████████████ 50 SP|

Total: 143 SP (130 SP core + 13 SP buffer)
```

---

## 🎯 COMPLEJIDAD POR HISTORIA

### **BAJA (1-3 SP)** - 2-3 días dev

- HU#003: DetailPanel Mock (3 SP) - Componente UI simple con mock data
- HU#007: Validaciones Frontend (3 SP) - Funciones puras, reutilizables
- HU#017: OpenAPI/Swagger (3 SP) - Configuración Spring, bajo esfuerzo

### **MEDIA (5 SP)** - 4-5 días dev

- HU#001: Backend POST Eventos (5 SP) - Endpoint standard layered
- HU#002: Frontend Botones (5 SP) - Hooks + API integration
- HU#004: Backend PUT Editar (5 SP) - Optimistic locking, versioning
- HU#005: Backend POST Crear (5 SP) - TABLE_COUNTERS pattern
- HU#006: Frontend Modal Bloqueo (5 SP) - Dual-mode UI component
- HU#009: Backend Tests ETA (5 SP) - Boundary value analysis
- HU#010: Frontend Calendario (5 SP) - Calendar grid + color mapping
- HU#011: Frontend AlertCenter (5 SP) - Polling + notification system
- HU#013: Backend Auditoría (5 SP) - AOP, audit trail
- HU#014: Frontend EditorPanel (3 SP) - Form component reutilizable
- HU#015: react-big-calendar (5 SP) - 3rd-party library integration
- HU#020: Backend Soft Delete (5 SP) - Soft delete + filtering
- HU#021: Frontend Export CSV/Excel (5 SP) - File generation
- HU#022: Frontend Dark Mode (5 SP) - Theme provider + CSS vars

### **ALTA (8-13 SP)** - 6-10 días dev

- HU#008: Backend Tests Engine (8 SP) - State machine testing, parametrized
- HU#012: Backend Historial Eventos (8 SP) - Paginated queries, complex filtering
- HU#016: Frontend Estadísticas (8 SP) - Dashboard con charts, exports
- HU#018: Backend SNS/SQS (8 SP) - AWS messaging, event-driven architecture
- HU#023: Backend Caching Redis (8 SP) - Cache strategy, invalidation
- HU#024: Tests E2E Cypress (8 SP) - E2E test suite, multi-browser
- HU#025: Frontend Responsive Mobile (8 SP) - Mobile-first, touch-optimized

### **MUY ALTA (13+ SP)** - 10+ días dev

- **HU#019: Backend Multi-tenant (13 SP)** - ⚠️ REFACTOR CRÍTICA
  - Modificar TODOS los repositories
  - Agregar TenantContext a todo
  - Riesgo alto de regresiones
  - Requiere testing exhaustivo

---

## ⚠️ RIESGOS Y MITIGACIONES

### **Alto Riesgo (HU#018, HU#019)**

| Riesgo | Impacto | Mitigación |
|--------|---------|-----------|
| AWS SQS/SNS Learning Curve | Delays, bugs | Spike de 2 días antes |
| Multi-tenant Refactor (HU#019) | Regresiones amplias | Refactor gradual + tests 95%+ |
| Estado de Máquina Complejo (HU#008) | Edge cases | Property-based testing |
| Performance Dashboard (HU#016) | Slowness | Caching + lazy loading |

### **Mitigaciones Generales**

```
✅ Code Reviews: 2 approvals antes de merge
✅ Testing: Unit 85%+, Integration 70%+, E2E happy path
✅ Spikes: 2-3 días antes de historias de riesgo alto
✅ Pair Programming: HU#019 (multi-tenant) con ambos backend devs
✅ Monitoring: Performance dashboards en staging
✅ Rollback Plan: Feature flags para HU#018, HU#023
```

---

## 💰 ESTIMACIÓN DE RECURSOS

### **Equipo Recomendado**

```
Backend (2 devs senior)
  - Dev 1: HU#001, #004, #005, #008, #009, #013, #017, #020
  - Dev 2: HU#008 (pair), #012, #018, #019 (pair), #023

Frontend (2 devs senior)
  - Dev 1: HU#002, #006, #007, #010, #011, #014, #022
  - Dev 2: HU#002 (pair), #015, #016, #021, #024 (pair), #025

QA (1 tester)
  - Tester 1: HU#024 (E2E), #025 (mobile), testing infrastructure

Arquitecto (0.5 FTE)
  - Review, pair programming en historias críticas
  - Decision-making en technical debt
```

### **Costo Estimado (USD)**

```
Sprint 1-2 (MVP + Core): 4 weeks × 5 devs × $200/hr = $40,000
Sprint 3 (Dashboard): 2 weeks × 5 devs × $200/hr = $20,000
Sprint 4 (Infra + Testing): 2 weeks × 5 devs × $200/hr = $20,000
Arquitecto oversight: 8 weeks × 0.5 × $250/hr = $10,000

TOTAL: $90,000 USD (aprox. 2 meses)
```

---

## 📋 TABLA CONSOLIDADA - TODAS LAS 25 HU

| # | HU | Título | SP | h | Complejidad | Riesgo | Sprint | Dev Backend | Dev Frontend |
|----|------|--------|----|----|--------|--------|--------|------------|----------|
| 1 | #001 | POST Eventos | 5 | 40 | Media | Bajo | S1 | B1 | - |
| 2 | #002 | Botones Frontend | 5 | 40 | Media | Bajo | S1 | - | F1 |
| 3 | #003 | DetailPanel Mock | 3 | 24 | Baja | Bajo | S1 | - | F2 |
| 4 | #004 | PUT Editar | 5 | 40 | Media | Bajo | S1 | B1 | - |
| 5 | #005 | POST Crear | 5 | 40 | Media | Bajo | S1 | B1 | - |
| 6 | #006 | Modal Bloqueo | 5 | 40 | Media | Medio | S2 | - | F1 |
| 7 | #007 | Validaciones | 3 | 24 | Baja | Bajo | S1 | - | F2 |
| 8 | #008 | Tests Engine | 8 | 64 | Alta | Medio | S2 | B1,B2 | - |
| 9 | #009 | Tests ETA | 5 | 40 | Media | Bajo | S2 | B2 | - |
| 10 | #010 | Calendario | 5 | 40 | Media | Bajo | S2 | - | F1 |
| 11 | #011 | AlertCenter | 5 | 40 | Media | Bajo | S2 | - | F1,F2 |
| 12 | #012 | Historial Eventos | 8 | 64 | Alta | Medio | S3 | B1 | - |
| 13 | #013 | Auditoría | 5 | 40 | Media | Bajo | S3 | B1 | - |
| 14 | #014 | EditorPanel | 3 | 24 | Baja | Bajo | S3 | - | F2 |
| 15 | #015 | react-big-calendar | 5 | 40 | Media | Bajo | S3 | - | F1 |
| 16 | #016 | Estadísticas | 8 | 64 | Alta | Medio | S3 | - | F1,F2 |
| 17 | #017 | OpenAPI/Swagger | 3 | 24 | Baja | Bajo | S4 | B1 | - |
| 18 | #018 | SNS/SQS | 8 | 64 | Alta | Alto | S4 | B2 | - |
| 19 | #019 | Multi-tenant | 13 | 104 | Muy Alta | Alto | S4 | B1,B2 | - |
| 20 | #020 | Soft Delete | 5 | 40 | Media | Bajo | S3 | B2 | - |
| 21 | #021 | Export CSV/Excel | 5 | 40 | Media | Bajo | S4 | - | F1 |
| 22 | #022 | Dark Mode | 5 | 40 | Media | Bajo | S3 | - | F2 |
| 23 | #023 | Caching Redis | 8 | 64 | Alta | Medio | S4 | B2 | - |
| 24 | #024 | E2E Tests | 8 | 64 | Alta | Medio | S4 | - | QA |
| 25 | #025 | Responsive Mobile | 8 | 64 | Alta | Medio | S4 | - | F1,F2 |
| | | **TOTAL** | **130** | **1040** | | | | | |

---

## 📌 DEPENDENCIAS CRÍTICAS

```
┌─ HU#001 (POST Eventos) ━━━━━━━━━━┐
│  Bloqueador para: HU#002, #004, #005, #008, #009, #012, #013
│  Status: Must do first
└────────────────────────────────────┘

┌─ HU#002 (Botones Frontend) ━━━━━━┐
│  Bloqueador para: HU#003, #006, #007
│  Status: Follow HU#001
└────────────────────────────────────┘

┌─ HU#003 (DetailPanel Mock) ━━━━━┐
│  Bloqueador para: HU#010, #011, #012, #014, #015, #016
│  Status: Mock accelerates HU#012
└────────────────────────────────────┘

┌─ HU#008 (Tests Engine) ━━━━━━━━━┐
│  Bloqueador para: Production readiness
│  Status: High quality gate
└────────────────────────────────────┘

┌─ HU#012 (Historial Eventos) ━━━━┐
│  Bloqueador para: HU#013, #016, #024
│  Status: Core for dashboard
└────────────────────────────────────┘

┌─ HU#019 (Multi-tenant) ━━━━━━━━━┐
│  Bloqueador para: Production deployment
│  Status: ⚠️ CRITICAL REFACTOR - Week 8 only
└────────────────────────────────────┘
```

---

## ✅ CRITERIOS DE ÉXITO POR SPRINT

### **Sprint 1 Success Criteria**
- ✅ MVP básico funcional (create/read/open/close)
- ✅ Backend y Frontend comunicando
- ✅ Tests 70%+ en backend
- ✅ Demo con cliente aprobada

### **Sprint 2 Success Criteria**
- ✅ Sistema rotacional completo
- ✅ Tests 85%+ en backend
- ✅ Validaciones robustas
- ✅ Manejo de errores completo

### **Sprint 3 Success Criteria**
- ✅ Dashboard profesional
- ✅ Auditoría implementada
- ✅ Performance baseline establecido
- ✅ Documentación completa

### **Sprint 4 Success Criteria**
- ✅ Multi-tenant validado
- ✅ E2E tests pasando
- ✅ Responsive mobile funcional
- ✅ Production ready

---

## 📅 CALENDARIO FINAL

```
Enero 2026:
  Semana 1-2 (9-22 Enero): Sprint 1 - MVP
  Semana 3-4 (23 Enero - 5 Febrero): Sprint 2 - Core

Febrero 2026:
  Semana 5-6 (6-19 Febrero): Sprint 3 - Dashboard
  Semana 7-8 (20 Febrero - 5 Marzo): Sprint 4 - Infrastructure

LAUNCH: Martes 11 Marzo 2026 ✨
```

---

## 🎯 RECOMENDACIONES FINALES

1. **Parallelizar Sprint 4** - HU#019 (multi-tenant) requiere ambos backend devs, pero HU#018 puede hacer otro dev
2. **QA desde Sprint 1** - Testing en paralelo, no al final
3. **Daily Standups** - 15 min máximo, enfoque en bloqueadores
4. **Risk Management** - HU#018 y #019 son riesgos, requieren spike
5. **Demo semanal** - Cliente feedback early and often
6. **Documentación Living** - Actualizar arquitectura en tiempo real

---

**Documento generado**: 2026-01-09
**Próxima revisión**: Después de Sprint 1 (ajuste de velocidad)
**Contacto**: Technical Lead / Arquitecto
