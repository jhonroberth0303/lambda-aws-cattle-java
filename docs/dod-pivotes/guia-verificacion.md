# 🔍 Guía de Verificación DoD - Proyecto Cattle

**Fecha**: 2026-01-09 | **Versión**: 1.0

## 🎯 Objetivo

Guía práctica step-by-step para verificar que una tarea cumple Definition of Done.

---

## 📋 Tabla de Contenidos

1. [Pre-Merge Checklist](#pre-merge-checklist)
2. [Code Review Checklist](#code-review-checklist)
3. [QA Verification Checklist](#qa-verification-checklist)
4. [Tech Lead Sign-Off Checklist](#tech-lead-sign-off-checklist)
5. [Automation Checks](#automation-checks)
6. [Manual Verification Guide](#manual-verification-guide)
7. [Evidence Template](#evidence-template)

---

## Pre-Merge Checklist

**Para: Developer** (Antes de crear PR)

### Step 1: Código Limpio

```bash
# 1.1 Lint check
npm run lint          # Frontend
mvn checkstyle:check  # Backend

# ✅ Esperado: 0 errors, 0 warnings

# 1.2 Compilación
npm run build         # Frontend
mvn clean compile     # Backend

# ✅ Esperado: BUILD SUCCESS

# 1.3 Inspect código
- [ ] Variables: camelCase
- [ ] Funciones: clear names
- [ ] Classes: PascalCase (Java)
- [ ] Constants: UPPER_SNAKE_CASE
- [ ] No secrets (grep para AWS_KEY, password, etc.)
- [ ] No console.log en production code
- [ ] No TODO/FIXME sin context

Validación Rápida:
grep -r "password\|secret\|api_key" src/
# ✅ Esperado: No results
```

### Step 2: Tests Pasan

```bash
# 2.1 Run tests
npm test          # Frontend
mvn test          # Backend

# ✅ Esperado: All tests pass, 0 skipped

# 2.2 Coverage report
npm test -- --coverage          # Frontend
mvn jacoco:report               # Backend

# ✅ Esperado: >= 75% coverage

# 2.3 Inspect coverage
- [ ] Critical paths: 100%
- [ ] Error paths: tested
- [ ] Edge cases: covered
- [ ] No tests marked @Skip/@Ignore (except documented)

Acceso:
- Frontend: open coverage/lcov-report/index.html
- Backend: open target/site/jacoco/index.html
```

### Step 3: Functional Verification

```bash
# 3.1 Manual testing en local

UI Features (si frontend):
- [ ] Feature funciona como se espera
- [ ] Casos error manejados (user feedback visible)
- [ ] Mobile responsive (test en browser dev tools)
- [ ] Accessibility (tab navigation, screen reader check)

API Endpoints (si backend):
- [ ] POST/GET/PUT/DELETE funcionan
- [ ] Validación de inputs trabajando
- [ ] Error responses correctas (400, 401, 404, 500)
- [ ] Performance acceptable (<100ms)

Db Changes (si aplica):
- [ ] Schema creado correctamente
- [ ] Migrations executables
- [ ] Rollback funciona
- [ ] No orphaned data

# 3.2 Regresion testing
- [ ] Ejecutó tests de módulos relacionados
- [ ] No rompió funcionalidad anterior
- [ ] App boots correctly
- [ ] No console errors

Verificación Rápida:
npm start              # o java -jar app.jar
# Observar logs: ¿algún ERROR?
# ✅ Esperado: Clean logs, app responsive
```

### Step 4: Documentación Mínima

```bash
# 4.1 JSDoc/JavaDoc completo

Verificación:
for each function:
  - [ ] @param documentado (tipo, descripción)
  - [ ] @return documentado
  - [ ] @throws documentado (si aplica)
  - [ ] Ejemplo en comments (si no obvious)

# Herramienta: ESLint rule "require-jsdoc"
npm run lint          # Debería flagear missing docs

# 4.2 Commit messages
- [ ] Tipo correcto (feat, fix, refactor, etc.)
- [ ] Scope definido: type(scope): message
- [ ] Mensaje descriptivo (>20 caracteres)
- [ ] Linked a issue (#ISSUE-XX)

Verificación:
git log --oneline -5
# ✅ Esperado ejemplo:
#   feat(bovineIdentityItems): add form validation for gender field
#   fix(milkingRecord): correct persistency calculation
#   docs: update README with setup instructions

# 4.3 README actualizado (si aplica)
- [ ] APIs documentadas (si nuevas)
- [ ] Setup instrucciones (si cambios)
- [ ] Breaking changes flagged (si aplica)
- [ ] Examples accionables

Verificación:
cat README.md
# Buscar sección relevante, verificar está updated
```

### Step 5: Security Check

```bash
# 5.1 Secrets scanning
grep -r "password\|secret\|api_key\|AWS_\|DATABASE_" src/
# ✅ Esperado: No matches (or only in config files)

# 5.2 Inspect input validation
Code Review Checklist:
- [ ] User inputs validated (frontend AND backend)
- [ ] No SQL injection possible (parameterized queries)
- [ ] No XSS possible (escaped outputs)
- [ ] Passwords hashed (if applicable)
- [ ] APIs autenticadas (if private)

# 5.3 Environment variables
- [ ] Secrets en .env (not in .env.example)
- [ ] .gitignore: includes .env
- [ ] GitHub secrets configured (if CI/CD)
- [ ] No hardcoded URLs (use env vars)
```

### Step 6: Git & Branching

```bash
# 6.1 Branch setup
- [ ] Branch: feature/ISSUE-XX-description
- [ ] Basado en: develop (git pull origin develop primero)
- [ ] Commits: significativos (no 1 mega-commit)
- [ ] History: limpio (no merge commits innecesarios)

Verificación:
git log --graph --oneline -10 origin/develop..HEAD
# ✅ Esperado: 2-5 logical commits, no merge commits

# 6.2 Pre-PR review
- [ ] Fetch latest from develop
git fetch origin develop
git rebase origin/develop
# (Si hay conflictos, resolver)

- [ ] Tests pasan DESPUÉS de rebase
npm test    o    mvn test

- [ ] Linting limpio DESPUÉS de rebase
npm run lint    o    mvn checkstyle:check
```

### ✅ Pre-Merge Checklist Template

```markdown
## Pre-Merge Self-Review

### Code Quality
- [ ] npm/mvn lint: 0 errors, 0 warnings
- [ ] npm/mvn build: SUCCESS
- [ ] npm/mvn test: all pass, >75% coverage
- [ ] No secrets en código
- [ ] No console.log en production code

### Functionality
- [ ] Feature completado y funcional
- [ ] Casos error manejados
- [ ] Mobile responsive (si UI)
- [ ] Accessibility mínima (si UI)

### Documentation
- [ ] JSDoc/JavaDoc: completo
- [ ] Commit messages: Conventional Commits format
- [ ] README: actualizado (si aplica)
- [ ] No TODO/FIXME sin contexto

### Security
- [ ] Validación de inputs presente
- [ ] No secrets expuesto
- [ ] Passwords hashed (si aplica)

### Git
- [ ] Branch: feature/ISSUE-XX-description
- [ ] Rebase en origin/develop
- [ ] Commits: significativos y limpios
- [ ] Linked a JIRA issue

**Status**: Ready for Code Review ✅
```

---

## Code Review Checklist

**Para: Code Reviewer** (Durante PR)

### Phase 1: Understanding

```bash
# 1.1 Read PR description
- [ ] Description: clara y completa (>100 palabras)
- [ ] Screenshots: incluidos si UI changes
- [ ] JIRA link: presente
- [ ] Soluciona el issue correctamente
- [ ] No scope creep

# 1.2 Entender cambios
git show --name-only
# ¿Qué archivos changed?
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
