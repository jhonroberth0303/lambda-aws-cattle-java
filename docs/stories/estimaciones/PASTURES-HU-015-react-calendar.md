# 🌱 PASTURES-HU#15: Frontend: Integrar react-big-calendar

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - 3rd-party Library + Event Mapping | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero integrar react-big-calendar para mejorar la visualización del calendario, de tal forma que:

1. Se use librería profesional react-big-calendar en lugar de custom
2. Se soporten múltiples vistas (mes, semana, día, agenda)
3. Se pueda hacer drag-and-drop de eventos (opcional)
4. Se muestren eventos de potreros con colores por estado
5. Se integre perfectamente con los datos de backend (HU#12)
6. Se mantengan todas las funcionalidades de HU#10
7. El calendario sea más robusto y performante

Esto habilitará que operarios tengan una visualización profesional y flexible de la rotación, similar a Google Calendar.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Instalación de react-big-calendar
```gherkin
Scenario: Instalar dependencia correctamente
  Given proyecto cattle-front sin react-big-calendar
  When ejecuta: npm install react-big-calendar
  And npm install date-fns (para manejo de fechas)
  Then:
    [ ] Dependencias se instalan correctamente
    [ ] package.json actualizado
    [ ] Sin conflictos de versiones
    [ ] Se puede importar: import { Calendar } from 'react-big-calendar'
```

### AC#2: Vista Mensual Renderiza
```gherkin
Scenario: Mostrar calendario en vista mensual
  Given usuario en PaddockPage
  When se carga el calendario
  Then:
    [ ] Se muestra vista mensual por defecto
    [ ] Grilla 7x6 (lunes-domingo, 6 semanas)
    [ ] Nombre del mes y año en encabezado
    [ ] Botones: "Hoy", "Anterior", "Siguiente"
    [ ] Navegación suave sin lag
```

### AC#3: Mostrar Eventos de Potreros
```gherkin
Scenario: Eventos de potreros como eventos del calendario
  Given potreros con diferentes estados
  When se cargan en calendario
  Then:
    [ ] Cada potrero = 1 evento por día (si aplica)
    [ ] Nombre del potrero: "P001", "P002", etc
    [ ] Color según estado: verde (disponible), naranja (descanso), etc
    [ ] Información en evento: "P001 - EN_DESCANSO (12d)"
    [ ] Sin overlap/conflictos visuales
```

### AC#4: Múltiples Vistas
```gherkin
Scenario: Cambiar entre vistas
  Given calendario visible
  When usuario hace click en botón "Semana"
  Then:
    [ ] Vista cambia a vista semanal
    [ ] Se muestra: lunes-domingo con horas (opcional)
    [ ] Potreros/eventos siguen visibles
    [ ] Transición suave
  
  When hace click en "Día"
  Then:
    [ ] Vista diaria con detalle horario
    [ ] Potreros disponibles para ese día
  
  When hace click en "Mes"
  Then:
    [ ] Vuelve a vista mensual
  
  When hace click en "Agenda"
  Then:
    [ ] Lista de eventos próximos (tipo Gmail)
    [ ] Formato: fecha, potrero, estado
```

### AC#5: Navegación Intuitiva
```gherkin
Scenario: Navegar por períodos
  Given calendario mostrando enero 2026
  When hace click "Anterior"
  Then:
    [ ] Cambia a diciembre 2025
    [ ] Eventos se cargan para nuevo período
  
  When hace click "Siguiente"
  Then:
    [ ] Cambia a enero 2026
  
  When hace click "Hoy"
  Then:
    [ ] Vuelve a la fecha actual
    [ ] Día actual está resaltado
```

### AC#6: Click en Evento Abre DetailPanel
```gherkin
Scenario: Seleccionar potrero desde evento
  Given evento en calendario (ej: "P001 - EN_DESCANSO")
  When usuario hace click en el evento
  Then:
    [ ] DetailPanel se abre (lado derecho)
    [ ] Muestra información de P001
    [ ] Potrero correcto seleccionado
    [ ] Calendario permanece visible
```

### AC#7: Colores Consistentes
```gherkin
Scenario: Colores coinciden con HU#10
  Given calendario con potreros en diferentes estados
  When se renderiza
  Then:
    [ ] DISPONIBLE: verde (#00aa00)
    [ ] EN_DESCANSO: naranja (#ff8800)
    [ ] EN_USO: amarillo (#ffff00)
    [ ] MANTENIMIENTO: rojo (#ff0000)
    [ ] Colores consistentes con DetailPanel y Calendar custom
```

### AC#8: Tooltip en Eventos
```gherkin
Scenario: Información al hover en evento
  When usuario hace hover en evento
  Then:
    [ ] Aparece tooltip con:
        * Nombre: "Potrero P001"
        * Estado: "EN_DESCANSO"
        * ETA: "12 días"
        * Altura: "15cm" (si aplica)
    [ ] Desaparece al mover mouse
    [ ] Sin delay
```

### AC#9: Integración con Backend
```gherkin
Scenario: Datos vienen del backend
  Given usuario carga calendario
  When calendar se monta
  Then:
    [ ] GET /farms/{farmId}/pastures?month=2026-01
    [ ] Datos actualizados desde BD
    [ ] Eventos se populan correctamente
    [ ] Sin datos hardcoded
```

### AC#10: Performance Optimizado
```gherkin
Scenario: Calendario rápido y sin lag
  Given calendario con 100+ potreros
  When se carga vista mensual
  Then:
    [ ] Renderizado en < 1 segundo
    [ ] Cambio de vista: < 300ms
    [ ] Cambio de mes: < 500ms
    [ ] Sin memory leaks
    [ ] Smooth scrolling
```

### AC#11: Responsive Design
```gherkin
Scenario: Calendario adaptado a pantallas
  Given usuario en desktop (1920px)
  When calendario carga
  Then:
    [ ] Ancho completo, bien espaciado
    [ ] Todos los eventos visibles
  
  Given usuario en tablet (768px)
  When calendario carga
  Then:
    [ ] Ancho adaptado
    [ ] Scroll si es necesario
    [ ] Eventos aún legibles
  
  Given usuario en mobile (375px)
  When calendario carga
  Then:
    [ ] Vistas: Agenda o Día primarias
    [ ] Mes disponible con scroll
    [ ] Eventos simplificados
    [ ] Fácil de interactuar
```

### AC#12: Personalización de Estilos
```gherkin
Scenario: Calendarios se vea bien integrado
  Given tema del app (Tailwind)
  When calendario se carga
  Then:
    [ ] Colores del calendario coinciden con app
    [ ] Fuentes consistentes
    [ ] Espaciado uniforme
    [ ] Sin conflicto de estilos CSS
    [ ] Fácil personalizar (variables CSS)
```

### AC#13: Sincronización en Tiempo Real
```gherkin
Scenario: Cambios se reflejan sin refrescar
  Given usuario A abre potrero en otra sesión
  When el estado cambia (DISPONIBLE → EN_USO)
  Then:
    [ ] Eventos en calendario se actualizan
    [ ] Color cambia automáticamente
    [ ] O polling cada 30-60 segundos
    [ ] Sin necesidad de refrescar
```

### AC#14: Atajos de Teclado (Bonus)
```gherkin
Scenario: Navegación rápida con teclado
  Given calendario visible
  When usuario presiona:
    [ ] 'm': Cambiar a vista mensual
    [ ] 'w': Cambiar a vista semanal
    [ ] 'd': Cambiar a vista diaria
    [ ] 'a': Cambiar a vista agenda
    [ ] 'h': Ir a hoy
    [ ] 'Flecha Izq': Período anterior
    [ ] 'Flecha Der': Período siguiente
  Then:
    [ ] Atajos funcionan
    [ ] Documentados en tooltip o help
```

### AC#15: Accesibilidad
```gherkin
Scenario: Calendario accesible
  Given calendario visible
  Then:
    [ ] role="presentation" o "table"
    [ ] aria-label para encabezados
    [ ] aria-current="date" para hoy
    [ ] Contraste suficiente
    [ ] Navegación con teclado (Tab, Enter)
    [ ] Screen reader compatible
```

---

## 📊 **Especificación Técnica**

### Instalación y Configuración

#### 1. Instalar Dependencias

```bash
npm install react-big-calendar date-fns
```

#### 2. Setup Básico - `CalendarView.jsx`

```javascript
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Calendar, dateFnsLocalizer } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay } from 'date-fns';
import esLocale from 'date-fns/locale/es';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import './calendar.css'; // Custom styles

const locales = {
  es: esLocale,
};

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales,
});

export function CalendarView({ farmId, selectedPasture, onPastureSelect }) {
  const [pastures, setPastures] = useState([]);
  const [events, setEvents] = useState([]);
  const [view, setView] = useState('month');
  const [date, setDate] = useState(new Date());
  const [loading, setLoading] = useState(false);
  
  // Convertir potreros a eventos del calendario
  const eventList = useMemo(() => {
    return events.map(event => ({
      id: event.id,
      title: event.title,
      start: new Date(event.start),
      end: new Date(event.end),
      resource: {
        pastureId: event.pastureId,
        status: event.status,
        eta: event.eta,
        color: getColorByStatus(event.status)
      }
    }));
  }, [events]);
  
  useEffect(() => {
    fetchPasturesForMonth(farmId, date);
  }, [farmId, date, view]);
  
  const fetchPasturesForMonth = useCallback(async (farmId, date) => {
    setLoading(true);
    try {
      const month = format(date, 'yyyy-MM');
      const data = await getPasturesForMonth(farmId, month);
      
      // Convertir potreros a eventos
      const newEvents = data.flatMap(pasture => 
        createEventsFromPasture(pasture, date.getFullYear(), date.getMonth())
      );
      
      setEvents(newEvents);
      setPastures(data);
    } catch (error) {
      console.error('Error cargando potreros:', error);
    } finally {
      setLoading(false);
    }
  }, []);
  
  const handleSelectEvent = (event) => {
    if (event.resource?.pastureId) {
      onPastureSelect?.(event.resource.pastureId);
    }
  };
  
  const handleNavigate = (newDate) => {
    setDate(newDate);
  };
  
  const handleViewChange = (newView) => {
    setView(newView);
  };
  
  const eventStyleGetter = (event) => {
    const backgroundColor = event.resource?.color || '#0066cc';
    return {
      style: {
        backgroundColor,
        borderRadius: '5px',
        opacity: 0.8,
        color: 'white',
        border: '0px',
        display: 'block'
      }
    };
  };
  
  return (
    <div className="calendar-wrapper">
      {loading && <div className="calendar-loader">Cargando...</div>}
      
      <Calendar
        localizer={localizer}
        events={eventList}
        startAccessor="start"
        endAccessor="end"
        style={{ height: '100%' }}
        view={view}
        onView={handleViewChange}
        date={date}
        onNavigate={handleNavigate}
        onSelectEvent={handleSelectEvent}
        eventPropGetter={eventStyleGetter}
        views={['month', 'week', 'day', 'agenda']}
        defaultView="month"
        toolbar={true}
        popup={true}
        selectable={false}
        culture="es"
      />
    </div>
  );
}

function getColorByStatus(status) {
  const colors = {
    'DISPONIBLE': '#00aa00',
    'EN_DESCANSO': '#ff8800',
    'EN_USO': '#ffff00',
    'MANTENIMIENTO': '#ff0000',
    'SOLD': '#999999',
    'REMOVED': '#999999'
  };
  return colors[status] || '#0066cc';
}

function createEventsFromPasture(pasture, year, month) {
  // Crear eventos para cada día del mes
  // Un evento por día si el potrero es relevante para ese día
  
  const events = [];
  const startOfMonth = new Date(year, month, 1);
  const endOfMonth = new Date(year, month + 1, 0);
  
  for (let day = startOfMonth; day <= endOfMonth; day.setDate(day.getDate() + 1)) {
    events.push({
      id: `${pasture.id}-${format(day, 'yyyy-MM-dd')}`,
      pastureId: pasture.id,
      title: formatEventTitle(pasture),
      start: new Date(day),
      end: new Date(day),
      status: pasture.status,
      eta: pasture.eta
    });
  }
  
  return events;
}

function formatEventTitle(pasture) {
  if (pasture.eta && pasture.eta > 0) {
    return `${pasture.id} (${pasture.eta}d)`;
  }
  return pasture.id;
}
```

### Servicio

#### `pastureCalendarService.js`

```javascript
export async function getPasturesForMonth(farmId, month) {
  const response = await fetch(
    `/api/farms/${farmId}/pastures?month=${month}`
  );
  
  if (!response.ok) {
    throw new Error('Error cargando potreros');
  }
  
  return response.json();
}
```

### Estilos Personalizados

#### `calendar.css`

```css
/* Override de react-big-calendar */

.rbc-calendar {
  font-family: inherit;
  font-size: 14px;
}

.rbc-header {
  padding: 12px 4px;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  color: #333;
  border-bottom: 2px solid #eee;
}

.rbc-today {
  background-color: #f0f8ff;
}

.rbc-off-range-bg {
  background-color: #fafafa;
}

.rbc-event {
  padding: 4px 6px;
  font-size: 12px;
  border-radius: 4px;
  border: none;
}

.rbc-event-label {
  font-size: 11px;
}

.rbc-toolbar {
  padding: 12px 0;
  flex-wrap: wrap;
  gap: 8px;
}

.rbc-toolbar button {
  padding: 6px 12px;
  font-size: 13px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
}

.rbc-toolbar button:hover {
  background: #f0f0f0;
}

.rbc-toolbar button.rbc-active {
  background: #0066cc;
  color: white;
  border-color: #0066cc;
}

.rbc-toolbar-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 16px;
}

.rbc-month-view {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.rbc-date-cell {
  padding: 8px 4px;
  text-align: right;
  font-weight: 500;
  color: #666;
}

.rbc-off-range-bg {
  color: #aaa;
}

.rbc-day-bg {
  border-right: 1px solid #eee;
  border-bottom: 1px solid #eee;
  min-height: 100px;
}

.rbc-time-slot {
  border-bottom: 1px solid #eee;
}

.rbc-timeslot-group {
  min-height: 80px;
  border-bottom: 1px solid #eee;
}

.rbc-current-time-indicator {
  background-color: #ff0000;
  height: 2px;
}

/* Tooltip personalizado */
.rbc-tooltip {
  background-color: #333;
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  z-index: 100;
}

/* Responsive */
@media (max-width: 768px) {
  .rbc-toolbar {
    flex-direction: column;
  }
  
  .rbc-toolbar-label {
    font-size: 14px;
    margin: 8px 0;
  }
  
  .rbc-event {
    font-size: 11px;
    padding: 2px 4px;
  }
  
  .rbc-day-bg {
    min-height: 60px;
  }
}

@media (max-width: 480px) {
  .rbc-header {
    padding: 8px 2px;
    font-size: 11px;
  }
  
  .rbc-event {
    font-size: 10px;
  }
  
  .rbc-date-cell {
    font-size: 12px;
  }
}

.calendar-wrapper {
  height: 600px;
  position: relative;
}

.calendar-loader {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 16px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
}
```

### Integración en PaddockPage

```javascript
export function PaddockPage() {
  const [selectedPasture, setSelectedPasture] = useState(null);
  
  return (
    <div className="paddock-page">
      <div className="paddock-main">
        <CalendarView
          farmId={farmId}
          selectedPasture={selectedPasture}
          onPastureSelect={setSelectedPasture}
        />
      </div>
      
      {selectedPasture && (
        <DetailPanel
          farmId={farmId}
          pastureId={selectedPasture}
          onClose={() => setSelectedPasture(null)}
        />
      )}
    </div>
  );
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`CalendarView.jsx`** - Nuevo componente con react-big-calendar
2. **`calendar.css`** - Estilos personalizados
3. **`CalendarView.test.jsx`** - Tests

### Archivos a Modificar

1. **`PaddockPage.jsx`** - Reemplazar CalendarView antigua
2. **`pastureCalendarService.js`** - Reutilizar (sin cambios mayores)
3. **`package.json`** - Agregar dependencias

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Instalar Dependencias
```bash
npm install react-big-calendar date-fns
```

### Paso 2: Crear CalendarView Base
- Importar Calendar de react-big-calendar
- Configurar localizer con date-fns
- Renderizar calendario

### Paso 3: Convertir Potreros a Eventos
- Mapear pastures a eventos
- Crear títulos formateados
- Agregar información (status, eta, color)

### Paso 4: Integrar Servicio
- GET /pastures?month=YYYY-MM
- Cargar datos según mes/vista

### Paso 5: Agregar Handlers
- Click en evento → DetailPanel
- Cambio de vista
- Cambio de período

### Paso 6: Personalizar Estilos
- Colores por estado
- Responsive design
- Tooltips

---

## 🧪 **Casos de Prueba**

### Test Unitarios

```javascript
describe('CalendarView', () => {
  
  test('renderiza vista mensual por defecto', () => {
    render(<CalendarView farmId="F001" />);
    expect(screen.getByText(/enero|diciembre|febrero/i)).toBeInTheDocument();
  });
  
  test('convierte potreros a eventos', () => {
    const pastures = [
      { id: 'P001', status: 'DISPONIBLE', eta: 0 }
    ];
    const events = createEventsFromPasture(pastures[0], 2026, 0);
    expect(events.length).toBeGreaterThan(0);
  });
  
  test('asigna color correcto por estado', () => {
    expect(getColorByStatus('DISPONIBLE')).toBe('#00aa00');
    expect(getColorByStatus('EN_DESCANSO')).toBe('#ff8800');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Ver Calendario Mensual
```gherkin
Scenario: Mostrar calendario mensual
  Given usuario en PaddockPage
  When se carga el calendario
  Then se muestra vista mensual con potreros
```

### Escenario 2: Cambiar Vistas
```gherkin
Scenario: Navegar entre vistas
  Given calendario visible
  When hace click en "Semana"
  Then vista cambia a semanal
```

### Escenario 3: Click en Evento
```gherkin
Scenario: Abrir DetailPanel desde evento
  Given evento en calendario
  When hace click
  Then DetailPanel se abre
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- react-big-calendar (npm)
- date-fns (npm)
- Locale es para date-fns

**Documentación**:
- https://jquense.github.io/react-big-calendar/
- https://date-fns.org/

---

## 🔧 **Refinamiento Técnico**

### react-big-calendar Integration

```javascript
import { Calendar, dayjsLocalizer } from 'react-big-calendar';
import dayjs from 'dayjs';

const localizer = dayjsLocalizer(dayjs);

export const BigCalendarView = ({ farmId, potreros }) => {
  const events = pasturesToEvents(potreros);
  
  const handleSelectEvent = (event) => {
    // Abrir DetailPanel
    showDetailPanel(event.resource);
  };
  
  return (
    <Calendar
      localizer={localizer}
      events={events}
      startAccessor="start"
      endAccessor="end"
      style={{ height: 500 }}
      onSelectEvent={handleSelectEvent}
      defaultView="month"
      views={['month', 'week', 'day', 'agenda']}
    />
  );
};
```

### Event Mapping

```javascript
export const pasturesToEvents = (potreros) => {
  return potreros.map(p => ({
    id: p.id,
    title: `${p.name} (${p.status})`,
    start: new Date(p.lastUseAt),
    end: new Date(p.lastUseAt),
    resource: p,
    backgroundColor: statusColorMap[p.status]
  }));
};
```

### Custom Styling

```css
.rbc-calendar {
  font-family: inherit;
}

.rbc-off-range-bg {
  background: #fafafa;
}

.rbc-event {
  padding: 4px;
  border-radius: 4px;
}

.status-DISPONIBLE { background-color: #dcfce7; }
.status-EN_USO { background-color: #fef3c7; }
.status-EN_DESCANSO { background-color: #fed7aa; }
```

### Testing Strategy

**Component Tests:**
- Todas las vistas funcionan (mes, semana, día, agenda)
- Click en evento abre DetailPanel
- Colores correctos por estado
- Responsive en móvil

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Dependencias instaladas (npm install)
- [ ] CalendarView.jsx creado
- [ ] react-big-calendar integrado
- [ ] Eventos se crean desde potreros
- [ ] Vista mensual funciona
- [ ] Vista semanal funciona
- [ ] Vista diaria funciona
- [ ] Vista agenda funciona
- [ ] Click en evento abre DetailPanel
- [ ] Colores por estado correctos
- [ ] Navegación (anterior, siguiente, hoy) funciona
- [ ] Personalización de estilos
- [ ] Responsive: desktop, tablet, mobile
- [ ] Integración con backend
- [ ] Tooltips funcionan
- [ ] Performance optimizado
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests componentes: todos los ACs

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Third-party Library Integration + Event Mapping + View Abstraction

**Justificación:** **Professional Calendar**: react-big-calendar es estándar industria. **Multiple Views**: Mes, semana, día, agenda. **Event Mapping**: Potreros → eventos. **Color Consistency**: Mantiene HU#010 colores. **Maintainability**: Separar lógica librería. **Performance**: Librería optimizada.

**Componentes Afectados:**

- **BigCalendar.jsx (Nuevo):** Wrapper de react-big-calendar. Props: `events`, `onSelectEvent`, `onNavigate`. State: `view`, `date`. Renderiza Calendar con toolbar custom.

- **eventMapper.js (Nuevo):** Convierte potreros a eventos. Función: `pasturesToEvents(pastures, farmId) → events[]`. Evento formato: `{ id, title, start, end, resource: pasture }`.

- **calendarEventStyles.js (Nuevo):** Estilos por estado. Mapeo: status → CSS classes. Color consistency con HU#010.

- **useCalendarEvents.js (Nuevo - Hook):** Obtiene eventos. Llama `useCalendarData` (HU#010). Mapea a eventos. Caching.

- **BigCalendarTooltip.jsx (Nuevo):** Popover custom. Props: `event`. Muestra: nombre, estado, ETA, acciones.

- **calendarLocalization.js (Nuevo):** i18n español. Meses, días semana en español. Formatos de fecha.

**Hitos:**
1. eventMapper.js + calendarEventStyles.js (mapping)
2. useCalendarEvents.js (data)
3. BigCalendarTooltip.jsx (tooltip)
4. BigCalendar.jsx (main)
5. calendarLocalization.js (i18n)

### Validación de Impacto

✅ **Professional UI**: react-big-calendar estándar
✅ **Multiple Views**: Mes/semana/día/agenda
✅ **Color Consistency**: Mismo sistema HU#010
✅ **Performance**: Librería optimizada
✅ **Maintainability**: Separación de concerns

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-010: Calendar custom (reemplaza)
- ✅ PASTURES-HU-012: Eventos (datos)
- → PASTURES-HU-015: react-big-calendar (esta)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** 3rd-party library integration (professional UI)

---

## ✅ **Definición de Completado**
- [ ] Accesibilidad completa
- [ ] Code review aprobado
- [ ] CI/CD green
- [ ] Sin warnings de linting

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
