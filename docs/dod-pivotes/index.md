# ✅ Definition of Done (DoD) - Proyecto Cattle

**Fecha**: 2026-01-09 | **Versión**: 1.0

## 🎯 Objetivo

Establecer criterios claros de aceptación para que una tarea se considere **COMPLETADA** y lista para:
- Integración a rama develop
- Despliegue a producción
- Cierre en jira

---

## 📋 Tabla de Contenidos

1. [DoD Global](#dod-global)
2. [DoD por Tipo de Tarea](#dod-por-tipo-de-tarea)
3. [Pivotes (Checkpoints Críticos)](#pivotes-checkpoints-críticos)
4. [Matriz de Aceptación](#matriz-de-aceptación)
5. [Proceso de Verificación](#proceso-de-verificación)
6. [Excepciones y Waivers](#excepciones-y-waivers)

---

## DoD Global

**Aplica a TODOS los tipos de tareas**

### ✅ Checklist Obligatorio

```
CÓDIGO
☐ Código escrito en rama feature/ISSUE-XX
☐ Sigue estándares del proyecto (ESLint/Checkstyle)
☐ No tiene errores de linting (warnings = fail)
☐ Sin código muerto o comentarios innecesarios
☐ Nombres de variables/funciones en inglés y claros
☐ Máximo complejidad ciclomática = 10
☐ Máximo anidación = 3 niveles
☐ Sin magic numbers (usar constantes)
☐ Sin secrets/passwords en código
☐ Cambios revisados por pair programming o code review

TESTING
☐ Tests unitarios escritos (mínimo 75% coverage)
☐ Tests integración si aplica (75%+ coverage)
☐ Todos los tests pasan (CI/CD green)
☐ Casos de error cubiertos
☐ Edge cases considerados
☐ Mocks/fixtures usados apropiadamente
☐ Performance tests si cambios de perf

DOCUMENTACIÓN
☐ JSDoc/JavaDoc completo (parámetros, retorno, excepciones)
☐ Comentarios en lógica compleja (>5 líneas)
☐ README actualizado si aplica
☐ APIs documentadas (parámetros, ejemplos)
☐ Migración documentada (si cambios BD)
☐ CHANGELOG.md actualizado

GIT & VERSIONADO
☐ Commit messages en Conventional Commits format
☐ Mínimo 2 commits (no single mega-commit)
☐ Historia de git limpia (sin merge commits innecesarios)
☐ PRs con descripción clara (mínimo 100 palabras)
☐ Linked a issue JIRA (#ISSUE-XX)
☐ Todos los comentarios de review resueltos

SEGURIDAD
☐ Validación de inputs (Frontend y Backend)
☐ Autorización verificada (si aplica)
☐ Injection attacks prevención (SQL, JS)
☐ Secrets en variables de entorno
☐ No secrets en .env.example
☐ Passwords hasheados (si aplica)
☐ CORS configurado (si backend)

PERFORMANCE
☐ Sin N+1 queries
☐ Lazy loading implementado (si aplica)
☐ Caching considerado
☐ Bundle size no aumentó >50KB
☐ Time to interactive aceptable (<3s)
☐ Lighthouse score > 80 (si frontend)
☐ Memory leaks descartados

CALIDAD
☐ No tiene TODO/FIXME sin contexto
☐ Errores manejados apropiadamente
☐ Respuestas HTTP apropiadas
☐ Timezones considerados (si aplica)
☐ Localization considerado (i18n)
☐ Accessibility (a11y) si UI
☐ Mobile responsive (si UI)

CONTROL DE CALIDAD
☐ Funcionalmente correcto (tester validation)
☐ Aceptación del usuario (si aplica)
☐ Regresiones descartadas (test suite completo)
☐ Cumple con el user story/acceptance criteria
☐ Demostrable en ambiente de staging
☐ Screenshots/videos si UI changes
```

### 🔍 Verificación

**Quién verifica**: Tech Lead + Code Reviewer
**Cuándo**: Antes de merge a develop
**Evidencia**: PR checklist completado + CI/CD green

---

## DoD por Tipo de Tarea

### 📝 TIPO: Feature (Nueva Funcionalidad)

**Además de DoD Global**:

```
ANÁLISIS
☐ Story tiene aceptación criteria clara
☐ Dependencias identificadas
☐ Impact analysis completado
☐ Estimación realista (story points)
☐ Riesgos documentados

IMPLEMENTACIÓN
☐ Todos los acceptance criteria cumplidos
☐ Backend endpoint implementado y probado
☐ Frontend UI completado y funcional
☐ Integración entre capas validada
☐ APIs contract establecido
☐ Business logic implementada correctamente

TESTING ESPECÍFICO
☐ User flow end-to-end (E2E)
☐ Casos positivos y negativos cubiertos
☐ Boundary conditions probadas
☐ Performance baseline establecido
☐ Load testing (si aplica)

DOCUMENTACIÓN ESPECÍFICA
☐ User guide/tutorial creado (si complejo)
☐ Admin guide (si configuración)
☐ API documentation (OpenAPI/Swagger)
☐ Screenshots en README
☐ Video demo (si UI compleja)

DEMOSTRACIÓN
☐ Feature demostrable a stakeholders
☐ En ambiente staging funcional
☐ Aceptación del PO registrada
☐ Feedback incorporado si aplicable

TIMING
☐ Completada en sprint estimado
☐ Entrega a tiempo (no spillover)
```

**Ejemplo**: Feature "Agregar formulario de bovino"
- ✅ Form component creado + validaciones
- ✅ API endpoint POST /bovineIdentityItems implementado
- ✅ Tests unitarios e integración (80%+ coverage)
- ✅ UI responsive en mobile
- ✅ User can create bovino → demostrado
- ✅ PO acepta

### 🐛 TIPO: Bug (Corrección)

**Además de DoD Global**:

```
ANÁLISIS
☐ Causa raíz identificada
☐ Reproducción documentada
☐ Severidad categorizada
☐ Impacto evaluado

IMPLEMENTACIÓN
☐ Bug fix implementado
☐ Causa raíz resuelta (no patch superficial)
☐ No introduce regressions
☐ Solución testeable

TESTING ESPECÍFICO
☐ Test que reproduce bug (red test primero)
☐ Test pasa post-fix (green)
☐ Casos similares descartados (regression)
☐ Performance no degradada
☐ Ambiente production simulado si es crítico

DOCUMENTACIÓN
☐ Commit message explica causa y solución
☐ Link a bug report incluído
☐ Cambios en comportamiento documentados
☐ Workarounds descartados si existían

VALIDACIÓN
☐ Reproduce en production (si aplica)
☐ Fix validado en staging por QA
☐ Síntomas desaparecidos
☐ Side effects evaluados

TIMING
☐ P0 critical: < 24h
☐ P1 high: < 3 días
☐ P2 medium: < 1 semana
☐ P3 low: próximo sprint
```

**Ejemplo**: Bug "Mastitis no se detecta correctamente"
- ✅ Causa: lógica de comparación con parseInt() en lugar de float
- ✅ Fix: usar parseFloat() + umbral correcto
- ✅ Test: new test que valida detección con 7.2L vs umbral 7.0L
- ✅ Regression: otros tests de detección siguen pasando
- ✅ QA valida: mastitis detectada correctamente en staging

### 🔧 TIPO: Refactoring

**Además de DoD Global**:

```
ANÁLISIS
☐ Razón clara para refactoring
☐ Beneficios cuantificables (perf, mantenibilidad)
☐ Impact analysis completado
☐ Alcance bien definido (no scope creep)
☐ Versioning strategy si breaking changes

IMPLEMENTACIÓN
☐ Comportamiento externo NO cambia
☐ Arquitectura/patrón mejorado
☐ Código más legible/mantenible
☐ Sin cambios funcionales
☐ Métodos private si helpers

TESTING
☐ Tests PRE-refactoring existen
☐ Todos los tests pasan POST-refactoring
☐ Cobertura no disminuye
☐ Performance benchmark (antes/después)
☐ Behavior idéntico validado

DOCUMENTACIÓN
☐ Patrón arquitectónico documentado (si nuevo)
☐ Razón del refactor en commit message
☐ Cambios de API documentados (si aplica)
☐ Migration guide si breaking changes
☐ Deprecation warnings si needed

VALIDACIÓN
☐ Cobertura >= cobertura anterior
☐ Performance mejorada o igual
☐ Complejidad reducida
☐ Duplicación de código reducida
☐ Métricas de código mejoraron

TIMING
☐ Refactor completado sin spillover
☐ Technical debt reducida
```

**Ejemplo**: Refactoring "Simplificar BovinesService"
- ✅ Antes: 500 líneas, complejidad 15
- ✅ Después: 250 líneas, complejidad 8 (todos los tests aún pasan)
- ✅ Beneficio: -50% líneas, -46% complejidad
- ✅ Tests antes/después idénticos
- ✅ Performance: igual o mejor

### 📚 TIPO: Documentación

**Además de DoD Global (pero less rigorous on code)**:

```
CONTENIDO
☐ Documentación clara y accionable
☐ Gramática y spelling correctos
☐ Ejemplos incluidos y funcionales
☐ Diagramas si necesarios (mermaid, plantuml)
☐ Links funcionales (no broken links)
☐ Formato consistente (markdown standard)

ESTRUCTURA
☐ Tabla de contenidos completa
☐ Índice jerárquico correcto
☐ Secciones bien organizadas
☐ Paragraphs concisos (<5 líneas)
☐ Code blocks con syntax highlighting

COMPLETITUD
☐ Para usuarios (end-user docs)
☐ Para desarrolladores (dev docs)
☐ Para operadores (ops docs)
☐ FAQs si aplica
☐ Troubleshooting si aplica

ACTUALIZACIÓN
☐ Reflects current state del código
☐ Versión actualizada
☐ Changelog registrado
☐ Old docs archived (si aplica)
☐ Links internos actualizados

VALIDACIÓN
☐ 2 personas lo revisaron
☐ Ejemplos probados/funcionales
☐ Feedback incorporado
☐ Screenshots/diagrams actualizados
☐ Enlaces validados

RELEVANCIA
☐ Resuelve pain point real
☐ Cubre user stories documentadas
☐ Útil para target audience
☐ Fácil de encontrar (indexable)
☐ SEO-friendly (si docs públicos)
```

**Ejemplo**: Documentación "Guía de Setup del Proyecto"
- ✅ Pasos claros (5 pasos máximo)
- ✅ Ejemplos de output esperado
- ✅ Troubleshooting para errores comunes
- ✅ Probado en machine limpia (verificación)
- ✅ Screenshots del resultado

### 🧪 TIPO: Testing/QA

**Además de DoD Global**:

```
TEST SUITE
☐ Tests significativos (no tautológicos)
☐ Nombres descriptivos (explain intent)
☐ Independientes (no interdependencias)
☐ Determinísticos (no flaky)
☐ Rápidos (<100ms unit, <1s integration)

COVERAGE
☐ Coverage >= 75% (target 85%+)
☐ Critical paths 100%
☐ Branch coverage completado
☐ Edge cases incluidos
☐ Error paths validados

FIXTURES Y MOCKS
☐ Setup claro y documentado
☐ Teardown completo (no state leaks)
☐ Fixtures reutilizables
☐ Mocks apropiados (no over-mocking)
☐ Spy cuando necesario

ASSERTIONS
☐ Específicas (no generic)
☐ Mensajes claros (custom messages)
☐ Comportamiento testeable
☐ No redundantes

MAINTENANCE
☐ Tests pasarán con cambios futuros
☐ No acoplados a implementación
☐ Fáciles de actualizar
☐ Documentación de intención

VALIDACIÓN
☐ Todos los tests pasan
☐ Coverage reports generados
☐ CI/CD valida coverage
☐ Regressions prevented
```

**Ejemplo**: Test Suite "BovineFormValidation"
- ✅ 15 tests (todos pasen)
- ✅ Coverage: 92% (exceeds 75%)
- ✅ Tests: normal inputs, empty fields, invalid dates, edge cases
- ✅ Names: "should_validate_name_required", "should_reject_future_date"

---

## Pivotes (Checkpoints Críticos)

**Estos son GATES que DEBEN cumplirse sin excepción**

### 🚫 Gate 1: Código Limpio (No Negociable)

```
VALIDACIÓN
├─ ESLint/Checkstyle: 0 errors, 0 warnings
├─ SpotBugs/SonarQube: 0 critical bugs
├─ No secrets en código
├─ No commented-out code
└─ No TODO/FIXME sin context

HERRAMIENTAS
├─ npm run lint → SUCCESS
├─ mvn checkstyle:check → BUILD SUCCESS
├─ SpotBugs → 0 HIGH priority
└─ SonarQube → Quality Gate PASS

VERIFICACIÓN: 100% AUTOMÁTICO (CI/CD)
```

### 🧪 Gate 2: Tests Pasan (No Negociable)

```
VALIDACIÓN
├─ npm test → all tests pass
├─ mvn test → all tests pass
├─ Coverage >= 75%
├─ No flaky tests
├─ No skipped tests (sin @skip/@Ignore)
└─ E2E tests (si aplica) pasan

HERRAMIENTAS
├─ Jest coverage report
├─ JaCoCo coverage report
├─ CI/CD logs verdes
└─ No test timeouts

VERIFICACIÓN: 100% AUTOMÁTICO (CI/CD)
```

### 📝 Gate 3: Documentación Mínima (No Negociable)

```
VALIDACIÓN
├─ JSDoc/JavaDoc completo
│   ├─ Todos parámetros documentados
│   ├─ Retorno documentado
│   ├─ Excepciones documentadas
│   └─ Ejemplos cuando complejo
│
├─ Commit messages significativos
│   ├─ Format: type(scope): message
│   ├─ Descripción en body (si necesario)
│   └─ Link a issue (#ISSUE-XX)
│
└─ README actualizado (si aplica)
    ├─ APIs nuevas documentadas
    ├─ Breaking changes flagged
    └─ Migration guide (si aplica)

VERIFICACIÓN: MANUAL (Code Review) + AUTOMÁTICO (linting)
```

### 🔒 Gate 4: Seguridad Mínima (No Negociable)

```
VALIDACIÓN
├─ Validación de inputs presente
│   ├─ Frontend: client-side validation
│   ├─ Backend: server-side validation
│   └─ No confiar en frontend validation
│
├─ Secrets:
│   ├─ No en código
│   ├─ En .env / environment variables
│   ├─ .env.example SIN secrets
│   └─ GitHub secrets si CI/CD
│
├─ Autorización:
│   ├─ Verified (si aplica)
│   ├─ Permisos checkeados
│   └─ Role-based access control
│
└─ Injection prevention:
    ├─ SQL injection: parametrized queries
    ├─ XSS: escaping de outputs
    └─ CSRF: tokens (si formularios)

HERRAMIENTAS
├─ OWASP dependency check
├─ Snyk scan
├─ Bandit (Python) / SpotBugs (Java)
└─ Secrets scanner

VERIFICACIÓN: AUTOMÁTICO (CI/CD) + MANUAL (Security Review)
```

### ✅ Gate 5: User Story Completado (No Negociable)

```
VALIDACIÓN
├─ TODOS los acceptance criteria cumplidos
│   ├─ Functional requirements: YES
│   ├─ Non-functional requirements: YES
│   └─ Edge cases: HANDLED
│
├─ Demostrable:
│   ├─ Feature funciona end-to-end
│   ├─ UI responsive (si aplica)
│   ├─ Error handling visible
│   └─ Staging environment validado
│
└─ Aceptación:
    ├─ Product Owner aprobó
    ├─ QA signed off
    └─ No open blocking issues

VERIFICACIÓN: MANUAL (QA + PO) + DEMO
```

### ⚡ Gate 6: Performance Aceptable (No Negociable)

```
VALIDACIÓN (si cambios de perf)
├─ Backend:
│   ├─ N+1 queries: NONE
│   ├─ Response time < 100ms (P95)
│   ├─ Memory usage: no increase >10%
│   └─ Database size: planned growth only
│
├─ Frontend:
│   ├─ Time to Interactive: < 3s
│   ├─ Largest Contentful Paint: < 2.5s
│   ├─ Bundle size: < 50KB increase
│   └─ Lighthouse score: >= 80
│
└─ Tests:
    ├─ Performance benchmarks: recorded
    ├─ Regression tests: in place
    └─ Load testing: passed (si crítico)

HERRAMIENTAS
├─ Lighthouse
├─ Chrome DevTools
├─ JMeter / k6 (load testing)
├─ GitHub Copilot analysis
└─ Database EXPLAIN PLAN

VERIFICACIÓN: AUTOMÁTICO (perf tests) + MANUAL (profiling)
```

---

## Matriz de Aceptación

### Por Tipo de Tarea vs Pivotes

```
┌──────────────┬──────────────┬──────────┬──────────┬──────────────┬─────────────┐
│ Tipo Tarea   │ Código Limpio│ Tests    │ Docs     │ Seguridad    │ Performance │
├──────────────┼──────────────┼──────────┼──────────┼──────────────┼─────────────┤
│ Feature      │ MUST ✅      │ MUST ✅  │ MUST ✅  │ MUST ✅      │ SHOULD 🟡   │
│ Bug          │ MUST ✅      │ MUST ✅  │ SHOULD 🟡│ MUST ✅      │ SHOULD 🟡   │
│ Refactor     │ MUST ✅      │ MUST ✅  │ SHOULD 🟡│ N/A          │ SHOULD 🟡   │
│ Testing      │ SHOULD 🟡    │ MUST ✅  │ SHOULD 🟡│ N/A          │ N/A         │
│ Docs         │ N/A          │ N/A      │ MUST ✅  │ N/A          │ N/A         │
│ Config       │ MUST ✅      │ MAY 🔵   │ SHOULD 🟡│ MUST ✅      │ N/A         │
│ DevOps       │ MUST ✅      │ SHOULD 🟡│ SHOULD 🟡│ MUST ✅      │ MUST ✅     │
└──────────────┴──────────────┴──────────┴──────────┴──────────────┴─────────────┘

LEYENDA:
✅ MUST: Obligatorio, no negotiable
🟡 SHOULD: Altamente recomendado, justificar si omitido
🔵 MAY: Opcional
N/A: No aplica
```

---

## Proceso de Verificación

### 📋 Checklist Pre-Merge (Developer)

**Antes de crear PR**, checklist personal:

```
☐ Código:
   ☐ Lint limpio (npm/mvn lint)
   ☐ Tests pasan (npm/mvn test)
   ☐ Coverage >= 75%
   ☐ Sin secrets/hardcoded values
   ☐ Nombres claros
   ☐ SonarQube PASS (si conectado)

☐ Git:
   ☐ Commits significativos (no 1 mega-commit)
   ☐ Conventional Commits format
   ☐ Branch name: feature/ISSUE-XX-description
   ☐ Basado en develop (sync)
   ☐ Rebase en develop si necesario

☐ Documentación:
   ☐ JSDoc/JavaDoc completo
   ☐ README actualizado
   ☐ Changelog entry
   ☐ README tiene ejemplos (si nuevo feature)

☐ PR:
   ☐ Descripción significativa (>100 palabras)
   ☐ Screenshots si UI changes
   ☐ Linked a JIRA issue
   ☐ Solicitó reviewers
   ☐ Respondió todos los comentarios

☐ Testing Manual:
   ☐ Función demostrada en local
   ☐ Casos happy path probados
   ☐ Casos error probados
   ☐ Regresiones descartadas
```

### 👥 Checklist Code Review (Reviewer)

**Durante code review**, validar:

```
FUNCIONALIDAD
☐ Acepta criteria cumplidos
☐ Feature/bug logra objetivo
☐ Lógica correcta
☐ Edge cases manejados
☐ No regressions aparentes

CÓDIGO
☐ Sigue estándares (ESLint/Checkstyle)
☐ Names significativos
☐ Complejidad aceptable (<10)
☐ No código muerto
☐ Comentarios claros (si needed)

TESTING
☐ Tests son significativos
☐ Coverage > 75%
☐ Error paths cubiertos
☐ Mocks apropiados
☐ Tests independientes

DOCUMENTACIÓN
☐ JSDoc/JavaDoc completo
☐ Commit messages claros
☐ README actualizado (si aplica)
☐ APIs documentadas (si aplica)

SEGURIDAD
☐ Validación presente
☐ Secrets seguros
☐ Autorización (si aplica)
☐ No injection vulnerabilities

PERFORMANCE
☐ N+1 queries: NO
☐ Lazy loading: SÍ (si aplica)
☐ Bundle size: OK
☐ Caching: considerado

APROBACIÓN
☐ Feedback es constructivo
☐ Sugerencias son alternativas claras
☐ Author respondió comentarios
☐ Todos los comentarios resueltos
```

### ✅ Checklist Pre-Deploy (Tech Lead)

**Antes de merge a develop/production**:

```
CI/CD
☐ Build verde (0 errors)
☐ Tests verdes (todos pasan)
☐ Linting verde (0 violations)
☐ Coverage verde (>75%)
☐ Security scan verde

APROBACIONES
☐ Code Review: ✅ 2 approvals
☐ QA/Tester: ✅ Sign-off
☐ Product Owner: ✅ Acceptance (features)
☐ Tech Lead: ✅ Architecture review
☐ Security: ✅ (si crítico)

GATES
☐ Gate 1: Código limpio → PASS
☐ Gate 2: Tests pasan → PASS
☐ Gate 3: Documentación → PASS
☐ Gate 4: Seguridad → PASS
☐ Gate 5: User story → PASS
☐ Gate 6: Performance → PASS (si aplica)

DOCUMENTACIÓN
☐ Release notes actualizado
☐ Migration guide (si breaking changes)
☐ Deployment instructions (si needed)
☐ Rollback plan (si crítico)

MERGE
☐ Branch rebased en develop (no merge commits)
☐ Commit squashed si needed
☐ Merge type: "Squash and Merge" o "Rebase and Merge"
☐ Branch deletado post-merge
```

---

## Excepciones y Waivers

### 🚨 Cuándo Pedir Waiver

**Excepciones permitidas bajo circunstancias específicas**

```
Escenario: Hotfix Crítico en Producción
├─ Severidad: P0 Critical (sistema down)
├─ Impacto: >100 usuarios afectados
├─ Waiver Posible:
│  ├─ Gate 6 (Performance): testear post-deploy
│  └─ Gate 3 (Docs): documentar después
├─ PERO OBLIGATORIO:
│  ├─ Gates 1-2-4-5: NO EXCEPTION
│  ├─ Tests deben pasar
│  ├─ Security validado
│  └─ Función correcta
└─ Aprobación: CTO + Tech Lead

Escenario: Documentación Interna Urgent
├─ No tiene impacto en código
├─ No tiene tests
├─ PERO OBLIGATORIO:
│  ├─ Contenido correcto
│  ├─ 2 personas lo revisaron
│  └─ Links funcionales
└─ Aprobación: Tech Lead

Escenario: Spike/Prueba de Concepto
├─ Objetivo: investigar viabilidad
├─ NO se merger a develop
├─ Throwaway code: OK
├─ Después: reimplementar con DoD completo
└─ Aprobación: N/A (no merge)

Escenario: Deuda Técnica Crítica
├─ Refactor masivo necesario
├─ Coverage puede bajar temporalmente
├─ PERO:
│  ├─ Tiene plan para recuperar coverage
│  ├─ Performance no degradada
│  └─ Gates 1-2-4-5: SI se cumplen
├─ Timeline: máximo 2 sprints para recuperar
└─ Aprobación: CTO + Product Manager

Waiver NO Permitido Para:
├─ ❌ Saltarse security validation
├─ ❌ Subir código con linting errors
├─ ❌ Tests que no pasan
├─ ❌ Secrets en código
├─ ❌ Feature incompleta (sin AC cumplidos)
└─ ❌ Perf degradation sin justificación
```

### 📋 Proceso Waiver

```
1. SOLICITAR:
   ├─ En PR, comment: "Requesting waiver for Gate X"
   ├─ Justificación clara (3-5 puntos)
   ├─ Impacto analysis
   └─ Plan de remedios

2. EVALUACIÓN:
   ├─ Tech Lead: evalúa riesgo
   ├─ Product Manager: evalúa business impact
   ├─ Security: evalúa si es Gate 4
   └─ Máximo 24h respuesta

3. DECISIÓN:
   ├─ APROBADO: Proceed con waiver
   ├─ DENEGADO: Cumplir DoD normal
   └─ CONDICIONES: Cumplir términos específicos

4. TRACKING:
   ├─ Registrar en JIRA: "Waiver granted for ISSUE-XX"
   ├─ Follow-up task: "Remediate Gate X"
   ├─ Deadline: máximo 2 sprints
   └─ Revisar en retrospective
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0

Ver también:
- [Pivotes por Tipo de Tarea](pivotes-por-tipo.md)
- [Guía de Verificación](guia-verificacion.md)
