# 🌱 PASTURES-HU#24: Tests E2E (Cypress/Playwright)

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - E2E Framework + Page Object Model | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **QA engineer o developer**, quiero ejecutar tests end-to-end automatizados para validar flujos completos, de tal forma que:

1. Los tests simulen acciones reales del usuario
2. Se prueben flujos completos: login → crear → editar → eliminar
3. Se valide UI, interacciones, y datos
4. Los tests corran en CI/CD automáticamente
5. Se generen reportes visibles
6. Se ejecuten en múltiples navegadores
7. Sean confiables y no flakey

Esto habilitará que la aplicación sea production-ready con confianza total.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Setup de Cypress
```gherkin
Scenario: Instalar y configurar Cypress
  Given proyecto cattle-front sin E2E tests
  When se ejecuta: npm install --save-dev cypress
  And se crea cypress.config.js
  Then:
    [ ] Cypress instalado correctamente
    [ ] Configuración mínima OK
    [ ] cypress open abre Cypress UI
    [ ] Carpeta cypress/ creada
    [ ] .gitignore actualizado
```

### AC#2: Flujo de Login
```gherkin
Scenario: Tests valida login correcto
  Given usuario en página de login
  When ingresa email y password válidos
  And hace click en "Entrar"
  Then:
    [ ] Redirecciona a dashboard
    [ ] JWT se guarda en localStorage
    [ ] Navbar muestra nombre usuario
    [ ] Sin errores en console
    [ ] Duración < 5 segundos
```

### AC#3: Flujo de Crear Potrero
```gherkin
Scenario: Tests crear nuevo potrero
  Given usuario logged-in en dashboard
  When hace click en "Crear Potrero"
  And completa formulario:
    [ ] Nombre: "Potrero Test"
    [ ] Área: 5.5 ha
    [ ] Estado: DISPONIBLE
  And hace click "Guardar"
  Then:
    [ ] API POST /pastures es llamada
    [ ] Success message aparece
    [ ] Nuevo potrero en lista
    [ ] Pueda buscarlo por nombre
```

### AC#4: Flujo de Editar Potrero
```gherkin
Scenario: Tests editar potrero existente
  Given usuario con potrero creado
  When hace click en "Editar"
  And cambia nombre: "Potrero Editado"
  And hace click "Guardar"
  Then:
    [ ] API PUT /pastures/{id} es llamada
    [ ] Cambio se refleja en lista
    [ ] Historial de cambios visible
    [ ] Sin errores
```

### AC#5: Flujo de Eliminar Potrero
```gherkin
Scenario: Tests soft delete de potrero
  Given usuario con potrero en estado DISPONIBLE
  When hace click en "Eliminar"
  And confirma en modal
  Then:
    [ ] API DELETE es llamada
    [ ] Potrero desaparece de lista
    [ ] Se puede "incluir eliminados" para verlo
    [ ] Status cambió a REMOVED
    [ ] Sin errores
```

### AC#6: Tests de Filtros
```gherkin
Scenario: Tests filtrado de potreros
  Given lista de 10+ potreros con diferentes estados
  When aplica filtro: estado=EN_DESCANSO
  Then:
    [ ] Lista muestra solo EN_DESCANSO
    [ ] Otros estados desaparecen
    [ ] Contador actualizado
    [ ] URL se actualiza con query params
    [ ] Se puede limpiar filtro
```

### AC#7: Tests de Búsqueda
```gherkin
Scenario: Tests búsqueda por nombre
  Given lista de potreros
  When ingresa "Potrero Norte" en search
  And presiona Enter o espera 500ms
  Then:
    [ ] API GET /pastures?search=... es llamada
    [ ] Solo "Potrero Norte" en lista
    [ ] Resaltado el texto buscado
    [ ] Clear button para limpiar búsqueda
```

### AC#8: Tests de Validación
```gherkin
Scenario: Tests validación de formularios
  Given formulario de crear potrero
  When intenta guardar sin nombre
  Then:
    [ ] Error message: "Nombre es requerido"
    [ ] Form no se submit
    [ ] Focus en campo inválido
    [ ] Sin llamadas a API
```

### AC#9: Tests de Autenticación
```gherkin
Scenario: Tests requiere autenticación
  Given usuario NO logged-in
  When intenta acceder /dashboard
  Then:
    [ ] Redirecciona a /login
    [ ] Token expirado: redirecciona a login
    [ ] Sin datos sensibles expuestos
    [ ] Refresh token funciona (si existe)
```

### AC#10: Tests de Errores
```gherkin
Scenario: Tests maneja errores de API
  Given API retorna 500 error
  When usuario intenta crear potrero
  Then:
    [ ] Error message visible
    [ ] User puede retry
    [ ] Sin datos corruptos
    [ ] Logs contienen error
```

### AC#11: Tests Responsive
```gherkin
Scenario: Tests en múltiples tamaños de pantalla
  Given tests ejecutándose
  When se ejecutan en:
    [ ] Desktop (1920x1080)
    [ ] Tablet (768x1024)
    [ ] Móvil (375x667)
  Then:
    [ ] UI se adapta correctamente
    [ ] Botones clickeables
    [ ] Sin overflow
    [ ] Touch gestures funcionan
```

### AC#12: Tests de Múltiples Navegadores
```gherkin
Scenario: Tests en Chrome, Firefox, Edge
  Given suite de tests E2E
  When se ejecutan con --headed o CI
  Then:
    [ ] Chrome: todos pasan
    [ ] Firefox: todos pasan
    [ ] Edge: todos pasan
    [ ] Safari: opcional (Mac required)
```

### AC#13: Videos y Screenshots
```gherkin
Scenario: Capturar evidence de tests
  Given tests ejecutándose
  When test falla
  Then:
    [ ] Video del test guardado
    [ ] Screenshot en momento de fallo
    [ ] Ubicación: cypress/videos/, cypress/screenshots/
    [ ] Útil para debugging
    [ ] Limpiarse en CI (opcional)
```

### AC#14: CI/CD Integration
```gherkin
Scenario: Tests ejecutan en GitHub Actions
  Given repository con tests
  When se hace push a rama
  Then:
    [ ] GitHub Actions inicia
    [ ] Tests E2E se ejecutan
    [ ] Reporting muestra resultados
    [ ] Falla la PR si tests fallan
    [ ] Pasa si tests OK
```

### AC#15: Reportes HTML
```gherkin
Scenario: Generar reporte HTML de tests
  Given tests completados
  When mireport=true en config
  Then:
    [ ] Reporte HTML generado
    [ ] Abre en navegador automáticamente
    [ ] Muestra results por test
    [ ] Muestra duración
    [ ] Incluye screenshots de fallos
```

---

## 📊 **Especificación Técnica**

### Instalación de Cypress

#### package.json
```json
{
  "devDependencies": {
    "cypress": "^13.6.0",
    "cypress-real-events": "^1.13.0",
    "@cypress/code-coverage": "^2.2.0"
  },
  "scripts": {
    "cypress:open": "cypress open",
    "cypress:run": "cypress run",
    "cypress:run:headed": "cypress run --headed",
    "cypress:run:chrome": "cypress run --browser chrome",
    "cypress:record": "cypress run --record --key <your-key>"
  }
}
```

### Configuración Cypress

#### cypress.config.js
```javascript
const { defineConfig } = require('cypress');

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://localhost:5173',
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    setupNodeEvents(on, config) {
      // Setup plugins si es necesario
    },
    specPattern: 'cypress/e2e/**/*.cy.js',
    supportFile: 'cypress/support/e2e.js',
  },
  component: {
    devServer: {
      framework: 'react',
      bundler: 'vite',
    },
  },
});
```

### Test de Login

#### cypress/e2e/auth/login.cy.js
```javascript
describe('Authentication - Login', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  it('should login with valid credentials', () => {
    // Arrange
    const email = 'test@example.com';
    const password = 'Password123!';

    // Act
    cy.get('input[name="email"]').type(email);
    cy.get('input[name="password"]').type(password);
    cy.get('button[type="submit"]').click();

    // Assert
    cy.url().should('include', '/dashboard');
    cy.get('.navbar-user-name').should('contain', 'Test User');
    cy.localStorage('authToken').should('exist');
  });

  it('should show error with invalid credentials', () => {
    // Arrange
    const email = 'invalid@example.com';
    const password = 'wrongpassword';

    // Act
    cy.get('input[name="email"]').type(email);
    cy.get('input[name="password"]').type(password);
    cy.get('button[type="submit"]').click();

    // Assert
    cy.get('.error-message').should('be.visible');
    cy.get('.error-message').should('contain', 'Credenciales inválidas');
    cy.url().should('include', '/login');
  });

  it('should require email field', () => {
    // Act
    cy.get('button[type="submit"]').click();

    // Assert
    cy.get('input[name="email"]:invalid').should('have.length', 1);
  });
});
```

### Test de Crear Potrero

#### cypress/e2e/pastures/create.cy.js
```javascript
describe('Pastures - Create', () => {
  beforeEach(() => {
    cy.login('test@example.com', 'Password123!');
    cy.visit('/dashboard/pastures');
  });

  it('should create pasture with valid data', () => {
    // Arrange
    const pastureName = `Potrero Test ${Date.now()}`;

    // Act
    cy.get('button:contains("Crear Potrero")').click();
    cy.get('input[name="name"]').type(pastureName);
    cy.get('input[name="areHa"]').type('5.5');
    cy.get('select[name="status"]').select('DISPONIBLE');
    cy.get('button[type="submit"]').click();

    // Assert
    cy.get('.success-message').should('be.visible');
    cy.get('.success-message').should('contain', 'Potrero creado exitosamente');
    cy.get('table tbody').should('contain', pastureName);
    
    // Verify API call
    cy.intercept('POST', '/api/pastures', (req) => {
      expect(req.body.name).to.equal(pastureName);
      expect(req.body.areHa).to.equal(5.5);
    }).as('createPasture');
    cy.wait('@createPasture');
  });

  it('should validate required fields', () => {
    // Act
    cy.get('button:contains("Crear Potrero")').click();
    cy.get('button[type="submit"]').click();

    // Assert
    cy.get('.field-error').should('have.length.greaterThan', 0);
    cy.get('input[name="name"]').should('have.class', 'is-invalid');
  });

  it('should validate area must be > 0', () => {
    // Act
    cy.get('button:contains("Crear Potrero")').click();
    cy.get('input[name="name"]').type('Test Pasture');
    cy.get('input[name="areHa"]').type('0');
    cy.get('button[type="submit"]').click();

    // Assert
    cy.get('.field-error').should('contain', 'Área debe ser mayor a 0');
  });
});
```

### Test de Filtros

#### cypress/e2e/pastures/filtering.cy.js
```javascript
describe('Pastures - Filtering', () => {
  beforeEach(() => {
    cy.login('test@example.com', 'Password123!');
    cy.visit('/dashboard/pastures');
  });

  it('should filter pastures by status', () => {
    // Act
    cy.get('select[name="filterStatus"]').select('EN_DESCANSO');

    // Assert
    cy.get('table tbody tr').each(($row) => {
      cy.wrap($row).should('contain', 'En descanso');
    });

    // URL should reflect filter
    cy.url().should('include', 'status=EN_DESCANSO');
  });

  it('should search by name', () => {
    // Act
    cy.get('input[name="search"]').type('Norte');
    cy.get('button:contains("Buscar")').click();

    // Assert
    cy.get('table tbody tr').should('contain', 'Potrero Norte');
    cy.get('table tbody').then(($tbody) => {
      // Should only contain rows with "Norte"
      cy.wrap($tbody).find('tr').should('have.length', 1);
    });
  });

  it('should reset filters', () => {
    // Arrange
    cy.get('select[name="filterStatus"]').select('EN_USO');
    
    // Act
    cy.get('button:contains("Limpiar Filtros")').click();

    // Assert
    cy.get('select[name="filterStatus"]').should('have.value', '');
    cy.url().should('not.include', 'status=');
  });
});
```

### Comandos Personalizados

#### cypress/support/commands.js
```javascript
// Login command
Cypress.Commands.add('login', (email, password) => {
  cy.visit('/login');
  cy.get('input[name="email"]').type(email);
  cy.get('input[name="password"]').type(password);
  cy.get('button[type="submit"]').click();
  cy.url().should('include', '/dashboard');
  cy.get('.navbar-user-name').should('exist');
});

// Crear potrero
Cypress.Commands.add('createPasture', (pastureData) => {
  cy.get('button:contains("Crear Potrero")').click();
  cy.get('input[name="name"]').type(pastureData.name);
  cy.get('input[name="areHa"]').type(pastureData.areHa);
  cy.get('select[name="status"]').select(pastureData.status);
  cy.get('button[type="submit"]').click();
  cy.get('.success-message').should('be.visible');
});

// API mock
Cypress.Commands.add('mockApi', (method, url, response) => {
  cy.intercept(method, url, {
    statusCode: 200,
    body: response,
  });
});
```

#### cypress/support/e2e.js
```javascript
import './commands';

// Configuración global
beforeEach(() => {
  // Clear localStorage
  cy.window().then((win) => {
    win.localStorage.clear();
  });
});

afterEach(() => {
  // Check for console errors
  cy.window().then((win) => {
    const errors = [];
    win.addEventListener('error', (e) => {
      errors.push(e.message);
    });
    
    if (errors.length > 0) {
      cy.log('Console errors detected:', errors);
    }
  });
});
```

### GitHub Actions CI/CD

#### .github/workflows/e2e.yml
```yaml
name: E2E Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  cypress:
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        node-version: [18.x]
        browser: [chrome, firefox]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: ${{ matrix.node-version }}
        cache: 'npm'
    
    - name: Install dependencies
      run: npm ci
    
    - name: Start dev server
      run: npm run dev &
      env:
        VITE_API_URL: http://localhost:3000
    
    - name: Wait for server
      run: npx wait-on http://localhost:5173 --timeout 30000
    
    - name: Run Cypress tests
      uses: cypress-io/github-action@v6
      with:
        browser: ${{ matrix.browser }}
        spec: cypress/e2e/**/*.cy.js
        record: false
    
    - name: Upload screenshots on failure
      if: failure()
      uses: actions/upload-artifact@v3
      with:
        name: cypress-screenshots-${{ matrix.browser }}
        path: cypress/screenshots
    
    - name: Upload videos
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: cypress-videos-${{ matrix.browser }}
        path: cypress/videos

  publish-results:
    needs: cypress
    runs-on: ubuntu-latest
    if: always()
    
    steps:
    - name: Publish test results
      uses: EnricoMi/publish-unit-test-result-action@v2
      if: always()
      with:
        files: results/junit.xml
        check_name: E2E Test Results
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`cypress.config.js`** - Configuración
2. **`cypress/e2e/**/*.cy.js`** - Tests (múltiples)
3. **`cypress/support/commands.js`** - Comandos
4. **`cypress/support/e2e.js`** - Setup
5. **`.github/workflows/e2e.yml`** - CI/CD

### Archivos a Modificar

1. **`package.json`** - Scripts de Cypress
2. **`.gitignore`** - Agregar cypress/
3. **Backend** - Posiblemente datos de test

---

## 🔧 **Refinamiento Técnico**

### Cypress Setup

```javascript
// cypress.config.js
module.exports = {
  e2e: {
    baseUrl: 'http://localhost:3000',
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    setupNodeEvents(on, config) {}
  }
};
```

### Page Object Model

```javascript
// cypress/support/pages/LoginPage.js
export class LoginPage {
  visit() { cy.visit('/login'); }
  fillEmail(email) { cy.get('[name="email"]').type(email); }
  fillPassword(pwd) { cy.get('[name="password"]').type(pwd); }
  clickLogin() { cy.get('button:contains("Entrar")').click(); }
}

// cypress/support/pages/PasturesPage.js
export class PasturesPage {
  visit() { cy.visit('/pastures'); }
  createPasture(data) { /* ... */ }
  editPasture(id, data) { /* ... */ }
  deletePasture(id) { /* ... */ }
}
```

### E2E Test Suite

```javascript
// cypress/e2e/auth.spec.js
describe('Authentication', () => {
  it('should login successfully', () => {
    const login = new LoginPage();
    login.visit();
    login.fillEmail('user@farm.com');
    login.fillPassword('password');
    login.clickLogin();
    cy.url().should('include', '/dashboard');
  });
});

// cypress/e2e/pastures.spec.js
describe('Pastures CRUD', () => {
  it('should create pasture', () => {
    const page = new PasturesPage();
    page.visit();
    page.createPasture({
      name: 'Test Pasture',
      areHa: 5.5
    });
    cy.get('[data-testid="success-toast"]')
      .should('contain', 'Creado');
  });
});
```

### CI/CD Integration

```yaml
# .github/workflows/e2e.yml
name: E2E Tests
on: [push]
jobs:
  cypress:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: cypress-io/github-action@v2
        with:
          start: npm run dev
          browser: chrome
```

### Testing Strategy

**Test Coverage:**
- Happy path (login, create, update, delete)
- Error scenarios (validation, 404, server error)
- Multi-browser (Chrome, Firefox)
- Screenshots/videos on failure

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Cypress instalado
- [ ] Config básica OK
- [ ] Tests de login funciona
- [ ] Tests de CRUD funciona
- [ ] Tests de filtros funciona
- [ ] Tests de validación funciona
- [ ] Tests de errores funciona
- [ ] Comandos personalizados
- [ ] Responsive tests
- [ ] Multi-browser tests
- [ ] Videos y screenshots
- [ ] CI/CD integrado
- [ ] Reportes HTML

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** End-to-End Testing Framework + Page Object Model + CI/CD Integration

**Justificación:** **E2E Testing**: Flujos completos usuario real. **Page Object Model**: Estructura mantenible. **CI/CD**: Automático. **Multi-browser**: Chrome, Firefox, Edge. **Reportes**: Visuales. **Confiables**: Tests determinísticos.

**Componentes Afectados:**

- **cypress.config.js (Nuevo):** Configuración Cypress. BaseUrl, timeouts, viewportSize. Screenshots/videos on fail.

- **pages/LoginPage.js (Nuevo):** Page Object. Métodos: `visit()`, `fillEmail()`, `fillPassword()`, `clickLogin()`.

- **pages/PasturesPage.js (Nuevo):** Page Object. Métodos: `createPasture()`, `editPasture()`, `deletePasture()`, `filterByStatus()`.

- **tests/auth.spec.js (Nuevo):** Tests login. Describe: "Authentication". It: "should login", "should logout", "should refresh token".

- **tests/crud.spec.js (Nuevo):** Tests CRUD. Describe: "Pastures". It: "create", "read", "update", "delete".

- **tests/e2e.spec.js (Nuevo):** Tests flujos completos. Complete user journey.

**Hitos:**
1. cypress.config.js (setup)
2. pages/LoginPage.js + pages/PasturesPage.js (POM)
3. tests/auth.spec.js (auth tests)
4. tests/crud.spec.js (CRUD tests)
5. .github/workflows/e2e.yml (CI/CD)

### Validación de Impacto

✅ **E2E Coverage**: Todos flujos principales
✅ **Maintainability**: Page Object Model
✅ **CI/CD**: Automático en cada push
✅ **Multi-browser**: Chrome + Firefox
✅ **Reportes**: Screenshots + videos on fail

### Referencias y Validación

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** E2E testing + Page Object Model

---

## ✅ **Definición de Completado**
- [ ] Mínimo 20 tests
- [ ] Cobertura principal flows
- [ ] Code review aprobado
- [ ] CI/CD green

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
