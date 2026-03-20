# 👥 MATRIZ DE ASIGNACIÓN DE RECURSOS - 25 HISTORIAS

**Proyecto**: Cattle Pastures
**Fecha**: 2026-01-09
**Versión**: 1.0

---

## 🎯 EQUIPO

### **Backend (2 Senior Developers)**

**Backend Developer 1 (B1):**
- Seniority: 5+ años Spring Boot
- Especialidad: API Design, Database Optimization
- Capacidad: 40 SP / sprint
- Historias: HU#001, #004, #005, #008, #013, #017, #020

**Backend Developer 2 (B2):**
- Seniority: 4+ años Java
- Especialidad: Testing, Event-driven Architecture
- Capacidad: 40 SP / sprint
- Historias: HU#008, #009, #012, #018, #023, #019 (pair)

### **Frontend (2 Senior Developers)**

**Frontend Developer 1 (F1):**
- Seniority: 5+ años React
- Especialidad: Component Architecture, Performance
- Capacidad: 40 SP / sprint
- Historias: HU#002, #006, #010, #011, #015, #016, #021

**Frontend Developer 2 (F2):**
- Seniority: 4+ años JavaScript
- Especialidad: UX/Styling, Accessibility
- Capacidad: 40 SP / sprint
- Historias: HU#003, #007, #014, #022, #025

### **QA (1 Tester)**

**QA Engineer:**
- Seniority: 3+ años QA Automation
- Especialidad: E2E Testing, Test Strategy
- Capacidad: 40 SP / sprint
- Historias: HU#024 (E2E Tests)

### **Soporte**

**Arquitecto** (0.5 FTE):
- Code reviews
- Pair programming en historias críticas
- Decision making

---

## 📊 ASIGNACIÓN POR SPRINT

### **SPRINT 1 (Week 1-2): MVP + Init Core**

```
WEEK 1: P0 - MVP Base (13 SP)
┌─────────────────────────────────────────────────────────┐
│ B1: HU#001 Backend POST Eventos (5 SP) 40h             │
│ F1: HU#002 Frontend Botones (5 SP) 40h                 │
│ F2: HU#003 DetailPanel Mock (3 SP) 24h                 │
│ Arquitecto: Pair on HU#001 (2 days)                    │
│ QA: Test planning                                       │
└─────────────────────────────────────────────────────────┘
Total: 13 SP ✓

WEEK 2: Start P1 (15 SP)
┌─────────────────────────────────────────────────────────┐
│ B1: HU#004 Backend PUT Editar (5 SP) 40h               │
│ B2: HU#005 Backend POST Crear (5 SP) 40h               │
│ F2: HU#007 Frontend Validaciones (3 SP) 24h            │
│ F1: HU#006 Modal Bloqueo (start) 16h                   │
│ QA: Unit test reviews                                   │
└─────────────────────────────────────────────────────────┘
Total: 15 SP ✓

SPRINT 1 TOTAL: 28 SP (Objetivo: 26-30 SP) ✅
```

### **SPRINT 2 (Week 3-4): Finish Core + Start Dashboard**

```
WEEK 3: Testing + Dashboard Init (17 SP)
┌─────────────────────────────────────────────────────────┐
│ B1: HU#008 Backend Tests Engine (5 SP) 40h             │
│ B2: HU#008 Backend Tests Engine (pair) + HU#009 (3 SP) │
│ F1: HU#010 Frontend Calendario (start) 24h             │
│ F1: HU#006 Modal Bloqueo (finish) 24h                  │
│ Arquitecto: Pair on HU#008 (3 days - state machine)    │
└─────────────────────────────────────────────────────────┘
Total: 17 SP ✓

WEEK 4: Dashboard Foundation (14 SP)
┌─────────────────────────────────────────────────────────┐
│ F1: HU#010 Calendario (finish) (3 SP) 24h              │
│ F1+F2: HU#011 AlertCenter (5 SP) 40h                   │
│ F2: HU#014 EditorPanel (3 SP) 24h                      │
│ B1: HU#012 Historial Eventos (start) 24h               │
│ QA: Integration test setup                              │
└─────────────────────────────────────────────────────────┘
Total: 14 SP ✓

SPRINT 2 TOTAL: 31 SP (Objetivo: 26-30 SP) ✅+
```

### **SPRINT 3 (Week 5-6): Dashboard Complete + Init Infrastructure**

```
WEEK 5: Dashboard + Audit (16 SP)
┌─────────────────────────────────────────────────────────┐
│ B1: HU#012 Historial Eventos (finish) (5 SP) 40h       │
│ B2: HU#013 Auditoría (5 SP) 40h                        │
│ F1: HU#015 react-big-calendar (5 SP) 40h               │
│ F2: HU#022 Dark Mode (start) 16h                       │
│ QA: Dashboard testing                                   │
└─────────────────────────────────────────────────────────┘
Total: 16 SP ✓

WEEK 6: Analytics + Infrastructure Init (18 SP)
┌─────────────────────────────────────────────────────────┐
│ F1+F2: HU#016 Estadísticas (8 SP) 64h                  │
│ B1: HU#017 OpenAPI/Swagger (3 SP) 24h                  │
│ B2: HU#020 Soft Delete (5 SP) 40h                      │
│ F2: HU#022 Dark Mode (finish) (2 SP) 16h               │
│ Arquitecto: Code review HU#016                          │
└─────────────────────────────────────────────────────────┘
Total: 18 SP ✓

SPRINT 3 TOTAL: 34 SP (Objetivo: 26-30 SP) ✅+ (High velocity)
```

### **SPRINT 4 (Week 7-8): Infrastructure + Testing**

```
WEEK 7: Events + Export + Cache (21 SP) ⚠️ COMPLEX WEEK
┌─────────────────────────────────────────────────────────┐
│ B2: HU#018 SNS/SQS (8 SP) 64h ⚠️ HIGH RISK             │
│ F1: HU#021 Export CSV/Excel (5 SP) 40h                 │
│ B1: HU#023 Redis Caching (8 SP) 64h (OPTIONAL)         │
│ Arquitecto: Pair on HU#018 (3+ days) ⚠️                │
│ QA: Spike testing SNS/SQS                               │
└─────────────────────────────────────────────────────────┘
Total: 21 SP (HU#023 optional if time constraint)

WEEK 8: Multi-tenant + E2E + Mobile (29 SP) ⚠️ CRITICAL WEEK
┌─────────────────────────────────────────────────────────┐
│ B1+B2: HU#019 Multi-tenant (13 SP) 104h ⚠️ REFACTOR   │
│ QA: HU#024 E2E Tests Cypress (8 SP) 64h                │
│ F1+F2: HU#025 Responsive Mobile (8 SP) 64h             │
│ Arquitecto: Pair on HU#019 (full week) ⚠️⚠️            │
└─────────────────────────────────────────────────────────┘
Total: 29 SP (If HU#023 pushed to post-launch)

SPRINT 4 TOTAL: 50 SP (High, but achievable with parallelization)
🚨 Note: HU#019 requires BOTH backend devs + Architect
```

---

## 📅 VELOCIDAD DEL EQUIPO

```
Sprint 1: 28 SP → Baseline established
Sprint 2: 31 SP → +10% (team ramping up)
Sprint 3: 34 SP → +10% (peak velocity)
Sprint 4: 50 SP → +47% (more concurrent work, but HU#023 optional)

Average: 35.75 SP/sprint
Recommended capacity per dev: 40 SP/sprint (sustainable)
```

---

## ⚠️ SOBRECARGAS DETECTADAS

### **Critical Path Issues**

**Sprint 1: ✅ OK**
- Todos los devs ~50% ocupados Week 1, 60% Week 2
- Margin: 20% para code review, refactoring, bugs

**Sprint 2: ✅ OK**
- Pair programming en HU#008 (testing crítica)
- Margin: 15%

**Sprint 3: ⚠️ MODERATE**
- Pico de velocidad (34 SP)
- HU#016 requiere 2 devs frontend
- Margin: 10%

**Sprint 4: 🚨 HIGH RISK**
- 50 SP total (120% del baseline)
- HU#019 es CRITICAL REFACTOR
- Solo viable si HU#023 (Redis) se pushes post-launch
- Requiere Architect full-time Week 8

### **Recomendación para Sprint 4**

```
OPCIÓN A: Include HU#023 (Full infrastructure)
  Risk: Muy alto, posible slip de HU#025
  Timeline: +1 semana
  
OPCIÓN B: Skip HU#023, Focus on Quality (RECOMENDADO)
  Risk: Bajo, contenible
  Timeline: Cumplir deadline
  Trade-off: Redis caching puede venir post-launch
  
OPCIÓN C: Contratar Backend contractor (Short-term)
  Risk: Medio (ramp-up time)
  Timeline: On-time
  Cost: +$15K
```

**RECOMENDACIÓN**: Opción B (skip HU#023 post-launch) ✅

---

## 🔄 DEPENDENCIAS CRÍTICAS Y SECUENCIAMIENTO

### **Critical Path (Ruta crítica del proyecto)**

```
MUST DO FIRST:
┌────────────────────────────────────┐
│ HU#001 Backend POST Eventos (S1W1) │ Sprint 1, Week 1
│ ↓ Bloqueador para:                 │
│ ├─ HU#002 Frontend Botones (S1W1)  │
│ ├─ HU#004 PUT Editar (S1W2)        │
│ ├─ HU#005 POST Crear (S1W2)        │
│ ├─ HU#008 Tests Engine (S2W1)      │
│ ├─ HU#009 Tests ETA (S2W1)         │
│ ├─ HU#012 Historial (S3W1)         │
│ └─ HU#013 Auditoría (S3W1)         │
└────────────────────────────────────┘

SECOND WAVE:
┌────────────────────────────────────┐
│ HU#002 Frontend Botones (S1W1)     │
│ ↓ Bloqueador para:                 │
│ ├─ HU#003 DetailPanel (S1W1)       │
│ ├─ HU#006 Modal Bloqueo (S2W1)     │
│ ├─ HU#007 Validaciones (S1W2)      │
│ └─ Dashboard todas las historias   │
└────────────────────────────────────┘

DASHBOARD FOUNDATION:
┌────────────────────────────────────┐
│ HU#003 DetailPanel Mock (S1W1)     │
│ HU#012 Historial Eventos (S3W1)    │
│ ↓ Bloqueadores para:               │
│ ├─ HU#010 Calendario (S2W1)        │
│ ├─ HU#011 AlertCenter (S2W2)       │
│ ├─ HU#015 react-calendar (S3W1)    │
│ ├─ HU#016 Estadísticas (S3W2)      │
│ └─ HU#024 E2E Tests (S4W2)         │
└────────────────────────────────────┘

INFRASTRUCTURE CRITICAL:
┌────────────────────────────────────┐
│ HU#019 Multi-tenant (S4W2)         │ ⚠️ ÚLTIMO
│ (Requiere refactor de todo)        │
│ Bloqueador para:                   │
│ ├─ Production deployment           │
│ └─ Customer acceptance             │
└────────────────────────────────────┘
```

---

## 👥 MATRIZ DE COMPETENCIAS

```
              Backend  Frontend  Testing  DB   AWS
B1             ⭐⭐⭐   ⭐⭐     ⭐⭐     ⭐⭐⭐ ⭐⭐
B2             ⭐⭐⭐   ⭐       ⭐⭐⭐   ⭐⭐   ⭐⭐
F1             ⭐⭐     ⭐⭐⭐   ⭐⭐     ⭐    ⭐
F2             ⭐       ⭐⭐⭐   ⭐      ⭐    ⭐
QA             ⭐       ⭐⭐     ⭐⭐⭐   ⭐    ⭐

⭐⭐⭐ = Expert (5+ años)
⭐⭐ = Proficient (2-4 años)
⭐ = Familiar (< 2 años)
```

### **Recomendaciones de Emparejamiento**

| Historia | Pair Recomendado | Razón |
|----------|-----------------|-------|
| HU#001 | B1 + Architect | API design review |
| HU#008 | B1 + B2 + Architect | State machine complexity |
| HU#012 | B1 + Architect | Database optimization |
| HU#016 | F1 + F2 + Architect | Dashboard architecture |
| HU#018 | B2 + Architect | AWS SNS/SQS learning |
| HU#019 | B1 + B2 + Architect | ⚠️ CRITICAL REFACTOR |
| HU#024 | QA + F1 | E2E test framework setup |

---

## 📊 CARGA LABORAL POR SPRINT

### **Sprint 1 Workload**

```
B1: HU#001(40) + HU#004(40) = 80h (100% capacity - tight)
B2: HU#005(40) = 40h (50% capacity - slack for review/spike)
F1: HU#002(40) + HU#006(16) = 56h (70% capacity - good)
F2: HU#003(24) + HU#007(24) = 48h (60% capacity - good)
QA: Planning (16h) = 20% capacity
Architect: HU#001 pair (16h) = 40% capacity

Balance: ✅ GOOD - B1 tight but manageable
```

### **Sprint 2 Workload**

```
B1: HU#008(40) = 40h (50% capacity - pair programming)
B2: HU#008(24) + HU#009(40) = 64h (80% capacity - high)
F1: HU#010(40) + HU#006(24) + HU#011(20) = 84h (105% 🚨 OVER)
F2: HU#014(24) = 24h (30% capacity - slack)
QA: Integration tests (32h) = 80% capacity
Architect: HU#008 pair (24h) = 50% capacity

⚠️ ISSUE: F1 overbooked by 5%
SOLUTION: Move HU#011 start to week 4, or have F2 help
```

**Sprint 2 Adjustment:**
```
Week 3:
  F1: HU#010 (40h) + HU#006 finish (16h) = 56h ✓
  F2: HU#014 (24h) = 24h ✓

Week 4:
  F1: HU#010 finish (8h) + HU#011 start (24h) = 32h ✓
  F2: Slack / HU#011 pair (16h) = 16h ✓
```

### **Sprint 3 Workload**

```
B1: HU#012(40) + HU#017(24) = 64h (80% capacity - high but ok)
B2: HU#013(40) + HU#020(40) = 80h (100% capacity - tight)
F1: HU#015(40) + HU#011(20) + HU#016(32) = 92h (115% 🚨 OVER)
F2: HU#022(40) + HU#016(32) = 72h (90% capacity)
QA: Dashboard QA (40h) = 100% capacity
Architect: Code review (16h) = 35% capacity

⚠️ ISSUE: F1 overbooked by 15%
SOLUTION: Push HU#015 partial to week 6, or hire contractor
```

**Sprint 3 Adjustment (CRITICAL):**
```
Week 5:
  F1: HU#015 (24h) + HU#011 (16h) = 40h ✓
  F2: HU#022 (32h) + HU#016 (24h) = 56h ✓

Week 6:
  F1: HU#015 finish (16h) + HU#016 (40h) = 56h ✓
  F2: HU#022 finish (8h) + HU#016 (24h) = 32h ✓

Result: Balanced ✅
```

### **Sprint 4 Workload - 🚨 HIGHEST RISK**

```
Scenario A: Include HU#023 (Redis)
B1: HU#023(64) + HU#019(52) = 116h (145% 🚨🚨 DANGER)
B2: HU#018(64) + HU#019(52) = 116h (145% 🚨🚨 DANGER)
F1: HU#021(40) + HU#025(48) = 88h (110% 🚨 OVER)
F2: HU#025(16) = 16h (20% capacity)
QA: HU#024(64) = 160% (need contractor QA)

✗ NOT VIABLE - Slip inevitable

Scenario B: Skip HU#023 (RECOMMENDED) ✅
B1: HU#019(52) = 52h (65% capacity - manageable)
B2: HU#018(64) + HU#019(52) = 116h (145% 🚨 still high)
F1: HU#021(40) + HU#025(48) = 88h (110% 🚨 OVER)
F2: HU#025(16) = 16h (20% capacity)
QA: HU#024(64) = 160% (need contractor QA)

⚠️ Still overloaded, but:
- HU#019 can be parallelized (pair work)
- F1/F2 can divide HU#025 (responsive is parallel-friendly)
- QA contractor can cover HU#024

✓ VIABLE with careful task division

Scenario C: Hire Backend Contractor + QA Contractor
B1: HU#023(64) = 64h (80% capacity)
B2: HU#018(64) + HU#019(52) = 116h (145% still high but doable with pair)
Contractor Backend: Remaining work from HU#018/019
F1+F2: HU#021(40) + HU#025(64) = 104h combined (52% each)
Contractor QA: HU#024(64) = 100% capacity

✓ BEST CASE - No slip, all work done
Cost: +$30K (1 backend + 1 QA contractor for 2 weeks)
```

**Sprint 4 Recommendation:**

```
✅ GO WITH SCENARIO B (Skip HU#023 post-launch)

Reasons:
1. Cost-effective ($0 contractor)
2. Manageable with discipline
3. Redis caching can be added post-launch (feature flag)
4. Team doesn't burn out
5. Quality stays high

Task Division Sprint 4:
Week 7:
  - B2 (lead) HU#018 SNS/SQS (64h dedicated)
  - Architect pairs with B2 (Mon-Wed)
  - B1 onboarding to HU#019 (half capacity)
  - F1 HU#021 Export (40h)
  - F2 buffering (20% capacity)
  - QA setup Cypress framework (32h)

Week 8:
  - B1 + B2 (BOTH FULL) HU#019 Multi-tenant (52h each + pair time)
  - F1 + F2 (BOTH FULL) HU#025 Responsive (combine 64h)
  - QA (FULL) HU#024 E2E Tests (64h)
  - Architect FULL TIME pair programming + decisions
```

---

## 🎯 HANDOFF STRATEGY

```
End Sprint 1:
  ├─ Backend: Fully tested POST/GET endpoints
  ├─ Frontend: Basic UI with mocked data
  ├─ Integration: Manual testing only
  └─ Deliverable: MVP working demo

End Sprint 2:
  ├─ Backend: Full CRUD + validation + tests 85%+
  ├─ Frontend: All modal/form logic
  ├─ Integration: Happy path manual tests
  └─ Deliverable: Functional system, ready for dashboard

End Sprint 3:
  ├─ Backend: Audit trail + history queries
  ├─ Frontend: Professional dashboard
  ├─ Integration: Test coverage 75%+
  └─ Deliverable: Business analytics ready

End Sprint 4:
  ├─ Backend: Multi-tenant + production hardening
  ├─ Frontend: Mobile responsive + dark mode
  ├─ Testing: E2E coverage + browser testing
  └─ Deliverable: Production-ready system
```

---

## 📈 RISK MITIGATION CHECKLIST

```
✓ Have contingency developer contacts for Sprint 4
✓ Schedule spikes for HU#018 (AWS) and HU#019 (refactor) Week 6-7
✓ Establish code review process Week 1 (prevent technical debt)
✓ Daily standup focus on blockers (not status updates)
✓ Weekly velocity tracking (adjust future sprints)
✓ Pair programming schedule for HU#008, #012, #016, #019
✓ QA involvement from Sprint 1 (not end-game testing)
✓ Architect code review minimum 2x/day
✓ Feature flags for HU#018 (SNS/SQS) and HU#023 (Redis optional)
✓ Staging environment parity with production by Sprint 3
```

---

**Documento generado**: 2026-01-09
**Última revisión**: 2026-01-09
**Siguiente revisión**: Post Sprint 1
