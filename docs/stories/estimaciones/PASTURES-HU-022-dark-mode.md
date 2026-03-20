# 🌱 PASTURES-HU#22: Frontend: Modo Oscuro (Dark Theme)

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Theme Provider + System Detection | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **usuario**, quiero poder usar la aplicación en modo oscuro, de tal forma que:

1. La interfaz sea cómoda de usar en ambientes de baja luz
2. Se reduzca el consumo de batería en dispositivos OLED
3. La preferencia se guarde localmente
4. Se respete la preferencia del sistema operativo
5. Pueda cambiar tema en cualquier momento
6. El tema oscuro sea profesional y accesible
7. Todos los componentes soporten modo oscuro

Esto habilitará que usuarios nocturnos y dispositivos móviles funcionen cómodamente.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Toggle de Tema
```gherkin
Scenario: Usuario puede cambiar de tema
  Given aplicación cargada en modo claro
  When usuario hace click en botón de tema (☀️/🌙)
  Then:
    [ ] Interfaz cambia a modo oscuro
    [ ] Todas las colores se invierten
    [ ] Contraste es legible
    [ ] Sin parpadeos/flasheos
    [ ] Transición suave (0.3s)
```

### AC#2: Detectar Preferencia del Sistema
```gherkin
Scenario: App respeta dark mode del SO
  Given usuario con dark mode activado en SO
  When abre la aplicación por primera vez
  Then:
    [ ] App carga en modo oscuro automáticamente
    [ ] Sin necesidad de configuración manual
    [ ] En Windows/Mac/Linux/iOS/Android
    [ ] Se respeta prefers-color-scheme
```

### AC#3: Persistencia Local
```gherkin
Scenario: Preferencia se guarda localmente
  Given usuario cambia a modo oscuro
  When cierra y vuelve a abrir la app
  Then:
    [ ] La app recuerda preferencia (dark mode)
    [ ] Guardado en localStorage
    [ ] Clave: 'theme' o 'darkMode'
    [ ] Sin necesidad de login
    [ ] Persiste entre sesiones
```

### AC#4: Colores Actualizados
```gherkin
Scenario: Todos los colores en modo oscuro
  Given tema oscuro activado
  When se visualiza cualquier página
  Then:
    [ ] Fondo: #1e1e1e o similar (oscuro)
    [ ] Texto: #e0e0e0 o blanco (claro)
    [ ] Bordes: gris oscuro
    [ ] Inputs: #2d2d2d
    [ ] Buttons: tonos saturados
    [ ] Sin colores muy claros que cieguen
    [ ] Contraste >= 4.5:1 (WCAG)
```

### AC#5: Componentes Soportan Tema
```gherkin
Scenario: Todos los componentes responden a tema
  Given aplicación completa
  When cambio de tema
  Then se actualizan:
    [ ] Navbar
    [ ] Sidebar
    [ ] Cards
    [ ] Tables
    [ ] Forms
    [ ] Buttons
    [ ] Alerts
    [ ] Modals
    [ ] Tooltips
    [ ] Dropdowns
    [ ] Badges
```

### AC#6: Contraste Accesible
```gherkin
Scenario: Modo oscuro cumple WCAG AA
  Given componentes en dark mode
  Then:
    [ ] Contraste texto/fondo >= 4.5:1 (normal)
    [ ] Contraste >= 3:1 (large text)
    [ ] Validar con axe, WAVE, etc
    [ ] No depender solo de color
    [ ] Accesible para daltónicos
```

### AC#7: Imágenes y Logos
```gherkin
Scenario: Imágenes legibles en dark mode
  Given logos e imágenes en app
  When modo oscuro activado
  Then:
    [ ] Logos ajustan (white/dark version)
    [ ] Imágenes con fondo se ven bien
    [ ] Sin imágenes blancas sobre fondo oscuro
    [ ] Iconos visibles
    [ ] SVG recoloreados si aplica
```

### AC#8: Modo Oscuro en Gráficos
```gherkin
Scenario: Gráficos se adaptan a tema
  Given gráficos (Recharts, etc)
  When cambio a modo oscuro
  Then:
    [ ] Colores de barras se invierten
    [ ] Eje Y/X legibles
    [ ] Leyenda visible
    [ ] Tooltip con fondo correcto
    [ ] Líneas del grid visibles
```

### AC#9: Transición Suave
```gherkin
Scenario: Cambio de tema sin parpadeos
  Given usuario hace click en toggle
  When aplicación cambia de tema
  Then:
    [ ] Transición suave (max 0.3s)
    [ ] Sin flash blanco/negro
    [ ] Sin reposicionamientos
    [ ] Scroll position se conserva
    [ ] Focus se mantiene
```

### AC#10: Almacenamiento en BD (Bonus)
```gherkin
Scenario: Preferencia guardada en cuenta
  Given usuario autenticado
  When cambia a modo oscuro
  Then:
    [ ] Preferencia se guarda en user profile
    [ ] Se sincroniza entre dispositivos
    [ ] API: PATCH /users/me/preferences
    [ ] Campo: preferredTheme
    [ ] Sin datos personales sensibles
```

### AC#11: CSS Variables para Temas
```gherkin
Scenario: Usar CSS variables para flexibilidad
  Given proyecto con muchos colores
  Then implementar:
    [ ] --bg-primary: #fff / #1e1e1e
    [ ] --bg-secondary: #f5f5f5 / #2d2d2d
    [ ] --text-primary: #000 / #fff
    [ ] --text-secondary: #666 / #aaa
    [ ] --border-color: #ddd / #444
    [ ] Fácil de extender
    [ ] Usado en Tailwind/CSS-in-JS
```

### AC#12: Sistema Completo de Colores
```gherkin
Scenario: Definir paleta completa
  Given sistema de diseño
  Then tener:
    [ ] Colores primarios
    [ ] Colores secundarios
    [ ] Estados (hover, active, disabled)
    [ ] Sombras (depth en dark mode)
    [ ] Transparencias
    [ ] Degradados (si aplica)
    [ ] Consistente en ambos temas
```

### AC#13: Móvil Responsive
```gherkin
Scenario: Dark mode funciona en móvil
  Given app en dispositivo móvil
  When cambio de tema
  Then:
    [ ] Toggle accesible (button grande)
    [ ] Tema se aplica correctamente
    [ ] Sin problemas de rendering
    [ ] Performance no se afecta
    [ ] Batería no se degrada
```

### AC#14: Testing
```gherkin
Scenario: Tests verifica dark mode
  Given suite de tests
  Then:
    [ ] Test: toggle cambia tema
    [ ] Test: localStorage persiste
    [ ] Test: SO preference se respeta
    [ ] Test: contraste WCAG ok
    [ ] Test: componentes se colorean
    [ ] Cobertura >= 80%
```

### AC#15: Documentación
```gherkin
Scenario: Documentar implementación
  Given dark mode completamente
  Then:
    [ ] Guía de cómo agregar nuevos componentes
    [ ] Variables CSS documentadas
    [ ] Paleta de colores
    [ ] Testing guide
    [ ] Troubleshooting
```

---

## 📊 **Especificación Técnica**

### CSS Variables

#### themes.css
```css
:root {
  /* Light Theme (Default) */
  --bg-primary: #ffffff;
  --bg-secondary: #f5f5f5;
  --bg-tertiary: #efefef;
  
  --text-primary: #000000;
  --text-secondary: #666666;
  --text-tertiary: #999999;
  
  --border-color: #dddddd;
  --border-light: #eeeeee;
  
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
  
  --color-primary: #0066cc;
  --color-primary-hover: #0052a3;
  --color-success: #10b981;
  --color-warning: #f59e0b;
  --color-danger: #ef4444;
  --color-info: #3b82f6;
}

html[data-theme="dark"] {
  /* Dark Theme */
  --bg-primary: #1e1e1e;
  --bg-secondary: #2d2d2d;
  --bg-tertiary: #3a3a3a;
  
  --text-primary: #e0e0e0;
  --text-secondary: #a0a0a0;
  --text-tertiary: #757575;
  
  --border-color: #444444;
  --border-light: #333333;
  
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.5);
  
  --color-primary: #4a9eff;
  --color-primary-hover: #3a8eef;
  --color-success: #34d399;
  --color-warning: #fbbf24;
  --color-danger: #f87171;
  --color-info: #60a5fa;
}

html[data-theme="dark"] {
  color-scheme: dark;
}

html[data-theme="light"] {
  color-scheme: light;
}
```

### ThemeProvider Context

#### ThemeContext.js
```javascript
import React, { createContext, useContext, useEffect, useState } from 'react';

const ThemeContext = createContext();

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');
  const [isLoading, setIsLoading] = useState(true);
  
  // Cargar preferencia al montar
  useEffect(() => {
    // 1. Verificar localStorage
    const stored = localStorage.getItem('theme');
    if (stored) {
      setTheme(stored);
      applyTheme(stored);
      setIsLoading(false);
      return;
    }
    
    // 2. Verificar preferencia del SO
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      setTheme('dark');
      applyTheme('dark');
    }
    
    setIsLoading(false);
  }, []);
  
  // Escuchar cambios de preferencia del SO
  useEffect(() => {
    if (!window.matchMedia) return;
    
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    const handleChange = (e) => {
      const newTheme = e.matches ? 'dark' : 'light';
      if (!localStorage.getItem('theme')) {
        setTheme(newTheme);
        applyTheme(newTheme);
      }
    };
    
    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);
  
  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    applyTheme(newTheme);
    localStorage.setItem('theme', newTheme);
  };
  
  const setThemeMode = (newTheme) => {
    setTheme(newTheme);
    applyTheme(newTheme);
    localStorage.setItem('theme', newTheme);
  };
  
  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, setThemeMode, isLoading }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme debe usarse dentro de ThemeProvider');
  }
  return context;
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  
  // Para compatibilidad con librerías que no respetan CSS variables
  if (theme === 'dark') {
    document.documentElement.style.colorScheme = 'dark';
  } else {
    document.documentElement.style.colorScheme = 'light';
  }
}
```

### ThemeToggle Button

#### ThemeToggle.jsx
```javascript
import React from 'react';
import { useTheme } from './ThemeContext';
import './ThemeToggle.css';

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  
  return (
    <button
      onClick={toggleTheme}
      className="theme-toggle"
      aria-label={`Cambiar a tema ${theme === 'light' ? 'oscuro' : 'claro'}`}
      title={`Tema actual: ${theme === 'light' ? 'Claro' : 'Oscuro'}`}
    >
      {theme === 'light' ? (
        <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="5" strokeWidth="2" />
          <line x1="12" y1="1" x2="12" y2="3" strokeWidth="2" />
          <line x1="12" y1="21" x2="12" y2="23" strokeWidth="2" />
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" strokeWidth="2" />
          <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" strokeWidth="2" />
          <line x1="1" y1="12" x2="3" y2="12" strokeWidth="2" />
          <line x1="21" y1="12" x2="23" y2="12" strokeWidth="2" />
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" strokeWidth="2" />
          <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" strokeWidth="2" />
        </svg>
      ) : (
        <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" strokeWidth="2" />
        </svg>
      )}
    </button>
  );
}
```

#### ThemeToggle.css
```css
.theme-toggle {
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 8px;
  cursor: pointer;
  color: var(--text-primary);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle:hover {
  background: var(--bg-secondary);
}

.theme-toggle:active {
  transform: scale(0.95);
}

.theme-toggle .icon {
  width: 20px;
  height: 20px;
  stroke-linecap: round;
  stroke-linejoin: round;
}
```

### Integración en App

#### App.jsx
```javascript
import React from 'react';
import { ThemeProvider } from './contexts/ThemeContext';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './layouts/DashboardLayout';
import PaddockPage from './pages/PaddockPage';
import './App.css';

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route path="/potreros" element={<PaddockPage />} />
            {/* Otras rutas */}
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
```

### Componentes con Tema

#### Card.jsx
```javascript
import React from 'react';
import './Card.css';

export function Card({ title, children, className }) {
  return (
    <div className={`card ${className}`}>
      {title && <h3 className="card-title">{title}</h3>}
      <div className="card-body">{children}</div>
    </div>
  );
}
```

#### Card.css
```css
.card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  padding: 16px;
  transition: all 0.3s ease;
}

.card-title {
  color: var(--text-primary);
  margin: 0 0 12px 0;
  font-weight: 600;
  font-size: 16px;
}

.card-body {
  color: var(--text-secondary);
}

.card:hover {
  box-shadow: var(--shadow-lg);
}
```

### Compatibilidad con Tailwind (Alternativa)

#### tailwind.config.js
```javascript
module.exports = {
  darkMode: 'class', // usar class en lugar de media
  theme: {
    extend: {
      colors: {
        // Variables de tema
      },
    },
  },
  plugins: [],
};
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`ThemeContext.js`** - Context global
2. **`ThemeToggle.jsx`** - Botón de toggle
3. **`themes.css`** - Variables CSS
4. **`ThemeToggle.test.jsx`** - Tests
5. **`hooks/useTheme.js`** - Custom hook

### Archivos a Modificar

1. **`App.jsx`** - Envolver con ThemeProvider
2. **`DashboardLayout.jsx`** - Agregar ThemeToggle
3. **`App.css`** - Usar variables CSS
4. **`Todos los componentes`** - Usar var(--color-*)
5. **`tailwind.config.js`** o **`index.css`** - Configurar dark mode

---

## 🔧 **Refinamiento Técnico**

### ThemeProvider Context

```javascript
export const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const [theme, setTheme] = useState(() => {
    // 1. localStorage
    const saved = localStorage.getItem('theme');
    if (saved) return saved;
    
    // 2. Sistema operativo
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) 
      return 'dark';
    
    return 'light';
  });
  
  useEffect(() => {
    localStorage.setItem('theme', theme);
    document.documentElement.className = theme;
  }, [theme]);
  
  const toggle = () => setTheme(t => t === 'light' ? 'dark' : 'light');
  
  return (
    <ThemeContext.Provider value={{ theme, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
};
```

### CSS Variables

```css
:root {
  --bg-primary: #ffffff;
  --text-primary: #000000;
  --border: #e0e0e0;
}

[data-theme="dark"] {
  --bg-primary: #1e1e1e;
  --text-primary: #e0e0e0;
  --border: #404040;
}
```

### ThemeToggle Button

```javascript
export const ThemeToggle = () => {
  const { theme, toggle } = useContext(ThemeContext);
  
  return (
    <button onClick={toggle} className="p-2">
      {theme === 'light' ? '🌙' : '☀️'}
    </button>
  );
};
```

### Testing Strategy

**Tests Críticos:**
- Theme persiste en localStorage
- SO preference se respeta al iniciar
- Transición suave (0.3s)
- Contraste >= 4.5:1 (WCAG AA)
- Todos componentes soportan dark mode

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] ThemeContext implementado
- [ ] ThemeToggle component creado
- [ ] CSS variables definidas
- [ ] localStorage integrado
- [ ] SO preference respetada
- [ ] Transiciones suaves
- [ ] Todos los componentes coloreados
- [ ] Contraste WCAG AA
- [ ] Gráficos actualizados
- [ ] Imágenes/logos ajustados
- [ ] Móvil compatible
- [ ] Tests >= 80%
- [ ] Sin parpadeos

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Theme Provider Pattern + CSS-in-JS + System Preference Detection

**Justificación:** **Theme Provider**: Context global para tema. **CSS-in-JS**: Cambio dinámico. **System Detection**: prefers-color-scheme. **Persistence**: localStorage. **Accessibility**: WCAG AA. **Smooth**: Sin parpadeos.

**Componentes Afectados:**

- **ThemeProvider.jsx (Nuevo):** Context provider. Provee theme global. State: currentTheme (light/dark). Inicializa desde SO preference.

- **useTheme.js (Nuevo - Hook):** Acceso a theme. Retorna: `{ theme, toggleTheme }`. Usa context.

- **darkTheme.js + lightTheme.js (Nuevos):** Colores. Mapeo completo: background, text, borders, inputs, buttons, etc.

- **themeToggle.jsx (Nuevo):** Botón cambiar tema. Icono: ☀️/🌙. Click cambia tema. Smooth transition.

- **themePersistence.js (Nuevo):** localStorage manager. Guarda/carga preferencia. Clave: 'theme'.

- **ThemeGlobalStyles.js (Nuevo):** Estilos globales. CSS variables. Aplica colores según theme.

**Hitos:**
1. darkTheme.js + lightTheme.js (colores)
2. ThemeProvider.jsx (context)
3. useTheme.js (hook)
4. ThemeGlobalStyles.js (estilos)
5. themeToggle.jsx + themePersistence.js (UI + storage)

### Validación de Impacto

✅ **System Detection**: respeta preferencia SO
✅ **Persistence**: localStorage entre sesiones
✅ **WCAG AA**: Contraste >= 4.5:1
✅ **Smooth**: Transición 0.3s sin parpadeos
✅ **Complete**: Todos componentes soportan

### Referencias y Validación

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Theme provider + system preference

---

## ✅ **Definición de Completado**
- [ ] Performance OK
- [ ] Documentación escrita
- [ ] Code review aprobado
- [ ] CI/CD green

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
