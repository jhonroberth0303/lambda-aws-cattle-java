# ⚛️ Estándares Frontend: React/TypeScript

**Fecha**: 2026-01-09 | **Versión**: 1.0

## 🎯 Objetivo

Estándares específicos para código React/TypeScript/JavaScript en cattle-front.

---

## 📋 Tabla de Contenidos

1. [Setup del Proyecto](#setup-del-proyecto)
2. [Configuración de Linting](#configuración-de-linting)
3. [Patrones de Componentes](#patrones-de-componentes)
4. [Gestión de Estado](#gestión-de-estado)
5. [Patrones Avanzados](#patrones-avanzados)
6. [Estilos CSS](#estilos-css)
7. [Testing](#testing-frontend)
8. [Performance](#performance-frontend)

---

## Setup del Proyecto

### .eslintrc.json

```json
{
  "env": {
    "browser": true,
    "es2021": true,
    "jest": true
  },
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:react-hooks/recommended"
  ],
  "parserOptions": {
    "ecmaVersion": "latest",
    "sourceType": "module",
    "ecmaFeatures": {
      "jsx": true
    }
  },
  "rules": {
    "react/react-in-jsx-scope": "off",
    "react/prop-types": "off",
    "no-unused-vars": "warn",
    "no-console": "warn",
    "camelcase": "error",
    "eqeqeq": ["error", "always"],
    "no-eval": "error",
    "no-implied-eval": "error"
  },
  "settings": {
    "react": {
      "version": "detect"
    }
  }
}
```

### .prettierrc.json

```json
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": false,
  "printWidth": 80,
  "tabWidth": 2,
  "useTabs": false,
  "arrowParens": "avoid",
  "endOfLine": "lf"
}
```

### Jest Configuration

```javascript
// jest.config.js
module.exports = {
  testEnvironment: "jsdom",
  setupFilesAfterEnv: ["<rootDir>/src/setupTests.js"],
  moduleNameMapper: {
    "\\.(css|less|scss|sass)$": "identity-obj-proxy",
    "\\.(jpg|jpeg|png|gif|svg)$": "<rootDir>/__mocks__/fileMock.js"
  },
  collectCoverageFrom: [
    "src/**/*.{js,jsx,ts,tsx}",
    "!src/**/*.d.ts",
    "!src/index.js",
    "!src/reportWebVitals.js"
  ],
  coverageThresholds: {
    global: {
      branches: 75,
      functions: 75,
      lines: 75,
      statements: 75
    }
  }
};
```

---

## Patrones de Componentes

### ✅ Patrón: Functional Component + Hooks

```jsx
import React, { useState, useEffect, useCallback } from "react";
import PropTypes from "prop-types";
import "./BovineList.css";

/**
 * Lista de bovinos en grid
 * 
 * Features:
 * - Carga automática al montar
 * - Búsqueda y filtros
 * - Paginación
 * 
 * @returns {JSX.Element}
 */
function BovineList() {
  // Estados
  const [bovineIdentityItems, setBovines] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [page, setPage] = useState(1);

  // Cargar bovinos
  useEffect(() => {
    const fetchBovines = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const data = await bovinesAPI.getAll();
        setBovines(data);
      } catch (err) {
        setError(err.message || "Error cargando bovinos");
        console.error("Fetch error:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchBovines();
  }, []);

  // Filtrar bovineIdentityItems
  const filtered = bovineIdentityItems.filter(b =>
    b.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    b.bovineId.toString().includes(searchTerm)
  );

  // Handlers
  const handleSearch = useCallback((e) => {
    setSearchTerm(e.target.value);
    setPage(1); // Reset paginación
  }, []);

  const handleAddBovine = useCallback(() => {
    navigate("/add");
  }, []);

  // Render
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorBanner message={error} />;

  return (
    <div className="bovineIdentityItem-list">
      <header className="bovineIdentityItem-list-header">
        <h1>Bovinos</h1>
        <div className="bovineIdentityItem-list-controls">
          <input
            type="text"
            placeholder="Buscar por nombre o ID..."
            value={searchTerm}
            onChange={handleSearch}
            aria-label="Buscar bovinos"
          />
          <button onClick={handleAddBovine} aria-label="Agregar nuevo bovino">
            Nuevo Bovino
          </button>
        </div>
      </header>

      <section className="bovineIdentityItem-list-grid" role="main">
        {filtered.length === 0 ? (
          <div className="empty-state">No se encontraron bovinos</div>
        ) : (
          filtered.map(bovineIdentityItem => (
            <BovineCard key={bovineIdentityItem.bovineId} bovineIdentityItem={bovineIdentityItem} />
          ))
        )}
      </section>
    </div>
  );
}

export default BovineList;
```

### ✅ Patrón: Componente Controlado

```jsx
/**
 * Formulario de bovino (crear/editar)
 * 
 * Props:
 *   - initialData (object): datos para editar
 *   - onSubmit (function): callback al enviar
 *   - isLoading (boolean): estado de carga
 * 
 * @component
 */
function BovineForm({ initialData = {}, onSubmit, isLoading = false }) {
  const [formData, setFormData] = useState(initialData);
  const [errors, setErrors] = useState({});

  // Manejadores
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    setFormData(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));

    // Limpiar error del campo
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  const validate = () => {
    const newErrors = {};
    
    if (!formData.name?.trim()) {
      newErrors.name = "Nombre requerido";
    }
    
    if (!formData.gender) {
      newErrors.gender = "Género requerido";
    }
    
    if (!formData.bornDate) {
      newErrors.bornDate = "Fecha de nacimiento requerida";
    } else {
      const born = new Date(formData.bornDate);
      if (born > new Date()) {
        newErrors.bornDate = "Fecha no puede ser en el futuro";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validate()) return;

    try {
      await onSubmit(formData);
    } catch (err) {
      setErrors({ submit: err.message });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bovineIdentityItem-form" noValidate>
      {errors.submit && (
        <div className="form-error" role="alert">
          {errors.submit}
        </div>
      )}

      <fieldset>
        <legend>Información Básica</legend>

        <div className="form-group">
          <label htmlFor="name">Nombre *</label>
          <input
            id="name"
            name="name"
            type="text"
            value={formData.name || ""}
            onChange={handleChange}
            aria-invalid={!!errors.name}
            aria-describedby={errors.name ? "name-error" : undefined}
            required
          />
          {errors.name && (
            <span id="name-error" className="field-error">
              {errors.name}
            </span>
          )}
        </div>

        <div className="form-group">
          <label>Género *</label>
          <div className="radio-group">
            {["female", "male"].map(gender => (
              <label key={gender} className="radio-label">
                <input
                  type="radio"
                  name="gender"
                  value={gender}
                  checked={formData.gender === gender}
                  onChange={handleChange}
                  aria-invalid={!!errors.gender}
                  required
                />
                {gender === "female" ? "Hembra" : "Macho"}
              </label>
            ))}
          </div>
          {errors.gender && (
            <span className="field-error">{errors.gender}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="bornDate">Fecha de nacimiento *</label>
          <input
            id="bornDate"
            name="bornDate"
            type="date"
            value={formData.bornDate || ""}
            onChange={handleChange}
            aria-invalid={!!errors.bornDate}
            aria-describedby={errors.bornDate ? "date-error" : undefined}
            required
          />
          {errors.bornDate && (
            <span id="date-error" className="field-error">
              {errors.bornDate}
            </span>
          )}
        </div>
      </fieldset>

      <div className="form-actions">
        <button
          type="submit"
          disabled={isLoading}
          aria-busy={isLoading}
        >
          {isLoading ? "Guardando..." : "Guardar"}
        </button>
        <button type="reset">Limpiar</button>
      </div>
    </form>
  );
}
```

---

## Gestión de Estado

### ✅ Patrón: useReducer para Estado Complejo

```javascript
// Hook personalizado para estado de formulario
function useFormState(initialState) {
  const [state, dispatch] = useReducer(
    (state, action) => {
      switch (action.type) {
        case "CHANGE_FIELD":
          return {
            ...state,
            data: { ...state.data, [action.field]: action.value },
            errors: { ...state.errors, [action.field]: null }
          };

        case "VALIDATE":
          return {
            ...state,
            errors: validateForm(state.data),
            touched: true
          };

        case "SUBMIT_START":
          return { ...state, isLoading: true, submitError: null };

        case "SUBMIT_SUCCESS":
          return {
            ...state,
            isLoading: false,
            data: initialState,
            touched: false
          };

        case "SUBMIT_ERROR":
          return {
            ...state,
            isLoading: false,
            submitError: action.error
          };

        case "RESET":
          return { ...initialState };

        default:
          return state;
      }
    },
    {
      data: initialState,
      errors: {},
      isLoading: false,
      submitError: null,
      touched: false
    }
  );

  return {
    ...state,
    handleChange: (field, value) =>
      dispatch({ type: "CHANGE_FIELD", field, value }),
    handleValidate: () => dispatch({ type: "VALIDATE" }),
    handleSubmitStart: () => dispatch({ type: "SUBMIT_START" }),
    handleSubmitSuccess: () => dispatch({ type: "SUBMIT_SUCCESS" }),
    handleSubmitError: error =>
      dispatch({ type: "SUBMIT_ERROR", error }),
    reset: () => dispatch({ type: "RESET" })
  };
}
```

### ✅ Patrón: Context API para Global State

```javascript
// Crear contexto
const BovineContext = createContext(null);

// Provider
export function BovineProvider({ children }) {
  const [bovineIdentityItems, setBovines] = useState([]);
  const [selectedBovine, setSelectedBovine] = useState(null);
  const [loading, setLoading] = useState(false);

  const value = {
    bovineIdentityItems,
    selectedBovine,
    loading,
    setBovines,
    setSelectedBovine,
    setLoading
  };

  return (
    <BovineContext.Provider value={value}>
      {children}
    </BovineContext.Provider>
  );
}

// Hook para usar
export function useBovineContext() {
  const context = useContext(BovineContext);
  if (!context) {
    throw new Error("useBovineContext debe usarse dentro BovineProvider");
  }
  return context;
}

// En App.jsx
<BovineProvider>
  <App />
</BovineProvider>
```

---

## Patrones Avanzados

### ✅ Patrón: Compound Components

```jsx
/**
 * Card con estructura flexible
 * Uso:
 *   <Card>
 *     <Card.Header title="..." />
 *     <Card.Body>...</Card.Body>
 *     <Card.Footer>...</Card.Footer>
 *   </Card>
 */

function Card({ children, ...props }) {
  return <div className="card" {...props}>{children}</div>;
}

Card.Header = function CardHeader({ title, subtitle }) {
  return (
    <div className="card-header">
      <h2>{title}</h2>
      {subtitle && <p>{subtitle}</p>}
    </div>
  );
};

Card.Body = function CardBody({ children }) {
  return <div className="card-body">{children}</div>;
};

Card.Footer = function CardFooter({ children }) {
  return <div className="card-footer">{children}</div>;
};
```

### ✅ Patrón: Render Props

```jsx
/**
 * Componente de carga con render prop
 * Uso:
 *   <DataFetcher url="/bovineIdentityItems">
 *     {(data, loading, error) => (
 *       loading ? <Spinner /> : <List data={data} />
 *     )}
 *   </DataFetcher>
 */

function DataFetcher({ url, children }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(url);
        setData(await response.json());
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [url]);

  return children(data, loading, error);
}
```

### ✅ Patrón: Higher Order Component (HOC)

```jsx
/**
 * HOC para agregar loading state
 */

function withLoading(Component) {
  return function WithLoadingComponent({ isLoading, ...props }) {
    if (isLoading) return <LoadingSpinner />;
    return <Component {...props} />;
  };
}

// Uso
const BovineListWithLoading = withLoading(BovineList);
<BovineListWithLoading isLoading={loading} />
```

---

## Estilos CSS

### ✅ Convención: CSS Modules / BEM

```css
/* BovineCard.css */

/* Componente base */
.bovineIdentityItem-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  background: white;
  transition: box-shadow 0.3s ease;
}

/* Estado */
.bovineIdentityItem-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Elementos hijos */
.bovineIdentityItem-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.bovineIdentityItem-card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.bovineIdentityItem-card-id {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
}

.bovineIdentityItem-card-details {
  flex: 1;
  margin-bottom: 12px;
}

.bovineIdentityItem-card-detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.bovineIdentityItem-card-detail-label {
  font-weight: 500;
  color: #666;
}

.bovineIdentityItem-card-detail-value {
  color: #333;
}

/* Acciones */
.bovineIdentityItem-card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.bovineIdentityItem-card-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.bovineIdentityItem-card-btn:hover {
  background: #f5f5f5;
}

.bovineIdentityItem-card-btn--primary {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.bovineIdentityItem-card-btn--primary:hover {
  background: #0056b3;
}

/* Responsive */
@media (max-width: 768px) {
  .bovineIdentityItem-card {
    padding: 12px;
  }

  .bovineIdentityItem-card-title {
    font-size: 16px;
  }

  .bovineIdentityItem-card-actions {
    flex-direction: column;
  }

  .bovineIdentityItem-card-btn {
    width: 100%;
  }
}
```

---

## Testing Frontend

### ✅ Patrón: React Testing Library

```javascript
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import BovineForm from "./BovineForm";

describe("BovineForm", () => {
  it("debe mostrar mensaje de error si nombre está vacío", async () => {
    const mockSubmit = jest.fn();
    const user = userEvent.setup();

    render(<BovineForm onSubmit={mockSubmit} />);

    const submitBtn = screen.getByRole("button", { name: /guardar/i });
    await user.click(submitBtn);

    expect(screen.getByText(/nombre requerido/i)).toBeInTheDocument();
    expect(mockSubmit).not.toHaveBeenCalled();
  });

  it("debe llamar onSubmit con datos válidos", async () => {
    const mockSubmit = jest.fn().mockResolvedValue({ id: 1 });
    const user = userEvent.setup();

    render(<BovineForm onSubmit={mockSubmit} />);

    const nameInput = screen.getByLabelText(/nombre/i);
    const genderRadio = screen.getByLabelText(/hembra/i);
    const dateInput = screen.getByLabelText(/fecha/i);
    const submitBtn = screen.getByRole("button", { name: /guardar/i });

    await user.type(nameInput, "Estrella");
    await user.click(genderRadio);
    await user.type(dateInput, "2023-05-10");
    await user.click(submitBtn);

    await waitFor(() => {
      expect(mockSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          name: "Estrella",
          gender: "female",
          bornDate: "2023-05-10"
        })
      );
    });
  });

  it("debe mostrar estado de carga", () => {
    render(<BovineForm onSubmit={jest.fn()} isLoading={true} />);

    const submitBtn = screen.getByRole("button", { name: /guardando/i });
    expect(submitBtn).toBeDisabled();
  });
});
```

---

## Performance Frontend

### ✅ Optimizaciones Clave

```javascript
// 1. Memoización de componentes
const BovineCard = memo(({ bovineIdentityItem, onEdit }) => {
  return <article>{/* ... */}</article>;
});

// 2. Memoización de callbacks
const handleEdit = useCallback((id) => {
  navigate(`/edit/${id}`);
}, [navigate]);

// 3. Lazy loading de componentes
const MilkingModule = lazy(() => import("./Milking"));

<Suspense fallback={<Loading />}>
  <MilkingModule />
</Suspense>

// 4. Code splitting por ruta
const routes = [
  { path: "/bovineIdentityItems", element: lazy(() => import("./Bovines")) },
  { path: "/milkingRecord", element: lazy(() => import("./Milking")) }
];

// 5. Optimizar listas grandes
const BigList = ({ items }) => {
  const itemHeight = 60;
  const containerHeight = 400;

  return (
    <FixedSizeList
      height={containerHeight}
      itemCount={items.length}
      itemSize={itemHeight}
    >
      {({ index, style }) => (
        <div style={style}>
          {items[index].name}
        </div>
      )}
    </FixedSizeList>
  );
};

// 6. Debouncing de búsqueda
const searchBovines = useMemo(
  () => debounce((term) => {
    fetchBovines(term);
  }, 300),
  []
);

const handleSearch = (e) => {
  setSearchTerm(e.target.value);
  searchBovines(e.target.value);
};
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
