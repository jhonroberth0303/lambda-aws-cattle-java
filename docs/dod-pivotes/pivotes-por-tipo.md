# 🎯 Pivotes por Tipo de Tarea - Proyecto Cattle

**Fecha**: 2026-01-09 | **Versión**: 1.0

## 🎯 Objetivo

Definir los pivotes (checkpoints críticos no negociables) específicos para cada tipo de tarea.

---

## 📋 Tabla de Contenidos

1. [Feature](#feature-nueva-funcionalidad)
2. [Bug](#bug-corrección)
3. [Refactoring](#refactoring)
4. [Testing](#testing)
5. [Documentation](#documentation)
6. [DevOps/Infrastructure](#devopsinfrastructure)
7. [Configuration](#configuration)
8. [Spike/Investigation](#spikeinvestigation)

---

## Feature (Nueva Funcionalidad)

### 🎁 Tipo: Feature Nueva

**Duración típica**: 5-10 días
**Complejidad**: Media-Alta
**Risk**: Media

### Pivotes CRÍTICOS

```
PIVOTE 1: Requirements Claros
═════════════════════════════════
Validación:
├─ User Story tiene acceptance criteria completos
├─ Edge cases identificados
├─ Error scenarios documentados
├─ Non-functional requirements definidos
└─ Mockups/wireframes aprobados (si UI)

Gate: JIRA story tiene AC checklist completo
Entidad: Product Owner
Timing: ANTES de comenzar implementación
Impacto: Sin esto → scope creep garantizado

Evidencia:
├─ JIRA story description: 200+ palabras
├─ AC checklist: mínimo 5 items
├─ Mockups en Figma (si UI)
└─ Signed off por PO en JIRA


PIVOTE 2: Arquitectura Validada
════════════════════════════════
Validación:
├─ Design review completado
├─ APIs definidas (endpoints/parámetros)
├─ Database schema definido
├─ Performance implications identified
└─ Security implications identified

Gate: Tech Lead aprobó arquitectura
Entidad: Tech Lead
Timing: ANTES de code
Impacto: Sin esto → refactor masivo probable

Evidencia:
├─ Architecture diagram (en JIRA)
├─ API spec (OpenAPI/Swagger draft)
├─ DB schema SQL statements
├─ Performance analysis
└─ Security review checklist


PIVOTE 3: Implementación Completa
═════════════════════════════════════
Validación:
├─ TODOS los AC cumplidos
├─ Backend: endpoints funcionan
├─ Frontend: UI completa y funcional
├─ Integration: todo integrado
├─ Error handling: presente
└─ Edge cases: handled

Gate: Funcionalmente completado
Entidad: Developer
Timing: Al finalizar implementación
Impacto: Feature debe ser 100% funcional

Evidencia:
├─ Code compila sin errores
├─ Todos AC checkbox: ✅
├─ Feature demostrable en local
├─ Video demo o screenshots
└─ Testeable en staging


PIVOTE 4: Testing Completo
═══════════════════════════
Validación:
├─ Unit tests: >= 80% coverage
├─ Integration tests: happy path + error paths
├─ E2E tests: user flow end-to-end
├─ Performance tests: baseline establecido
├─ No regressions: test suite anterior pasa

Gate: Coverage >= 80% + tests pasan
Entidad: Developer + QA
Timing: Antes de PR
Impacto: Sin tests → bugs en producción

Evidencia:
├─ Coverage report: 80%+ (screenshot)
├─ Test run log: all tests pass
├─ No @Skip / @Ignore
└─ Coverage diff: shows increase


PIVOTE 5: User Acceptance
════════════════════════════
Validación:
├─ PO probó en staging
├─ Todos AC en checklist: ✅
├─ UI/UX acorde a expectativas
├─ Feedback incorporado
└─ Sign-off en JIRA

Gate: PO aprobó feature
Entidad: Product Owner
Timing: Después de testing, antes de merge
Impacto: Sin esto → incorrect feature

Evidencia:
├─ JIRA comment: "Accepted by PO"
├─ Demo completado ante stakeholders
├─ AC photo evidence
└─ No blocking feedback


PIVOTE 6: Código Limpio
════════════════════════
Validación:
├─ ESLint/Checkstyle: 0 errors, 0 warnings
├─ No secrets en código
├─ Nombres claros (no bId, bovId)
├─ Complejidad <= 10
├─ Cobertura >80%

Gate: npm/mvn lint → SUCCESS
Entidad: CI/CD automático
Timing: En cada PR
Impacto: Sin esto → deuda técnica

Evidencia:
├─ CI/CD green checkmark
├─ SonarQube quality gate: PASS
├─ No code smells flagged
└─ Metrics dashboard: OK


PIVOTE 7: Documentación
═════════════════════════
Validación:
├─ JSDoc/JavaDoc: completo
├─ API documented (Swagger)
├─ README: actualizado
├─ Changelog: entry creada
├─ User guide (si complejo)

Gate: Code review: docs validated
Entidad: Code Reviewer
Timing: Durante PR review
Impacto: Sin esto → otros devs pierden tiempo

Evidencia:
├─ JSDoc con @param/@return/@throws
├─ API spec actualizado
├─ README updated section
├─ CHANGELOG.md entry
└─ Examples en comentarios


PIVOTE 8: Performance OK
════════════════════════════
Validación:
├─ No N+1 queries (si aplica)
├─ Response time acceptable (<100ms P95)
├─ Bundle size: <50KB increase
├─ Memory: no leaks
└─ Lighthouse: >=80 (si UI)

Gate: Performance tests pasan
Entidad: Developer + performance testing
Timing: Antes de merge
Impacto: Sin esto → users ven delays

Evidencia:
├─ Performance benchmark results
├─ Load test report (si crítico)
├─ Lighthouse screenshot (si UI)
├─ Database EXPLAIN PLAN
└─ Memory profiler: no leaks
```

### 🎯 Cómo Completar Feature

**Checklist de Hitos**:

```
Semana 1: Setup + Design
Day 1-2:
  ☐ Lee user story
  ☐ Aclara dudas con PO (preguntas en JIRA)
  ☐ Design review con Tech Lead
  ☐ Crea rama feature/ISSUE-XX-description

Day 3-5:
  ☐ Arquitectura decidida
  ☐ DB schema diseñado
  ☐ APIs definidas
  ☐ Tech Lead: "Approved"

Semana 2: Implementación
Day 6-10:
  ☐ Backend implementado
  ☐ Tests unitarios: escritos
  ☐ Integration tests: escritos
  ☐ Performance baseline: established

Semana 2-3: Finalización
Day 11-12:
  ☐ Frontend integrado
  ☐ E2E tests: escritos
  ☐ Documentation: completa
  ☐ Code review: requested

Day 13:
  ☐ Feedback incorporated
  ☐ QA testing en staging
  ☐ PO acceptance: signed

Day 14:
  ☐ Merge a develop
  ☐ Deploy a staging
  ☐ Final validation
```

---

## Bug (Corrección)

### 🐛 Tipo: Bug Fix

**Duración típica**: 2-5 días
**Complejidad**: Baja-Media
**Risk**: Media-Alta (regressions)

### Pivotes CRÍTICOS

```
PIVOTE 1: Causa Raíz Identificada
═════════════════════════════════════
Validación:
├─ Bug reproducible step-by-step
├─ Causa raíz documentada
├─ No es síntoma, es causa real
├─ Severidad categorizada (P0-P3)
└─ Impacto cuantificado

Gate: Bug analysis completo en JIRA
Entidad: QA + Developer
Timing: ANTES de escribir código
Impacto: Sin esto → fijar síntoma, no causa

Evidencia:
├─ JIRA bug: "Steps to reproduce" completo
├─ Root cause analysis en JIRA
├─ "Production impact: X usuarios" documented
├─ Severity/Priority tagged


PIVOTE 2: Fix Implementado Correctamente
════════════════════════════════════════════
Validación:
├─ Bug no ocurre más (demostrable)
├─ Causa raíz resuelta (no patch superficial)
├─ Solución es limpia (no workaround)
├─ Performance no degradada
└─ Side effects evaluados

Gate: Fix demostrable, bug no reproduce
Entidad: Developer
Timing: Después de implementación
Impacto: Sin esto → bug persiste o empeora

Evidencia:
├─ Video mostrando bug antes/después
├─ Code changes: mínimos y claros
├─ Comments: explican por qué
└─ Manual testing: documented


PIVOTE 3: Regression Test
════════════════════════════
Validación:
├─ Test que reproduce bug (red test)
├─ Test pasa post-fix (green test)
├─ Casos similares: descartados
├─ Related tests: todos pasan
└─ Coverage: no decreases

Gate: Test suite completo pasa
Entidad: Developer + CI/CD
Timing: Antes de PR
Impacto: Sin esto → bug vuelve mañana

Evidencia:
├─ Test name: "should_detect_mastitis_correctly"
├─ Test initially fails (red)
├─ Test passes after fix (green)
├─ No other tests broken
└─ Coverage: equal or better


PIVOTE 4: Validation en Staging
════════════════════════════════════
Validación:
├─ Bug no reproduce en staging
├─ Normal operations OK
├─ Síntomas completamente desaparecidos
├─ Environment != production? OK anyway
└─ QA signed off

Gate: QA approval en JIRA
Entidad: QA Tester
Timing: Después de deploy a staging
Impacto: Sin esto → produção environment sin validar

Evidencia:
├─ JIRA comment: "Verified in staging"
├─ Screenshot showing fix
├─ "No regressions observed"
└─ Tested on multiple browsers (si UI)


PIVOTE 5: Performance Validated
═════════════════════════════════════
Validación (si fix toca performance-sensitive code):
├─ Performance no degradada
├─ Response times igual/mejor
├─ Memory usage igual/mejor
├─ Database load igual/mejor
└─ Benchmark: compared

Gate: Performance metrics OK
Entidad: Developer
Timing: Antes de production deploy
Impacto: Sin esto → fix crea nuevo problema

Evidencia:
├─ Before/after performance graphs
├─ Load test results
├─ Database EXPLAIN PLAN
└─ Memory profiler: no new leaks


PIVOTE 6: Production Ready
════════════════════════════════
Validación:
├─ Código limpio (linting OK)
├─ Tests pasan (100%)
├─ Security validated (no new vulns)
├─ Documentation updated
└─ Rollback plan si crítico

Gate: CI/CD green + ready to deploy
Entidad: Tech Lead
Timing: Antes de merge
Impacto: Sin esto → rollout de riesgo

Evidencia:
├─ CI/CD all green
├─ Security scan: OK
├─ Rollback plan documented (si P0)
└─ Deployment checklist completed
```

### 🎯 Timing por Severidad

```
P0 CRITICAL (System Down)
├─ Target: < 24 hours
├─ SLA: 4h mitigation, 24h permanent fix
├─ Escalation: CTO
├─ Verification: Done immediately
└─ Monitoring: 24/7 post-fix

P1 HIGH (Major Feature Broken)
├─ Target: < 3 days
├─ SLA: 2h confirmation, 24h workaround
├─ Escalation: Tech Lead
├─ Verification: Same day if possible
└─ Monitoring: Enhanced

P2 MEDIUM (Feature Degraded)
├─ Target: < 1 week
├─ SLA: Next sprint
├─ Escalation: None (normal process)
├─ Verification: Normal
└─ Monitoring: Standard

P3 LOW (Minor Issue)
├─ Target: Next sprint
├─ SLA: Backlog
├─ Escalation: None
├─ Verification: Normal
└─ Monitoring: Standard
```

---

## Refactoring

### 🔧 Tipo: Refactoring / Technical Debt

**Duración típica**: 3-7 días
**Complejidad**: Media-Alta
**Risk**: Media (behavior must not change)

### Pivotes CRÍTICOS

```
PIVOTE 1: Justificación Clara
═════════════════════════════════
Validación:
├─ Por qué se refactoriza (not "code is ugly")
├─ Beneficios cuantificables:
│  ├─ Lines of code: 500 → 250 (-50%)
│  ├─ Complexity: 15 → 8 (-46%)
│  ├─ Maintenance: easier by X%
│  └─ Performance: +20% if applicable
├─ Alcance: bien definido (no scope creep)
└─ Timeline: realista

Gate: JIRA story tiene clear justification
Entidad: Product Manager / Tech Lead
Timing: Antes de trabajar
Impacto: Sin esto → refactor innecesario

Evidencia:
├─ JIRA story: "Why" section detailed
├─ Metrics before: documented
├─ Expected metrics after: realistic
└─ Business/Technical value: clear


PIVOTE 2: Behavior Preserving
═════════════════════════════════════
Validación:
├─ Externa: interfaces NO cambian
├─ Interna: implementación puede cambiar
├─ Métodos públicos: signatures igual
├─ Return types: idénticos
├─ Exceptions: idénticas
└─ No breaking changes para clients

Gate: API/behavior contracts preserved
Entidad: Code Reviewer
Timing: Durante PR review
Impacto: Sin esto → breaking changes silenciosos

Evidencia:
├─ Public methods: same signatures
├─ Integration tests: all pass (unchanged)
├─ Client code: no changes needed
└─ Backwards compatible: certified


PIVOTE 3: Tests Pasan Antes y Después
═════════════════════════════════════════
Validación:
├─ Tests PRE-refactor: todos pasan
├─ Tests POST-refactor: todos pasan
├─ Coverage PRE: X%
├─ Coverage POST: >= X%
├─ No tests skipped/disabled
└─ Performance: equal or better

Gate: npm test / mvn test = SUCCESS (both)
Entidad: Developer + CI/CD
Timing: Antes y después refactor
Impacto: Sin esto → behavior silent breaks

Evidencia:
├─ Git history: all tests pass on main
├─ PR: test results before/after
├─ Coverage diff: >= or equal
└─ No @Skip annotations added


PIVOTE 4: Code Quality Improved
════════════════════════════════════
Validación:
├─ Cyclomatic complexity: decreased
├─ Code duplication: decreased
├─ Lines of code: decreased or justified
├─ Readability: improved
├─ Maintainability: improved
└─ SonarQube: quality gate PASS

Gate: SonarQube quality metrics improved
Entidad: CI/CD + Code Review
Timing: Después refactor
Impacto: Sin esto → deuda técnica no reducida

Evidencia:
├─ SonarQube dashboard: metrics better
├─ Code analysis: violations decreased
├─ Duplication report: lower %
└─ Complexity report: lower average


PIVOTE 5: Documentation Updated
═════════════════════════════════════
Validación:
├─ Public API docs: updated if changed
├─ Architecture docs: updated if pattern changed
├─ Code comments: reflect new structure
├─ CHANGELOG: "Refactoring" section
└─ Migration guide: if breaking changes

Gate: Docs reviewed and approved
Entidad: Code Reviewer
Timing: Durante PR review
Impacto: Sin esto → docs out of sync

Evidencia:
├─ JSDoc/JavaDoc: updated
├─ Architecture diagram: updated (if new pattern)
├─ Code comments: clear
├─ CHANGELOG.md: entry added
└─ Migration guide: created (if needed)


PIVOTE 6: Performance Benchmarked
═══════════════════════════════════════
Validación (if performance-critical code):
├─ Benchmark before: run and recorded
├─ Benchmark after: run and recorded
├─ Performance: equal or better
├─ Memory: equal or better
├─ No performance regression
└─ Results: compared and analyzed

Gate: Performance metrics not degraded
Entidad: Developer + Performance testing
Timing: Antes de merge
Impacto: Sin esto → refactor degrada perf

Evidencia:
├─ Benchmark results: before/after
├─ Performance graphs: compared
├─ Load test: passed
└─ Conclusion: "Performance maintained or improved"
```

---

## Testing

### 🧪 Tipo: Testing / Test Suite

**Duración típica**: 3-5 días
**Complejidad**: Media
**Risk**: Baja (no production code)

### Pivotes CRÍTICOS

```
PIVOTE 1: Tests Significativos
═════════════════════════════════
Validación:
├─ Tests no son tautológicos (assert real behavior)
├─ Nombres describen intent (not "test1", "test2")
├─ Casos cubiertos:
│  ├─ Happy path
│  ├─ Error paths
│  ├─ Edge cases
│  ├─ Boundary conditions
│  └─ Null/undefined inputs
├─ Mocks: apropiados (no over-mocking)
└─ Fixtures: reutilizables

Gate: Code review: tests are meaningful
Entidad: Code Reviewer
Timing: Durante PR
Impacto: Sin esto → false security


PIVOTE 2: Coverage >= 75%
═════════════════════════════
Validación:
├─ Line coverage: >= 75%
├─ Branch coverage: >= 75%
├─ Critical paths: 100%
├─ Error paths: 100%
└─ Coverage diff: maintained or increased

Gate: Coverage report: >= 75%
Entidad: CI/CD + Code Review
Timing: En cada commit
Impacto: Sin esto → untested code en prod


PIVOTE 3: Tests Pasan Consistentemente
═══════════════════════════════════════════
Validación:
├─ Determinísticos (not flaky)
├─ Independientes (no state leakage)
├─ Rápidos (< 100ms unit, < 1s integration)
├─ Aislados (mock externos)
└─ Repetibles (mismo resultado siempre)

Gate: npm test / mvn test = SUCCESS
Entidad: CI/CD
Timing: En cada PR
Impacto: Sin esto → false failures


PIVOTE 4: Documentación
═════════════════════════
Validación:
├─ Test names: describe what/why
├─ Comments: explain complex setups
├─ Fixtures: documented
├─ Mocks: explained
└─ Edge cases: justified

Gate: Code review: intent clear
Entidad: Code Reviewer
Timing: Durante PR
Impacto: Sin esto → tests confusos para others
```

---

## Documentation

### 📚 Tipo: Documentation

**Duración típica**: 2-5 días
**Complejidad**: Baja-Media
**Risk**: Baja (content risk, no code)

### Pivotes CRÍTICOS

```
PIVOTE 1: Contenido Correcto
════════════════════════════════
Validación:
├─ Información accurata
├─ Ejemplos funcionan (probados)
├─ Links no están rotos
├─ Reflects current state del código
└─ No contradice otras docs

Gate: 2+ people reviewed and verified
Entidad: Code Reviewer + SME
Timing: Antes de merge
Impacto: Sin esto → misinformation


PIVOTE 2: Estructura Clara
═════════════════════════════
Validación:
├─ Tabla de contenidos: presente
├─ Headings: jerarquía correcta
├─ Párrafos: concisos (<5 líneas)
├─ Code blocks: con syntax highlighting
├─ Listas: bien formateadas
└─ Diagramas: claros (si aplica)

Gate: Formatting review: structure OK
Entidad: Code Reviewer
Timing: PR review
Impacto: Sin esto → docs confusos


PIVOTE 3: Completitud
═══════════════════════
Validación:
├─ Cubre target audience:
│  ├─ Users: end-user guide
│  ├─ Developers: dev setup
│  ├─ Operators: operations guide
│  └─ Admins: admin guide (if applicable)
├─ Ejemplos: suficientes y accionables
└─ FAQs: covered (if complex)

Gate: Doc review: complete per intent
Entidad: Product/Tech Lead
Timing: Before merge
Impacto: Sin esto → incomplete guidance
```

---

## DevOps/Infrastructure

### 🚀 Tipo: DevOps / Infrastructure

**Duración típica**: 3-10 días
**Complejidad**: Alta
**Risk**: Alta (affects availability)

### Pivotes CRÍTICOS

```
PIVOTE 1: Security Validated
════════════════════════════════
Validación:
├─ No secrets expuesto
├─ IAM roles: minimal necessary
├─ Network: restricted (no open to 0.0.0.0)
├─ Encryption: in-transit y at-rest
├─ Audit logging: enabled
└─ Security review: completed

Gate: Security review: PASS
Entidad: Security Team / Tech Lead
Timing: Antes de deploy
Impacto: Critical


PIVOTE 2: Performance Benchmarked
═══════════════════════════════════
Validación:
├─ Load testing: completed
├─ Capacity: planned
├─ Scaling: validated
├─ Database: performance OK
└─ Monitoring: in place

Gate: Load test: PASS
Entidad: DevOps / Performance
Timing: Antes de production
Impacto: Sin esto → outages on traffic spike


PIVOTE 3: Disaster Recovery
═══════════════════════════════
Validación:
├─ Backups: automated and tested
├─ Rollback: plan documented
├─ RTO: defined and achievable
├─ RPO: defined and achievable
└─ DR test: completed successfully

Gate: DR plan: reviewed and approved
Entidad: Tech Lead / DevOps
Timing: Antes de production
Impacto: Sin esto → data loss risk


PIVOTE 4: Monitoring & Alerting
════════════════════════════════════
Validación:
├─ Metrics: key metrics defined
├─ Dashboards: created
├─ Alerts: configured for critical thresholds
├─ Logging: structured logs
└─ On-call: rotation configured

Gate: Monitoring: ready
Entidad: DevOps
Timing: Antes de production
Impacto: Sin esto → incidents undetected
```

---

## Configuration

### ⚙️ Tipo: Configuration Changes

**Duración típica**: 1-3 días
**Complejidad**: Baja
**Risk**: Media (if production)

### Pivotes CRÍTICOS

```
PIVOTE 1: Impact Analyzed
════════════════════════════
Validación:
├─ Qué cambia: documentado
├─ Quién afecta: identificado
├─ Cuándo aplica: timing definido
└─ Rollback: plan ready

Gate: Impact analysis: completed
Entidad: Tech Lead
Timing: Antes de aplicar
Impacto: Sin esto → unexpected consequences


PIVOTE 2: Tested in Staging
════════════════════════════════
Validación:
├─ Configuration: applied in staging
├─ Behavior: verified correct
├─ Side effects: none observed
└─ Performance: OK

Gate: Staging validation: PASS
Entidad: QA / DevOps
Timing: Antes de production
Impacto: Sin esto → configuration errors in prod


PIVOTE 3: Rollback Plan
═════════════════════════
Validación:
├─ Rollback: documented step-by-step
├─ Rollback: tested
├─ Time to rollback: < 5 minutes
└─ Confirmation: can we verify rollback worked?

Gate: Rollback: tested and ready
Entidad: DevOps
Timing: Antes de aplicar
Impacto: Sin esto → stuck in broken state


PIVOTE 4: Monitoring Enhanced
═════════════════════════════════
Validación (if production):
├─ Relevant metrics: being watched
├─ Alerts: configured
├─ Dashboards: updated
└─ Team: aware of change

Gate: Monitoring: in place
Entidad: DevOps
Timing: Simultaneous with change
Impacto: Sin esto → problems undetected
```

---

## Spike/Investigation

### 🔍 Tipo: Spike / Proof of Concept

**Duración típica**: 2-5 días
**Complejidad**: Variable
**Risk**: Low (throwaway code)

### Pivotes CRÍTICOS

```
PIVOTE 1: Research Complete
════════════════════════════════
Validación:
├─ Pregunta respondida
├─ Opciones investigadas
├─ Trade-offs documentados
├─ Recomendación clara
└─ Riescos identificados

Gate: Spike report: complete
Entidad: Developer / Tech Lead
Timing: Cuando se termine spike
Impacto: Sin esto → incompleto


PIVOTE 2: Findings Documented
═════════════════════════════════════
Validación:
├─ Spike report: written (3-5 pages)
├─ Findings: clear conclusions
├─ Evidence: data/screenshots included
├─ Recommendation: actionable
└─ Next steps: defined

Gate: Spike report: reviewed and approved
Entidad: Tech Lead
Timing: Al terminar spike
Impacto: Sin esto → findings lost


PIVOTE 3: No Code Merged (POC remains POC)
═══════════════════════════════════════════════
Validación:
├─ Spike code: branch no merged
├─ Prototype code: not productionized
├─ If production needed: reimplemented with DoD
└─ POC: clear disclaimer que no es production

Gate: Code NOT merged to develop
Entidad: Tech Lead
Timing: Always
Impacto: Critical - prototypes must not go to prod
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
