# 🌱 PASTURES-HU#25: Frontend: Responsive Design Móvil

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Mobile-First + Touch-Optimized | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **usuario en móvil/tablet**, quiero usar la aplicación cómodamente en dispositivos pequeños, de tal forma que:

1. El layout se adapte a todos los tamaños
2. Los controles sean tocables (44px+)
3. No haya horizontal scroll innecesario
4. Las imágenes se escalen correctamente
5. El rendimiento sea óptimo en móvil
6. La navegación sea touch-friendly
7. Los formularios sean usables en móvil

Esto habilitará que usuarios usen la app desde cualquier dispositivo.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Viewport Configuration
```gherkin
Scenario: Viewport configurado correctamente
  Given HTML head
  When contiene meta viewport
  Then:
    [ ] <meta name="viewport" content="width=device-width, initial-scale=1.0">
    [ ] Máximo zoom: 5.0
    [ ] Sin disable-user-scalability (accesibilidad)
    [ ] Device-width scale funciona
```

### AC#2: Mobile-First CSS
```gherkin
Scenario: Diseño mobile-first con breakpoints
  Given estilos CSS
  When se define media queries
  Then:
    [ ] Mobile: 320px-640px (default)
    [ ] Tablet: 641px-1024px (@media (min-width: 641px))
    [ ] Desktop: 1025px+ (@media (min-width: 1025px))
    [ ] Fluido entre breakpoints
```

### AC#3: Botones Tocables
```gherkin
Scenario: Todos los botones son tocables
  Given botones en la aplicación
  Then:
    [ ] Tamaño mínimo: 44x44px (WCAG)
    [ ] Padding: 12px+ en móvil
    [ ] Touch target grande
    [ ] Sin elementos muy pequeños
    [ ] Espacio entre botones (8px+)
```

### AC#4: Navegación Móvil
```gherkin
Scenario: Navegación adaptada a móvil
  Given aplicación en móvil (< 768px)
  When navbar visible
  Then:
    [ ] Hamburger menu (☰) en móvil
    [ ] Logo visible y clickeable
    [ ] Links en vertical dropdown
    [ ] Back button en páginas interiores
    [ ] Sin horizontal scroll
```

### AC#5: Imágenes Responsivas
```gherkin
Scenario: Imágenes se escalan según pantalla
  Given <img src="...">
  When se carga en diferentes tamaños
  Then:
    [ ] max-width: 100% en CSS
    [ ] height: auto (mantiene ratio)
    [ ] Sin overflow
    [ ] Carga apropiada para tamaño
    [ ] Picture tags para art direction
```

### AC#6: Tablas Responsivas
```gherkin
Scenario: Tablas legibles en móvil
  Given tabla de datos
  When pantalla < 768px
  Then:
    [ ] Se convierte a cards apiladas
    [ ] O scroll horizontal (si necesario)
    [ ] Headers visibles
    [ ] Datos legibles
    [ ] Sin cortes
```

### AC#7: Formularios Móvil
```gherkin
Scenario: Formularios usables en móvil
  Given formulario de creación
  When usuario accede en móvil
  Then:
    [ ] Input height >= 44px
    [ ] Label visible y bien posicionada
    [ ] Teclado numérico para números
    [ ] Teclado email para emails
    [ ] Dropdown simplificado
    [ ] Validación clara
    [ ] Sin scroll horizontal
```

### AC#8: Touch-Friendly Interactions
```gherkin
Scenario: Interacciones optimizadas para touch
  Given controles interactivos
  When usuario toca en móvil
  Then:
    [ ] Hover states -> active states
    [ ] Click feedback visible
    [ ] Sin 300ms delay (FastClick)
    [ ] Swipe gestures si aplica
    [ ] Long-press para opciones
```

### AC#9: Performance Móvil
```gherkin
Scenario: Aplicación rápida en móvil
  Given 4G connection (LTE)
  When usuario abre app
  Then:
    [ ] First Contentful Paint < 2.5s
    [ ] Largest Contentful Paint < 4s
    [ ] Cumulative Layout Shift < 0.1
    [ ] Lighthouse Mobile score >= 90
```

### AC#10: Imágenes Optimizadas
```gherkin
Scenario: Imágenes optimizadas por tamaño
  Given imágenes en app
  When se cargan en móvil
  Then:
    [ ] Usar WebP con fallback
    [ ] Srcset para diferentes DPI
    [ ] Lazy loading (native)
    [ ] Tamaño total < 2MB por página
    [ ] CDN con compresión
```

### AC#11: Legibilidad en Móvil
```gherkin
Scenario: Texto legible sin zoom
  Given contenido de texto
  Then:
    [ ] Font size >= 16px (default)
    [ ] Line height >= 1.4
    [ ] Line length <= 80 caracteres
    [ ] Contraste >= 4.5:1
    [ ] Sin justify (alignment)
```

### AC#12: Orientación Portrait/Landscape
```gherkin
Scenario: Aplicación funciona en ambas orientaciones
  Given usuario rota dispositivo
  When cambia orientación
  Then:
    [ ] Layout se adapta automáticamente
    [ ] Sin contenido perdido
    [ ] Scroll position se conserva (si es posible)
    [ ] Sin recarga de página
    [ ] Transición suave
```

### AC#13: Mobile Keyboard
```gherkin
Scenario: Teclado móvil integrado
  Given input fields
  When usuario toca en móvil
  Then:
    [ ] Teclado apropiado (email, tel, number)
    [ ] Input type correcto
    [ ] Autocomplete habilitado
    [ ] Password visible toggle (si aplica)
    [ ] Teclado se cierra al blur
```

### AC#14: Testing Responsive
```gherkin
Scenario: Tests verifican responsive
  Given suite de tests
  Then:
    [ ] Test: 320px (Mobile)
    [ ] Test: 768px (Tablet)
    [ ] Test: 1024px (Desktop)
    [ ] Test: Orientación portrait
    [ ] Test: Orientación landscape
    [ ] Lighthouse checks
    [ ] WebPageTest checks
```

### AC#15: Documentación Responsive
```gherkin
Scenario: Documentar breakpoints y estrategia
  Given implementación responsive
  Then:
    [ ] Breakpoints definidos
    [ ] Guía de cómo usar Tailwind
    [ ] Ejemplos de componentes
    [ ] Performance guidelines
    [ ] Testing procedures
```

---

## 📊 **Especificación Técnica**

### HTML Viewport

#### index.html
```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes">
    <meta name="description" content="Gestor de potreros para ganadería">
    <meta name="theme-color" content="#0066cc">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
    <link rel="apple-touch-icon" href="/apple-touch-icon.png">
    <title>Cattle - Gestor de Potreros</title>
    <link rel="stylesheet" href="/src/main.css">
</head>
<body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
</body>
</html>
```

### Tailwind Configuration

#### tailwind.config.js
```javascript
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      screens: {
        'xs': '320px',    // Mobile
        'sm': '640px',    // Mobile landscape / small tablet
        'md': '768px',    // Tablet
        'lg': '1024px',   // Desktop
        'xl': '1280px',   // Large desktop
        '2xl': '1536px',  // Extra large
      },
      spacing: {
        'touch': '44px',  // Botón tocable mínimo
      },
      fontSize: {
        'xs': ['12px', { lineHeight: '16px' }],
        'sm': ['14px', { lineHeight: '20px' }],
        'base': ['16px', { lineHeight: '24px' }], // Mínimo para móvil
        'lg': ['18px', { lineHeight: '28px' }],
        'xl': ['20px', { lineHeight: '28px' }],
      },
    },
  },
  plugins: [],
};
```

### Estilos Base Responsivos

#### index.css
```css
/* Mobile first */
html {
  font-size: 16px;
  -webkit-text-size-adjust: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.5;
  color: var(--text-primary);
  background: var(--bg-primary);
}

/* Ensure images don't overflow */
img, video {
  max-width: 100%;
  height: auto;
  display: block;
}

/* Touch-friendly inputs */
input, select, textarea, button {
  font-size: 16px; /* Prevent zoom on iOS */
  min-height: 44px;
  padding: 12px;
}

/* Tablet and up */
@media (min-width: 768px) {
  html {
    font-size: 17px;
  }
  
  input, select, textarea, button {
    min-height: 40px;
    padding: 10px 16px;
  }
}

/* Desktop and up */
@media (min-width: 1024px) {
  html {
    font-size: 18px;
  }
}
```

### Componentes Responsivos

#### Container.jsx
```javascript
import React from 'react';
import './Container.css';

export function Container({ children, className = '' }) {
  return (
    <div className={`container ${className}`}>
      {children}
    </div>
  );
}
```

#### Container.css
```css
.container {
  width: 100%;
  padding: 0 16px; /* Padding móvil */
  margin: 0 auto;
}

@media (min-width: 640px) {
  .container {
    padding: 0 20px;
  }
}

@media (min-width: 1024px) {
  .container {
    max-width: 1200px;
    padding: 0 24px;
  }
}
```

#### Card.jsx (Responsiva)
```javascript
export function Card({ title, children, className = '' }) {
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
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-md);
}

@media (min-width: 768px) {
  .card {
    padding: 20px;
    margin-bottom: 20px;
    border-radius: 12px;
  }
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}

@media (min-width: 768px) {
  .card-title {
    font-size: 20px;
    margin-bottom: 16px;
  }
}
```

### Tabla Responsiva

#### Table.jsx
```javascript
import React from 'react';
import './Table.css';

export function Table({ headers, rows, mobileRender }) {
  return (
    <>
      {/* Tabla desktop */}
      <table className="table hidden md:table">
        <thead>
          <tr>
            {headers.map((h, i) => (
              <th key={i}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={i}>
              {row.map((cell, j) => (
                <td key={j}>{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      {/* Cards móvil */}
      <div className="table-mobile md:hidden">
        {rows.map((row, i) => (
          <div key={i} className="card">
            {headers.map((header, j) => (
              <div key={j} className="table-row">
                <span className="table-label">{header}</span>
                <span className="table-value">{row[j]}</span>
              </div>
            ))}
          </div>
        ))}
      </div>
    </>
  );
}
```

#### Table.css
```css
.table {
  width: 100%;
  border-collapse: collapse;
}

.table th, .table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
}

@media (min-width: 768px) {
  .table th, .table td {
    padding: 16px;
    font-size: 16px;
  }
}

.table-mobile {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.table-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.table-label {
  font-weight: 600;
  color: var(--text-secondary);
}

.table-value {
  color: var(--text-primary);
}
```

### Navbar Responsivo

#### Navbar.jsx
```javascript
import React, { useState } from 'react';
import './Navbar.css';

export function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <nav className="navbar">
      <div className="navbar-content">
        <div className="navbar-brand">
          <h1>🌱 Cattle</h1>
        </div>

        {/* Hamburger menu (móvil) */}
        <button
          className="navbar-toggle md:hidden"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Abrir menú"
        >
          ☰
        </button>

        {/* Links (desktop) */}
        <ul className={`navbar-links ${mobileMenuOpen ? 'open' : ''}`}>
          <li><a href="/dashboard">Dashboard</a></li>
          <li><a href="/pastures">Potreros</a></li>
          <li><a href="/profile">Perfil</a></li>
          <li><a href="/logout">Salir</a></li>
        </ul>
      </div>
    </nav>
  );
}
```

#### Navbar.css
```css
.navbar {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  padding: 12px 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-brand h1 {
  font-size: 20px;
  margin: 0;
}

@media (min-width: 768px) {
  .navbar-brand h1 {
    font-size: 24px;
  }
}

.navbar-toggle {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 8px;
  min-height: 44px;
  min-width: 44px;
}

.navbar-links {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-primary);
  flex-direction: column;
  padding: 0;
  margin: 0;
  list-style: none;
  border-top: 1px solid var(--border-color);
}

.navbar-links.open {
  display: flex;
}

.navbar-links li a {
  display: block;
  padding: 16px;
  color: var(--text-primary);
  text-decoration: none;
  border-bottom: 1px solid var(--border-color);
  font-size: 16px;
}

@media (min-width: 768px) {
  .navbar-links {
    display: flex !important;
    position: static;
    background: transparent;
    flex-direction: row;
    border-top: none;
  }

  .navbar-links li a {
    padding: 12px 16px;
    border-bottom: none;
    font-size: 15px;
  }
}
```

### Performance Optimization

#### LazyImage.jsx
```javascript
import React from 'react';

export function LazyImage({ src, alt, className = '' }) {
  return (
    <img
      src={src}
      alt={alt}
      loading="lazy"
      decoding="async"
      className={className}
    />
  );
}
```

#### Picture con WebP
```javascript
export function ResponsiveImage({ webp, jpg, alt, className = '' }) {
  return (
    <picture>
      <source srcSet={webp} type="image/webp" />
      <source srcSet={jpg} type="image/jpeg" />
      <img
        src={jpg}
        alt={alt}
        className={className}
        loading="lazy"
        decoding="async"
      />
    </picture>
  );
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`index.html`** - Actualizar viewport
2. **`tailwind.config.js`** - Breakpoints responsivos
3. **`Container.jsx`** - Contenedor responsivo
4. **`LazyImage.jsx`** - Imágenes lazy
5. **`ResponsiveImage.jsx`** - Picture tags

### Archivos a Modificar

1. **`index.css`** - Estilos base responsivos
2. **`Navbar.jsx`** - Hamburger menu móvil
3. **`Card.css`** - Padding responsivo
4. **`Table.jsx`** - Cards en móvil
5. **`Todos los componentes`** - Usar Tailwind responsive

---

## 🔧 **Refinamiento Técnico**

### Viewport Meta Tag

```html
<meta name="viewport" 
  content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
```

### Tailwind Breakpoints

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    screens: {
      'sm': '640px',   // Mobile
      'md': '768px',   // Tablet
      'lg': '1024px',  // Desktop
      'xl': '1280px'   // Large
    }
  }
};
```

### Responsive Component Example

```javascript
export const PastureTable = ({ potreros }) => {
  return (
    // Desktop: tabla normal
    <div className="hidden md:block">
      <table className="w-full">
        {/* Tabla */}
      </table>
    </div>
  );
};

// Mobile: cards apiladas
<div className="md:hidden space-y-2">
  {potreros.map(p => (
    <PastureCard key={p.id} pasture={p} />
  ))}
</div>
```

### Touch-Optimized Buttons

```css
button, a {
  min-height: 44px;  /* WCAG touch target */
  min-width: 44px;
  padding: 12px;
}

/* Mobile spacing */
@media (max-width: 640px) {
  .spacing-mobile {
    padding: 8px;
    margin: 4px;
  }
}
```

### Performance - Lighthouse

```
FCP (First Contentful Paint): < 1.8s
LCP (Largest Contentful Paint): < 2.5s
CLS (Cumulative Layout Shift): < 0.1
TTI (Time to Interactive): < 3.8s
```

### Testing Strategy

**Responsive Tests:**
- Mobile (320px, 375px)
- Tablet (768px)
- Desktop (1024px, 1280px)
- All orientations (portrait, landscape)
- Touch interactions (tap, swipe, long-press)

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Viewport configurado
- [ ] Tailwind configurado
- [ ] Mobile-first CSS
- [ ] Botones tocables (44px+)
- [ ] Navbar responsive
- [ ] Imágenes responsivas
- [ ] Tablas responsivas
- [ ] Formularios usables
- [ ] Touch interactions
- [ ] Performance optimizado
- [ ] Lazy loading
- [ ] Orientaciones funcionan
- [ ] Keyboard integrado

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Mobile-First Design + CSS Media Queries + Touch-Optimized Components

**Justificación:** **Mobile-First**: Diseño desde móvil. **Breakpoints**: 320px, 768px, 1024px. **Touch-Optimized**: Botones 44px+. **Performance**: Optimizado móvil. **Accessibility**: Viewport + scaling. **Flexible**: Fluido entre tamaños.

**Componentes Afectados:**

- **responsiveTheme.js (Nuevo):** Breakpoints centralizados. Constantes: mobile, tablet, desktop. Reutilizado en styled-components.

- **useMobileDetect.js (Nuevo - Hook):** Detecta dispositivo. Retorna: `{ isMobile, isTablet, isDesktop }`. Basado en viewport.

- **MobileMenu.jsx (Nuevo):** Menú hamburger. Props: links. Renderiza botón ☰ en móvil. Dropdown vertical.

- **ResponsiveTable.jsx (Nuevo):** Tabla adaptable. Móvil: cards apiladas. Desktop: tabla normal. Props: data, columns.

- **mobileOptimized.css (Nuevo):** Media queries. Tailwind @apply. Breakpoints responsive. Imágenes, botones, inputs.

- **touchOptimizations.js (Nuevo):** Handlers touch. Swipe gestures. Long-press. Active states. FastClick.

**Hitos:**
1. responsiveTheme.js (breakpoints)
2. mobileOptimized.css (media queries)
3. MobileMenu.jsx + ResponsiveTable.jsx (components)
4. useMobileDetect.js + touchOptimizations.js (logic)
5. Tests responsive + lighthouse

### Validación de Impacto

✅ **Mobile-First**: 320px base
✅ **Touch-Optimized**: Botones 44px+
✅ **Performance**: Lighthouse >= 90
✅ **Accessibility**: Touch-friendly
✅ **Flexible**: Fluido 320px-1920px

### Referencias y Validación

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Mobile-first responsive design

---

## ✅ **Definición de Completado**
- [ ] Testing responsive
- [ ] Lighthouse >= 90
- [ ] Code review aprobado
- [ ] CI/CD green

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
