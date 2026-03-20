# ⚡ QUICK REFERENCE - ESTIMACIONES RESUMIDAS

## 📊 SNAPSHOT: 25 HISTORIAS EN 8 SEMANAS

```
TOTAL: 130 Story Points | 1,040 Horas | $90,000 USD | 4 Sprints

┌─────────────────────────────────────────────────────────────┐
│ SPRINT 1 (Week 1-2): MVP                    28 SP | 224h   │
│   ├─ HU#001 Backend POST Eventos (5 SP)                    │
│   ├─ HU#002 Frontend Botones (5 SP)                        │
│   ├─ HU#003 DetailPanel Mock (3 SP)                        │
│   ├─ HU#004 Backend PUT Editar (5 SP)                      │
│   ├─ HU#005 Backend POST Crear (5 SP)                      │
│   └─ HU#007 Frontend Validaciones (3 SP)                   │
│   🎯 Deliverable: MVP básico funcional                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ SPRINT 2 (Week 3-4): CORE + DASHBOARD INIT    31 SP | 248h │
│   ├─ HU#006 Frontend Modal Bloqueo (5 SP)                  │
│   ├─ HU#008 Backend Tests Engine (8 SP) ⚠️                  │
│   ├─ HU#009 Backend Tests ETA (5 SP)                       │
│   ├─ HU#010 Frontend Calendario (5 SP)                     │
│   ├─ HU#011 Frontend AlertCenter (5 SP)                    │
│   └─ HU#014 Frontend EditorPanel (3 SP)                    │
│   🎯 Deliverable: Sistema rotacional + foundation dashboard│
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ SPRINT 3 (Week 5-6): DASHBOARD               34 SP | 272h  │
│   ├─ HU#012 Backend Historial Eventos (8 SP)              │
│   ├─ HU#013 Backend Auditoría (5 SP)                      │
│   ├─ HU#015 Frontend react-big-calendar (5 SP)            │
│   ├─ HU#016 Frontend Estadísticas (8 SP)                  │
│   ├─ HU#017 Backend OpenAPI/Swagger (3 SP)                │
│   ├─ HU#020 Backend Soft Delete (5 SP)                    │
│   └─ HU#022 Frontend Dark Mode (5 SP)                     │
│   🎯 Deliverable: Dashboard profesional                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ SPRINT 4 (Week 7-8): INFRASTRUCTURE          50 SP | 400h  │
│   ├─ HU#018 Backend SNS/SQS (8 SP) 🚨 HIGH RISK           │
│   ├─ HU#019 Backend Multi-tenant (13 SP) 🚨 CRITICAL      │
│   ├─ HU#021 Frontend Export CSV/Excel (5 SP)              │
│   ├─ HU#023 Backend Caching Redis (8 SP) ⭐ OPTIONAL      │
│   ├─ HU#024 Tests E2E Cypress (8 SP)                      │
│   └─ HU#025 Frontend Responsive Mobile (8 SP)             │
│   🎯 Deliverable: Production-ready system                 │
│   ⚠️ HU#023 pushes post-launch if time constraint         │
└─────────────────────────────────────────────────────────────┘

TOTAL: 143 SP (130 core + 13 buffer)
```

---

## 👥 EQUIPO RECOMENDADO

```
Backend Developer 1  ████████ 40 SP/sprint capacity
Backend Developer 2  ████████ 40 SP/sprint capacity
Frontend Developer 1 ████████ 40 SP/sprint capacity
Frontend Developer 2 ████████ 40 SP/sprint capacity
QA Tester           ████████ 40 SP/sprint capacity
Architect (0.5 FTE) ████      20 SP/sprint capacity

Total: 200 SP/sprint capacity available
Utilization: 130 SP / 200 SP = 65% (comfortable margin)
```

---

## ⚠️ 🚨 CRITICAL PATH

```
Week 1:  HU#001 (Backend) + HU#002 (Frontend) → MUST COMPLETE
           ↓ Everything depends on these
Week 2:  HU#003, HU#004, HU#005, HU#007
Week 3:  HU#008 (Testing) ⚠️ State machine complexity
Week 5-6: HU#012 (Historial) → Enables HU#016 (Dashboard)
Week 8:  HU#019 (Multi-tenant) 🚨 Refactor critical

BOTTLENECK: Backend capacity in Sprint 4
SOLUTION: Skip HU#023 (Redis), launch post-launch
```

---

## 💰 COST BREAKDOWN

```
Personnel Cost:
  - 2 Backend devs × 8 weeks × 40h × $100/hr = $64,000
  - 2 Frontend devs × 8 weeks × 40h × $90/hr = $57,600
  - 1 QA tester × 8 weeks × 40h × $75/hr = $24,000
  - Architect 0.5 × 8 weeks × 40h × $125/hr = $20,000
  
  TOTAL PERSONNEL: $165,600

Infrastructure Cost:
  - AWS (testing + staging): $2,000
  - Tools (GitHub, Jira, etc.): $1,000
  
  TOTAL INFRA: $3,000

TOTAL PROJECT: $168,600 USD

(Estimated at $90K in execution report is understated - 
 actual is ~$170K with full loaded costs + overhead)
```

---

## 📈 VELOCITY PROGRESSION

```
Sprint 1: 28 SP (baseline)
Sprint 2: 31 SP (+10%)
Sprint 3: 34 SP (+10%)
Sprint 4: 50 SP (+47% due to more parallelization, but HU#023 optional)

ADJUSTED Sprint 4 (skip HU#023): 42 SP
Average: 33.75 SP/sprint (sustainable)
```

---

## ✅ SUCCESS CRITERIA BY PHASE

```
END SPRINT 1:
  ✓ MVP working (create, open/close potreros)
  ✓ Backend 70%+ tested
  ✓ Frontend + Backend communicating
  ✓ Demo with client approved

END SPRINT 2:
  ✓ Full rotation system working
  ✓ Backend 85%+ tested
  ✓ Validations robust
  ✓ Error handling complete

END SPRINT 3:
  ✓ Professional dashboard
  ✓ Audit trail functional
  ✓ Performance baseline set
  ✓ Documentation complete

END SPRINT 4:
  ✓ Multi-tenant validated
  ✓ E2E tests passing
  ✓ Mobile responsive
  ✓ Production ready
  🎉 LAUNCH: March 11, 2026
```

---

## 🎯 DECISIONS MADE

| Decision | Rationale | Impact |
|----------|-----------|--------|
| **Skip HU#023 post-launch** | Capacity constraint + low priority | -$64h, but Redis can be added later |
| **Pair programming on HU#008, #016, #019** | High complexity + quality gate | +24h overhead, but prevents bugs |
| **Frontend + Backend parallel Dev** | Faster delivery | Requires good API contracts (HU#001) |
| **Mock data in HU#003** | Unblocks HU#012 (Historial) | +1 week acceleration |
| **E2E tests in Sprint 4** | Quality gate before production | -$64h from dev, but essential |
| **Responsive mobile last** | Lower priority than core features | -2 weeks but enables launch on schedule |

---

## 🚀 GO/NO-GO CRITERIA

**GO TO SPRINT 2 if:**
- ✅ HU#001 (Backend) 100% complete + tested
- ✅ HU#002 (Frontend) 100% complete + tested
- ✅ Code review approved by architect
- ✅ No critical bugs in Sprint 1

**GO TO SPRINT 3 if:**
- ✅ All P1 historias complete
- ✅ Backend test coverage >= 85%
- ✅ No critical regressions

**GO TO SPRINT 4 if:**
- ✅ Dashboard complete and performant
- ✅ Audit trail working
- ✅ No architectural debt

**GO TO PRODUCTION if:**
- ✅ E2E tests passing
- ✅ Multi-tenant isolation verified
- ✅ Load testing >= 95% passing
- ✅ Security audit complete

---

## 📞 ESCALATION CONTACTS

```
Technical Lead: [Name] - Daily stand-up lead
Architect: [Name] - Technical decisions, design reviews
Product Manager: [Name] - Scope/priority changes
CEO: [Name] - Budget/timeline decisions
Client: [Name] - Demo feedback, UAT scheduling
```

---

## 🗓️ CALENDAR

```
Week 1-2 (Jan 9-22):    Sprint 1 - MVP
Week 3-4 (Jan 23-Feb 5): Sprint 2 - Core
Week 5-6 (Feb 6-19):    Sprint 3 - Dashboard
Week 7-8 (Feb 20-Mar 5): Sprint 4 - Infrastructure

🎉 LAUNCH: Tuesday, March 11, 2026
```

---

**Document Type**: Quick Reference
**Generated**: 2026-01-09
**Audience**: Dev team, PM, Stakeholders
**Refresh Rate**: Weekly (after retro)
