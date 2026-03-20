# 🎨 Componentes Frontend: Milking

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentación detallada de cada componente Milking con código fuente, props, y cómo extender.

---

## 📚 Tabla de Contenidos

1. [Estructura del Módulo](#estructura-del-módulo)
2. [MilkingPage](#milkingpage)
3. [MilkingAdd](#milkingadd)
4. [MilkingTable](#milkingtable)
5. [BovineSelect](#bovineselect)
6. [Hooks Personalizados](#hooks-personalizados)
7. [Servicios](#servicios)

---

## Estructura del Módulo

```
cattle-front/src/components/Milking/
├── page/
│   ├── MilkingPage.jsx
│   └── MilkingPage.css
│
├── table/
│   ├── MilkingTable.jsx
│   └── MilkingTable.css
│
├── add/
│   ├── MilkingAdd.jsx
│   └── MilkingAdd.css
│
├── BovineSelect.jsx
├── BovineSelect.css
│
└── hooks/
    └── MilkingHooks.js
```

---

## MilkingPage

### 📍 Ubicación
`cattle-front/src/components/Milking/page/MilkingPage.jsx`

### 🎯 Responsabilidad
Componente raíz que coordina el flujo de Milking. Gestiona:
- Fetch de datos de lactancia
- Estado global (bovineId, records, loading)
- Renderizado de MilkingAdd y MilkingTable

### 📋 Props
```javascript
{
  bovineIdFromProp: string (opcional)  // ID de bovino inicial
}
```

### 🔄 Estado Local
```javascript
const {
  bovineId,          // ID bovino seleccionado
  setBovineId,       // Setter
  records,           // Array de FarmMilking
  loading,           // Estado de carga
  fetchData,         // Función para fetch
  onQuery            // Función para query
} = useMilkingRecords(bovineIdFromProp);

const {
  form,              // Objeto del formulario
  setForm,           // Setter
  onChange,          // Handler para input changes
  onSubmit           // Handler para submit
} = useMilkingForm(bovineId, fetchData);
```

### 📊 Código Completo

```jsx
import React from "react";
import { useMilkingRecords, useMilkingForm } from "../hooks/MilkingHooks";
import MilkingTable from "../table/MilkingTable";
import MilkingAdd from "../add/MilkingAdd";
import "./MilkingPage.css";

/**
 * Componente raíz del módulo Milking
 * 
 * Props:
 *   - bovineIdFromProp (optional): ID inicial de bovino
 */
export default function MilkingPage({ bovineIdFromProp }) {
  
  // Hook para gestionar records
  const {
    bovineId,
    setBovineId,
    records,
    loading,
    fetchData,
    onQuery,
  } = useMilkingRecords(bovineIdFromProp);

  // Hook para gestionar formulario
  const {
    form,
    setForm,
    onChange,
    onSubmit,
  } = useMilkingForm(bovineId, fetchData);

  return (
    <div>
      
      {/* SECCIÓN: Título */}
      <section>
        <h2>Lactancia</h2>
        <p>Registra la producción lechera diaria de los bovinos</p>
      </section>

      {/* SECCIÓN: Formulario para agregar */}
      <section>
        <MilkingAdd 
          form={form} 
          onChange={onChange} 
          onSubmit={onSubmit} 
        />
      </section>

      {/* SECCIÓN: Tabla de registros */}
      <section>        
        <MilkingTable
          records={records}
          bovineId={bovineId}
          setBovineId={setBovineId}
          onQuery={onQuery}
          loading={loading}
        />
      </section>
  
    </div>
  );
}
```

### 🔄 Flujo de Datos

```
MilkingPage (orquestador)
    │
    ├─ useMilkingRecords() → bovineId, records, fetchData, onQuery
    │
    ├─ useMilkingForm() → form, onChange, onSubmit
    │
    ├─ MilkingAdd (form, onChange, onSubmit)
    │       │
    │       └─ onSubmit → POST /milkingRecord → fetchData (refetch)
    │
    └─ MilkingTable (records, bovineId, setBovineId, onQuery, loading)
            │
            └─ onQuery → fetchData(bovineId)
```

---

## MilkingAdd

### 📍 Ubicación
`cattle-front/src/components/Milking/add/MilkingAdd.jsx`

### 🎯 Responsabilidad
Renderizar formulario en accordion para agregar nuevos registros de lactancia.

### 📋 Props
```javascript
{
  form: {
    bovineId: string,
    date: string,
    shift: string,          // "AM" o "PM"
    liters: string,
    status: string,         // "completo", "parcial", "omitido"
    recordedBy: string,
    observations: string
  },
  onChange: (e) => void,    // Handler para input changes
  onSubmit: (e) => void     // Handler para submit
}
```

### 📊 Código Completo

```jsx
import React, { useState } from "react";
import Button from "../../Shared/Button";
import BovineSelect from "../BovineSelect";
import "./MilkingAdd.css";

/**
 * Formulario para agregar registro de lactancia
 * Renderizado en accordion (expandible)
 * 
 * Props:
 *   - form: objeto con datos del formulario
 *   - onChange: handler para cambios de input
 *   - onSubmit: handler al guardar
 */
export default function MilkingAdd({ form, onChange, onSubmit }) {
  const [open, setOpen] = useState(false);

  return (
    <div className="milkingRecord-add-accordion">
      
      {/* BOTÓN: Expandir/Contraer */}
      <button 
        className="button-milkingRecord"
        onClick={() => setOpen((v) => !v)}>
        Nuevo registro
      </button>
      
      {/* CONTENEDOR: Accordion con animación */}
      <div
        className={`milkingRecord-add-panel${open ? " open" : ""}`}
        style={{
          maxHeight: open ? 900 : 0,
          overflow: "hidden",
          transition: "max-height 0.4s cubic-bezier(.4,0,.2,1)",
        }}
        aria-hidden={!open}>
            
        {/* FORMULARIO */}
        <form onSubmit={onSubmit}>
          
          {/* Campo: Bovino */}
          <div>
            <label htmlFor="bovineId">Bovino</label>
            <BovineSelect
              value={form.bovineId}
              onChange={onChange}
            />
          </div>

          {/* Campo: Fecha */}
          <div>
            <label htmlFor="date">Fecha</label>
            <input
              id="date"
              name="date"
              type="date"
              value={form.date}
              onChange={onChange}
              required
            />
          </div>

          {/* Campo: Turno */}
          <div>
            <label htmlFor="shift">Turno</label>
            <select 
              id="shift" 
              name="shift" 
              value={form.shift} 
              onChange={onChange}
              required>
              <option value="AM">Mañana (AM)</option>
              <option value="PM">Tarde (PM)</option>
            </select>
          </div>

          {/* Campo: Litros */}
          <div>
            <label htmlFor="liters">Litros</label>
            <input
              id="liters"
              name="liters"
              type="number"
              step="0.1"
              placeholder="ej: 15.5"
              value={form.liters}
              onChange={onChange}
              required
            />
          </div>

          {/* Campo: Estado */}
          <div>
            <label htmlFor="status">Estado del ordeno</label>
            <select 
              id="status" 
              name="status" 
              value={form.status} 
              onChange={onChange}
              required>
              <option value="completo">Completo</option>
              <option value="parcial">Parcial</option>
              <option value="omitido">Omitido</option>
            </select>
          </div>

          {/* Campo: Registrado por */}
          <div>
            <label htmlFor="recordedBy">Registrado por</label>
            <input
              id="recordedBy"
              name="recordedBy"
              placeholder="nombre del operario"
              value={form.recordedBy}
              onChange={onChange}
            />
          </div>

          {/* Campo: Observaciones */}
          <div className="observations-row">
            <label htmlFor="observations">Observaciones</label>
            <textarea
              id="observations"
              name="observations"
              placeholder="ej: Mastitis detectada, problemas respiratorios"
              value={form.observations}
              onChange={onChange}
              rows="3"
            />
          </div>

          {/* Botones: Acciones */}
          <div className="actions-row">
            <Button 
              variant="primary" 
              type="submit"
              onClick={(e) => {
                onSubmit(e);
                // Opcional: contraer accordion después de submit
                setOpen(false);
              }}>
              Guardar
            </Button>
            <Button 
              variant="secondary" 
              type="button"
              onClick={() => setOpen(false)}>
              Cancelar
            </Button>
          </div>

        </form>
      </div>
    </div>
  );
}
```

### 📋 Campos del Formulario

| Campo | Tipo | Requerido | Validación | Ejemplo |
|-------|------|-----------|-----------|---------|
| bovineId | select | ✅ | > 0 | 5 |
| date | date | ✅ | YYYY-MM-DD | 2025-12-10 |
| shift | select | ✅ | AM \| PM | AM |
| liters | number | ✅ | > 0 | 15.5 |
| status | select | ✅ | completo \| parcial \| omitido | completo |
| recordedBy | text | ❌ | string | jhonroberth |
| observations | textarea | ❌ | string | Normal |

### TO-DO: Mejoras

- [ ] Validación de liters > 0
- [ ] Validación de status en enum
- [ ] Mostrar errores de validación
- [ ] Desabilitar botón mientras se guarda
- [ ] Mostrar mensaje de éxito/error
- [ ] Limpiar formulario tras éxito

---

## MilkingTable

### 📍 Ubicación
`cattle-front/src/components/Milking/table/MilkingTable.jsx`

### 🎯 Responsabilidad
Mostrar tabla de registros agrupados por fecha, con columnas AM/PM.

### 📋 Props
```javascript
{
  records: [FarmMilking],      // Array de registros
  bovineId: string,            // ID bovino actual
  setBovineId: (id) => void,   // Setter
  onQuery: () => void,         // Callback para buscar
  loading: boolean             // Estado de carga
}
```

### 📊 Código Completo

```jsx
import React, { useMemo } from "react";
import { groupByDate } from "../../../utils/milkingUtils";
import Button from "../../Shared/Button";
import BovineSelect from "../BovineSelect";
import "./MilkingTable.css";

/**
 * Tabla de registros de lactancia
 * Agrupa por fecha y muestra AM/PM por separado
 * 
 * Props:
 *   - records: array de FarmMilking
 *   - bovineId: ID bovino seleccionado
 *   - setBovineId: setter para cambiar bovino
 *   - onQuery: callback para buscar
 *   - loading: estado de carga
 */
export default function MilkingTable({
  records = [],
  bovineId,
  setBovineId,
  onQuery,
  loading
}) {
  
  // Agrupar registros por fecha
  const rows = useMemo(() => groupByDate(records), [records]);

  return (
    <div>
      
      {/* SECCIÓN: Filtros */}
      <div className="milkingRecord-filter-row">
        <BovineSelect
          value={bovineId}
          onChange={e => setBovineId && setBovineId(e.target.value)}
        />
        <Button onClick={onQuery}>Buscar</Button>
      </div>

      {/* SECCIÓN: Tabla */}
      {loading ? (
        <p>Cargando…</p>
      ) : (
        <div className="milkingRecord-table-container">
          <table className="milkingRecord-table">
            
            {/* ENCABEZADOS */}
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Mañana (AM)</th>
                <th>Obs AM</th>
                <th>Tarde (PM)</th>
                <th>Obs PM</th>
                <th>Total día</th>
                <th>Acciones</th>
              </tr>
            </thead>

            {/* CUERPO */}
            <tbody>
              {rows.map(({ date, AM, PM }) => {
                const amLiters = AM?.liters ?? null;
                const pmLiters = PM?.liters ?? null;
                const total = (amLiters ?? 0) + (pmLiters ?? 0);

                return (
                  <tr key={date}>
                    {/* Celda: Fecha */}
                    <td className="milkingRecord-date">{date}</td>

                    {/* Celda: Litros AM */}
                    <td className="milkingRecord-liters">
                      {amLiters != null ? `${amLiters} L` : "—"}
                    </td>

                    {/* Celda: Observaciones AM */}
                    <td 
                      className="milkingRecord-obs"
                      title={AM?.observations || ""}>
                      {AM?.status ? AM.status : "—"}
                    </td>

                    {/* Celda: Litros PM */}
                    <td className="milkingRecord-liters">
                      {pmLiters != null ? `${pmLiters} L` : "—"}
                    </td>

                    {/* Celda: Observaciones PM */}
                    <td 
                      className="milkingRecord-obs"
                      title={PM?.observations || ""}>
                      {PM?.status ? PM.status : "—"}
                    </td>

                    {/* Celda: Total */}
                    <td className="milkingRecord-total">
                      <strong>{total.toFixed(2)} L</strong>
                    </td>

                    {/* Celda: Acciones */}
                    <td className="milkingRecord-actions">
                      <Button variant="small">Editar</Button>
                      <Button variant="small" className="danger">Eliminar</Button>
                    </td>
                  </tr>
                );
              })}

              {/* Fila: Sin registros */}
              {rows.length === 0 && (
                <tr>
                  <td colSpan="7" style={{ textAlign: "center", padding: 12 }}>
                    Sin registros
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
```

### 📋 Agrupación por Fecha

Función `groupByDate` (ubicada en `/utils/milkingUtils.js`):

```javascript
export function groupByDate(records) {
  const grouped = records.reduce((acc, record) => {
    if (!acc[record.date]) {
      acc[record.date] = { date: record.date, AM: null, PM: null };
    }
    
    if (record.shift === "AM") {
      acc[record.date].AM = record;
    } else if (record.shift === "PM") {
      acc[record.date].PM = record;
    }
    
    return acc;
  }, {});

  // Convertir a array ordenado por fecha
  return Object.values(grouped).sort((a, b) => 
    new Date(b.date) - new Date(a.date)
  );
}
```

### 📊 Estructura de Salida

```javascript
// Input
[
  { date: "2025-12-10", shift: "AM", liters: 15.5, status: "completo", ... },
  { date: "2025-12-10", shift: "PM", liters: 14.2, status: "completo", ... },
  { date: "2025-12-11", shift: "AM", liters: 16.0, status: "completo", ... }
]

// Output (grouped)
[
  {
    date: "2025-12-11",
    AM: { date: "2025-12-11", shift: "AM", liters: 16.0, ... },
    PM: null
  },
  {
    date: "2025-12-10",
    AM: { date: "2025-12-10", shift: "AM", liters: 15.5, ... },
    PM: { date: "2025-12-10", shift: "PM", liters: 14.2, ... }
  }
]
```

### TO-DO: Mejoras

- [ ] Botones Editar/Eliminar funcionales
- [ ] Paginación si hay muchos registros
- [ ] Filtro por rango de fechas
- [ ] Exportar a CSV
- [ ] Gráfico de tendencia
- [ ] Alertas visuales (producción baja, etc.)

---

## BovineSelect

### 📍 Ubicación
`cattle-front/src/components/Milking/BovineSelect.jsx`

### 🎯 Responsabilidad
Dropdown reutilizable para seleccionar bovino.

### 📋 Props
```javascript
{
  value: string,              // ID bovino seleccionado
  onChange: (e) => void       // Handler al cambiar
}
```

### 📊 Código

```jsx
import React from "react";
import "./BovineSelect.css";

/**
 * Dropdown reutilizable para seleccionar bovino
 * 
 * Props:
 *   - value: ID bovino seleccionado
 *   - onChange: callback al cambiar selección
 * 
 * TO-DO: Obtener bovinos de API en lugar de mock
 */
export default function BovineSelect({ value, onChange }) {
  
  // Mock: lista de bovinos disponibles
  const BOVINES = [
    { id: 5, name: "Bossy" },
    { id: 10, name: "Daisy" },
    { id: 15, name: "Molly" },
    { id: 20, name: "Buttercup" },
    { id: 25, name: "Clara" }
  ];

  return (
    <select 
      id="bovineId"
      name="bovineId"
      value={value} 
      onChange={onChange}
      required>
      <option value="">Selecciona bovino</option>
      {BOVINES.map(bovineIdentityItem => (
        <option key={bovineIdentityItem.id} value={bovineIdentityItem.id}>
          {bovineIdentityItem.name} (ID: {bovineIdentityItem.id})
        </option>
      ))}
    </select>
  );
}
```

### TO-DO: Mejoras

- [ ] Obtener bovinos de API
- [ ] Búsqueda/filtro en dropdown
- [ ] Mostrar más info (raza, edad, etc.)

---

## Hooks Personalizados

### useMilkingRecords

**Ubicación**: `cattle-front/src/components/Milking/hooks/MilkingHooks.js`

**Responsabilidad**: Gestionar fetch de registros de lactancia.

```javascript
export function useMilkingRecords(bovineIdFromProp) {
  const [bovineId, setBovineId] = useState(bovineIdFromProp || "");
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);

  // Función para fetch
  const fetchData = async (id) => {
    setLoading(true);
    try {
      const data = await getMilkingByBovineId(id);
      setRecords(data);
    } catch (e) {
      console.error(e);
      alert("Error cargando registros de milkingRecord");
    } finally {
      setLoading(false);
    }
  };

  // Fetch inicial si bovineIdFromProp
  useEffect(() => {
    if (bovineIdFromProp) {
      setBovineId(bovineIdFromProp);
      fetchData(bovineIdFromProp);
    }
  }, [bovineIdFromProp]);

  // Callback para buscar (validar + fetch)
  const onQuery = () => {
    if (!bovineId) return alert("Ingresa bovineId");
    fetchData(bovineId);
  };

  return { bovineId, setBovineId, records, loading, fetchData, onQuery };
}
```

### useMilkingForm

**Responsabilidad**: Gestionar estado del formulario.

```javascript
export function useMilkingForm(bovineId, fetchData) {
  const [form, setForm] = useState({
    bovineId: "",
    date: "",
    shift: "AM",
    liters: "",
    status: "completo",
    observations: "",
    recordedBy: "jhonroberth"
  });

  // Handler para cambios de input
  const onChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  // Handler para submit
  const onSubmit = async (e) => {
    e.preventDefault();
    
    const payload = {
      ...form,
      bovineId: Number(form.bovineId || bovineId)
    };

    // Validaciones
    if (!payload.bovineId || !payload.date || !payload.shift) {
      return alert("bovineId, date y shift son obligatorios");
    }

    try {
      await addMilkingRecord(payload);
      alert("Registro creado");
      
      // Refetch datos
      fetchData(payload.bovineId);
      
      // Limpiar formulario
      setForm(prev => ({ 
        ...prev, 
        liters: "", 
        observations: "",
        date: ""
      }));
    } catch (err) {
      console.error(err);
      alert("Error creando registro");
    }
  };

  return { form, setForm, onChange, onSubmit };
}
```

---

## Servicios

### milkingService.js

**Ubicación**: `cattle-front/src/services/milkingService.js`

```javascript
/**
 * Obtener registros de un bovino
 * @param {number} bovineId - ID del bovino
 * @returns {Promise<Array>} - Array de registros
 */
export async function getMilkingByBovineId(bovineId) {
  const response = await fetch(
    `https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/milkingRecord/${bovineId}`
  );
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return response.json();
}

/**
 * Crear nuevo registro de lactancia
 * @param {object} payload - Datos del registro
 * @returns {Promise<object>} - Registro creado
 */
export async function addMilkingRecord(payload) {
  const response = await fetch(
    `https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/milkingRecord`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }
  );
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return response.json();
}
```

### TO-DO: Mejoras

- [ ] Usar variable de entorno para URL
- [ ] Agregar timeout
- [ ] Retry logic
- [ ] Request interceptors

---

**Generado**: 2026-01-09 | **Versión**: 1.0
