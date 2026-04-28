# Guia de Verificacion de Pivotes DoD - Proyecto Cattle

## Objetivo

Dar una secuencia practica para verificar pivotes DoD en cambios de frontend, backend o documentación técnica del proyecto.

Esta guía usa solo validaciones y evidencia que sí son comprobables en el workspace actual.

## Cómo usar esta guía

1. Identificar el tipo de cambio.
2. Tomar los pivotes aplicables desde `pivotes-por-tipo.md`.
3. Ejecutar la verificación mínima de esta guía.
4. Registrar estado por pivote: `cumplido`, `cumplido con observación`, `pendiente` o `exceptuado`.

## Verificación mínima por tipo de repositorio

### Frontend

Comandos o checks preferidos:

```bash
npm run lint
npm run build
```

Si el cambio es documental o fuera del alcance del lint, aceptar como mínimo:

- revisión del diff
- chequeo de errores del editor sobre archivos tocados
- comprobación de que la documentación quedó alineada al código

### Backend

Comandos o checks preferidos:

```bash
gradlew test
gradlew build
gradlew jacocoTestReport
```

Si el cambio es documental o la validación ejecutable no aplica, aceptar como mínimo:

- revisión del diff
- chequeo de errores del editor sobre archivos tocados
- coherencia con `build.gradle`, código y arquitectura documentada

## Pivotes que siempre deben revisarse

### 1. Alcance

Preguntas guía:

- ¿el cambio tiene intención clara?
- ¿se entiende qué problema resuelve?
- ¿el repositorio afectado está identificado?

Evidencia aceptable:

- mensaje de trabajo claro
- documento asociado
- descripción de PR

### 2. Patrón técnico

Preguntas guía:

- ¿el cambio respeta el patrón existente del slice?
- ¿se evitó introducir una arquitectura paralela?

Verificación esperada:

- frontend: dominio, componente, hook o servicio liviano coherente
- backend: capas `Controller -> Processor -> Service -> Repository`

### 3. Validación ejecutable

Preguntas guía:

- ¿se ejecutó la validación más cercana disponible?
- ¿la validación es proporcional al riesgo?

Si no hubo comando ejecutable, debe anotarse por qué.

### 4. Riesgo residual

Preguntas guía:

- ¿queda alguna deuda técnica explícita?
- ¿hay acoplamientos, variables, endpoints o gaps no resueltos?

Debe quedar anotado al menos un resumen corto del riesgo cuando exista.

### 5. Impacto documental

Preguntas guía:

- ¿cambió un endpoint, flujo, contrato, seguridad o configuración?
- ¿eso obliga a actualizar `docs/`?

Si la respuesta es sí, el pivote no debe cerrarse sin revisar documentación.

## Guía operativa por rol

### Para quien implementa

Antes de cerrar el cambio:

1. Confirmar el tipo de cambio.
2. Ejecutar el comando de validación más cercano.
3. Revisar si cambió comportamiento real o documentación derivada.
4. Dejar anotado cualquier pivote exceptuado o pendiente.

### Para quien revisa

Durante review:

1. Confirmar que el patrón técnico es coherente.
2. Verificar que la validación ejecutable o el chequeo equivalente sí ocurrió.
3. Buscar riesgos ocultos no mencionados.
4. Comprobar si faltó actualización documental.

## Plantilla corta de evidencia

Usar este formato cuando convenga dejar trazabilidad breve:

```md
## Pivotes DoD

- Tipo de cambio: <feature|bug|refactor|docs|config|integracion>
- Alcance claro: <cumplido|cumplido con observación|pendiente|exceptuado>
- Patrón técnico: <estado>
- Validación ejecutable: <estado>
- Riesgo residual: <estado>
- Impacto documental: <estado>

### Evidencia
- Validación ejecutada: <comando o check>
- Riesgos abiertos: <si/no + detalle>
- Documentación actualizada: <si/no + archivos>
```

## Cuándo rechazar el cierre

No debería darse por cerrado un cambio si ocurre alguno de estos casos:

- el pivote de validación está pendiente sin justificación
- el patrón técnico del módulo quedó roto sin motivo claro
- se cambió comportamiento real y no se revisó impacto documental
- el riesgo residual es alto y no quedó explicitado
# ¿Alcance es razonable?

# 1.3 Check against AC
- [ ] Todos los acceptance criteria cumplidos
- [ ] No requirements faltantes
- [ ] Edge cases handled
```

### Phase 2: Code Review

```
## Funcionalidad
- [ ] Código logra lo que se propone
- [ ] Lógica es correcta
- [ ] Casos error handled
- [ ] No regressions aparentes

## Calidad de Código
- [ ] Sigue estándares (linting auto, pero manual review también)
- [ ] Nombres claros (no "x", "temp", "data")
- [ ] Complejidad aceptable (<10)
- [ ] No código muerto
- [ ] Comentarios claros (si lógica compleja)
- [ ] No abreviaciones (bovId → bovineId)

## Testing
- [ ] Tests son significativos (no trivial)
- [ ] Nombres descriptivos (explain intent)
- [ ] Happy path + error paths
- [ ] Edge cases cubiertos
- [ ] Mocks apropiados
- [ ] Coverage >= 75%
- [ ] Tests independientes

## Documentación
- [ ] JSDoc/JavaDoc completo
- [ ] Parámetros documentados
- [ ] Return type documentado
- [ ] Excepciones documentadas
- [ ] Commit messages claros
- [ ] README actualizado (si aplica)

## Seguridad
- [ ] Validación de inputs presente
- [ ] No secrets en código
- [ ] Autorización verificada (si aplica)
- [ ] No SQL injection
- [ ] No XSS
- [ ] Passwords hashed (si aplica)

## Performance
- [ ] N+1 queries: NO
- [ ] Lazy loading: SÍ (si aplica)
- [ ] Caching: considerado
- [ ] Memory leaks: none
- [ ] Bundle size: reasonable
```

### Phase 3: Approve or Request Changes

```
APPROVE (only if):
- [ ] Todos los checkpoints: ✅
- [ ] Tests pasan: ✅
- [ ] Linting limpio: ✅
- [ ] AC cumplidos: ✅
- [ ] Feedback anterior: incorporated ✅

REQUEST CHANGES (if):
- [ ] Critical issues: needs fixing
- [ ] Security concerns: must address
- [ ] Major design issues: discuss
- [ ] Coverage < 75%: add tests

COMMENT (suggestions):
- [ ] Nice-to-have improvements
- [ ] Performance suggestions
- [ ] Style/readability suggestions
  (No blocking, but author puede considerar)
```

### ✅ Code Review Template

```markdown
## Code Review

### Understanding ✅
- [x] PR description clear
- [x] JIRA issue linked
- [x] Requirements understood
- [x] Scope appropriate

### Functionality ✅
- [x] Feature complete and works
- [x] Error handling present
- [x] No regressions apparent

### Code Quality ✅
- [x] Follows standards (ESLint/Checkstyle pass)
- [x] Names clear and meaningful
- [x] Complexity acceptable
- [x] No dead code
- [x] Comments clear where needed

### Testing ✅
- [x] Tests meaningful and comprehensive
- [x] Coverage >= 75%
- [x] Both happy and error paths tested
- [x] Edge cases covered

### Documentation ✅
- [x] JSDoc/JavaDoc complete
- [x] Commit messages clear
- [x] README updated (if needed)

### Security ✅
- [x] Input validation present
- [x] No secrets exposed
- [x] Authorization verified

### Performance ✅
- [x] No N+1 queries
- [x] Memory usage reasonable
- [x] Caching considered

**Approved** ✅
```

---

## QA Verification Checklist

**Para: QA Tester** (Después de deployment a staging)

### Phase 1: Setup & Environment

```bash
# 1.1 Verificar ambiente
- [ ] Staging environment: UP
- [ ] Feature branch: deployed
- [ ] Database: schema applied
- [ ] Migrations: executed successfully
- [ ] No deployment errors en logs

# 1.2 Smoke test
- [ ] Application boots
- [ ] Login funciona
- [ ] Navigation works
- [ ] No console errors
```

### Phase 2: Feature Testing

```
## User Story Requirements
Para cada acceptance criterion:
- [ ] Criterio entendido claramente
- [ ] Feature cumple requirement
- [ ] Result visible to user
- [ ] Behavior matches specification

## Happy Path
- [ ] Principal user flow works end-to-end
- [ ] Data saved correctly
- [ ] UI responsive to actions
- [ ] Feedback visible (success messages, etc.)

## Error Scenarios
- [ ] Invalid inputs handled gracefully
- [ ] Error messages clear and helpful
- [ ] User can recover from errors
- [ ] No silent failures

## Edge Cases
- [ ] Boundary conditions tested
- [ ] Empty states handled
- [ ] Null/undefined inputs managed
- [ ] Large data sets handled

## Regression Testing
- [ ] Related features still work
- [ ] No broken functionality
- [ ] Performance not degraded
- [ ] No data loss
- [ ] Migrations reversible

## Cross-Browser (if UI)
- [ ] Chrome: works
- [ ] Firefox: works
- [ ] Safari: works
- [ ] Edge: works

## Mobile (if UI)
- [ ] iPhone: responsive
- [ ] Android: responsive
- [ ] Touch inputs: work
- [ ] Landscape/portrait: both OK

## Accessibility (if UI)
- [ ] Tab navigation: works
- [ ] Screen reader: readable
- [ ] Color contrast: acceptable
- [ ] Forms: labeled correctly
```

### Phase 3: Sign-Off

```
PASS (all tests passed):
- [ ] Feature complete and correct
- [ ] No blocking issues
- [ ] Ready for production

FAIL (issues found):
- [ ] Document issues clearly
- [ ] Steps to reproduce: detailed
- [ ] Expected vs actual: documented
- [ ] Severity: categorized

CONDITIONAL PASS (minor issues):
- [ ] Non-blocking issues found
- [ ] Can go to production with noted issues
- [ ] Follow-up task created
```

### ✅ QA Verification Template

```markdown
## QA Verification - ISSUE-XX

### Environment
- [x] Staging deployed and stable
- [x] Database schema applied
- [x] No deployment errors

### User Story Verification
- [x] AC #1: Feature X works
- [x] AC #2: Feature Y works
- [x] AC #3: Error handling present

### Testing
- [x] Happy path: verified
- [x] Error cases: handled
- [x] Edge cases: tested
- [x] Regressions: none observed

### Browsers/Devices
- [x] Chrome: OK
- [x] Firefox: OK
- [x] Mobile: responsive

### Sign-Off
**Status**: ✅ PASS
**Tester**: [Name]
**Date**: [Date]
```

---

## Tech Lead Sign-Off Checklist

**Para: Tech Lead** (Antes de merge)

### Final Gate

```
GATE 1: Código Limpio
├─ ESLint/Checkstyle: 0 errors, 0 warnings ✅
├─ SonarQube: Quality gate PASS ✅
├─ SpotBugs: No critical bugs ✅
└─ CI/CD: All checks green ✅

GATE 2: Tests Pasan
├─ Unit tests: all pass ✅
├─ Integration tests: all pass ✅
├─ Coverage: >= 75% ✅
└─ No flaky tests ✅

GATE 3: Documentación
├─ JSDoc/JavaDoc: completo ✅
├─ Commit messages: format correcto ✅
├─ README: actualizado ✅
└─ API docs: updated (si aplica) ✅

GATE 4: Seguridad
├─ Validación: presente ✅
├─ Secrets: none en código ✅
├─ Authorization: verified ✅
└─ Security review: passed ✅

GATE 5: User Story
├─ AC cumplidos: todos ✅
├─ Demostrable: funciona ✅
├─ QA approved: ✅
└─ PO signed off: ✅

GATE 6: Performance (si aplica)
├─ N+1 queries: none ✅
├─ Response time: acceptable ✅
├─ Memory: no leaks ✅
└─ Lighthouse: >= 80 (si UI) ✅

DECISIÓN: MERGE o HOLD
```

### Merge Strategy

```bash
# OPCIÓN 1: Squash & Merge (recomendado para features)
# Pro: Historia limpia en develop
# Con: Pierde histórico de commits individuales
git merge --squash feature/ISSUE-XX
git commit -m "feat(bovineIdentityItems): add form validation (#ISSUE-XX)"

# OPCIÓN 2: Rebase & Merge (recomendado para fixes)
# Pro: Historia lineal
# Con: Reescribe historia si conflictos
git rebase origin/develop
git merge --ff-only feature/ISSUE-XX

# OPCIÓN 3: Create Merge Commit (no recomendado)
# Pro: Preserva rama history
# Con: Introduce merge commits innecesarios
git merge --no-ff feature/ISSUE-XX
# ❌ Evitar: crea ruido en history

# POST-MERGE
git push origin develop
git branch -d feature/ISSUE-XX
git push origin --delete feature/ISSUE-XX
```

---

## Automation Checks

**Lo que CI/CD debe validar automáticamente**

### Pre-Commit (local hooks)

```bash
# .git/hooks/pre-commit

# 1. Lint check
npm run lint
if [ $? -ne 0 ]; then
  echo "❌ Linting failed. Please fix errors."
  exit 1
fi

# 2. Test check
npm test
if [ $? -ne 0 ]; then
  echo "❌ Tests failed. Please fix."
  exit 1
fi

# 3. Secrets scan
git diff --cached | grep -E "password|secret|api_key|AWS_KEY"
if [ $? -eq 0 ]; then
  echo "❌ Detected secrets in code. Remove them."
  exit 1
fi

echo "✅ Pre-commit checks passed"
exit 0
```

### CI/CD Pipeline (GitHub Actions / Jenkins)

```yaml
# .github/workflows/ci.yml

name: CI

on: [push, pull_request]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install
        run: npm install
      - name: Lint
        run: npm run lint
      - name: Report
        run: echo "ESLint: ✅ PASS"

  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install
        run: npm install
      - name: Test
        run: npm test -- --coverage
      - name: Coverage
        uses: codecov/codecov-action@v2
      - name: Report
        run: |
          if [ $(cat coverage/coverage-summary.json | grep lines | grep pct | head -1 | cut -d: -f2 | cut -d, -f1 | tr -d ' ."') -lt 75 ]; then
            echo "❌ Coverage < 75%"
            exit 1
          fi

  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Dependency scan
        run: npm audit
      - name: Secrets scan
        uses: gitleaks/gitleaks-action@v1

  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: SonarQube scan
        run: |
          npm install -g sonar-scanner
          sonar-scanner \
            -Dsonar.projectKey=cattle \
            -Dsonar.sources=src \
            -Dsonar.host.url=$SONAR_HOST_URL \
            -Dsonar.login=$SONAR_TOKEN

# PR Status: All checks must be green before merge
```

### Mandatory Checks for Merge

```
✅ Build: PASS
✅ Lint: PASS (0 errors, 0 warnings)
✅ Tests: PASS (100% green)
✅ Coverage: >= 75%
✅ Security Scan: PASS (no vulnerabilities)
✅ Dependency Check: PASS (no unsafe deps)
✅ SonarQube: Quality gate PASS
✅ Code Review: 2 approvals
✅ QA Approval: signed off
✅ Conflicts: none

Bloquea merge si ANY of above falla
```

---

## Manual Verification Guide

### How to Verify Feature Works

```bash
# 1. Checkout branch
git checkout feature/ISSUE-XX

# 2. Install dependencies
npm install    # Frontend
mvn clean install  # Backend

# 3. Start application
npm run dev    # Frontend
java -jar target/cattle-lambda.jar  # Backend

# 4. Test in browser
## For Frontend:
- [ ] Open http://localhost:3000
- [ ] Navigate to feature
- [ ] Click buttons, fill forms
- [ ] Verify data saves
- [ ] Check error messages

## For Backend:
- [ ] Use Postman or curl
curl -X POST http://localhost:8080/bovineIdentityItems \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Estrella",
    "gender": "female",
    "bornDate": "2023-05-10"
  }'
- [ ] Check response: status 201
- [ ] Verify data in database
- [ ] Test validation (empty name, invalid date)
- [ ] Check error response: status 400

# 5. Run tests
npm test       # Frontend
mvn test       # Backend
# ✅ Expected: All pass

# 6. Check coverage
npm test -- --coverage  # Frontend
# ✅ Expected: >= 75%

# 7. Verify no console errors
# Frontend: Open DevTools → Console tab
# Backend: Check logs for ERROR
# ✅ Expected: Clean (no errors)

# 8. Test mobile (if UI)
- Open DevTools (F12)
- Toggle device toolbar (Ctrl+Shift+M)
- Test in iPhone/Android viewport
- Verify responsive

# 9. Test accessibility (if UI)
- Open DevTools → Lighthouse
- Run Accessibility audit
- ✅ Expected: Score >= 80

# 10. Regression testing
- Test features relacionadas
- ✅ Expected: No broken functionality
```

---

## Evidence Template

**Qué documentar cuando completas verificación**

### DoD Verification Evidence

```markdown
## ISSUE-XX: [Feature Name] - DoD Verification

**Verificado por**: [Name]
**Fecha**: [Date]
**Status**: ✅ COMPLETE

### Gate 1: Código Limpio
- ESLint: ✅ 0 errors, 0 warnings
  ```
  [paste screenshot or console output]
  ```
- Linting Summary: ✅ PASS
- Secrets Scan: ✅ No secrets found

### Gate 2: Tests Pasan
- Test Results: ✅ All pass
  ```
  [paste: npm test output]
  ```
- Coverage Report: ✅ 82%
  ```
  [paste: coverage summary]
  ```
- Critical Paths: ✅ 100%

### Gate 3: Documentación
- JSDoc: ✅ Completo
- Commit Messages: ✅ Conventional Commits format
- README: ✅ Actualizado
  ```
  [paste: updated section]
  ```

### Gate 4: Seguridad
- Input Validation: ✅ Present
- Secrets Management: ✅ OK
- Authorization: ✅ Verified

### Gate 5: User Story
- AC #1 (bovino creation): ✅ PASS
  - Steps: Open form → Fill name, gender, date → Click Save
  - Expected: Bovino created in DB
  - Actual: Bovino #47 created successfully
  - Evidence: [screenshot]

- AC #2 (validation): ✅ PASS
  - Steps: Submit empty name → See error
  - Expected: "Name required" message
  - Actual: Error message shown correctly
  - Evidence: [screenshot]

### Gate 6: Performance
- Response Time: ✅ < 100ms
  ```
  [paste: network tab screenshot]
  ```
- Load Test (if applicable): ✅ PASS
- Bundle Size: ✅ No increase

### QA Sign-Off
- **QA Tester**: [Name]
- **Status**: ✅ PASS
- **Testing Date**: [Date]
- **Issues Found**: None blocking

### Tech Lead Sign-Off
- **Tech Lead**: [Name]
- **Status**: ✅ APPROVED FOR MERGE
- **Merge Strategy**: Squash & Merge
- **Expected Deploy**: [Date]

---
**Ready for Production**: ✅ YES
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0

**Próximas acciones**:
- Integrar checklists en JIRA workflow
- Configurar CI/CD checks
- Entrenar al equipo en DoD process
