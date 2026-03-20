# 🌱 PASTURES-HU#10: Frontend: Calendario Funcional

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Calendar Grid + Color State Mapping | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero implementar un Calendario Funcional que visualice la rotación de potreros, de tal forma que:

1. Se muestre un calendario mensual con los potreros y su disponibilidad
2. Se vea código de color para cada estado (verde=disponible, naranja=descansando, rojo=bloqueado)
3. Se pueda navegar entre meses hacia adelante y atrás
4. Se muestre información de ETA (días hasta disponible)
5. Al hacer clic en un potrero/fecha, se abra el DetailPanel
6. Se pueda cambiar de vista (mes, semana, si el tiempo lo permite)
7. Se integre con los datos reales del backend

Esto habilitará que operarios planifiquen la rotación visualmente, evitando sobrerreposo o usar potreros no listos.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Calendario Mensual Renderiza Correctamente
```gherkin
Scenario: Mostrar calendario mensual
  Given usuario en PaddockPage
  And hay 10 potreros con diferentes estados
  When calendario se carga
  Then:
    [ ] Se muestra mes actual (enero 2026)
    [ ] Grilla de 7 columnas (lunes-domingo)
    [ ] Encabezado con nombre del mes y año
    [ ] Navegación: botones "Mes Anterior" y "Mes Siguiente"
    [ ] Cada día muestra lista de potreros disponibles
    [ ] Sin errores o crashes
```

### AC#2: Código de Color para Estados
```gherkin
Scenario: Potreros se muestran con color según estado
  Given calendario con potreros en diferentes estados
  When se renderiza
  Then:
    [ ] DISPONIBLE: fondo verde (#00aa00) con icono ✓
    [ ] EN_DESCANSO: naranja (#ff8800) con ETA "15 días"
    [ ] EN_USO: amarillo (#ffff00) con icono de uso
    [ ] MANTENIMIENTO: rojo (#ff0000) con icono 🔒
    [ ] SOLD/REMOVED: gris (#999999) tachado
    [ ] Colores consistentes con diseño del sistema
```

### AC#3: Mostrar ETA en Potreros
```gherkin
Scenario: ETA se muestra clara para potreros en descanso
  Given potrero en EN_DESCANSO con ETA = 12 días
  When se muestra en calendario
  Then:
    [ ] Se muestra etiqueta "P001 (12d)" o similar
    [ ] ETA es visible en la tarjeta del potrero
    [ ] Si ETA <= 0, muestra "Disponible" en verde
    [ ] Si ETA vencido, muestra "Vencido (3d)" en rojo
    [ ] Tooltip al hover: "Potrero P001 - Disponible en 12 días"
```

### AC#4: Navegación Entre Meses
```gherkin
Scenario: Cambiar mes en el calendario
  Given calendario mostrando enero 2026
  When usuario hace click en "Mes Siguiente"
  Then:
    [ ] Calendario cambia a febrero 2026
    [ ] Se cargan datos del nuevo mes desde backend
    [ ] Nombre del mes actualizado en encabezado
    [ ] Sin lag (transición suave)
  
  When usuario hace click en "Mes Anterior"
  Then:
    [ ] Regresa a enero 2026
    [ ] Datos se recargan correctamente
    [ ] Botón "Mes Anterior" deshabilitado si es enero (o histórico)
```

### AC#5: Click en Potrero Abre DetailPanel
```gherkin
Scenario: Seleccionar potrero desde calendario
  Given calendario mostrando potreros
  When usuario hace click en "P001 (12d)"
  Then:
    [ ] DetailPanel se abre (lado derecho o modal)
    [ ] Muestra información completa de P001
    [ ] Botones de acción disponibles (Abrir, Cerrar, Bloquear)
    [ ] Calendario permanece visible detrás (o en split view)
    [ ] Close DetailPanel sin cerrar calendario
```

### AC#6: Responsive - Desktop y Mobile
```gherkin
Scenario: Calendario adaptado a diferentes pantallas
  Given usuario en desktop (1920px)
  When calendario se carga
  Then:
    [ ] Ancho completo, bien espaciado
    [ ] Letra clara, fácil de leer
    [ ] Potreros muestran nombre + ETA
  
  Given usuario en tablet (768px)
  When calendario se carga
  Then:
    [ ] Ancho adaptado, comprimido un poco
    [ ] Aún legible
    [ ] Detalles accesibles con tooltip
  
  Given usuario en mobile (375px)
  When calendario se carga
  Then:
    [ ] Scroll horizontal habilitado si es necesario
    [ ] Potreros simplificados (solo código + color)
    [ ] Información detallada en tooltip/popup
```

### AC#7: Integración con Backend - Real-time
```gherkin
Scenario: Datos del calendario vienen del backend
  Given backend devuelve potreros de enero 2026
  When se carga el calendario
  Then:
    [ ] GET /farms/{farmId}/pastures?month=2026-01
    [ ] Datos actualizados desde BD
    [ ] Cada potrero tiene: id, name, status, eta, lastUseAt, etc.
    [ ] Cambios en detailPanel se reflejan en calendario
    [ ] Si otro usuario abre un potrero, calendario se actualiza
```

### AC#8: Filtros (Opcional pero Deseable)
```gherkin
Scenario: Filtrar potreros en calendario
  Given calendario con 10 potreros
  When usuario selecciona filtro "Solo Disponibles"
  Then:
    [ ] Se muestran solo potreros en DISPONIBLE (verdes)
    [ ] Otros estados ocultos
    [ ] Contador: "3 de 10 disponibles"
  
  When selecciona "Solo en Descanso"
  Then:
    [ ] Solo EN_DESCANSO (naranjas)
  
  When selecciona "Ver Todos"
  Then:
    [ ] Vuelve a mostrar todos
```

### AC#9: Leyenda de Estados
```gherkin
Scenario: Mostrar leyenda de colores
  Given calendario visible
  Then:
    [ ] Existe leyenda en esquina (superior derecho o inferior)
    [ ] Muestra: Verde=Disponible, Naranja=Descanso, Amarillo=Uso, etc.
    [ ] Fácil de entender para nuevo usuario
    [ ] No ocupa espacio excesivo
```

### AC#10: Performance - Carga Rápida
```gherkin
Scenario: Calendario carga rápidamente
  When se carga el calendario
  Then:
    [ ] Renderizado en < 2 segundos
    [ ] Sin lag al cambiar mes
    [ ] Sin lag al click en potrero
    [ ] Optimizado para 100+ potreros (si es posible)
```

### AC#11: Accesibilidad
```gherkin
Scenario: Calendario es accesible
  Given calendario cargado
  Then:
    [ ] role="table" o role="grid" apropiado
    [ ] Encabezados (th) para días de semana
    [ ] aria-label para cada celda (ej: "Lunes, 6 de enero")
    [ ] Labels claros en botones (navegación)
    [ ] Contraste suficiente (colores vs texto)
    [ ] Fácil navegar con teclado (Tab, Enter, Arrow keys)
    [ ] Screen reader anuncio de cambios
```

### AC#12: Tooltip/Popover con Detalles
```gherkin
Scenario: Información detallada en hover
  When usuario hace hover en potrero
  Then:
    [ ] Aparece tooltip/popover con:
        * Nombre completo del potrero
        * Estado actual
        * ETA en días (si aplica)
        * Última acción (abierto/cerrado hace X días)
        * Botón "Ver detalles" (abre DetailPanel)
    [ ] Desaparece al mover mouse
    [ ] En mobile, trigger por tap/long-press
```

### AC#13: Sincronización con Tab Activa
```gherkin
Scenario: Calendario se actualiza si datos cambian
  Given usuario abre potrero (DISPONIBLE → EN_USO)
  When el cambio se guarda en backend
  Then:
    [ ] Si calendario está visible, color cambia a amarillo
    [ ] ETA se recalcula si es necesario
    [ ] Sin necesidad de refrescar página
    [ ] Real-time o polling cada 30 segundos
```

### AC#14: Casos Edge - Espacios Vacíos
```gherkin
Scenario: Manejo de días sin potreros disponibles
  Given un día en el calendario sin potreros
  When se renderiza
  Then:
    [ ] Se muestra como vacío o mensaje "Sin potreros"
    [ ] No hay error
    [ ] Grilla sigue alineada correctamente
```

### AC#15: Print-Friendly (Bonus)
```gherkin
Scenario: Calendario se puede imprimir
  Given usuario presiona Ctrl+P en calendario
  Then:
    [ ] Se abre diálogo de impresión
    [ ] Calendario se ve legible en versión impresa
    [ ] Colores se adaptan a impresora B&W
    [ ] Sin elementos innecesarios (botones navegación)
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### Componente Principal - `CalendarView.jsx`

```javascript
export function CalendarView({ farmId, selectedPasture, onPastureSelect }) {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [pastures, setPastures] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState('ALL'); // ALL, AVAILABLE, RESTING, BLOCKED
  
  useEffect(() => {
    // Cargar potreros para el mes
    fetchPasturesForMonth(farmId, currentMonth);
  }, [currentMonth, farmId]);
  
  const handlePrevMonth = () => { ... };
  const handleNextMonth = () => { ... };
  const handlePastureClick = (pasture) => { ... };
  
  return (
    <div className="calendar-view">
      <CalendarHeader 
        month={currentMonth}
        onPrevMonth={handlePrevMonth}
        onNextMonth={handleNextMonth}
      />
      <CalendarLegend />
      <CalendarGrid 
        pastures={pastures}
        filters={filters}
        onPastureClick={handlePastureClick}
      />
    </div>
  );
}
```

#### Componente - `CalendarHeader.jsx`

```javascript
export function CalendarHeader({ month, onPrevMonth, onNextMonth }) {
  return (
    <div className="calendar-header">
      <button onClick={onPrevMonth} className="btn-nav">
        ← Mes Anterior
      </button>
      <h2 className="month-title">
        {month.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })}
      </h2>
      <button onClick={onNextMonth} className="btn-nav">
        Mes Siguiente →
      </button>
    </div>
  );
}
```

#### Componente - `CalendarGrid.jsx`

```javascript
export function CalendarGrid({ pastures, onPastureClick }) {
  const daysInMonth = getDaysInMonth(currentMonth);
  const startingDayOfWeek = getStartingDayOfWeek(currentMonth);
  
  return (
    <div className="calendar-grid">
      {/* Encabezado: L, M, X, J, V, S, D */}
      <div className="weekday-header">
        {['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sab', 'Dom'].map(day => (
          <div key={day} className="weekday">{day}</div>
        ))}
      </div>
      
      {/* Días del mes */}
      {Array.from({ length: startingDayOfWeek }).map((_, i) => (
        <div key={`empty-${i}`} className="calendar-cell empty" />
      ))}
      
      {Array.from({ length: daysInMonth }).map((_, dayIndex) => {
        const day = dayIndex + 1;
        const pasturesForDay = getPasturesForDay(pastures, day);
        
        return (
          <div key={day} className="calendar-cell">
            <div className="cell-date">{day}</div>
            <div className="cell-pastures">
              {pasturesForDay.map(pasture => (
                <PastureCard
                  key={pasture.id}
                  pasture={pasture}
                  onClick={() => onPastureClick(pasture)}
                />
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
```

#### Componente - `PastureCard.jsx`

```javascript
export function PastureCard({ pasture, onClick }) {
  const statusColor = getStatusColor(pasture.status);
  const etaText = getETAText(pasture.eta);
  
  return (
    <div
      className="pasture-card"
      style={{ backgroundColor: statusColor }}
      onClick={onClick}
      title={`${pasture.name} - ${etaText}`}
    >
      <span className="pasture-name">{pasture.id}</span>
      {pasture.status === 'EN_DESCANSO' && (
        <span className="pasture-eta">{pasture.eta}d</span>
      )}
      {pasture.status === 'DISPONIBLE' && (
        <span className="pasture-status">✓</span>
      )}
    </div>
  );
}
```

#### Componente - `CalendarLegend.jsx`

```javascript
export function CalendarLegend() {
  return (
    <div className="calendar-legend">
      <div className="legend-item">
        <div className="legend-color" style={{ backgroundColor: '#00aa00' }} />
        <span>Disponible</span>
      </div>
      <div className="legend-item">
        <div className="legend-color" style={{ backgroundColor: '#ff8800' }} />
        <span>En Descanso</span>
      </div>
      <div className="legend-item">
        <div className="legend-color" style={{ backgroundColor: '#ffff00' }} />
        <span>En Uso</span>
      </div>
      <div className="legend-item">
        <div className="legend-color" style={{ backgroundColor: '#ff0000' }} />
        <span>Bloqueado</span>
      </div>
    </div>
  );
}
```

### Servicios

#### `pastureCalendarService.js`

```javascript
export async function getPasturesForMonth(farmId, month) {
  const startDate = getMonthStart(month);
  const endDate = getMonthEnd(month);
  
  // GET /farms/{farmId}/pastures?month=2026-01
  return fetch(`/api/farms/${farmId}/pastures?month=${month}`)
    .then(r => r.json());
}

// Utility
function getMonthStart(date) {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function getMonthEnd(date) {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}
```

### Estilos CSS

```css
.calendar-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.month-title {
  font-size: 24px;
  font-weight: 600;
  text-transform: capitalize;
}

.btn-nav {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-nav:hover {
  background: #f0f0f0;
}

.calendar-legend {
  display: flex;
  gap: 20px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
  font-size: 14px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 24px;
  height: 24px;
  border-radius: 4px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.weekday {
  font-weight: 600;
  text-align: center;
  padding: 8px;
  font-size: 12px;
  color: #666;
}

.calendar-cell {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-cell.empty {
  background: transparent;
  border: none;
}

.cell-date {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.cell-pastures {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.pasture-card {
  padding: 6px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pasture-card:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.pasture-name {
  font-weight: 600;
}

.pasture-eta {
  font-size: 10px;
  opacity: 0.9;
}

.pasture-status {
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .calendar-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .calendar-cell {
    min-height: 80px;
    padding: 6px;
  }
  
  .pasture-card {
    font-size: 11px;
    padding: 4px 6px;
  }
}

@media (max-width: 480px) {
  .calendar-grid {
    gap: 4px;
  }
  
  .calendar-cell {
    min-height: 60px;
    padding: 4px;
  }
  
  .cell-date {
    font-size: 12px;
  }
  
  .pasture-card {
    font-size: 10px;
    padding: 3px 4px;
  }
  
  .calendar-legend {
    flex-wrap: wrap;
    gap: 12px;
    font-size: 12px;
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`CalendarView.jsx`** - Componente principal
2. **`CalendarHeader.jsx`** - Encabezado con navegación
3. **`CalendarGrid.jsx`** - Grilla del calendario
4. **`PastureCard.jsx`** - Tarjeta de potrero
5. **`CalendarLegend.jsx`** - Leyenda de colores
6. **`pastureCalendarService.js`** - Servicios API
7. **`calendarUtils.js`** - Funciones auxiliares (getDaysInMonth, getStatusColor, etc.)
8. **`calendar.css`** - Estilos

### Archivos a Modificar

1. **`PaddockPage.jsx`** - Integrar CalendarView
2. **`constants/statusColors.js`** - Asegurar colores consistentes

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear componentes base
- CalendarView (estado, props)
- CalendarHeader (botones de navegación)
- CalendarLegend (leyenda visual)

### Paso 2: Implementar grilla
- CalendarGrid con días del mes
- Cálculo de inicio de semana
- Renderizado de celdas

### Paso 3: Integrar datos
- Conectar con servicio getPasturesForMonth
- Mapeo de potreros a fechas
- Estados y ETA

### Paso 4: Interactividad
- Click en potrero → DetailPanel
- Navegación mes anterior/siguiente
- Actualización de datos

### Paso 5: Estilos y responsive
- CSS responsive
- Colores y visualización
- Tooltip/popover

### Paso 6: Testing
- Unit tests para utilidades
- Tests de componentes
- Tests E2E

---

## 🧪 **Casos de Prueba**

### Test Unitarios

```javascript
describe('calendarUtils', () => {
  
  test('getDaysInMonth para enero = 31', () => {
    expect(getDaysInMonth(2026, 0)).toBe(31);
  });
  
  test('getStatusColor para DISPONIBLE = verde', () => {
    expect(getStatusColor('DISPONIBLE')).toBe('#00aa00');
  });
});
```

### Test de Componentes

```javascript
describe('CalendarView', () => {
  
  test('renderiza calendario para mes actual', () => {
    render(<CalendarView farmId="F001" />);
    expect(screen.getByText(/enero/i)).toBeInTheDocument();
  });
  
  test('abre DetailPanel al click en potrero', async () => {
    render(<CalendarView farmId="F001" />);
    fireEvent.click(screen.getByText('P001'));
    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
  });
});
```

### Test E2E

```javascript
describe('Calendario - E2E', () => {
  
  it('navega entre meses', () => {
    cy.visit('/farms/F001/paddocks');
    cy.contains('enero').should('be.visible');
    cy.contains('Mes Siguiente').click();
    cy.contains('febrero').should('be.visible');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Ver Calendario Mes Actual
```gherkin
Scenario: Mostrar calendario para enero 2026
  Given usuario en PaddockPage
  When se carga el calendario
  Then se muestra enero 2026 con todos los potreros
  And colores corresponden a estados
```

### Escenario 2: Navegar Meses
```gherkin
Scenario: Cambiar de mes
  Given calendario mostrando enero
  When hace click en "Mes Siguiente"
  Then se muestra febrero
  And datos se actualizan
```

### Escenario 3: Ver Detalles
```gherkin
Scenario: Abrir DetailPanel desde calendario
  Given calendario visible
  When hace click en un potrero
  Then DetailPanel se abre con información
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- React (hooks)
- React-dom
- CSS Grid/Flexbox

**Componentes relacionados**:
- DetailPanel (HU#3)
- pastureService.js

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Calendar Grid Component + Color State Mapping + Data Aggregation Layer

**Justificación:** **Calendar Grid**: Componente reutilizable para renderizar mes/semana (CSS Grid). **Color State Mapping**: Constantes centralizadas (DISPONIBLE→verde, EN_DESCANSO→naranja) - single source of truth. **Data Aggregation**: Agrupar potreros por fecha. **Responsividad**: CSS grid adaptable + tooltips. **Integración**: Se conecta con DetailPanel (HU#3). **Performance**: Lazy-load meses, caching.

**Componentes Afectados:**

- **Calendar.jsx (Nuevo):** Componente principal. Props: `farmId`, `month`. Renderiza: encabezado (mes/año + navegación), grid 7 columnas, celdas. onClick abre DetailPanel.

- **CalendarDay.jsx (Nuevo):** Celda individual. Props: `day`, `pastures`. Renderiza: fecha + potreros con colores. Tooltip en hover.

- **CalendarTooltip.jsx (Nuevo):** Popover. Props: `pasture`. Muestra: nombre, estado, ETA, acciones.

- **useCalendarData.js (Nuevo - Hook):** Obtiene/cachea datos por mes. GET `/farms/{farmId}/pastures?month=2026-01`. Retorna: `{ pastures, loading, error }`.

- **colorStateMap.js (Nuevo):** Mapeo estado→color. DISPONIBLE:#00aa00, EN_DESCANSO:#ff8800, etc. Single source of truth.

- **calendarUtils.js (Nuevo):** Helpers: daysInMonth, formatDate, groupPasturesByDate.

**Hitos:**
1. colorStateMap.js + calendarUtils.js (sin dependencias)
2. CalendarTooltip.jsx (reutilizable)
3. CalendarDay.jsx (depende: colorStateMap, tooltip)
4. useCalendarData.js (depende: API)
5. Calendar.jsx (depende: CalendarDay, useCalendarData)

### Validación de Impacto

✅ **Grid Component**: Reutilizable para diferentes vistas
✅ **Color Mapping**: Consistencia visual
✅ **Aggregation**: Eficiente para render
✅ **Responsive**: Desktop/Tablet/Mobile
✅ **Performance**: Lazy-load, caching

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-003: Detail Panel (integración)
- → PASTURES-HU-010: Calendar (esta)
- → PASTURES-HU-011: AlertCenter (complementario)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Calendar Grid + Color Mapping

---

## 🔧 **Refinamiento Técnico**

### Component Structure

**CalendarView.jsx:**
```javascript
export const CalendarView = ({ farmId, potreros }) => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedPasture, setSelectedPasture] = useState(null);
  const days = generateCalendarDays(currentDate);
  
  return (
    <div className="bg-white rounded-lg p-6">
      <CalendarHeader 
        currentDate={currentDate} 
        onPrevious={() => setCurrentDate(addMonths(currentDate, -1))}
        onNext={() => setCurrentDate(addMonths(currentDate, 1))}
        onToday={() => setCurrentDate(new Date())}
      />
      <CalendarGrid days={days} potreros={potreros} onSelectPasture={setSelectedPasture} />
      <CalendarLegend />
      {selectedPasture && <DetailPanel pasture={selectedPasture} />}
    </div>
  );
};
```

### Color Mapping by Status

```javascript
const statusColorMap = {
  'DISPONIBLE': 'bg-green-100 text-green-800 border-green-300',
  'EN_USO': 'bg-yellow-100 text-yellow-800 border-yellow-300',
  'EN_DESCANSO': 'bg-orange-100 text-orange-800 border-orange-300',
  'MANTENIMIENTO': 'bg-red-100 text-red-800 border-red-300'
};

export const PastureCard = ({ pasture }) => (
  <div className={`p-2 rounded border-2 cursor-pointer hover:shadow-md ${statusColorMap[pasture.status]}`}>
    <p className="font-semibold text-sm">{pasture.name}</p>
    <p className="text-xs">{pasture.status}</p>
    {pasture.eta && <p className="text-xs">ETA: {pasture.eta}d</p>}
  </div>
);
```

### Helper Functions

```javascript
// calendarUtils.js
export const generateCalendarDays = (date) => {
  const firstDay = startOfMonth(date);
  const lastDay = endOfMonth(date);
  const daysInMonth = getDaysInMonth(date);
  // Return array de Day objects con week/day index
};

export const getPasturesForDay = (potreros, date) => {
  return potreros.filter(p => 
    p.lastUseAt && isSameDay(p.lastUseAt, date) ||
    p.closedAt && isSameDay(p.closedAt, date)
  );
};
```

### Testing Strategy

**Component Tests (RTL):**
```javascript
test('CalendarView muestra mes actual', () => {
  render(<CalendarView farmId="F001" potreros={mockPotreros} />);
  expect(screen.getByText('January 2026')).toBeInTheDocument();
});

test('Click en pasture abre DetailPanel', async () => {
  render(<CalendarView farmId="F001" potreros={mockPotreros} />);
  fireEvent.click(screen.getByText('P001'));
  expect(screen.getByText('Potrero 1')).toBeInTheDocument();
});
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `CalendarView.jsx` implementado
- [ ] `CalendarHeader.jsx` con navegación
- [ ] `CalendarGrid.jsx` con días
- [ ] `PastureCard.jsx` renderizando
- [ ] `CalendarLegend.jsx` visible
- [ ] `pastureCalendarService.js` integrado
- [ ] `calendarUtils.js` con helpers
- [ ] Estilos CSS responsive
- [ ] Código de color por estado
- [ ] ETA se muestra correctamente
- [ ] Navegación mes anterior/siguiente
- [ ] Click abre DetailPanel
- [ ] Responsive: desktop, tablet, mobile
- [ ] Backend integration funciona
- [ ] Real-time updates (si aplica)
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests componentes: todos los ACs
- [ ] Tests E2E: flujos completos
- [ ] Accesibilidad: ARIA labels, etc.
- [ ] Performance: carga < 2s
- [ ] Tooltip/popover implementado
- [ ] Sincronización con cambios
- [ ] Code review aprobado
- [ ] Sin warnings de linting
- [ ] CI/CD green
- [ ] Documentación actualizada

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
