# 🌱 PASTURES-HU#7: Frontend: Validaciones en Formularios

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟠 ALTO (P1) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Validadores puros + FormField reutilizable | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero implementar un sistema robusto de validaciones en formularios que muestre errores en tiempo real, de tal forma que:

1. Los campos se validan mientras el usuario escribe (real-time validation)
2. Los mensajes de error aparecen debajo del campo relevante
3. El botón "Confirmar" se deshabilita si hay errores
4. Los campos con error tienen borde rojo y icono de error
5. Se limpian errores cuando el usuario corrige el valor
6. Los validadores son reutilizables entre formularios
7. La accesibilidad está garantizada (ARIA labels, etc.)

Esto habilitará una experiencia de usuario mucho más fluida y clara, reduciendo errores de entrada y frustraciones.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Validación en Tiempo Real
```gherkin
Scenario: Campos se validan mientras el usuario escribe
  Given formulario abierto (ej: OpenPastureModal)
  And campo "Altura Residual" vacío
  When usuario comienza a escribir "abc" (no numérico)
  Then:
    [ ] Campo muestra borde rojo en tiempo real
    [ ] Icono de error (❌) aparece a la derecha del campo
    [ ] Mensaje de error: "Debe ser un número válido" aparece debajo
    [ ] Botón "Confirmar" se deshabilita automáticamente
  
  When usuario borra "abc" y escribe "15" (válido)
  Then:
    [ ] Borde rojo desaparece (o pasa a verde)
    [ ] Icono de error desaparece
    [ ] Mensaje de error desaparece
    [ ] Si otros campos válidos: botón "Confirmar" se habilita
```

### AC#2: Mensajes de Error Vinculados
```gherkin
Scenario: Mensajes de error específicos por campo
  Given OpenPastureModal abierto
  When usuario intenta confirmar sin llenar "Altura Residual"
  Then:
    [ ] Mensaje aparece bajo el campo específico
    [ ] aria-invalid="true" en el input
    [ ] aria-describedby apunta al mensaje de error
    [ ] Focus va al primer campo con error (accessibility)
```

### AC#3: Validadores Reutilizables
```gherkin
Scenario: Validadores usados en múltiples formularios
  Given OpenPastureModal, ClosePastureModal, MaintenanceModal
  When cada uno necesita validar "Altura Residual"
  Then:
    [ ] Todos usan validador: validateHeight(value)
    [ ] Validador centralizado en utils/validators.js
    [ ] Mismo mensaje de error en todos lados
    [ ] Fácil mantener: cambios en un solo lugar
```

### AC#4: Altura Residual - Validación
```gherkin
Scenario: Validar altura residual correctamente
  When usuario escribe "-5" (negativo)
  Then error: "Altura no puede ser negativa"
  
  When usuario escribe "0" (cero)
  Then error: "Altura debe ser mayor a 0"
  
  When usuario escribe "abc" (no numérico)
  Then error: "Debe ser un número válido"
  
  When usuario escribe "150" (potrero solo permite 100cm máximo)
  Then error: "Altura no puede exceder 100 cm"
  
  When usuario escribe "25" (válido)
  Then error desaparece
```

### AC#5: Duración Bloqueo (Días) - Validación
```gherkin
Scenario: Validar días de bloqueo
  When usuario escribe "-1" (negativo)
  Then error: "Días no pueden ser negativos"
  
  When usuario escribe "0" (cero)
  Then error: "Mínimo 1 día"
  
  When usuario escribe "abc" (no numérico)
  Then error: "Debe ser un número válido"
  
  When usuario escribe "1000" (más de 365)
  Then error: "Máximo 365 días"
  
  When usuario escribe "7" (válido)
  Then error desaparece
```

### AC#6: Campo de Texto - Validación
```gherkin
Scenario: Validar campos de texto (nombre, notas)
  When usuario escribe "A" (menos de 1 carácter válido)
  Then: Sin error (si permite un carácter)
  
  When usuario escribe "" (vacío pero no requerido)
  Then: Sin error
  
  When usuario escribe "X" * 101 (más de 100 caracteres)
  Then error: "Máximo 100 caracteres"
  
  When usuario escribe "Potrero Grande" (válido)
  Then error desaparece, contador: "14/100"
```

### AC#7: Contador de Caracteres
```gherkin
Scenario: Mostrar contador de caracteres para textarea/text largo
  Given campo de notas (máximo 200 caracteres)
  When usuario escribe texto
  Then:
    [ ] Contador visible debajo: "25/200"
    [ ] Se actualiza en tiempo real
    [ ] Si alcanza 80%: contador cambia a color naranja
    [ ] Si alcanza 100%: rojo y no permite escribir más
    [ ] Color verde si < 80%
```

### AC#8: Estados Visuales de Campos
```gherkin
Scenario: Campos tienen estados visuales claros
  Given formulario abierto
  Then campo validado correctamente tiene:
    [ ] Borde verde (opcional)
    [ ] Icono checkmark (✓) verde a la derecha (opcional)
    [ ] Fondo blanco limpio
  
  When campo con error:
    [ ] Borde rojo (#ff0000)
    [ ] Icono X rojo (❌) a la derecha
    [ ] Fondo rosado suave (#ffe5e5) (opcional)
  
  When campo sin validar aún (pristine):
    [ ] Borde gris neutral
    [ ] Sin icono
    [ ] Sin error
```

### AC#9: Botón Confirmar Disabled/Enabled
```gherkin
Scenario: Botón responde a cambios de validación
  Given formulario con campos requeridos
  When campos están vacíos o inválidos
  Then:
    [ ] Botón "Confirmar" disabled
    [ ] Cursor: not-allowed
    [ ] Opacidad: 0.5
    [ ] Tooltip hover: "Completa los campos requeridos"
  
  When todos los campos válidos
  Then:
    [ ] Botón enabled
    [ ] Cursor: pointer
    [ ] Opacidad: 1
    [ ] Color azul/verde
```

### AC#10: Limpiar Errores al Editar
```gherkin
Scenario: Errores desaparecen cuando usuario corrige
  Given campo "Altura" con error "Debe ser número"
  When usuario borra el contenido y escribe "25" (válido)
  Then:
    [ ] Error desaparece inmediatamente
    [ ] Borde pasa a gris o verde
    [ ] Icono de error desaparece
    [ ] Sin esperar a que pierda el focus
```

### AC#11: Validación al Perder Focus (onBlur)
```gherkin
Scenario: También validar cuando campo pierde focus
  Given campo de altura
  When usuario hace focus, escribe algo, luego hace focus en otro campo
  Then:
    [ ] Se valida al blur
    [ ] Si hay error, se muestra
    [ ] Proporciona feedback adicional al cambio de focus
```

### AC#12: Mensajes de Error Accesibles
```gherkin
Scenario: Mensajes de error son accesibles
  Given formulario con validaciones
  Then:
    [ ] Cada campo tiene aria-invalid="true/false"
    [ ] aria-describedby apunta a elemento error
    [ ] Error message tiene id unique: "error-field-name"
    [ ] Screen reader anuncia: "Campo inválido: Altura debe ser número"
    [ ] Labels correctamente vinculados con htmlFor
```

### AC#13: Validación de Fecha
```gherkin
Scenario: Validar campo de fecha (MaintenanceModal)
  When usuario intenta seleccionar fecha en el pasado
  Then:
    [ ] Date picker la desabilita (no clickeable)
    [ ] O mensaje: "Fecha no puede ser en el pasado"
  
  When usuario intenta seleccionar > 365 días
  Then también deshabilitada
  
  When usuario selecciona fecha válida (mañana a 365 días)
  Then sin error
```

### AC#14: Validación de Select/Dropdown
```gherkin
Scenario: Validar select requerido
  When select está vacío (default)
  Then error: "Selecciona una opción"
  
  When usuario selecciona opción válida
  Then error desaparece
```

### AC#15: Validación de Formulario Completo
```gherkin
Scenario: Validar todo el formulario antes de enviar
  Given OpenPastureModal con 3 campos
  When usuario intenta hacer submit (click Confirmar)
  And al menos un campo inválido
  Then:
    [ ] Form NO se envía
    [ ] Todos los errores se muestran
    [ ] Focus va al primer campo con error
    [ ] Toast/message: "Por favor completa los campos marcados en rojo"
  
  When todos los campos válidos
  Then Form se envía normalmente
```

---

## 📊 **Especificación Técnica**

### Estructura de Validadores

#### `utils/validators.js` (NUEVO - centralizado)
```javascript
// Validadores reutilizables

export function validateHeight(value) {
  if (!value) return 'Campo requerido';
  const num = parseFloat(value);
  if (isNaN(num)) return 'Debe ser un número válido';
  if (num <= 0) return 'Altura debe ser mayor a 0';
  if (num > 100) return 'Altura no puede exceder 100 cm';
  return null; // Sin error
}

export function validateDays(value) {
  if (!value) return 'Campo requerido';
  const num = parseInt(value, 10);
  if (isNaN(num)) return 'Debe ser un número válido';
  if (num < 1) return 'Mínimo 1 día';
  if (num > 365) return 'Máximo 365 días';
  return null;
}

export function validateName(value, maxLength = 100) {
  if (!value) return 'Campo requerido';
  if (value.trim().length === 0) return 'Campo no puede estar vacío';
  if (value.length > maxLength) return `Máximo ${maxLength} caracteres`;
  return null;
}

export function validateNotes(value, maxLength = 200) {
  if (!value) return null; // Optional
  if (value.length > maxLength) return `Máximo ${maxLength} caracteres`;
  return null;
}

export function validateDate(value) {
  if (!value) return 'Campo requerido';
  const date = new Date(value);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  if (date <= today) return 'Fecha debe ser en el futuro';
  return null;
}

export function validateSelect(value) {
  if (!value) return 'Selecciona una opción';
  return null;
}

// Validador genérico para múltiples campos
export function validateForm(formData, schema) {
  const errors = {};
  
  for (const [fieldName, validator] of Object.entries(schema)) {
    const error = validator(formData[fieldName]);
    if (error) {
      errors[fieldName] = error;
    }
  }
  
  return errors;
}
```

### Hook para Validación

#### `hooks/useFormValidation.js` (NUEVO)
```javascript
export function useFormValidation(schema) {
  const [formData, setFormData] = useState({});
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({}); // Qué campos fueron modificados
  
  const validate = useCallback((field, value) => {
    if (!schema[field]) return null;
    return schema[field](value);
  }, [schema]);
  
  const validateAll = useCallback(() => {
    const newErrors = validateForm(formData, schema);
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [formData, schema]);
  
  const handleChange = useCallback((field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Validar en tiempo real solo si el campo fue tocado antes
    if (touched[field]) {
      const error = validate(field, value);
      setErrors(prev => ({
        ...prev,
        [field]: error
      }));
    }
  }, [validate, touched]);
  
  const handleBlur = useCallback((field) => {
    setTouched(prev => ({ ...prev, [field]: true }));
    const error = validate(field, formData[field]);
    setErrors(prev => ({
      ...prev,
      [field]: error
    }));
  }, [validate, formData]);
  
  const isValid = useCallback(() => {
    return validateAll() === true;
  }, [validateAll]);
  
  return {
    formData,
    setFormData,
    errors,
    touched,
    handleChange,
    handleBlur,
    isValid,
    validateAll
  };
}
```

### Componente Reutilizable de Input

#### `components/FormInput.jsx` (NUEVO)
```javascript
export function FormInput({
  label,
  name,
  type = 'text',
  value,
  error,
  touched = false,
  required = false,
  maxLength,
  placeholder,
  onChange,
  onBlur,
  disabled = false,
  hint,
  showCounter = false, // Para fields con maxLength
  min,
  max,
  step,
  ...props
}) {
  const hasError = touched && error;
  const showSuccessIcon = touched && !error && value;
  
  return (
    <div className="form-group">
      <label htmlFor={name} className="form-label">
        {label}
        {required && <span className="required-asterisk">*</span>}
      </label>
      
      <div className="form-input-wrapper">
        <input
          id={name}
          name={name}
          type={type}
          value={value}
          onChange={(e) => onChange(name, e.target.value)}
          onBlur={() => onBlur(name)}
          disabled={disabled}
          placeholder={placeholder}
          maxLength={maxLength}
          min={min}
          max={max}
          step={step}
          aria-invalid={hasError}
          aria-describedby={hasError ? `error-${name}` : hint ? `hint-${name}` : undefined}
          className={`form-input ${hasError ? 'error' : showSuccessIcon ? 'success' : ''}`}
          {...props}
        />
        
        {hasError && <span className="input-icon error-icon">❌</span>}
        {showSuccessIcon && <span className="input-icon success-icon">✓</span>}
      </div>
      
      {hasError && (
        <div id={`error-${name}`} className="form-error" role="alert">
          {error}
        </div>
      )}
      
      {hint && !hasError && (
        <div id={`hint-${name}`} className="form-hint">{hint}</div>
      )}
      
      {showCounter && maxLength && (
        <div className="form-counter" style={{
          color: value.length / maxLength > 0.8 ? 
            (value.length >= maxLength ? '#ff0000' : '#ffa500') : '#666'
        }}>
          {value.length}/{maxLength}
        </div>
      )}
    </div>
  );
}
```

#### `components/FormSelect.jsx` (NUEVO)
```javascript
export function FormSelect({
  label,
  name,
  value,
  error,
  touched = false,
  required = false,
  options, // [{ value, label }, ...]
  onChange,
  onBlur,
  disabled = false,
  placeholder = "Selecciona una opción",
  ...props
}) {
  const hasError = touched && error;
  
  return (
    <div className="form-group">
      <label htmlFor={name} className="form-label">
        {label}
        {required && <span className="required-asterisk">*</span>}
      </label>
      
      <select
        id={name}
        name={name}
        value={value}
        onChange={(e) => onChange(name, e.target.value)}
        onBlur={() => onBlur(name)}
        disabled={disabled}
        aria-invalid={hasError}
        aria-describedby={hasError ? `error-${name}` : undefined}
        className={`form-select ${hasError ? 'error' : ''}`}
        {...props}
      >
        <option value="">{placeholder}</option>
        {options.map(opt => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      
      {hasError && <span className="input-icon error-icon">❌</span>}
      
      {hasError && (
        <div id={`error-${name}`} className="form-error" role="alert">
          {error}
        </div>
      )}
    </div>
  );
}
```

### Cambios en Componentes Existentes

#### `OpenPastureModal.jsx` - Usar validaciones
```javascript
export function OpenPastureModal({ ... }) {
  const schema = {
    residualHeight: (value) => validateHeight(value),
    notes: (value) => validateNotes(value)
  };
  
  const { formData, errors, touched, handleChange, handleBlur, isValid } = 
    useFormValidation(schema);
  
  return (
    <div className="modal">
      <FormInput
        label="Altura Residual (cm)"
        name="residualHeight"
        type="number"
        value={formData.residualHeight}
        error={errors.residualHeight}
        touched={touched.residualHeight}
        onChange={handleChange}
        onBlur={handleBlur}
        required={true}
      />
      
      <FormInput
        label="Notas"
        name="notes"
        value={formData.notes}
        error={errors.notes}
        touched={touched.notes}
        onChange={handleChange}
        onBlur={handleBlur}
        maxLength={200}
        showCounter={true}
      />
      
      <button
        onClick={() => {
          if (isValid()) {
            // Enviar
          } else {
            // Mostrar errores
          }
        }}
        disabled={!isValid()}
      >
        Confirmar
      </button>
    </div>
  );
}
```

### Estilos CSS

```css
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-label {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.required-asterisk {
  color: #ff0000;
  margin-left: 2px;
}

.form-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input,
.form-select {
  flex: 1;
  padding: 10px 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #0066ff;
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

.form-input.error,
.form-select.error {
  border-color: #ff0000;
  background-color: #ffe5e5;
}

.form-input.success {
  border-color: #00aa00;
  background-color: #e5ffe5;
}

.input-icon {
  position: absolute;
  right: 12px;
  font-size: 16px;
  pointer-events: none;
}

.input-icon.error-icon {
  color: #ff0000;
}

.input-icon.success-icon {
  color: #00aa00;
}

.form-error {
  font-size: 12px;
  color: #ff0000;
  margin-top: 2px;
  font-weight: 500;
}

.form-hint {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.form-counter {
  font-size: 12px;
  text-align: right;
  margin-top: 4px;
  transition: color 0.2s;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button:not(:disabled):hover {
  background-color: #0052cc;
  transform: translateY(-1px);
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`utils/validators.js`**
   - Validadores reutilizables (height, days, name, notes, date, select)
   - validateForm genérico

2. **`hooks/useFormValidation.js`**
   - Hook para manejo de estado y validación
   - handleChange, handleBlur, validateAll

3. **`components/FormInput.jsx`**
   - Input reutilizable con validación visual

4. **`components/FormSelect.jsx`**
   - Select reutilizable con validación visual

5. **`components/FormTextarea.jsx`** (opcional)
   - Textarea con contador de caracteres

### Archivos a Modificar

1. **`OpenPastureModal.jsx`**
   - Usar useFormValidation hook
   - Reemplazar inputs con FormInput
   - Validación en tiempo real

2. **`ClosePastureModal.jsx`**
   - Mismo patrón que OpenPastureModal

3. **`MaintenanceModal.jsx`**
   - Validaciones con FormSelect, FormInput

4. **`pastureEventService.js`** (optional)
   - Sin cambios (ya está validado en frontend)

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear validadores centralizados
```javascript
// utils/validators.js
export function validateHeight(value) { ... }
export function validateDays(value) { ... }
// etc.
```

### Paso 2: Crear hook useFormValidation
```javascript
export function useFormValidation(schema) { ... }
```

### Paso 3: Crear componentes FormInput y FormSelect
```javascript
export function FormInput({ ... }) { ... }
export function FormSelect({ ... }) { ... }
```

### Paso 4: Actualizar componentes existentes
- OpenPastureModal: usar useFormValidation + FormInput
- ClosePastureModal: mismo patrón
- MaintenanceModal: mismo patrón

### Paso 5: Probar validaciones
- Tests unitarios para validadores
- Tests componentes para UI
- Tests E2E para flujos

---

## 🧪 **Casos de Prueba**

### Test Unitarios (Vitest)

```javascript
describe('validators', () => {
  
  test('validateHeight rechaza números inválidos', () => {
    expect(validateHeight('abc')).toBe('Debe ser un número válido');
    expect(validateHeight('-5')).toBe('Altura debe ser mayor a 0');
    expect(validateHeight('150')).toBe('Altura no puede exceder 100 cm');
    expect(validateHeight('25')).toBeNull();
  });
  
  test('validateDays rechaza fuera de rango', () => {
    expect(validateDays('0')).toBe('Mínimo 1 día');
    expect(validateDays('1000')).toBe('Máximo 365 días');
    expect(validateDays('7')).toBeNull();
  });
});
```

### Test de Componentes

```javascript
describe('FormInput', () => {
  
  test('muestra error cuando campo inválido', () => {
    render(
      <FormInput
        name="height"
        value="abc"
        error="Debe ser número"
        touched={true}
      />
    );
    
    expect(screen.getByText('Debe ser número')).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveAttribute('aria-invalid', 'true');
  });
  
  test('deshabilita botón si hay errores', () => {
    const { rerender } = render(
      <OpenPastureModal />
    );
    
    expect(screen.getByText('Confirmar')).toBeDisabled();
    
    // Llenar campos válidos
    // Botón se habilita
  });
});
```

### Test E2E

```javascript
describe('Validaciones - E2E', () => {
  
  it('muestra error real-time al escribir', () => {
    cy.get('[name="residualHeight"]').type('abc');
    cy.contains('Debe ser un número válido').should('be.visible');
    
    cy.get('[name="residualHeight"]').clear().type('25');
    cy.contains('Debe ser un número válido').should('not.exist');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Validación Real-Time
```gherkin
Scenario: Usuario ve errores mientras escribe
  Given OpenPastureModal abierto
  When escribe "abc" en altura
  Then ve error: "Debe ser número" inmediatamente
  When borra y escribe "25"
  Then error desaparece
```

### Escenario 2: Botón Disabled
```gherkin
Scenario: Botón responde a validación
  Given formulario con campos requeridos
  When todos inválidos
  Then botón "Confirmar" disabled
  When completa correctamente
  Then botón enabled y clickeable
```

### Escenario 3: Contador de Caracteres
```gherkin
Scenario: Mostrar progreso de caracteres
  Given campo notas (máx 200)
  When usuario escribe 150 caracteres
  Then contador: "150/200" en color verde
  When alcanza 160 (80%)
  Then contador en color naranja
  When alcanza 200
  Then contador rojo, no permite escribir más
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otros componentes**:
- ✅ OpenPastureModal (HU#2)
- ✅ ClosePastureModal (HU#2)
- ✅ MaintenanceModal (HU#6)

**Documentación relacionada**:
- [HU#2: Frontend Botones](../P0/PASTURES-HU-002-frontend-botones.md)
- [HU#6: Modal Bloqueo](./PASTURES-HU-006-modal-bloqueo.md)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Validadores Puros Centralizados + FormField Component Reutilizable + Hook useFormValidation

**Justificación:** **Validadores centralizados**: Funciones puras (validateHeight, validateDate, validateText) reutilizables en todos formularios - single source of truth. **FormField component**: Wrapper reutilizable (input + error + icon + counter) asegura UX consistente. **Hook useFormValidation**: Encapsula lógica validación + estado local. **Real-time validation**: onChange handler valida instantáneamente, mejor UX. **Accesibilidad**: aria-invalid, aria-describedby, aria-label. **Validación técnica completada**: Integrado con HU#2 (modales) y HU#6 (maintenance modal).

**Componentes Afectados:**

- **validators/ (Nuevo - Directorio):** heightValidators.js, dateValidators.js, textValidators.js, numberValidators.js. Funciones puras: `validate*(value, min, max, required) → { isValid, error }`. Sin efectos secundarios, 100% testeables, reutilizables.

- **FormField.jsx (Nuevo):** Props: value, onChange, onBlur, error, isValid, label, type, placeholder, counter. Renderiza: label + input + error message + icon (✓/❌) + contador. Estilos: borde rojo/verde, aria-invalid, aria-describedby.

- **useFormValidation.js (Nuevo - Hook):** Maneja múltiples campos. Retorna: { formData, errors, touched, isValid, handleChange, handleBlur }. Validación on-change, on-blur, on-submit.

- **Modificar modales**: OpenPastureModal, ClosePastureModal, MaintenanceModal usan FormField + useFormValidation + validadores.

**Hitos:**
1. validators/ - Funciones puras (sin dependencias)
2. FormField.jsx - Wrapper (depende: validators)
3. useFormValidation.js - Hook (depende: validators)
4. Modificar modales (depende: 1-3)

### Validación de Impacto

✅ **Validadores Centralizados**: Single source of truth, reutilizables
✅ **FormField Reutilizable**: Consistencia visual + aria completo
✅ **Real-time Validation**: Feedback instantáneo, botones auto-disable
✅ **Testing**: Validadores 100% unit, FormField RTL, E2E Cypress
✅ **Accesibilidad**: aria-invalid, aria-describedby, labels vinculados

### Notas Técnicas

**Validador Puro:**
```javascript
export function validateHeight(value, min = 0, max = 100, required = true) {
  if (required && !value) return { isValid: false, error: 'Requerida' };
  const num = parseFloat(value);
  if (isNaN(num)) return { isValid: false, error: 'Número válido' };
  if (num <= min) return { isValid: false, error: `Mayor a ${min}` };
  if (num > max) return { isValid: false, error: `No exceder ${max}` };
  return { isValid: true, error: null };
}
```

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-002: Frontend Botones (modales existentes)
- ✅ PASTURES-HU-006: Modal Bloqueo (usa validadores)
- → PASTURES-HU-007: Frontend Validaciones (esta - centralizadas)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Validadores centralizados + FormField reutilizable

---

## 🔧 **Refinamiento Técnico**

### Validadores Centralizados

```javascript
// validators/pastureValidators.js

export const validateName = (name) => {
  if (!name?.trim()) return 'Nombre es requerido';
  if (name.length < 3) return 'Nombre muy corto (min 3)';
  if (name.length > 100) return 'Nombre muy largo (max 100)';
  return null;
};

export const validateArea = (area) => {
  if (!area || area <= 0) return 'Área debe ser > 0';
  if (area > 10000) return 'Área muy grande (max 10000 ha)';
  return null;
};

export const validateAnimalCount = (count) => {
  if (count < 0) return 'No puede ser negativo';
  if (count > 100000) return 'Demasiados animales';
  return null;
};

export const validateHeight = (height) => {
  if (!height || height <= 0) return 'Altura debe ser > 0';
  if (height > 100) return 'Altura máxima 100 cm';
  return null;
};
```

### useFormValidation Hook

```javascript
export const useFormValidation = (initialData, validators) => {
  const [data, setData] = useState(initialData);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  
  const validate = (field, value) => {
    const validator = validators[field];
    return validator ? validator(value) : null;
  };
  
  const handleChange = (field, value) => {
    setData(prev => ({ ...prev, [field]: value }));
    if (touched[field]) {
      const error = validate(field, value);
      setErrors(prev => ({ ...prev, [field]: error }));
    }
  };
  
  const handleBlur = (field) => {
    setTouched(prev => ({ ...prev, [field]: true }));
    const error = validate(field, data[field]);
    setErrors(prev => ({ ...prev, [field]: error }));
  };
  
  return { data, setData, errors, touched, handleChange, handleBlur };
};
```

### FormField Component

```javascript
export const FormField = ({ label, name, value, error, touched, onChange, onBlur, type = 'text' }) => (
  <div className="space-y-2">
    <label className="block text-sm font-medium">{label}</label>
    <input
      type={type}
      name={name}
      value={value}
      onChange={(e) => onChange(name, e.target.value)}
      onBlur={() => onBlur(name)}
      className={`w-full px-3 py-2 border rounded ${
        touched && error ? 'border-red-500 bg-red-50' : 'border-gray-300'
      }`}
    />
    {touched && error && <span className="text-sm text-red-600">{error}</span>}
  </div>
);
```

### Testing Strategy

**Unit Tests (Validadores Puros):**
```javascript
test('validateName requiere 3+ caracteres', () => {
  expect(validateName('ab')).toBe('Nombre muy corto');
  expect(validateName('abc')).toBeNull();
});

test('validateArea requiere > 0', () => {
  expect(validateArea(0)).not.toBeNull();
  expect(validateArea(5)).toBeNull();
});
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `utils/validators.js` con todos los validadores
- [ ] `hooks/useFormValidation.js` implementado
- [ ] `components/FormInput.jsx` con validación visual
- [ ] `components/FormSelect.jsx` con validación visual
- [ ] `components/FormTextarea.jsx` (si aplica)
- [ ] OpenPastureModal actualizado con validaciones
- [ ] ClosePastureModal actualizado
- [ ] MaintenanceModal actualizado
- [ ] Validación en tiempo real funciona
- [ ] Mensajes de error específicos por campo
- [ ] Botón Confirmar disabled cuando hay errores
- [ ] Campos con error tienen borde rojo
- [ ] Icono X rojo para errores
- [ ] Icono ✓ verde para success (opcional)
- [ ] Contador de caracteres funciona
- [ ] aria-invalid y aria-describedby correcto
- [ ] Focus management (focus en primer error)
- [ ] ESC cierra modal sin enviar
- [ ] Tests unitarios: cobertura >= 85%
- [ ] Tests componentes: todos los ACs probados
- [ ] Tests E2E: flujos completos validados
- [ ] Sin memory leaks en validación
- [ ] Performance: validación sin lag
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Documentación actualizada (JSDoc, comentarios)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
