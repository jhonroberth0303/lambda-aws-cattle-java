# Administracion de Pivotes DoD - Proyecto Cattle

## Objetivo

Definir como se administran los pivotes de Definition of Done para cambios en `cattle-front` y `lambda-aws-cattle-java` usando criterios verificables con el repositorio real.

En esta carpeta, un pivote es un checkpoint de calidad o trazabilidad que debe quedar resuelto antes de dar por cerrado un cambio.

## Evidencia revisada

- `docs/estandares-codigo/index.md`
- `docs/estandares-codigo/frontend-standards.md`
- `docs/estandares-codigo/backend-standards.md`
- `cattle-front/package.json`
- `cattle-front/eslint.config.js`
- `lambda-aws-cattle-java/build.gradle`

## Documentos de esta carpeta

- `guia-verificacion.md`: cómo comprobar pivotes y qué evidencia aceptar
- `pivotes-por-tipo.md`: pivotes mínimos por tipo de cambio
- `plantilla-cierre-historia-pr.md`: plantilla reutilizable para cierre de historia o PR

## Principios de administracion

1. Los pivotes son artefactos vivos, no una lista ceremonial.
2. Un pivote solo cuenta si tiene evidencia verificable.
3. No se exigen gates que el repositorio no demuestra poder ejecutar hoy.
4. Si hay excepción, debe quedar anotado el riesgo y la compensación.
5. Si el cambio modifica comportamiento real, también debe evaluarse el impacto documental.

## Pivotes globales

Estos pivotes aplican a cualquier cambio de código o documentación técnica relevante.

### Pivote 1. Alcance claro

Debe quedar claro:

- qué se cambia
- por qué se cambia
- qué módulo o repositorio toca
- qué riesgo principal introduce

Evidencia válida:

- descripción de PR
- historia o incidente
- documento técnico asociado
- mensaje de trabajo claramente trazable

### Pivote 2. Patrón correcto

El cambio debe respetar el patrón dominante del slice afectado.

Ejemplos:

- frontend por dominio, componente, hook o servicio ligero
- backend por capas `Controller -> Processor -> Service -> Repository`

Evidencia válida:

- diff del cambio
- revisión del archivo tocado
- referencia al estándar aplicable

### Pivote 3. Validación ejecutable

El cambio debe tener al menos una validación proporcional al riesgo.

Validaciones reales observadas en este workspace:

- `npm run lint`
- `npm run build`
- `gradlew test`
- `gradlew build`
- chequeo de errores del editor sobre archivos tocados

Si no existe validación ejecutable razonable, debe dejarse explícito.

### Pivote 4. Riesgos explícitos

Si queda una deuda abierta, debe anotarse junto con su impacto.

Ejemplos válidos:

- endpoint hardcodeado
- ausencia de lint para `ts/tsx`
- documentación legacy aún no saneada
- dependencia de variable de entorno no modelada en SAM

### Pivote 5. Impacto documental

Si el cambio altera contratos, journeys, seguridad, configuración o flujos de negocio, debe verificarse si `docs/` necesita actualización.

## Estado de un pivote

Cada pivote debe marcarse en uno de estos estados:

- `cumplido`
- `cumplido con observación`
- `pendiente`
- `exceptuado`

## Regla para excepciones

Un pivote puede exceptuarse solo si se documenta:

- qué pivote no se cumple
- por qué no se cumple ahora
- qué riesgo deja abierto
- qué acción compensatoria se tomó

## Evidencia mínima esperada

### Frontend

- diff comprensible
- validación con `npm run lint` o justificación si no aplica
- evidencia de que no se mezcló lectura con escritura o summary con CRUD
- actualización documental si cambió navegación, contrato o integración

### Backend

- diff comprensible
- validación con `gradlew test`, `gradlew build` o check razonable equivalente
- errores manejados en la capa correcta
- actualización documental si cambió endpoint, seguridad, integración o proyección

## Flujo sugerido de administración

1. Identificar el tipo de cambio.
2. Ir a `pivotes-por-tipo.md`.
3. Ejecutar la verificación usando `guia-verificacion.md`.
4. Registrar excepciones y riesgos si existen.
5. Cerrar el cambio solo cuando los pivotes críticos estén en `cumplido` o `cumplido con observación` bien justificada.

## Qué no debe hacerse

- exigir Maven, Checkstyle, SonarQube o JIRA como gate automático si el repo no los prueba como flujo activo
- marcar un pivote como cumplido solo por intención verbal
- tratar una checklist heredada como fuente de verdad por encima del código vigente
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
