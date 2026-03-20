# 🎨 Componentes Frontend: Bovines

**Fecha**: 2026-01-09

## 🎯 Objetivo

Documentación detallada de cada componente Bovines con código fuente, props, y cómo extender.

---

## 📚 Tabla de Contenidos

1. [Estructura del Módulo](#estructura-del-módulo)
2. [BovineList](#bovinelist)
3. [BovineCard](#bovinecard)
4. [AddBovine](#addbovine)
5. [BovineDetail](#bovinedetail)
6. [Hooks Personalizados](#hooks-personalizados)
7. [Servicios](#servicios)

---

## Estructura del Módulo

```
cattle-front/src/components/Bovines/
├── BovineList.jsx
├── BovineList.css
│
├── cards/
│   ├── BovineCard.jsx
│   └── BovineCard.css
│
├── forms/
│   ├── AddBovine.jsx
│   ├── AddBovine.css
│   ├── BovineDetail.jsx
│   └── EditBovineWrapper.jsx
│
├── hooks/
│   └── useBovineForm.ts
│
└── BovineCard.jsx
    └── BovineCard.css
```

---

## BovineList

### 📍 Ubicación
`cattle-front/src/components/Bovines/BovineList.jsx`

### 🎯 Responsabilidad
Componente raíz que listay renderiza grid de bovinos.

### 📋 Props
Ninguno. Componente self-contained.

### 🔄 Estado Local
```javascript
const [bovineIdentityItems, setBovines] = useState([]);  // Array de bovinos
```

### 📊 Código Completo

```jsx
import React, { useEffect, useState } from "react";
import BovineCard from "./cards/BovineCard";

/**
 * Lista de bovinos en grid
 * Fetch automático al montar
 */
function BovineList() {
  const [bovineIdentityItems, setBovines] = useState([]);

  useEffect(() => {
    // Fetch inicial
    fetch("https://44xpamzadd.execute-api.us-east-1.amazonaws.com/dev/bovineIdentityItems")
      .then((res) => res.json())
      .then((data) => setBovines(data))
      .catch((err) => console.error("Error fetching bovineIdentityItems:", err));
  }, []);

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">Bovinos</h2>
      
      {/* Grid responsive */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {bovineIdentityItems.map((bovineIdentityItem) => (
          <BovineCard key={bovineIdentityItem.bovineId} bovineIdentityItem={bovineIdentityItem} />
        ))}
      </div>

      {/* Mensaje cuando no hay bovineIdentityItems */}
      {bovineIdentityItems.length === 0 && (
        <div className="bg-white p-4 rounded shadow text-center text-gray-500 mt-4">
          No se encontraron bovinos
        </div>
      )}
    </div>
  );
}

export default BovineList;
```

### 🔄 Flujo de Datos

```
BovineList monta
    ↓
useEffect fetch GET /bovineIdentityItems
    ↓
setState(bovineIdentityItems)
    ↓
map bovineIdentityItems → BovineCard × N
```

### TO-DO: Mejoras

- [ ] Loading spinner mientras fetch
- [ ] Error handling
- [ ] Búsqueda/filtros
- [ ] Paginación
- [ ] Opción "Nuevo Bovino"

---

## BovineCard

### 📍 Ubicación
`cattle-front/src/components/Bovines/cards/BovineCard.jsx`

### 🎯 Responsabilidad
Renderizar tarjeta individual de bovino con acciones.

### 📋 Props
```javascript
{
  bovineIdentityItem: {
    bovineId: number,
    name: string,
    breed: string,
    gender: string,
    age: string,
    color: string,
    bornDate: string
  }
}
```

### 📊 Código Completo

```jsx
import React from "react";
import { useNavigate } from "react-router-dom";
import { ICON_MENU } from "../../../utils/icons.svg.jsx";
import "./BovineCard.css";

/**
 * Tarjeta individual de bovino
 * 
 * Props:
 *   - bovineIdentityItem: objeto con datos del bovino
 */
function BovineCard({ bovineIdentityItem }) {
  const navigate = useNavigate();

  return (
    <article className="bovineIdentityItem-card">
      
      {/* ENCABEZADO: Nombre e ID */}
      <header className="bovineIdentityItem-card-header">
        <span className="bovineIdentityItem-card-avatar">
          <span className="bovineIdentityItem-card-icon">{ICON_MENU}</span>
        </span>
        <div className="bovineIdentityItem-card-header-main">
          <span className="bovineIdentityItem-card-title">{bovineIdentityItem.name}</span>
          <span className="bovineIdentityItem-card-id">ID: {bovineIdentityItem.bovineId}</span>
        </div>
        <time className="bovineIdentityItem-card-date" dateTime={bovineIdentityItem.bornDate}>
          {formatBorn(bovineIdentityItem.bornDate)}
        </time>
      </header>

      {/* DETALLES: Información principal */}
      <section className="bovineIdentityItem-card-details" aria-label="Detalles del bovino">
        <dl>
          <div className="bovineIdentityItem-card-detail-row">
            <dt className="bovineIdentityItem-card-detail-label">Raza:</dt>
            <dd className="bovineIdentityItem-card-detail-value">{bovineIdentityItem.breed}</dd>
          </div>

          <div className="bovineIdentityItem-card-detail-row">
            <dt className="bovineIdentityItem-card-detail-label">Género:</dt>
            <dd className="bovineIdentityItem-card-detail-value">
              {bovineIdentityItem.gender === "female" ? "Hembra" : "Macho"}
            </dd>
          </div>

          <div className="bovineIdentityItem-card-detail-row">
            <dt className="bovineIdentityItem-card-detail-label">Color:</dt>
            <dd className="bovineIdentityItem-card-detail-value capitalize">{bovineIdentityItem.color}</dd>
          </div>

          <div className="bovineIdentityItem-card-detail-row">
            <dt className="bovineIdentityItem-card-detail-label">Edad:</dt>
            <dd className="bovineIdentityItem-card-detail-value">{bovineIdentityItem.age}</dd>
          </div>
        </dl>
      </section>

      {/* ACCIONES: Botones */}
      <nav className="bovineIdentityItem-card-actions" aria-label="Acciones">
        <button 
          className="bovineIdentityItem-card-btn view" 
          onClick={() => navigate(`/detail/${bovineIdentityItem.bovineId}`)}>
          Ver
        </button>
        <button 
          className="bovineIdentityItem-card-btn edit" 
          onClick={() => navigate(`/edit/${bovineIdentityItem.bovineId}`)}>
          Editar
        </button>
      </nav>
    </article>
  );
}

/**
 * Formatea fecha de nacimiento a formato local
 */
function formatBorn(born) {
  if (!born) return "-";
  try {
    const d = new Date(born);
    return d.toLocaleDateString();
  } catch (e) {
    return born;
  }
}

export default BovineCard;
```

### 🎨 Estructura

| Sección | Contenido |
|---------|-----------|
| Avatar | Icono (placeholder) |
| Nombre | Nombre del bovino |
| ID | Número identificador |
| Fecha | Fecha de nacimiento |
| Raza | Breed |
| Género | Female/Male |
| Color | Descripción de color |
| Edad | Calculada automáticamente |
| Botones | Ver, Editar (navegar) |

---

## AddBovine

### 📍 Ubicación
`cattle-front/src/components/Bovines/forms/AddBovine.jsx`

### 🎯 Responsabilidad
Formulario completo para crear/editar bovinos.

### 📋 Props
```javascript
{
  onBovineAdded: (bovineIdentityItem) => void,  // Callback después de crear
  initialBovine: object (optional)   // Datos para editar
}
```

### 📊 Código Simplificado

```jsx
import React from "react";
import { ICON_SCAN, ICON_ADD, ICON_CLEAN } from "../../../utils/icons.svg.jsx";
import { useBovineForm } from "../hooks/useBovineForm.ts";
import { today } from "../../../utils/date.ts";
import "./AddBovine.css";

/**
 * Formulario para crear/editar bovino
 * 
 * Props:
 *   - onBovineAdded: callback
 *   - initialBovine: datos iniciales (edit mode)
 */
const AddBovine = ({ onBovineAdded, initialBovine }) => {
  const {
    formData,
    setFormData,
    scanning,
    saving,
    ageLabel,
    handleChange,
    handleScanTag,
    handleSubmit,
    maleStatuses,
    femaleStatuses,
    breeds,
    genders,
    isEditMode,
  } = useBovineForm({ onBovineAdded, initialBovine });

  return (
    <div className="add-bovineIdentityItem-container">
      
      {/* ENCABEZADO */}
      <header className="add-bovineIdentityItem-header">
        <h1>{isEditMode ? "Editar animal" : "Añadir animal"}</h1>
        <p>Registra un bovino en la base de datos. Los campos marcados con * son obligatorios.</p>
      </header>

      {/* RESUMEN: Edad e información rápida */}
      <div className="add-bovineIdentityItem-summary">
        <div className="add-bovineIdentityItem-summary-flex">
          <div>
            <div className="add-bovineIdentityItem-summary-label">Edad estimada</div>
            <div className="add-bovineIdentityItem-summary-value">{ageLabel || "—"}</div>
          </div>
          <div className="add-bovineIdentityItem-summary-actions">
            
            {/* Checkbox: Activo/Inactivo */}
            <label className="add-bovineIdentityItem-summary-checkbox">
              <input
                type="checkbox"
                name="enabled"
                checked={!!formData.enabled}
                onChange={handleChange}
              />
              Activo
            </label>

            {/* Botón: Escanear arete */}
            <button
              type="button"
              onClick={handleScanTag}
              disabled={scanning}
              aria-busy={scanning}
              title="Escanear arete / RFID"
              className="add-bovineIdentityItem-scan-btn"
            >
              <span className={`add-bovineIdentityItem-scan-icon${scanning ? " add-bovineIdentityItem-scan-spin" : ""}`}>
                {ICON_SCAN}
              </span>
              Escanear arete
            </button>
          </div>
        </div>
      </div>

      {/* FORMULARIO */}
      <form onSubmit={handleSubmit} className="add-bovineIdentityItem-form">
        
        {/* SECCIÓN 1: Nombre y género */}
        <section className="add-bovineIdentityItem-form-section">
          <div className="add-bovineIdentityItem-form-row add-bovineIdentityItem-form-row--full">
            <label htmlFor="name">Nombre *</label>
            <input
              id="name"
              type="text"
              name="name"
              placeholder="Ej. Estrella"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>

          <div className="add-bovineIdentityItem-form-row">
            <label>Sexo *</label>
            <div className="add-bovineIdentityItem-gender-group">
              {genders.map((g) => (
                <label key={g} className="add-bovineIdentityItem-gender-label">
                  <input
                    type="radio"
                    name="gender"
                    value={g}
                    checked={formData.gender === g}
                    onChange={handleChange}
                    required
                  />
                  {g === "female" ? "Hembra" : "Macho"}
                </label>
              ))}
            </div>
          </div>

          <div className="add-bovineIdentityItem-form-row">
            <label htmlFor="status">Estado reproductivo</label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleChange}
            >
              {(formData.gender === "female" ? femaleStatuses : maleStatuses).map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
        </section>

        {/* SECCIÓN 2: Información biológica */}
        <section className="add-bovineIdentityItem-form-section">
          <div className="add-bovineIdentityItem-form-row">
            <label htmlFor="bornDate">Fecha de nacimiento *</label>
            <input
              id="bornDate"
              type="date"
              name="bornDate"
              max={today()}
              value={formData.bornDate}
              onChange={handleChange}
              required
            />
          </div>

          <div className="add-bovineIdentityItem-form-row">
            <label htmlFor="breed">Raza</label>
            <select
              id="breed"
              name="breed"
              value={formData.breed}
              onChange={handleChange}
            >
              {breeds.map((b) => (
                <option key={b} value={b}>{b}</option>
              ))}
            </select>
          </div>

          <div className="add-bovineIdentityItem-form-row">
            <label htmlFor="color">Color</label>
            <input
              id="color"
              type="text"
              name="color"
              placeholder="Ej. negro y blanco"
              value={formData.color}
              onChange={handleChange}
            />
          </div>
        </section>

        {/* SECCIÓN 3: Parentaje (opcional) */}
        <section className="add-bovineIdentityItem-form-section">
          <h3>Parentaje (opcional)</h3>

          <div className="add-bovineIdentityItem-form-row">
            <label htmlFor="fatherNameSnapshot">Padre</label>
            <input
              id="fatherNameSnapshot"
              type="text"
              name="fatherNameSnapshot"
              placeholder="Nombre del padre"
              value={formData.fatherNameSnapshot}
              onChange={handleChange}
            />
          </div>

          <div className="add-bovineIdentityItem-form-row">
            <label htmlFor="motherNameSnapshot">Madre</label>
            <input
              id="motherNameSnapshot"
              type="text"
              name="motherNameSnapshot"
              placeholder="Nombre de la madre"
              value={formData.motherNameSnapshot}
              onChange={handleChange}
            />
          </div>
        </section>

        {/* SECCIÓN 4: RFID */}
        <section className="add-bovineIdentityItem-form-section">
          <div className="add-bovineIdentityItem-form-row add-bovineIdentityItem-form-row--full">
            <label htmlFor="tag">Arete / RFID</label>
            <input
              id="tag"
              type="text"
              name="tag"
              placeholder="Código del arete"
              value={formData.tag}
              onChange={handleChange}
            />
          </div>
        </section>

        {/* BOTONES DE ACCIÓN */}
        <div className="add-bovineIdentityItem-form-actions">
          <button 
            type="submit" 
            disabled={saving}
            className="add-bovineIdentityItem-btn-submit">
            {saving ? "Guardando..." : isEditMode ? "Actualizar" : "Guardar"}
          </button>
          <button 
            type="reset" 
            className="add-bovineIdentityItem-btn-reset">
            Limpiar
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddBovine;
```

### 📋 Campos del Formulario

| Campo | Tipo | Requerido | Validación | Ejemplo |
|-------|------|-----------|-----------|---------|
| name | text | ✅ | string | Estrella |
| gender | radio | ✅ | female \| male | female |
| status | select | ❌ | enum por género | OPEN |
| bornDate | date | ✅ | ≤ hoy | 2023-05-10 |
| breed | select | ❌ | enum | Holstein |
| color | text | ❌ | string | black & white |
| fatherNameSnapshot | text | ❌ | string | Bull01 |
| motherNameSnapshot | text | ❌ | string | Daisy |
| tag | text | ❌ | RFID code | ABC123 |
| enabled | checkbox | ❌ | boolean | true |

### TO-DO: Mejoras

- [ ] Validaciones mejoradas
- [ ] Mensajes de error en campo
- [ ] Confirmación antes de cambios
- [ ] Preview de imagen
- [ ] Selector de padre/madre (autocomplete)

---

## BovineDetail

### 📍 Ubicación
`cattle-front/src/components/Bovines/forms/BovineDetail.jsx`

### 🎯 Responsabilidad
Vista detalle (lectura) de un bovino con toda la información.

### 📋 Props
```javascript
{
  bovineId: string | number  // ID del bovino
}
```

### 📊 Código Básico

```jsx
import React from "react";
import { useBovineDetail } from "../hooks/useBovineForm.ts";
import { useNavigate } from "react-router-dom";

/**
 * Detalle completo de un bovino (lectura)
 */
function BovineDetail({ bovineId }) {
  const navigate = useNavigate();
  const { bovineIdentityItem, loading, error } = useBovineDetail(bovineId);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!bovineIdentityItem) return <div>No encontrado</div>;

  return (
    <div className="bovineIdentityItem-detail">
      <header>
        <h1>{bovineIdentityItem.name}</h1>
        <button onClick={() => navigate(-1)}>← Volver</button>
        <button onClick={() => navigate(`/edit/${bovineIdentityItem.bovineId}`)}>Editar</button>
      </header>

      <section>
        <h2>Información General</h2>
        <dl>
          <div><dt>ID:</dt><dd>{bovineIdentityItem.bovineId}</dd></div>
          <div><dt>Nombre:</dt><dd>{bovineIdentityItem.name}</dd></div>
          <div><dt>Género:</dt><dd>{bovineIdentityItem.gender === "female" ? "Hembra" : "Macho"}</dd></div>
          <div><dt>Raza:</dt><dd>{bovineIdentityItem.breed}</dd></div>
          <div><dt>Color:</dt><dd>{bovineIdentityItem.color}</dd></div>
        </dl>
      </section>

      <section>
        <h2>Datos Biológicos</h2>
        <dl>
          <div><dt>Fecha de nacimiento:</dt><dd>{bovineIdentityItem.bornDate}</dd></div>
          <div><dt>Edad:</dt><dd>{bovineIdentityItem.age}</dd></div>
          <div><dt>Estado:</dt><dd>{bovineIdentityItem.status}</dd></div>
        </dl>
      </section>

      <section>
        <h2>Parentaje</h2>
        <dl>
          <div><dt>Padre:</dt><dd>{bovineIdentityItem.fatherNameSnapshot || "—"}</dd></div>
          <div><dt>Madre:</dt><dd>{bovineIdentityItem.motherNameSnapshot || "—"}</dd></div>
        </dl>
      </section>

      <section>
        <h2>Identificación</h2>
        <dl>
          <div><dt>Arete RFID:</dt><dd>{bovineIdentityItem.tag || "—"}</dd></div>
          <div><dt>Activo:</dt><dd>{bovineIdentityItem.enabled ? "Sí" : "No"}</dd></div>
        </dl>
      </section>

      <section>
        <h2>Auditoría</h2>
        <dl>
          <div><dt>Creado:</dt><dd>{bovineIdentityItem.createdAt}</dd></div>
          <div><dt>Actualizado:</dt><dd>{bovineIdentityItem.updatedAt}</dd></div>
        </dl>
      </section>
    </div>
  );
}

export default BovineDetail;
```

---

## Hooks Personalizados

### useBovineForm

**Ubicación**: `cattle-front/src/components/Bovines/hooks/useBovineForm.ts`

**Responsabilidad**: Gestionar estado del formulario, validaciones, submit.

```typescript
export function useBovineForm({ onBovineAdded, endpoint, initialBovine }) {
  const [formData, setFormData] = useState<BovineFormData>({
    bovineId: "",
    name: "",
    gender: "female",
    bornDate: today(),
    age: "",
    breed: "Holstein",
    color: "",
    fatherNameSnapshot: "",
    motherNameSnapshot: "",
    autoId: true,
    enabled: true,
    status: "OPEN",
    tag: "",
  });

  const [scanning, setScanning] = useState(false);
  const [saving, setSaving] = useState(false);
  const isEditMode = !!(initialBovine && initialBovine.bovineId);

  // Cargar datos iniciales en edit mode
  useEffect(() => {
    if (initialBovine) {
      setFormData(prev => ({ ...prev, ...initialBovine }));
    }
  }, [initialBovine]);

  // Handler para input changes
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));
  };

  // Simular escaneo RFID
  const handleScanTag = async () => {
    setScanning(true);
    // Simular delay de escaneo
    setTimeout(() => {
      setFormData(prev => ({
        ...prev,
        tag: "SCANNED_" + Date.now()  // Mock
      }));
      setScanning(false);
    }, 1000);
  };

  // Calcular edad automáticamente
  const ageLabel = useMemo(() => {
    if (!formData.bornDate) return "";
    // Calcular edad en años/meses
    const born = new Date(formData.bornDate);
    const now = new Date();
    const years = now.getFullYear() - born.getFullYear();
    const months = now.getMonth() - born.getMonth();
    return `${years} años ${months} meses`;
  }, [formData.bornDate]);

  // Submit: crear o actualizar
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      if (isEditMode) {
        // PUT
        const response = await updateBovine(formData.bovineId, formData);
        onBovineAdded?.(response);
      } else {
        // POST
        const response = await createBovine(formData);
        onBovineAdded?.(response);
      }
    } catch (err) {
      console.error(err);
      alert("Error: " + err.message);
    } finally {
      setSaving(false);
    }
  };

  return {
    formData,
    setFormData,
    scanning,
    saving,
    ageLabel,
    handleChange,
    handleScanTag,
    handleSubmit,
    maleStatuses: ["BULL", "STEER", "CALF"],
    femaleStatuses: ["OPEN", "PREGNANT", "DRY", "LACTATING"],
    breeds: ["Holstein", "Jersey", "Angus", "Brahman"],
    genders: ["female", "male"],
    isEditMode,
  };
}
```

### useBovineDetail

**Responsabilidad**: Fetch detalle de un bovino.

```typescript
export function useBovineDetail(id?: string | number) {
  const [bovineIdentityItem, setBovine] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    let mounted = true;
    setLoading(true);
    setError(null);

    const fetchBovine = async () => {
      try {
        const data = await getBovineById(id);
        if (mounted) setBovine(data);
      } catch (err: any) {
        if (mounted) setBovine(null);
        if (mounted) setError(err?.message || "Error al cargar");
      } finally {
        if (mounted) setLoading(false);
      }
    };

    fetchBovine();
    return () => { mounted = false; };
  }, [id]);

  return { bovineIdentityItem, loading, error };
}
```

---

## Servicios

### bovinesServices.js

**Ubicación**: `cattle-front/src/services/bovinesServices.js`

```javascript
const API_BASE = "https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev";

/**
 * Obtener lista de todos los bovinos
 */
export async function getBovinesEndpoint() {
  const response = await fetch(`${API_BASE}/bovineIdentityItems`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}

/**
 * Obtener bovino por ID
 */
export async function getBovineById(id) {
  const response = await fetch(`${API_BASE}/bovineIdentityItems/${id}`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}

/**
 * Crear nuevo bovino
 */
export async function createBovine(payload) {
  const response = await fetch(`${API_BASE}/bovineIdentityItems`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}

/**
 * Actualizar bovino
 */
export async function updateBovine(id, payload) {
  const response = await fetch(`${API_BASE}/bovineIdentityItems/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ bovineId: id, ...payload })
  });
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
