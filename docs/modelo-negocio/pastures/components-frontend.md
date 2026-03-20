# 🎨 Componentes Frontend: Paddock Dashboard

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentación detallada de cada componente del módulo Paddock con código fuente, propiedades, y cómo extender.

---

## 📚 Tabla de Contenidos

1. [Estructura del Módulo](#estructura-del-módulo)
2. [PaddockPage (Raíz)](#paddockpage-raíz)
3. [ListKpiCards](#listkpicards)
4. [PastureTable](#pasturetable)
5. [RotationSemaphore](#rotationsemaphore)
6. [CalendarMini](#calendarmini)
7. [AlertCenter](#alertcenter)
8. [DetailPanel](#detailpanel)
9. [Componentes Auxiliares](#componentes-auxiliares)
10. [Hooks Personalizados](#hooks-personalizados)

---

## Estructura del Módulo

```
cattle-front/src/components/Paddock/
├── page/
│   ├── PaddockPage.jsx         ← Componente raíz
│   ├── listKpiCards.jsx
│   ├── PaddockPage.css
│   └── listKpiCards.css
│
├── pastureTable/
│   ├── pastureTable.jsx
│   └── pastureTable.css
│
├── rotationSemaphore/
│   ├── rotationSemaphore.jsx
│   └── rotationSemaphore.css
│
├── calendarMini/
│   ├── calendarMini.jsx
│   └── calendarMini.css
│
├── alertCenter/
│   ├── alertCenter.jsx
│   └── alertCenter.css
│
├── detailPanel/
│   ├── detailPanel.jsx
│   └── detailPanel.css
│
├── drawer/
│   └── drawer.jsx              ← Componente reutilizable
│
├── StatusChip/
│   └── StatusChip.jsx          ← Componente reutilizable
│
├── section/
│   └── section.jsx             ← Componente reutilizable
│
├── kpiCard/
│   └── kpiCard.jsx             ← Componente reutilizable
│
├── hooks/
│   └── padockHooks.js          ← useFilteredPastures
│
├── paddockConstants/
│   └── paddockSelectOptions.js ← Constantes
│
└── mocks/
    └── mockPastures.js         ← Datos mock
```

---

## PaddockPage (Raíz)

### 📍 Ubicación
`cattle-front/src/components/Paddock/page/PaddockPage.jsx`

### 🎯 Responsabilidad
Componente raíz que coordina todo el dashboard. Gestiona:
- Fetch de datos del backend
- Estado global de filtros
- Selección de potrero
- Renderizado de todas las secciones

### 📋 Props
Ninguna. Componente self-contained.

### 🔄 Estado Local

```javascript
const [species, setSpecies] = useState("ALL");        // Filtro por especie
const [status, setStatus] = useState("ALL");          // Filtro por estado
const [query, setQuery] = useState("");               // Búsqueda textual
const [pastures, setPastures] = useState([]);         // Lista de potreros
const [selected, setSelected] = useState(null);       // Potrero seleccionado
```

### 🔌 Datos Fetched

```javascript
useEffect(() => {
  fetch("https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/farms/F001/pastures")
    .then(res => res.json())
    .then(data => setPastures(data))
    .catch(() => setPastures([]));
}, []);
```

**TO-DO**: Usar variable de entorno para URL.

### 📊 KPIs Calculados

```javascript
const kpiList = useMemo(() => [
  {
    title: "Hectáreas totales",
    value: `${pastures.reduce((a, p) => a + p.areaHa, 0).toFixed(2)} ha`,
  },
  {
    title: "Potreros disponibles",
    value: pastures.filter(p => p.status === "Disponible").length,
  },
  {
    title: "En uso",
    value: pastures.filter(p => p.status === "En uso").length,
  },
  {
    title: "En descanso",
    value: pastures.filter(p => p.status === "En descanso").length,
  },
], [pastures]);
```

### 🔍 Filtrado

```javascript
const filtered = useFilteredPastures(pastures, species, status, query);
```

Filtra por:
1. Especie (si ≠ "ALL")
2. Estado (si ≠ "ALL")
3. Búsqueda textual (nombre, notas)

### 🎨 Estructura de Renderizado

```jsx
<div className="paddockpage-root">
  <header>Título + Subtítulo</header>
  
  <div className="paddockpage-kpi-grid">
    <ListKpiCards kpis={kpiList} />
  </div>
  
  <Section title="Filtros">
    {/* Selectores de especie, estado, búsqueda */}
  </Section>
  
  <RotationSemaphore pastures={pastures} onOpen={handleOpen} />
  
  <Section title="Tabla de Potreros">
    <PastureTable rows={filtered} onOpen={handleOpen} />
  </Section>
  
  <Section title="Calendario">
    <CalendarMini />
  </Section>
  
  <Section title="Alertas">
    <AlertCenter />
  </Section>
  
  <DetailPanel pasture={selected} />  {/* Drawer lateral */}
</div>
```

### 🔄 Flujo de Interacción

```
Usuario selecciona potrero en tabla
    ↓
handleOpen(pasture) → setSelected(pasture)
    ↓
DetailPanel renderiza datos del potrero
```

---

## ListKpiCards

### 📍 Ubicación
`cattle-front/src/components/Paddock/page/listKpiCards.jsx`

### 🎯 Responsabilidad
Renderizar grid de tarjetas KPI.

### 📋 Props

```javascript
{
  kpis: [
    { title: string, value: string|number }
  ]
}
```

### 📊 Código

```jsx
import React from "react";
import KpiCard from "../kpiCard/kpiCard";
import "./listKpiCards.css";

export default function ListKpiCards({ kpis }) {
  return (
    <div className="listkpicards-root">
      {kpis.map(kpi => (
        <KpiCard key={kpi.title} title={kpi.title} value={kpi.value} />
      ))}
    </div>
  );
}
```

### 🧩 Componente Dependiente

**KpiCard**:
```jsx
// cattle-front/src/components/Paddock/kpiCard/kpiCard.jsx
export default function KpiCard({ title, value }) {
  return (
    <div className="kpicard-root">
      <div className="kpicard-title">{title}</div>
      <div className="kpicard-value">{value}</div>
    </div>
  );
}
```

---

## PastureTable

### 📍 Ubicación
`cattle-front/src/components/Paddock/pastureTable/pastureTable.jsx`

### 🎯 Responsabilidad
Renderizar tabla HTML de potreros con acciones.

### 📋 Props

```javascript
{
  rows: [
    {
      pastureId: string,
      name: string,
      species: string,
      areaHa: number,
      status: string,
      lastUseAt: string,
      daysRest: number,
      etaOpenDays: number,
      currentHeightCm: number,
      residualPrevCm: number
    }
  ],
  onOpen: (pasture) => void
}
```

### 📊 Código Completo

```jsx
import React from "react";
import "./pastureTable.css";
import { classNames, Chip } from "../StatusChip/StatusChip";
import StatusChip from "../StatusChip/StatusChip";

export default function PastureTable({ rows, onOpen }) {
  return (
    <div className="pasturetable-root">
      <table className="pasturetable-table">
        <thead className="pasturetable-thead">
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Especie</th>
            <th>Área (ha)</th>
            <th>Estado</th>
            <th>Último uso</th>
            <th>Descanso (d)</th>
            <th>ETA (d)</th>
            <th>Altura (cm)</th>
            <th>Residual (cm)</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((p, i) => (
            <tr key={p.pastureId} 
                className={classNames(
                  "pasturetable-row", 
                  i % 2 ? "pasturetable-row-even" : "pasturetable-row-odd"
                )}> 
              <td>{p.pastureId}</td>
              <td>{p.name}</td>
              <td><Chip label={p.species} /></td>
              <td>{p.areaHa.toFixed(2)}</td>
              <td><StatusChip status={p.status} /></td>
              <td>{p.lastUseAt}</td>
              <td>{p.daysRest}</td>
              <td>{p.etaOpenDays}</td>
              <td>{p.currentHeightCm}</td>
              <td>{p.residualPrevCm}</td>
              <td>
                <div className="pasturetable-actions">
                  <button onClick={() => onOpen(p)}>Detalle</button>
                  <button>Abrir</button>      {/* TO-DO: Conectar evento */}
                  <button>Cerrar</button>     {/* TO-DO: Conectar evento */}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

### 🔄 Acciones

| Botón | Acción Actual | TO-DO |
|-------|---------------|-------|
| Detalle | `onOpen(p)` → Abre DetailPanel | ✅ Funcional |
| Abrir | Sin implementación | POST evento OPEN |
| Cerrar | Sin implementación | POST evento CLOSE |

### 💡 Cómo Conectar Eventos

```javascript
// En PaddockPage
const handleOpen = (pasture) => setSelected(pasture);

const handleOpenPasture = (pasture) => {
  fetch(`/farms/F001/pastures/${pasture.pastureId}/events`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      eventType: "OPEN",
      user: "user@farm.com",
      lotId: "LOT001",
      animals: 15
    })
  })
  .then(res => res.json())
  .then(() => {
    // Refetch pastures
    setPastures([...pastures]);  // Trigger update
  })
  .catch(err => console.error(err));
};

// Pasar callback a PastureTable
<PastureTable rows={filtered} onOpen={handleOpen} onOpenPasture={handleOpenPasture} />
```

---

## RotationSemaphore

### 📍 Ubicación
`cattle-front/src/components/Paddock/rotationSemaphore/rotationSemaphore.jsx`

### 🎯 Responsabilidad
Mostrar semáforo visual (lista ordenada) de potreros por disponibilidad.

### 📋 Props

```javascript
{
  pastures: [
    {
      pastureId: string,
      name: string,
      species: string,
      status: string,
      etaOpenDays: number,
      currentHeightCm: number
    }
  ],
  onOpen: (pasture) => void
}
```

### 📊 Código Completo

```jsx
import React from "react";
import "./rotationSemaphore.css";
import StatusChip from "../StatusChip/StatusChip";
import { RULES } from "../mocks/mockPastures";

export default function RotationSemaphore({ pastures, onOpen }) {
  // Ordenar por estado prioritario y ETA
  const ordered = [...pastures].sort((a, b) => {
    const orderStatus = (s) => ({
      "Disponible": 0,      // Prioridad 0 (verde)
      "En uso": 1,          // Prioridad 1 (azul)
      "En descanso": 2,     // Prioridad 2 (gris)
      "Mantenimiento": 3    // Prioridad 3 (rojo)
    }[s]);
    
    const d = orderStatus(a.status) - orderStatus(b.status);
    if (d !== 0) return d;
    
    // Dentro del mismo estado, ordenar por ETA
    return a.etaOpenDays - b.etaOpenDays;
  });

  return (
    <ul className="rotationsem-root">
      {ordered.slice(0, 20).map(p => {
        const rule = RULES[p.species];
        const entryTarget = p.species === "CUBA22"
          ? `${rule.entryHeightCm} cm`
          : `${rule.entryHeightCm} cm / ${rule.restDaysMin} d`;
        
        return (
          <li key={p.pastureId} className="rotationsem-item">
            {/* Icono de semáforo */}
            <span className="rotationsem-icon">
              {p.status === "Disponible" ? "🟢" : 
               p.etaOpenDays <= 3 ? "🟡" : 
               p.status === "Mantenimiento" ? "🔴" : 
               "⚪"}
            </span>
            
            {/* Estado como chip */}
            <StatusChip status={p.status} />
            
            {/* Información principal */}
            <div className="rotationsem-main">
              <div className="rotationsem-title">
                {p.name}
                <span className="rotationsem-species">({p.species})</span>
              </div>
              <div className="rotationsem-meta">
                Altura {p.currentHeightCm} cm · 
                ETA apertura {p.etaOpenDays} d · 
                Objetivo: {entryTarget}
              </div>
            </div>
            
            {/* Botón de acción */}
            <button className="rotationsem-btn" onClick={() => onOpen(p)}>
              Detalle
            </button>
          </li>
        );
      })}
    </ul>
  );
}
```

### 🎨 Lógica de Colores

| Icono | Condición | Significado |
|-------|-----------|------------|
| 🟢 | status === "Disponible" | Listo para abrir |
| 🟡 | etaOpenDays <= 3 | Próximo a disponible |
| 🔴 | status === "Mantenimiento" | Bloqueado |
| ⚪ | Otros | Esperando |

### 📊 Límite
- Máximo 20 potreros mostrados: `.slice(0, 20)`

---

## CalendarMini

### 📍 Ubicación
`cattle-front/src/components/Paddock/calendarMini/calendarMini.jsx`

### 🎯 Responsabilidad
Mostrar calendario de próximas 7-10 días con tareas/eventos planificados.

### 📋 Props
Ninguna. Usa datos mock.

### 📊 Código Completo

```jsx
import React from "react";
import "./calendarMini.css";
import { TASKS } from "../mocks/mockPastures";

export default function CalendarMini() {
  // Agrupar tareas por fecha
  const grouped = TASKS.reduce((acc, t) => {
    (acc[t.date] ||= []).push(t);
    return acc;
  }, {});
  
  // Ordenar fechas
  const dates = Object.keys(grouped).sort();

  return (
    <div className="calendarm-root">
      <div className="calendarm-title">Calendario (próximos 7–10 días)</div>
      <ul className="calendarm-list">
        {dates.map(d => (
          <li key={d} className="calendarm-date">
            <div className="calendarm-date-label">{d}</div>
            <div className="calendarm-tasks">
              {grouped[d].map(t => (
                <div key={t.id} className="calendarm-task">
                  <span>{t.kind}</span>
                  <span className="calendarm-task-id">{t.pastureId}</span>
                </div>
              ))}
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

### 📊 Estructura de TASKS Mock

```javascript
export const TASKS = [
  {
    id: "T001",
    date: "2025-12-10",
    kind: "Fertilizar",
    pastureId: "P-01"
  },
  {
    id: "T002",
    date: "2025-12-11",
    kind: "Abrir pastoreo",
    pastureId: "P-02"
  },
  // ...
];
```

### TO-DO: Mejoras

1. Conectar con datos reales (API)
2. Permitir crear/editar tareas
3. Diferente color por tipo de tarea
4. Click en fecha → mostrar detalles

---

## AlertCenter

### 📍 Ubicación
`cattle-front/src/components/Paddock/alertCenter/alertCenter.jsx`

### 🎯 Responsabilidad
Mostrar centro de alertas/notificaciones del dashboard.

### 📋 Props
Ninguna. Usa datos mock.

### 📊 Código Completo

```jsx
import React from "react";
import "./alertCenter.css";
import { ALERTS } from "../mocks/mockPastures";
import { classNames } from "../StatusChip/StatusChip";

export default function AlertCenter() {
  return (
    <div className="alertcenter-root">
      <div className="alertcenter-title">Alertas</div>
      <ul className="alertcenter-list">
        {ALERTS.map(a => (
          <li key={a.id}
              className={classNames(
                "alertcenter-item",
                a.type === "error" && "alertcenter-error",
                a.type === "warning" && "alertcenter-warning",
                a.type === "info" && "alertcenter-info"
              )}>
            {a.text}
          </li>
        ))}
      </ul>
    </div>
  );
}
```

### 📊 Estructura de ALERTS Mock

```javascript
export const ALERTS = [
  { id: "A1", type: "info", text: "P-01 disponible para abrir" },
  { id: "A2", type: "warning", text: "P-03 fertilización vence el 15/12" },
  { id: "A3", type: "error", text: "P-05 en mantenimiento, altura < mínima" },
];
```

### 🎨 Tipos de Alertas

| Tipo | Color | Caso de Uso |
|------|-------|-----------|
| `info` | Azul | Información general |
| `warning` | Amarillo | Atención requerida |
| `error` | Rojo | Problema crítico |

### TO-DO: Mejoras

1. Conectar con datos reales
2. Generar alertas automáticas (ETA <= 0, holdUntil próximo, etc.)
3. Marcar como leída
4. Click en alerta → ir a potrero

---

## DetailPanel

### 📍 Ubicación
`cattle-front/src/components/Paddock/detailPanel/detailPanel.jsx`

### 🎯 Responsabilidad
Panel lateral (drawer) con detalles completos del potrero seleccionado.

### 📋 Props

```javascript
{
  pasture: {
    id: string,
    name: string,
    species: string,
    areaHa: number,
    status: string,
    lastUseAt: string,
    daysRest: number,
    etaOpenDays: number,
    currentHeightCm: number,
    residualPrevCm: number,
    notes: string
  }
}
```

### 📊 Código Completo

```jsx
import React from "react";
import "./detailPanel.css";
import { RULES } from "../mocks/mockPastures";

export default function DetailPanel({ pasture }) {
  const rule = RULES[pasture.species];
  const entryTarget = pasture.species === "CUBA22"
    ? `${rule.entryHeightCm} cm`
    : `${rule.entryHeightCm} cm / ${rule.restDaysMin} d`;

  return (
    <div className="detailpanel-root">
      
      {/* SECCIÓN: General */}
      <div className="detailpanel-section">
        <div className="detailpanel-label">General</div>
        <div className="detailpanel-grid">
          <div><span className="detailpanel-key">ID:</span> {pasture.id}</div>
          <div><span className="detailpanel-key">Nombre:</span> {pasture.name}</div>
          <div><span className="detailpanel-key">Especie:</span> {pasture.species}</div>
          <div><span className="detailpanel-key">Área:</span> {pasture.areaHa} ha</div>
          <div><span className="detailpanel-key">Estado:</span> {pasture.status}</div>
          <div><span className="detailpanel-key">Último uso:</span> {pasture.lastUseAt}</div>
        </div>
      </div>

      {/* SECCIÓN: Métricas */}
      <div className="detailpanel-section">
        <div className="detailpanel-label">Métricas</div>
        <div className="detailpanel-grid">
          <div><span className="detailpanel-key">Descanso:</span> {pasture.daysRest} días</div>
          <div><span className="detailpanel-key">ETA apertura:</span> {pasture.etaOpenDays} días</div>
          <div><span className="detailpanel-key">Altura actual:</span> {pasture.currentHeightCm} cm</div>
          <div><span className="detailpanel-key">Residual previo:</span> {pasture.residualPrevCm} cm</div>
        </div>
      </div>

      {/* SECCIÓN: Reglas (objetivos) */}
      <div className="detailpanel-section">
        <div className="detailpanel-label">Reglas (objetivos)</div>
        <div className="detailpanel-rules">
          Entrada objetivo: <b>{entryTarget}</b> · 
          Residual objetivo: <b>{rule.exitResidualCm} cm</b>
        </div>
        {pasture.species === "CUBA22" && (
          <div className="detailpanel-cut">
            Intervalo de corte: {rule.cutIntervalDays} días
          </div>
        )}
      </div>

      {/* SECCIÓN: Acciones Rápidas */}
      <div className="detailpanel-section">
        <div className="detailpanel-label">Acciones rápidas</div>
        <div className="detailpanel-actions">
          <button>Registrar inicio pastoreo</button>
          <button>Registrar cierre</button>
          <button>Programar fertilización</button>
          <button>Nueva tarea</button>
        </div>
      </div>

      {/* SECCIÓN: Notas (si existen) */}
      {!!pasture.notes && (
        <div className="detailpanel-section">
          <div className="detailpanel-label">Notas</div>
          <div className="detailpanel-notes">{pasture.notes}</div>
        </div>
      )}
    </div>
  );
}
```

### 🔄 Secciones

| Sección | Contenido | Editable |
|---------|-----------|----------|
| General | ID, nombre, especie, área, estado, último uso | ❌ No (lectura) |
| Métricas | Descanso, ETA, altura, residual | ❌ No (calculadas) |
| Reglas | Objetivos por especie | ❌ No (del plan) |
| Acciones | Botones para eventos | ✅ Sí (botones) |
| Notas | Campo de observaciones | ✅ Sí (TO-DO) |

### TO-DO: Mejoras

1. Conectar botones de acciones rápidas
2. Permitir editar notas
3. Mostrar historial de eventos
4. Botón Editar atributos (nombre, área, notas)
5. Botón Bloquear para mantenimiento

---

## Componentes Auxiliares

### StatusChip

**Ubicación**: `cattle-front/src/components/Paddock/StatusChip/StatusChip.jsx`

**Props**:
```javascript
{
  status: "EN_DESCANSO" | "DISPONIBLE" | "EN_USO" | "MANTENIMIENTO"
}
```

**Renderizado**: Etiqueta coloreada con icono.

### Drawer

**Ubicación**: `cattle-front/src/components/Paddock/drawer/drawer.jsx`

**Props**:
```javascript
{
  isOpen: boolean,
  onClose: () => void,
  children: ReactNode
}
```

**Uso**: Mostrar DetailPanel lateralmente.

### Section

**Ubicación**: `cattle-front/src/components/Paddock/section/section.jsx`

**Props**:
```javascript
{
  title: string,
  children: ReactNode
}
```

**Renderizado**: Título + contenido envuelto.

---

## Hooks Personalizados

### useFilteredPastures

**Ubicación**: `cattle-front/src/components/Paddock/hooks/padockHooks.js`

**Firma**:
```javascript
useFilteredPastures(pastures, species, status, query) → Array
```

**Lógica**:
```javascript
export function useFilteredPastures(pastures, species, status, query) {
  return useMemo(() => {
    return pastures.filter(p =>
      (species === "ALL" || p.species === species) &&
      (status === "ALL" || p.status === status) &&
      (query === "" || 
       p.name.toLowerCase().includes(query.toLowerCase()) ||
       p.notes?.toLowerCase().includes(query.toLowerCase()))
    );
  }, [pastures, species, status, query]);
}
```

**Retorna**: Array filtrado de potreros.

---

## 📝 Constantes

### paddockSelectOptions.js

```javascript
export const SPECIES_OPTIONS = [
  { value: "ALL", label: "Todas" },
  { value: "Kikuyo", label: "Kikuyo" },
  { value: "Pasto azul", label: "Pasto azul" },
  { value: "Trébol", label: "Trébol" },
  // ...
];

export const STATUS_OPTIONS = [
  { value: "ALL", label: "Todos" },
  { value: "EN_DESCANSO", label: "En descanso" },
  { value: "DISPONIBLE", label: "Disponible" },
  { value: "EN_USO", label: "En uso" },
  { value: "MANTENIMIENTO", label: "Mantenimiento" },
];
```

---

## 🔗 Referencias

- [pastures-overview.md](../pastures-overview.md): Documentación técnica
- [events-architecture.md](../events-architecture.md): Eventos de potreros
- [tasks-pending.md](../tasks-pending.md): Tareas de implementación

---

**Generado**: 2026-01-09 | **Versión**: 1.0
