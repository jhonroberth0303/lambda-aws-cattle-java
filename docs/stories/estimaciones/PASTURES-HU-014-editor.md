# 🌱 PASTURES-HU#14: Frontend: EditorPanel para Editar Potrero

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Form Component + Real-time Validation | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero crear un EditorPanel que permita editar información de potreros, de tal forma que:

1. Se muestre un formulario para editar campos (nombre, descripción, área, carga animal)
2. Se validen todos los campos en tiempo real
3. Se muestren errores claros si hay problemas
4. Se pueda cancelar la edición sin guardar
5. Se integre con el endpoint PUT /farms/{farmId}/pastures/{pastureId}
6. Se muestre loading y success/error states
7. Se actualice automáticamente en calendar y detailpanel

Esto habilitará que operarios actualicen información de potreros, mejorando precisión de datos y facilidad de uso.

---

## 🎯 **Criterios de Aceptación**

### AC#1: EditorPanel Renderiza Correctamente
```gherkin
Scenario: Mostrar formulario de edición
  Given usuario abre DetailPanel de potrero P001
  And hace click en botón "Editar"
  When EditorPanel se abre
  Then:
    [ ] Se muestra en panel lado derecho o modal
    [ ] Campos pre-llenados con valores actuales
    [ ] Botones: "Guardar", "Cancelar"
    [ ] Validación en tiempo real
    [ ] Sin errores
```

### AC#2: Campos Editables
```gherkin
Scenario: Editar campos de potrero
  Given EditorPanel abierto para P001
  When usuario modifica:
    | Campo | Valor Anterior | Valor Nuevo |
    | Nombre | P001 | Potrero 1 |
    | Descripción | - | Pasto de prueba |
    | Área (ha) | 5 | 6 |
    | Carga (animales) | 20 | 25 |
  Then:
    [ ] Campos se actualizan en tiempo real
    [ ] Cambios reflejados en inputs
    [ ] Sin delay
```

### AC#3: Validación en Tiempo Real
```gherkin
Scenario: Validar campos mientras escribe
  Given usuario en EditorPanel
  When escribe en campo "Nombre":
    [ ] Nombre vacío: ERROR "Nombre es requerido"
    [ ] Nombre < 3 caracteres: WARNING "Nombre muy corto"
    [ ] Nombre > 100 caracteres: ERROR "Nombre muy largo"
    [ ] Nombre válido (3-100): ✓ verde
  
  When escribe en campo "Área (ha)":
    [ ] Área = 0: ERROR "Área debe ser > 0"
    [ ] Área = -5: ERROR "Área no puede ser negativa"
    [ ] Área = 0.5: ✓ válido
    [ ] Área = 1000: ✓ válido
  
  When escribe en campo "Carga (animales)":
    [ ] Carga = 0: WARNING "Sin animales?"
    [ ] Carga = -10: ERROR "Carga no puede ser negativa"
    [ ] Carga = 5000: WARNING "¿Carga muy alta para el área?"
    [ ] Carga válida: ✓ verde
```

### AC#4: Botón Guardar
```gherkin
Scenario: Guardar cambios exitosamente
  Given EditorPanel con cambios válidos
  When usuario hace click "Guardar"
  Then:
    [ ] Se muestra loading spinner
    [ ] PUT /farms/F001/pastures/P001 se envía
    [ ] Status: 200 OK retorna
    [ ] Se muestra mensaje: "Potrero actualizado ✓"
    [ ] EditorPanel se cierra automáticamente
    [ ] DetailPanel se actualiza con nuevos datos
    [ ] Calendar se actualiza si es necesario
```

### AC#5: Botón Cancelar
```gherkin
Scenario: Cancelar sin guardar
  Given EditorPanel con cambios sin guardar
  When usuario hace click "Cancelar"
  Then:
    [ ] Cambios se descartan (sin guardar)
    [ ] EditorPanel se cierra
    [ ] DetailPanel sigue mostrando datos antiguos
    [ ] Sin error
```

### AC#6: Manejo de Errores
```gherkin
Scenario: Manejar error al guardar
  Given cambios válidos en EditorPanel
  When PUT al backend falla (500 error)
  Then:
    [ ] Se muestra error rojo: "Error al guardar - Intenta de nuevo"
    [ ] Loading se detiene
    [ ] EditorPanel sigue abierto
    [ ] Usuario puede reintentar
  
  When conexión de red falla
  Then:
    [ ] Error: "Sin conexión - Verifica tu internet"
```

### AC#7: Deshabilitación Condicional de Botones
```gherkin
Scenario: Botón Guardar disabled según estado
  Given EditorPanel vacío (sin cambios)
  When no hay cambios respecto a valores originales
  Then:
    [ ] Botón "Guardar" está disabled (gris)
    [ ] Tooltip: "Sin cambios para guardar"
  
  When hay cambios pero validación falla
  Then:
    [ ] Botón "Guardar" está disabled
    [ ] Rojo o gris indicando error
  
  When cambios válidos y hay diferencia
  Then:
    [ ] Botón "Guardar" está enabled (azul)
    [ ] Click abre operación
```

### AC#8: Campos Sólo Lectura
```gherkin
Scenario: Algunos campos no son editables
  Given EditorPanel abierto
  Then:
    [ ] Campo "ID": sólo lectura (P001)
    [ ] Campo "Estado": sólo lectura (DISPONIBLE)
    [ ] Campo "Fecha de Creación": sólo lectura
    [ ] Otros editables: Nombre, Descripción, Área, Carga
```

### AC#9: Comparación Antes/Después
```gherkin
Scenario: Mostrar qué cambió
  Given valores originales: { nombre: "P001", area: 5 }
  When usuario edita: { nombre: "Potrero 1", area: 6 }
  Then:
    [ ] Se muestra diferencia (highlight en campos modificados)
    [ ] Opcionalmente: "Antes: 5 ha | Después: 6 ha"
```

### AC#10: Responsive Design
```gherkin
Scenario: EditorPanel adaptado a diferentes pantallas
  Given usuario en desktop (1920px)
  When EditorPanel se abre
  Then:
    [ ] Ancho: 400-500px, bien espaciado
    [ ] Todos los campos visibles
  
  Given usuario en tablet (768px)
  When EditorPanel se abre
  Then:
    [ ] Ancho: 100% o 300-400px
    [ ] Scroll si es necesario
    [ ] Fácil de usar
  
  Given usuario en mobile (375px)
  When EditorPanel se abre
  Then:
    [ ] Modal a pantalla completa o split
    [ ] Campos uno por uno
    [ ] Botones accesibles
```

### AC#11: Integración con DetailPanel
```gherkin
Scenario: Abrir EditorPanel desde DetailPanel
  Given usuario viendo DetailPanel de P001
  When hace click en botón "Editar" o ícono pencil
  Then:
    [ ] EditorPanel se abre a la derecha (o modal)
    [ ] DetailPanel se desvanece un poco (background)
    [ ] Puede cerrar EditorPanel y volver a DetailPanel
    [ ] Sin recargar página
```

### AC#12: Actualización en Tiempo Real
```gherkin
Scenario: Otros componentes se actualizan después de guardar
  Given usuario edita potrero en EditorPanel
  And guarda exitosamente
  When EditorPanel se cierra
  Then:
    [ ] DetailPanel muestra nuevos datos
    [ ] Calendar actualiza nombre/info si es visible
    [ ] PaddockList se actualiza (si existe)
    [ ] Sin necesidad de refrescar
```

### AC#13: Confirmación antes de Descartar
```gherkin
Scenario: Advertencia si hay cambios sin guardar
  Given EditorPanel con cambios pendientes
  When usuario intenta cerrar (click X o fuera del panel)
  Then:
    [ ] Modal de confirmación: "¿Descartar cambios?"
    [ ] Opciones: "Guardar", "Descartar", "Cancelar"
    [ ] Si "Guardar": guarda primero, luego cierra
    [ ] Si "Descartar": cierra sin guardar
    [ ] Si "Cancelar": sigue abierto
```

### AC#14: Accesibilidad
```gherkin
Scenario: EditorPanel es accesible
  Given EditorPanel abierto
  Then:
    [ ] role="form" o similar
    [ ] Labels para cada campo
    [ ] aria-required para campos obligatorios
    [ ] aria-invalid para campos con error
    [ ] aria-describedby para mensajes de error
    [ ] Navegación con Tab, Enter
    [ ] Screen reader compatible
```

### AC#15: Historial de Cambios (Bonus)
```gherkin
Scenario: Ver quién editó y cuándo (opcional)
  Given EditorPanel abierto
  When usuario hace scroll o expande "Historial"
  Then:
    [ ] Se muestra: "Última edición: 2026-01-05 10:30 por Carlos López"
    [ ] Ícono de auditoría (opcional link a HU#13)
    [ ] Información de quién modificó qué, cuándo
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### Componente Principal - `EditorPanel.jsx`

```javascript
export function EditorPanel({ 
  farmId, 
  pasture, 
  onClose, 
  onSuccess 
}) {
  const [formData, setFormData] = useState({
    name: pasture?.name || '',
    description: pasture?.description || '',
    areHa: pasture?.areHa || 0,
    animalLoad: pasture?.animalLoad || 0,
  });
  
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [loading, setLoading] = useState(false);
  const [savedSuccessfully, setSavedSuccessfully] = useState(false);
  
  const originalValues = useMemo(() => ({
    name: pasture?.name || '',
    description: pasture?.description || '',
    areHa: pasture?.areHa || 0,
    animalLoad: pasture?.animalLoad || 0,
  }), [pasture]);
  
  const hasChanges = useMemo(() => {
    return JSON.stringify(formData) !== JSON.stringify(originalValues);
  }, [formData, originalValues]);
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Validar en tiempo real si campo fue tocado
    if (touched[name]) {
      const newErrors = validateField(name, value);
      setErrors(prev => ({
        ...prev,
        [name]: newErrors[name] || null
      }));
    }
  };
  
  const handleBlur = (e) => {
    const { name } = e.target;
    setTouched(prev => ({ ...prev, [name]: true }));
    
    const newErrors = validateField(name, formData[name]);
    setErrors(prev => ({
      ...prev,
      [name]: newErrors[name] || null
    }));
  };
  
  const handleSave = async () => {
    // Validar todo antes de guardar
    const newErrors = validateForm(formData);
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      setTouched(Object.keys(formData).reduce((acc, key) => {
        acc[key] = true;
        return acc;
      }, {}));
      return;
    }
    
    setLoading(true);
    try {
      await updatePasture(farmId, pasture.id, formData);
      setSavedSuccessfully(true);
      setTimeout(() => {
        onSuccess?.(formData);
        onClose?.();
      }, 1500);
    } catch (error) {
      setErrors({ general: error.message || 'Error al guardar' });
    } finally {
      setLoading(false);
    }
  };
  
  const handleCancel = () => {
    if (hasChanges) {
      if (window.confirm('¿Descartar cambios?')) {
        onClose?.();
      }
    } else {
      onClose?.();
    }
  };
  
  return (
    <div className="editor-panel">
      <div className="editor-header">
        <h3>Editar {pasture?.name || 'Potrero'}</h3>
        <button onClick={handleCancel} className="btn-close">×</button>
      </div>
      
      {errors.general && (
        <div className="alert alert-error">{errors.general}</div>
      )}
      
      {savedSuccessfully && (
        <div className="alert alert-success">Potrero actualizado ✓</div>
      )}
      
      <div className="editor-form">
        <FormInput
          label="Nombre"
          name="name"
          value={formData.name}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.name && errors.name}
          required
        />
        
        <FormTextarea
          label="Descripción"
          name="description"
          value={formData.description}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.description && errors.description}
          rows={3}
        />
        
        <FormInput
          label="Área (ha)"
          name="areHa"
          type="number"
          value={formData.areHa}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.areHa && errors.areHa}
          required
          min="0.1"
          step="0.1"
        />
        
        <FormInput
          label="Carga de Animales"
          name="animalLoad"
          type="number"
          value={formData.animalLoad}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.animalLoad && errors.animalLoad}
          min="0"
          step="1"
        />
        
        <div className="editor-info">
          <p className="readonly">
            <strong>ID:</strong> {pasture?.id}
          </p>
          <p className="readonly">
            <strong>Estado:</strong> {pasture?.status}
          </p>
          {pasture?.lastModifiedAt && (
            <p className="readonly" style={{ fontSize: '0.85em', color: '#666' }}>
              Última edición: {formatDate(pasture.lastModifiedAt)}
            </p>
          )}
        </div>
      </div>
      
      <div className="editor-actions">
        <button
          onClick={handleCancel}
          className="btn btn-secondary"
          disabled={loading}
        >
          Cancelar
        </button>
        <button
          onClick={handleSave}
          className="btn btn-primary"
          disabled={loading || !hasChanges || Object.keys(errors).length > 0}
        >
          {loading ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </div>
  );
}
```

#### Hook - `useEditorPanel.js`

```javascript
export function useEditorPanel(farmId, pastureId) {
  const [isOpen, setIsOpen] = useState(false);
  const [pasture, setPasture] = useState(null);
  const [loading, setLoading] = useState(false);
  
  const open = useCallback(async (pastureData) => {
    setPasture(pastureData);
    setIsOpen(true);
  }, []);
  
  const close = useCallback(() => {
    setIsOpen(false);
    setPasture(null);
  }, []);
  
  const update = useCallback(async (newData) => {
    setPasture(prev => ({
      ...prev,
      ...newData
    }));
  }, []);
  
  return {
    isOpen,
    pasture,
    loading,
    open,
    close,
    update
  };
}
```

### Servicios

#### `pastureService.js`

```javascript
export async function updatePasture(farmId, pastureId, data) {
  const response = await fetch(
    `/api/farms/${farmId}/pastures/${pastureId}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Error al actualizar potrero');
  }
  
  return response.json();
}
```

### Validadores

#### `editorValidators.js`

```javascript
export function validateField(name, value) {
  const errors = {};
  
  switch(name) {
    case 'name':
      if (!value || value.trim().length === 0) {
        errors.name = 'Nombre es requerido';
      } else if (value.length < 3) {
        errors.name = 'Nombre debe tener al menos 3 caracteres';
      } else if (value.length > 100) {
        errors.name = 'Nombre no puede exceder 100 caracteres';
      }
      break;
    
    case 'areHa':
      const area = parseFloat(value);
      if (isNaN(area) || area <= 0) {
        errors.areHa = 'Área debe ser mayor a 0';
      }
      break;
    
    case 'animalLoad':
      const load = parseInt(value);
      if (!isNaN(load) && load < 0) {
        errors.animalLoad = 'Carga no puede ser negativa';
      } else if (!isNaN(load) && load === 0) {
        // Warning, no error
      }
      break;
    
    case 'description':
      if (value && value.length > 500) {
        errors.description = 'Descripción no puede exceder 500 caracteres';
      }
      break;
  }
  
  return errors;
}

export function validateForm(formData) {
  const errors = {};
  
  Object.keys(formData).forEach(key => {
    const fieldErrors = validateField(key, formData[key]);
    if (fieldErrors[key]) {
      errors[key] = fieldErrors[key];
    }
  });
  
  return errors;
}
```

### Estilos CSS

```css
.editor-panel {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 450px;
  background: white;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.editor-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #333;
}

.editor-form {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.editor-info {
  background: #f9f9f9;
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
}

.editor-info p {
  margin: 6px 0;
}

.editor-info .readonly {
  color: #666;
  font-size: 13px;
}

.editor-actions {
  display: flex;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid #eee;
  background: #f9f9f9;
}

.editor-actions .btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.editor-actions .btn-primary {
  background: #0066cc;
  color: white;
}

.editor-actions .btn-primary:hover:not(:disabled) {
  background: #0052a3;
}

.editor-actions .btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.editor-actions .btn-secondary {
  background: white;
  color: #333;
  border: 1px solid #ddd;
}

.editor-actions .btn-secondary:hover {
  background: #f0f0f0;
}

.alert {
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
}

.alert-error {
  background: #ffe0e0;
  color: #cc0000;
  border-left: 4px solid #cc0000;
}

.alert-success {
  background: #e0ffe0;
  color: #00cc00;
  border-left: 4px solid #00cc00;
}

/* Responsive */
@media (max-width: 768px) {
  .editor-panel {
    width: 100%;
    position: absolute;
    animation: slideInUp 0.3s ease-out;
  }
  
  @keyframes slideInUp {
    from {
      transform: translateY(100%);
    }
    to {
      transform: translateY(0);
    }
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`EditorPanel.jsx`** - Componente principal
2. **`useEditorPanel.js`** - Custom hook
3. **`editorValidators.js`** - Funciones de validación
4. **`editor.css`** - Estilos
5. **`EditorPanel.test.jsx`** - Tests

### Archivos a Modificar

1. **`DetailPanel.jsx`** - Agregar botón "Editar"
2. **`PaddockPage.jsx`** - Integrar EditorPanel
3. **`pastureService.js`** - Agregar updatePasture()

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear EditorPanel base
- Estructura HTML
- Estados (formData, errors, touched, loading)
- Handlers (handleChange, handleBlur, handleSave, handleCancel)

### Paso 2: Agregar validación
- Validadores para cada campo
- Validación en tiempo real (onBlur)
- Mostrar/ocultar errores

### Paso 3: Integrar servicio
- Conectar con PUT /pastures/{id}
- Loading state
- Error handling

### Paso 4: Agregar UI feedback
- Loading spinner
- Success message
- Error messages

### Paso 5: Integración con otros componentes
- Botón en DetailPanel
- Actualizar DetailPanel después de guardar
- Actualizar Calendar si es necesario

### Paso 6: Testing
- Tests unitarios para validación
- Tests componentes
- Tests E2E

---

## 🧪 **Casos de Prueba**

### Test Unitarios

```javascript
describe('editorValidators', () => {
  
  test('valida nombre requerido', () => {
    const errors = validateField('name', '');
    expect(errors.name).toBeTruthy();
  });
  
  test('valida nombre longitud mínima', () => {
    const errors = validateField('name', 'AB');
    expect(errors.name).toBeTruthy();
  });
  
  test('acepta nombre válido', () => {
    const errors = validateField('name', 'Potrero 1');
    expect(errors.name).toBeFalsy();
  });
});
```

### Test Componentes

```javascript
describe('EditorPanel', () => {
  
  test('renderiza campos prellenados', () => {
    const pasture = { id: 'P001', name: 'Potrero 1', areHa: 5 };
    render(<EditorPanel pasture={pasture} />);
    
    expect(screen.getByDisplayValue('Potrero 1')).toBeInTheDocument();
    expect(screen.getByDisplayValue('5')).toBeInTheDocument();
  });
  
  test('deshabilita guardar si no hay cambios', () => {
    const pasture = { id: 'P001', name: 'Potrero 1' };
    render(<EditorPanel pasture={pasture} />);
    
    expect(screen.getByRole('button', { name: /guardar/i }))
      .toHaveAttribute('disabled');
  });
  
  test('guarda cambios exitosamente', async () => {
    const onSuccess = jest.fn();
    render(
      <EditorPanel pasture={pasture} onSuccess={onSuccess} />
    );
    
    fireEvent.change(screen.getByDisplayValue('Potrero 1'), {
      target: { value: 'Nuevo Nombre' }
    });
    
    fireEvent.click(screen.getByRole('button', { name: /guardar/i }));
    
    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalled();
    });
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Editar Nombre y Guardar
```gherkin
Scenario: Usuario edita nombre de potrero
  Given EditorPanel abierto con P001
  When cambia nombre a "Potrero Premium"
  And hace click "Guardar"
  Then potrero se actualiza exitosamente
  And EditorPanel se cierra
  And DetailPanel muestra nuevo nombre
```

### Escenario 2: Validación en Tiempo Real
```gherkin
Scenario: Mostrar errores mientras escribe
  Given usuario en EditorPanel
  When borra nombre completamente
  Then se muestra error: "Nombre es requerido"
  And botón Guardar está disabled
```

### Escenario 3: Cancelar sin Guardar
```gherkin
Scenario: Descartar cambios
  Given cambios sin guardar
  When hace click Cancelar
  Then pide confirmación
  And al confirmar, descarta cambios
```

---

## 📚 **Referencias y Dependencias**

**Dependencias**:
- React (hooks)
- Componentes existentes (FormInput, FormTextarea)
- pastureService.js

**Componentes relacionados**:
- DetailPanel (HU#3)
- Calendar (HU#10)

---

## 🔧 **Refinamiento Técnico**

### EditorPanel Component

```javascript
export const EditorPanel = ({ pasture, farmId, onClose, onSuccess }) => {
  const { data, errors, touched, isDirty, handleChange, handleBlur } = 
    useFormValidation(pasture, validators);
  const { updatePasture, loading } = useUpdatePasture(farmId);
  
  const handleSave = async () => {
    // Validar
    if (Object.values(errors).some(e => e)) return;
    
    // Guardar (optimistic update)
    try {
      const updated = await updatePasture(pasture.id, data);
      onSuccess(updated);
      onClose();
    } catch (err) {
      showError(err.message);
    }
  };
  
  return (
    <div className="border rounded-lg p-6">
      <h2>Editar {pasture.name}</h2>
      
      <FormField label="Nombre" name="name" 
        value={data.name} error={errors.name}
        onChange={handleChange} onBlur={handleBlur} />
        
      <FormField label="Área (ha)" name="areHa" type="number"
        value={data.areHa} error={errors.areHa}
        onChange={handleChange} onBlur={handleBlur} />
        
      <button onClick={handleSave} disabled={!isDirty || loading}>
        {loading ? 'Guardando...' : 'Guardar'}
      </button>
    </div>
  );
};
```

### useUpdatePasture Hook

```javascript
export const useUpdatePasture = (farmId) => {
  const [loading, setLoading] = useState(false);
  
  const updatePasture = async (pastureId, data) => {
    setLoading(true);
    try {
      // Optimistic update en UI
      const response = await axios.put(
        `/farms/${farmId}/pastures/${pastureId}`,
        data
      );
      return response.data;
    } finally {
      setLoading(false);
    }
  };
  
  return { updatePasture, loading };
};
```

### Testing Strategy

**Component Tests:**
- Validación real-time
- Botón Guardar disabled sin cambios
- PUT se envía correctamente
- Error handling completo

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `EditorPanel.jsx` renderiza
- [ ] Campos son editables
- [ ] Validación funciona (tiempo real, onBlur)
- [ ] Botón Guardar disabled cuando no hay cambios
- [ ] Botón Guardar disabled cuando hay errores
- [ ] PUT se envía correctamente
- [ ] Success message muestra
- [ ] Error handling completo
- [ ] Botón Cancelar funciona
- [ ] Confirmación al descartar cambios
- [ ] Campos sólo lectura no editable
- [ ] Responsive: desktop, tablet, mobile
- [ ] Integración con DetailPanel
- [ ] Integración con Calendar (actualiza)
- [ ] Integración con PaddockPage
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests componentes: todos los ACs
- [ ] Tests E2E: flujos completos
- [ ] Accesibilidad: ARIA labels, etc.

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Form Component + Real-time Validation + Optimistic Updates

**Justificación:** **Form Component**: Panel reutilizable para edición. **Real-time Validation**: Feedback instantáneo (on-change + on-blur). **Optimistic Updates**: UI actualiza antes de respuesta. **Error Handling**: Rollback si falla. **Integration**: DetailPanel + Calendar. **State Management**: Dirty state tracking.

**Componentes Afectados:**

- **EditorPanel.jsx (Nuevo):** Componente principal. Props: `pasture`, `farmId`, `onClose`, `onSuccess`. State: `formData`, `errors`, `isDirty`, `isLoading`. Renderiza: formulario + botones. Integra FormField (HU#007).

- **FormField.jsx (Reutilizable de HU#007):** Campos editables. Soporta: text, number, textarea. Validación real-time. Error/success states.

- **useEditorState.js (Nuevo - Hook):** Maneja estado del formulario. Retorna: `{ formData, errors, isDirty, setField, resetForm }`. Compara con valores originales para `isDirty`.

- **editPastureValidators.js (Nuevo):** Validadores específicos. validateName, validateArea, validateAnimalLoad. Reutiliza funciones HU#007.

- **EditButtons.jsx (Nuevo):** Botones Guardar/Cancelar. Guardar disabled si isDirty=false o errors. Cancelar pide confirmación si hay cambios.

- **useUpdatePasture.js (Nuevo - Hook):** Ejecuta PUT. Optimistic update en UI. Rollback si error. Notifica onSuccess si exitoso.

**Hitos:**
1. useEditorState.js + editPastureValidators.js (state)
2. EditButtons.jsx (UI buttons)
3. EditorPanel.jsx (main component)
4. useUpdatePasture.js (API call)
5. Tests + integration

### Validación de Impacto

✅ **Real-time Validation**: Feedback instantáneo
✅ **Optimistic Updates**: UX fluida
✅ **Error Handling**: Rollback graceful
✅ **Reusability**: FormField from HU#007
✅ **Integration**: DetailPanel + Calendar updated

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-003: DetailPanel (integración)
- ✅ PASTURES-HU-007: Validadores (reutiliza)
- ✅ PASTURES-HU-010: Calendar (actualiza)
- → PASTURES-HU-014: EditorPanel (esta)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Form component + optimistic updates (UX)

---

## ✅ **Definición de Completado**
- [ ] Sin lag, smooth animations
- [ ] Code review aprobado
- [ ] CI/CD green
- [ ] Documentación actualizada

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
