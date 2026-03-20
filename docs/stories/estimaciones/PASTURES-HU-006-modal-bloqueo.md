# 🌱 PASTURES-HU#6: Frontend: Modal de Bloqueo/Mantenimiento

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟠 ALTO (P1) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Modal dual-mode + ConfirmDialog reutilizable | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero crear un Modal de Bloqueo/Mantenimiento que permita bloquear potreros durante reparaciones o mantenimiento, de tal forma que:

1. El modal se abre desde botón "Bloquear" en DetailPanel o tabla
2. Permite seleccionar razón de bloqueo (Fertilización, Reparación, Cuarentena, Otro)
3. Permite seleccionar fecha hasta cuándo bloqueado
4. Valida fechas (no puede ser en el pasado)
5. Envía evento MAINTENANCE_SET al backend
6. Actualiza el potrero a estado MANTENIMIENTO
7. Desbloquea con confirmación simple cuando fecha vence o usuario lo requiere

Esto habilitará que operarios bloqueen potreros de forma fácil y visual sin perder el historial de mantenimientos.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Modal Bloqueo Se Abre Correctamente
```gherkin
Scenario: Abrir modal de bloqueo desde botón
  Given DetailPanel o tabla muestra potrero en estado DISPONIBLE
  And existe botón "Bloquear Potrero"
  When usuario hace clic en "Bloquear Potrero"
  Then:
    [ ] Se abre modal con overlay semi-transparent
    [ ] Título: "Bloquear Potrero - {nombre}"
    [ ] Modal es modal centrado (no drawer)
    [ ] Ancho: 500px (desktop) o 95vw (mobile)
    [ ] Botones: "Confirmar", "Cancelar"
```

### AC#2: Formulario de Bloqueo - Campos Requeridos
```gherkin
Scenario: Modal muestra campos requeridos
  Given modal está abierto
  Then se muestran campos:
    [ ] Razón: select con opciones
        - Fertilización
        - Reparación de cercas
        - Cuarentena
        - Tratamiento médico
        - Otro
        - Default: vacío (requerido)
    [ ] Bloqueado Hasta: date picker
        - Default: hoy (pero no se permite seleccionar)
        - Mínimo: mañana (hoy + 1 día)
        - Máximo: 365 días en el futuro
    [ ] Notas (opcional): textarea
        - Max 200 caracteres
        - Placeholder: "Detalles adicionales"
```

### AC#3: Validación - Razón Requerida
```gherkin
Scenario: Rechazar si razón está vacía
  Given modal abierto con formulario
  When usuario no selecciona razón
  And hace clic en "Confirmar"
  Then:
    [ ] Muestra error: "Razón es requerida"
    [ ] Botón "Confirmar" permanece deshabilitado
    [ ] Modal permanece abierto
```

### AC#4: Validación - Fecha Requerida
```gherkin
Scenario: Rechazar si fecha está vacía o en el pasado
  When usuario no selecciona fecha:
    [ ] Muestra error: "Fecha de desbloqueo es requerida"
    [ ] Botón deshabilitado
  
  When usuario selecciona fecha = hoy:
    [ ] Muestra error: "Fecha debe ser en el futuro"
  
  When usuario selecciona fecha en el pasado:
    [ ] No es posible seleccionar (date picker muestra disabled)
```

### AC#5: Validación - Rango de Fechas
```gherkin
Scenario: Validar rango de fechas permitidas
  Given hoy es 2025-12-09
  When usuario abre date picker
  Then:
    [ ] Dates antes de 2025-12-10 están deshabilitadas
    [ ] Dates a partir de 2025-12-10 habilitadas
    [ ] Dates después de 2026-12-08 deshabilitadas (máximo 365 días)
    [ ] Calendario muestra mes actual por defecto
```

### AC#6: Envío del Evento - MAINTENANCE_SET
```gherkin
Scenario: Enviar evento MAINTENANCE_SET al backend
  Given modal completado correctamente:
    | Razón | Fertilización |
    | Fecha | 2025-12-20 |
    | Notas | Esperar 10 días |
  When usuario hace clic en "Confirmar"
  Then:
    [ ] Se envía POST /farms/{farmId}/pastures/{pastureId}/events
    [ ] Payload contiene:
        {
          "eventType": "MAINTENANCE_SET",
          "reason": "Fertilización",
          "holdUntil": "2025-12-20T00:00:00Z",
          "notes": "Esperar 10 días"
        }
    [ ] Muestra loading spinner durante envío
```

### AC#7: Manejo de Error - Éxito
```gherkin
Scenario: Evento enviado exitosamente (201)
  When POST retorna 201 Created
  Then:
    [ ] Modal se cierra
    [ ] DetailPanel se actualiza:
        * Status: MANTENIMIENTO
        * Substatus: FERTILIZANDO (según razón)
        * HoldUntil: 2025-12-20
        * ETA: INFINITY (no está disponible)
    [ ] Tabla detrás se actualiza automáticamente
    [ ] Toast notification: "Potrero bloqueado hasta 2025-12-20"
```

### AC#8: Manejo de Error - Fallo en Backend
```gherkin
Scenario: POST falla (error 400 o 500)
  When backend retorna error:
    [ ] Modal permanece abierto
    [ ] Muestra error: "No se pudo bloquear potrero. Intenta de nuevo."
    [ ] Botón "Reintentar" disponible
    [ ] Botón "Cancelar" cierra sin cambios
```

### AC#9: Desbloqueo - Confirmación Simple
```gherkin
Scenario: Desbloquear potrero cuando está en MANTENIMIENTO
  Given potrero está en estado MANTENIMIENTO
  And existe botón "Desbloquear Potrero" en DetailPanel
  When usuario hace clic en "Desbloquear"
  Then:
    [ ] Se abre diálogo de confirmación (no modal completo)
    [ ] Mensaje: "¿Desbloquear potrero {nombre}?"
    [ ] Detalle: "Cuarentena, bloqueado hasta 2025-12-20"
    [ ] Botones: "Confirmar desbloqueo", "Cancelar"
```

### AC#10: Desbloqueo - Envío de Evento
```gherkin
Scenario: Enviar evento MAINTENANCE_CLEAR
  Given diálogo de confirmación abierto
  When usuario confirma desbloqueo
  Then:
    [ ] Se envía POST /farms/{farmId}/pastures/{pastureId}/events
    [ ] Payload contiene:
        {
          "eventType": "MAINTENANCE_CLEAR"
        }
    [ ] Si éxito (201):
        * Diálogo se cierra
        * DetailPanel actualiza:
          - Status: EN_DESCANSO (o DISPONIBLE si ETA <= 0)
          - HoldUntil: null
          - Substatus: NINGUNO
        * Toast: "Potrero desbloqueado"
    [ ] Si error:
        * Diálogo permanece abierto
        * Muestra error y botón "Reintentar"
```

### AC#11: Estilos Visuales y UX
```gherkin
Scenario: Modal tiene estilos limpios y profesionales
  Given modal abierto
  Then:
    [ ] Fondo gris/neutral (no muy oscuro)
    [ ] Texto claro y legible
    [ ] Labels de campos en negrita
    [ ] Campos con border y padding
    [ ] Select y date picker con icono
    [ ] Botones bien espaciados
    [ ] Botón "Confirmar" azul/verde (activo)
    [ ] Botón "Cancelar" gris/neutral
    [ ] Botones deshabilitados con opacidad 0.5
    [ ] Hover effects en botones
```

### AC#12: Responsivo - Desktop y Mobile
```gherkin
Scenario: Modal adaptado a pantallas pequeñas
  Given usuario en desktop (1920px)
  When modal abre
  Then:
    [ ] Ancho: 500px
    [ ] Centrado en pantalla
    [ ] Z-index: suficiente para estar encima
  
  Given usuario en mobile (375px)
  When modal abre
  Then:
    [ ] Ancho: 95vw (casi pantalla completa)
    [ ] Padding mínimo: 8px
    [ ] Scroll si contenido > 80vh
    [ ] Botones full-width o apilados
```

### AC#13: Accesibilidad
```gherkin
Scenario: Modal es accesible
  Given modal abierto
  Then:
    [ ] role="dialog" en elemento modal
    [ ] aria-labelledby="modal-title"
    [ ] aria-describedby para descripción
    [ ] Labels vinculados a inputs con htmlFor
    [ ] Tab order correcto (campos → botones)
    [ ] ESC cierra modal
    [ ] Focus trap dentro del modal
    [ ] Mensajes de error vinculados con aria-invalid
```

### AC#14: Notas Opcionales - Comportamiento
```gherkin
Scenario: Campo notes es completamente opcional
  When usuario deja notes vacío
  Then:
    [ ] Se envía sin el campo o como null
    [ ] No hay error
    [ ] Formulario válido
  
  When usuario escribe más de 200 caracteres
  Then:
    [ ] Se trunca automáticamente a 200
    [ ] O muestra error: "Máximo 200 caracteres"
```

### AC#15: Estados del Botón
```gherkin
Scenario: Botón "Confirmar" cambia de estado
  Given modal abierto con formulario vacío
  Then:
    [ ] Botón "Confirmar" está deshabilitado
    [ ] Cursor: not-allowed
    [ ] Opacidad: 0.5
  
  When usuario completa razón y fecha válida
  Then:
    [ ] Botón "Confirmar" se habilita
    [ ] Cursor: pointer
    [ ] Color azul/verde
  
  When usuario hace clic y se envía
  Then:
    [ ] Botón deshabilitado mientras se procesa
    [ ] Muestra spinner o texto: "Bloqueando..."
    [ ] Impide clicks múltiples
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### MaintenanceModal - `MaintenanceModal.jsx` (NUEVO)
```javascript
export function MaintenanceModal({ 
  pasture,           // { id, name, status, ... }
  isOpen,            // boolean
  onClose,           // callback para cerrar
  onSuccess,         // callback después de éxito
  farmId,            // para construir URLs
  isClearMode = false // true si es desbloqueo
}) {
  const [formData, setFormData] = useState({
    reason: '',
    holdUntil: '',
    notes: ''
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  
  // Estados y handlers para validación, submit, etc.
}
```

#### ConfirmDialog - `ConfirmDialog.jsx` (NUEVO - reutilizable)
```javascript
export function ConfirmDialog({ 
  isOpen,
  title,
  message,
  description,
  confirmText = "Confirmar",
  cancelText = "Cancelar",
  isLoading = false,
  onConfirm,
  onCancel,
  isDangerous = false  // para cambiar color de botón
}) {
  // Diálogo simple de confirmación
}
```

#### Hook - `useMaintenanceEvent.js` (NUEVO)
```javascript
export function useMaintenanceEvent(farmId, pastureId) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const setMaintenance = async (reason, holdUntil, notes) => {
    // POST MAINTENANCE_SET event
  };
  
  const clearMaintenance = async () => {
    // POST MAINTENANCE_CLEAR event
  };
  
  return { setMaintenance, clearMaintenance, isLoading, error };
}
```

### Servicios

#### `pastureEventService.js` (AMPLIAR)
```javascript
export async function postMaintenanceEvent(farmId, pastureId, eventData) {
  // POST /farms/{farmId}/pastures/{pastureId}/events
  // eventData: { eventType: 'MAINTENANCE_SET', reason, holdUntil, notes }
  // o { eventType: 'MAINTENANCE_CLEAR' }
}
```

### Constantes

#### `maintenanceReasons.js` (NUEVO)
```javascript
export const MAINTENANCE_REASONS = {
  FERTILIZING: 'Fertilización',
  FENCE_REPAIR: 'Reparación de cercas',
  QUARANTINE: 'Cuarentena',
  MEDICAL_TREATMENT: 'Tratamiento médico',
  OTHER: 'Otro'
};

export const REASON_TO_SUBSTATUS = {
  [MAINTENANCE_REASONS.FERTILIZING]: 'FERTILIZANDO',
  [MAINTENANCE_REASONS.FENCE_REPAIR]: 'REPARANDO',
  [MAINTENANCE_REASONS.QUARANTINE]: 'CUARENTENA',
  [MAINTENANCE_REASONS.MEDICAL_TREATMENT]: 'TRATAMIENTO',
  [MAINTENANCE_REASONS.OTHER]: 'MANTENIMIENTO'
};
```

### Cambios en Componentes Existentes

#### `DetailPanel.jsx` - Agregar Botones
```javascript
// En la sección de botones de acción:
{pasture.status === 'DISPONIBLE' && (
  <button onClick={() => onModalOpen('BLOQUEO', pasture)}>
    🔒 Bloquear Potrero
  </button>
)}

{pasture.status === 'MANTENIMIENTO' && (
  <button onClick={() => onModalOpen('DESBLOQUEO', pasture)}>
    🔓 Desbloquear
  </button>
)}
```

#### `PaddockPage.jsx` - Orquestar Modal
```javascript
const [modalType, setModalType] = useState(null); // BLOQUEO, DESBLOQUEO
const [selectedPasture, setSelectedPasture] = useState(null);

const handleModalOpen = (type, pasture) => {
  setModalType(type);
  setSelectedPasture(pasture);
};

const handleModalSuccess = () => {
  // Cerrar modal, actualizar tabla, mostrar toast
  setModalType(null);
  refetchPastures();
  showToast('Potrero bloqueado/desbloqueado exitosamente');
};

return (
  <>
    {modalType === 'BLOQUEO' && selectedPasture && (
      <MaintenanceModal
        pasture={selectedPasture}
        isOpen={true}
        isClearMode={false}
        farmId={farmId}
        onClose={() => setModalType(null)}
        onSuccess={handleModalSuccess}
      />
    )}
    
    {modalType === 'DESBLOQUEO' && selectedPasture && (
      <MaintenanceModal
        pasture={selectedPasture}
        isOpen={true}
        isClearMode={true}
        farmId={farmId}
        onClose={() => setModalType(null)}
        onSuccess={handleModalSuccess}
      />
    )}
  </>
);
```

### Estilos CSS

```css
.maintenance-modal {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
}

.maintenance-modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  transition: opacity 0.2s;
}

.maintenance-modal-content {
  position: relative;
  background: white;
  border-radius: 12px;
  width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.maintenance-modal-header {
  margin-bottom: 20px;
}

.maintenance-modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.maintenance-modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-label.required::after {
  content: ' *';
  color: #ff0000;
}

.form-input,
.form-select,
.form-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #0066ff;
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

.form-input.error,
.form-select.error {
  border-color: #ff0000;
}

.form-error {
  font-size: 12px;
  color: #ff0000;
  margin-top: 2px;
}

.form-hint {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.maintenance-modal-footer {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.btn-primary {
  background-color: #0066ff;
  color: white;
  flex: 1;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0052cc;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f0f0f0;
  color: #333;
  flex: 1;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .maintenance-modal-content {
    width: 95vw;
    margin: 12px;
  }
  
  .maintenance-modal-footer {
    flex-direction: column;
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`MaintenanceModal.jsx`**
   - Modal principal para bloqueo/desbloqueo
   - Dos modos: BLOQUEO (formulario) y DESBLOQUEO (confirmación)

2. **`ConfirmDialog.jsx`**
   - Componente reutilizable de confirmación
   - Usado para desbloqueo y otras confirmaciones

3. **`useMaintenanceEvent.js`** (Hook)
   - Lógica de POST MAINTENANCE_SET y MAINTENANCE_CLEAR
   - Manejo de loading y errors

4. **`maintenanceReasons.js`**
   - Constantes de razones y substatus

5. **`maintenanceValidators.js`**
   - Validadores para formulario de bloqueo
   - `validateReason()`, `validateDate()`, etc.

### Archivos a Modificar

1. **`DetailPanel.jsx`**
   - Agregar botones "Bloquear" y "Desbloquear"
   - Callbacks para abrir modal

2. **`PaddockPage.jsx`**
   - Estados para modal (tipo, pasture seleccionado)
   - Handlers para abrir/cerrar/éxito

3. **`pastureEventService.js`**
   - Agregar postMaintenanceEvent() si no existe

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear constantes y validadores
```javascript
// maintenanceReasons.js
export const MAINTENANCE_REASONS = { ... };

// maintenanceValidators.js
export function validateReason(reason) {
  return reason && Object.values(MAINTENANCE_REASONS).includes(reason);
}

export function validateDate(dateString) {
  const date = new Date(dateString);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  if (date <= today) return false;
  if (date > new Date(today.getTime() + 365 * 24 * 60 * 60 * 1000)) return false;
  
  return true;
}
```

### Paso 2: Crear hook useMaintenanceEvent
```javascript
export function useMaintenanceEvent(farmId, pastureId) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const setMaintenance = async (reason, holdUntil, notes) => {
    setIsLoading(true);
    setError(null);
    try {
      await postMaintenanceEvent(farmId, pastureId, {
        eventType: 'MAINTENANCE_SET',
        reason,
        holdUntil,
        notes
      });
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };
  
  const clearMaintenance = async () => {
    setIsLoading(true);
    setError(null);
    try {
      await postMaintenanceEvent(farmId, pastureId, {
        eventType: 'MAINTENANCE_CLEAR'
      });
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };
  
  return { setMaintenance, clearMaintenance, isLoading, error };
}
```

### Paso 3: Crear ConfirmDialog
```javascript
export function ConfirmDialog({ 
  isOpen, title, message, description,
  confirmText = "Confirmar", cancelText = "Cancelar",
  isLoading = false, onConfirm, onCancel
}) {
  if (!isOpen) return null;
  
  return (
    <div className="confirm-dialog">
      <div className="confirm-overlay" onClick={onCancel} />
      <div className="confirm-content" role="dialog">
        <h2>{title}</h2>
        <p>{message}</p>
        {description && <p className="confirm-description">{description}</p>}
        <div className="confirm-buttons">
          <button onClick={onCancel} disabled={isLoading}>
            {cancelText}
          </button>
          <button onClick={onConfirm} disabled={isLoading} className="btn-primary">
            {isLoading ? <Spinner /> : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
```

### Paso 4: Crear MaintenanceModal
```javascript
export function MaintenanceModal({ 
  pasture, isOpen, onClose, onSuccess, farmId, isClearMode 
}) {
  const { setMaintenance, clearMaintenance, isLoading, error } = 
    useMaintenanceEvent(farmId, pasture?.id);
  
  const [formData, setFormData] = useState({
    reason: '',
    holdUntil: '',
    notes: ''
  });
  const [errors, setErrors] = useState({});
  
  if (isClearMode) {
    // Mostrar confirmación simple para desbloqueo
    return (
      <ConfirmDialog
        isOpen={isOpen}
        title="Desbloquear Potrero"
        message={`¿Desbloquear ${pasture?.name}?`}
        description={`${pasture?.notes ? 'Notas: ' + pasture.notes : ''}`}
        confirmText="Desbloquear"
        isLoading={isLoading}
        onConfirm={async () => {
          try {
            await clearMaintenance();
            onSuccess?.();
          } catch (err) {
            // Error ya está en state
          }
        }}
        onCancel={onClose}
      />
    );
  }
  
  // Modo BLOQUEO: mostrar formulario
  if (!isOpen) return null;
  
  return (
    <div className="maintenance-modal">
      <div className="maintenance-modal-overlay" onClick={onClose} />
      <div className="maintenance-modal-content" role="dialog">
        <h2 className="maintenance-modal-title">
          Bloquear Potrero - {pasture?.name}
        </h2>
        
        <form className="maintenance-modal-form">
          {/* Razón */}
          <div className="form-group">
            <label className="form-label required">Razón</label>
            <select
              value={formData.reason}
              onChange={(e) => setFormData({...formData, reason: e.target.value})}
              className={`form-select ${errors.reason ? 'error' : ''}`}
            >
              <option value="">Selecciona una razón</option>
              {Object.entries(MAINTENANCE_REASONS).map(([key, label]) => (
                <option key={key} value={label}>{label}</option>
              ))}
            </select>
            {errors.reason && <div className="form-error">{errors.reason}</div>}
          </div>
          
          {/* Fecha */}
          <div className="form-group">
            <label className="form-label required">Bloqueado Hasta</label>
            <input
              type="date"
              min={getTomorrowDate()}
              max={getMaxDate()}
              value={formData.holdUntil}
              onChange={(e) => setFormData({...formData, holdUntil: e.target.value})}
              className={`form-input ${errors.holdUntil ? 'error' : ''}`}
            />
            {errors.holdUntil && <div className="form-error">{errors.holdUntil}</div>}
          </div>
          
          {/* Notas */}
          <div className="form-group">
            <label className="form-label">Notas Adicionales</label>
            <textarea
              value={formData.notes}
              onChange={(e) => setFormData({...formData, notes: e.target.value.substring(0, 200)})}
              maxLength={200}
              rows={3}
              placeholder="Detalles adicionales sobre el bloqueo"
              className="form-textarea"
            />
            <div className="form-hint">{formData.notes.length}/200</div>
          </div>
          
          {/* Error general */}
          {error && <div className="form-error" style={{marginTop: 16}}>{error}</div>}
          
          {/* Botones */}
          <div className="maintenance-modal-footer">
            <button
              type="button"
              onClick={onClose}
              disabled={isLoading}
              className="btn btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={async () => {
                // Validar
                const newErrors = {};
                if (!formData.reason) newErrors.reason = 'Razón es requerida';
                if (!formData.holdUntil) newErrors.holdUntil = 'Fecha es requerida';
                
                if (Object.keys(newErrors).length > 0) {
                  setErrors(newErrors);
                  return;
                }
                
                // Enviar
                try {
                  await setMaintenance(
                    formData.reason,
                    formData.holdUntil,
                    formData.notes || null
                  );
                  onSuccess?.();
                } catch (err) {
                  // Error ya en state
                }
              }}
              disabled={isLoading || !formData.reason || !formData.holdUntil}
              className="btn btn-primary"
            >
              {isLoading ? (
                <span className="btn-loading">
                  <span className="spinner"></span>
                  Bloqueando...
                </span>
              ) : (
                'Confirmar'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
```

### Paso 5: Integrar en DetailPanel y PaddockPage
- Agregar botones en DetailPanel
- Pasar callbacks a PaddockPage
- Manejar estados de modal

---

## 🧪 **Casos de Prueba**

### Test Unitarios (Vitest)

```javascript
describe('maintenanceValidators', () => {
  
  test('valida razón correctamente', () => {
    expect(validateReason('Fertilización')).toBe(true);
    expect(validateReason('Inválida')).toBe(false);
  });
  
  test('valida fecha (no pasado, máximo 365 días)', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    expect(validateDate(tomorrow.toISOString())).toBe(true);
    expect(validateDate(new Date().toISOString())).toBe(false);
  });
});
```

### Test de Componentes (React Testing Library)

```javascript
describe('MaintenanceModal', () => {
  
  test('abre modal en modo bloqueo', () => {
    render(
      <MaintenanceModal
        pasture={mockPasture}
        isOpen={true}
        isClearMode={false}
        {...props}
      />
    );
    expect(screen.getByText(/Bloquear Potrero/)).toBeInTheDocument();
  });
  
  test('requiere razón para enviar', async () => {
    render(<MaintenanceModal {...props} />);
    
    fireEvent.click(screen.getByText('Confirmar'));
    
    expect(screen.getByText('Razón es requerida')).toBeInTheDocument();
  });
  
  test('envía evento con datos válidos', async () => {
    render(<MaintenanceModal {...props} />);
    
    const reasonSelect = screen.getByRole('combobox');
    const dateInput = screen.getByType('date');
    
    fireEvent.change(reasonSelect, { target: { value: 'Fertilización' } });
    fireEvent.change(dateInput, { target: { value: '2025-12-20' } });
    fireEvent.click(screen.getByText('Confirmar'));
    
    await waitFor(() => {
      expect(mockPostEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          eventType: 'MAINTENANCE_SET',
          reason: 'Fertilización'
        })
      );
    });
  });
});
```

### Test E2E (Cypress)

```javascript
describe('MaintenanceModal - E2E', () => {
  
  it('bloquea potrero exitosamente', () => {
    cy.intercept('POST', '/farms/*/pastures/*/events', {
      statusCode: 201,
      body: { id: 'P001', status: 'MANTENIMIENTO' }
    }).as('blockPasture');
    
    cy.contains('button', 'Bloquear').click();
    cy.get('[role="dialog"]').should('be.visible');
    cy.get('select').select('Fertilización');
    cy.get('input[type="date"]').type('2025-12-20');
    cy.contains('button', 'Confirmar').click();
    
    cy.wait('@blockPasture');
    cy.get('[role="dialog"]').should('not.exist');
    cy.contains('Potrero bloqueado').should('be.visible');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: Bloquear Potrero Exitosamente
```gherkin
Scenario: Usuario bloquea potrero para fertilización
  Given potrero en estado DISPONIBLE
  When usuario hace clic en "Bloquear"
  And selecciona razón "Fertilización"
  And selecciona fecha 2025-12-20
  And presiona "Confirmar"
  Then modal se cierra
  And potrero actualiza a MANTENIMIENTO
  And DetailPanel muestra "Bloqueado hasta 2025-12-20"
  And toast muestra éxito
```

### Escenario 2: Desbloquear Potrero
```gherkin
Scenario: Desbloquear potrero manualmente
  Given potrero en MANTENIMIENTO
  When usuario hace clic en "Desbloquear"
  And confirma en diálogo
  Then evento MAINTENANCE_CLEAR se envía
  And potrero vuelve a EN_DESCANSO
  And toast muestra éxito
```

### Escenario 3: Validación de Fechas
```gherkin
Scenario: No permite fecha en el pasado o muy lejana
  Given modal abierto
  When intenta seleccionar fecha = hoy
  Then date picker la desabilita (no se puede seleccionar)
  
  When intenta seleccionar fecha > 365 días
  Then también deshabilitada
```

### Escenario 4: Error Handling
```gherkin
Scenario: Manejo de error si POST falla
  Given modal completado
  When POST retorna 500
  Then modal permanece abierto
  And muestra error: "No se pudo bloquear potrero"
  And botón "Reintentar" disponible
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otros componentes**:
- ✅ Backend POST /events (HU#1)
- ✅ DetailPanel (HU#3)
- ✅ usePastureEvent hook (HU#2)

**Documentación relacionada**:
- [HU#3: Frontend DetailPanel](../P0/PASTURES-HU-003-detailpanel.md)
- [HU#1: Backend POST Eventos](../P0/PASTURES-HU-001-post-eventos.md)
- [Pastures Overview](../../pastures/pastures-overview.md)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** React Modal Component Dual-Mode + ConfirmDialog Pattern Reutilizable + Hook Custom + Validadores Puros

**Justificación:** **Modal dual-mode**: Un único componente (`isClearMode` prop) maneja BLOQUEO (formulario) y DESBLOQUEO (confirmación) - DRY principle, menos código duplicado. **ConfirmDialog reutilizable**: Componente genérico útil para futuras confirmaciones (delete, etc.), decoupled de lógica de mantenimiento. **Hook custom**: `useMaintenanceEvent` encapsula lógica HTTP (POST MAINTENANCE_SET/CLEAR), permite testing y reutilización (patrón HU#2). **Validadores puros**: Funciones sin efectos secundarios, testeables al 100%, mantenibles. **Constantes centralizadas**: MAINTENANCE_REASONS + REASON_TO_SUBSTATUS - single source of truth evita duplicación. **Accesibilidad first**: role="dialog", aria labels, keyboard nav (ESC), focus trapping - WCAG compliant. **Validación técnica completada**: Backend eventos HU#1 verificado, DetailPanel HU#3 disponible para integración, TailwindCSS estilos confirmados.

**Componentes Afectados:**

- **MaintenanceModal.jsx (Nuevo):** Modal dual-mode. Props: `pasture`, `isOpen`, `onClose`, `onSuccess`, `farmId`, `isClearMode` (boolean). **Modo BLOQUEO** (`isClearMode=false`): Formulario con 3 campos (reason select, holdUntil date picker, notes textarea). **Modo DESBLOQUEO** (`isClearMode=true`): Renderiza ConfirmDialog. State: `formData` (reason, holdUntil, notes), `errors`, `isLoading` (del hook), `submitError`. CSS: Fixed position, z-index 1100, overlay backdrop, responsive (500px desktop / 95vw mobile). Validación local antes de POST. ESC y click-overlay cierra. Button disabled states según validación.

- **ConfirmDialog.jsx (Nuevo - Reutilizable):** Diálogo genérico de confirmación. Props: `isOpen`, `title`, `message`, `description`, `confirmText`, `cancelText`, `isLoading`, `onConfirm`, `onCancel`, `isDangerous` (para color). Fixed position, modal with overlay. Botones simétricos (horizontal desktop, vertical mobile). Loading state con spinner. role="dialog", aria-labelledby, aria-describedby. ESC y click-overlay cierra.

- **useMaintenanceEvent.js (Nuevo - Hook Custom):** Orquesta POST MAINTENANCE_SET y MAINTENANCE_CLEAR. Retorna `{ setMaintenance, clearMaintenance, isLoading, error }`. `setMaintenance(reason, holdUntil, notes)` → POST con eventType=MAINTENANCE_SET. `clearMaintenance()` → POST con eventType=MAINTENANCE_CLEAR. State: `isLoading` (boolean), `error` (string|null). Llama `postMaintenanceEvent()` de pastureEventService. Manejo: Network error, timeout 30s, 400/404/500. AbortController para cleanup.

- **maintenanceReasons.js (Nuevo - Constantes):** Centralizar razones y mappeos. `MAINTENANCE_REASONS` object con keys y labels. `REASON_TO_SUBSTATUS` mapeo de razón → substatus backend (Fertilización→FERTILIZANDO, Reparación→REPARANDO, etc.). Single source of truth.

- **maintenanceValidators.js (Nuevo - Funciones Puras):** `validateReason(reason)` → boolean. `validateDate(dateString)` → boolean (futuro, ≤365 días). `validateNotes(notes)` → boolean (max 200 chars). `getTomorrowDate()` → string (yyyy-MM-dd para min date picker). `getMaxDate()` → string (hoy+365 días). Sin efectos secundarios.

- **DetailPanel.jsx (Modificación - Agregar botones):** Nueva sección en botones acción. Botón "🔒 Bloquear" (si DISPONIBLE) → `onModalOpen('BLOQUEO', pasture)`. Botón "🔓 Desbloquear" (si MANTENIMIENTO) → `onModalOpen('DESBLOQUEO', pasture)`. Condicionales por `pasture.status`.

- **PaddockPage.jsx (Modificación - Orquestar):** Estado: `modalType` (null|'BLOQUEO'|'DESBLOQUEO'), `selectedPasture`. Handlers: `handleModalOpen(type, pasture)`, `handleModalClose()`, `handleModalSuccess()`. Renderizar MaintenanceModal cuando `modalType !== null`. Callback onSuccess actualiza tabla + toast.

- **pastureEventService.js (Modificación - Agregar función):** Función `postMaintenanceEvent(farmId, pastureId, eventData)` si no existe. POST a `/farms/{farmId}/pastures/{pastureId}/events`. Payload MAINTENANCE_SET: `{ eventType, reason, holdUntil, notes }`. Payload MAINTENANCE_CLEAR: `{ eventType }`.

**Hitos de Implementación:**

1. **maintenanceReasons.js** - Constantes (sin dependencias)
2. **maintenanceValidators.js** - Validadores puros (sin dependencias)
3. **ConfirmDialog.jsx** - Componente confirmación reutilizable (depende: TailwindCSS)
4. **useMaintenanceEvent.js** - Hook custom (depende: pastureEventService)
5. **MaintenanceModal.jsx** - Modal principal dual-mode (depende: ConfirmDialog, useMaintenanceEvent, validadores, constantes)
6. **DetailPanel.jsx - Agregar botones** (depende: MaintenanceModal callback)
7. **PaddockPage.jsx - Orquestar** (depende: MaintenanceModal, estados)
8. **pastureEventService.js - Agregar función** (si no existe)

### Validación de Impacto

**Hallazgos de validación técnica:**

✅ **Componentes Existentes Reutilizables:**
- `DetailPanel` (HU#3) - Disponible para agregar botones ✅
- `PaddockPage` (HU#3) - Disponible para orquestar ✅
- `usePastureEvent` hook (HU#2) - Patrón similar para MAINTENANCE ✅
- TailwindCSS (proyecto) - Estilos modal + responsive ✅

✅ **Endpoints Backend Disponibles:**
- POST /farms/{farmId}/pastures/{pastureId}/events (HU#1) ✅
- Acepta eventType=MAINTENANCE_SET + reason + holdUntil ✅
- Acepta eventType=MAINTENANCE_CLEAR ✅

✅ **Impacto en UX:**
- Modal dual-mode: Menos código, más reutilizable
- ConfirmDialog genérica: Útil para futuras confirmaciones
- Validaciones locales: Feedback instantáneo
- Date picker nativo: Restricciones built-in (pasado deshabilitado)

✅ **Impacto en Performance:**
- Dos POST: MAINTENANCE_SET y MAINTENANCE_CLEAR (ambas rápidas)
- No recarga tabla inicial - solo actualización local
- Lazy-loaded component - no impacta carga inicial

✅ **Testing:**
- Validadores: Funciones puras, 100% coverage
- Hook: Mockeable con jest.mock fetch
- Componentes: React Testing Library estándar
- E2E: Cypress con intercept

✅ **Flujo de Usuario Verificado:**
```
Click "Bloquear" en DetailPanel
  ↓ onModalOpen('BLOQUEO', pasture)
  ↓ MaintenanceModal abre (formulario)
  ↓ Usuario selecciona: Razón, Fecha, Notas (opt)
  ↓ Validación local: validateReason + validateDate
  ↓ Botón "Confirmar" se habilita
  ↓ Click → useMaintenanceEvent.setMaintenance()
  ↓ POST MAINTENANCE_SET → Backend HU#1
  ↓ Si 201: Modal cierra, DetailPanel actualiza
  ↓ Toast: "Potrero bloqueado hasta 2025-12-20"
```

✅ **Riesgos Mitigables:**
- Razón no seleccionada → Botón deshabilitado + error inline
- Fecha inválida → Date picker desabilita pasado
- Servidor error → Error message + reintentar
- Race condition → AbortController cleanup
- ESC pulsado → Cierra modal (user-friendly)

### Notas Técnicas

**Modal Dual-Mode Pattern:**
```javascript
export function MaintenanceModal({ isClearMode, ...props }) {
  if (isClearMode) {
    return <ConfirmDialog {...confirmProps} />;
  }
  
  return <div className="modal">/* Formulario */</div>;
}
```

**Date Input HTML5 - Restricciones Nativas:**
```javascript
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);

const maxDate = new Date();
maxDate.setDate(maxDate.getDate() + 365);

<input
  type="date"
  min={tomorrow.toISOString().split('T')[0]}
  max={maxDate.toISOString().split('T')[0]}
/>
```

**Hook Pattern - similar a HU#2:**
```javascript
const setMaintenance = async (reason, holdUntil, notes) => {
  setIsLoading(true);
  try {
    await postMaintenanceEvent(farmId, pastureId, {
      eventType: 'MAINTENANCE_SET',
      reason,
      holdUntil,
      notes
    });
  } catch (err) {
    setError(err.message);
  } finally {
    setIsLoading(false);
  }
};
```

**Accesibilidad - Modal Setup:**
```javascript
<div 
  role="dialog"
  aria-labelledby="modal-title"
  aria-modal="true"
  ref={modalRef}  // Focus trap
>
```

**Validación Local Integrada:**
```javascript
const isFormValid = 
  formData.reason && 
  formData.holdUntil && 
  validateDate(formData.holdUntil);

<button disabled={!isFormValid}>
  Confirmar
</button>
```

### Referencias y Validación

**Documentación Consultada:**
- [flujo-registro-bovino.md](../../architecture/flujo-registro-bovino.md) - Modal patterns
- [PASTURES-HU-003](../P0/PASTURES-HU-003-detailpanel.md) - DetailPanel integración ✅
- [PASTURES-HU-002](../P0/PASTURES-HU-002-frontend-botones.md) - Hook pattern ✅
- [PASTURES-HU-001](../P0/PASTURES-HU-001-post-eventos.md) - Endpoint backend ✅

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (eventos)
- ✅ PASTURES-HU-002: Frontend Botones (hook pattern)
- ✅ PASTURES-HU-003: Detail Panel (integración)
- → PASTURES-HU-006: Frontend Modal Bloqueo (esta - Modal dual-mode)

**Stack Tecnológico Verificado:**
- React 19.1.0 - Hooks, State, Refs
- TailwindCSS 3.4.3 - Estilos modal + responsive
- Fetch API - HTTP client
- HTML5 Date Input - Restricciones nativas

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Modal dual-mode + ConfirmDialog reutilizable + validadores puros (componentes reutilizables)

---

## 🔧 **Refinamiento Técnico**

### Component Structure

**MaintenanceModal.jsx - Dual Mode:**
```javascript
export const MaintenanceModal = ({ pasture, isOpen, onClose, onSuccess }) => {
  const [mode, setMode] = useState(null); // 'set' | 'clear'
  const [formData, setFormData] = useState({});
  const [error, setError] = useState(null);
  const { applyEvent, loading } = usePastureEvent(pasture.farmId);
  
  const handleConfirm = async () => {
    const eventType = mode === 'set' 
      ? 'MAINTENANCE_SET' 
      : 'MAINTENANCE_CLEAR';
    
    try {
      const result = await applyEvent(pasture.id, eventType, formData);
      onSuccess(result);
      onClose();
    } catch (err) {
      setError(err.message);
    }
  };
  
  return (
    <Dialog isOpen={isOpen} onClose={onClose}>
      {!mode && <ModeSelector onSelect={setMode} />}
      {mode === 'set' && <MaintenanceSetForm data={formData} onChange={setFormData} />}
      {mode === 'clear' && <MaintenanceClearForm />}
      <button onClick={handleConfirm} disabled={loading}>Confirmar</button>
    </Dialog>
  );
};
```

**ModeSelector Component:**
```javascript
const ModeSelector = ({ onSelect }) => (
  <div className="space-y-4">
    <h3>¿Qué acción deseas?</h3>
    <button onClick={() => onSelect('set')} className="btn btn-primary">
      ⛔ Bloquear (Mantenimiento)
    </button>
    <button onClick={() => onSelect('clear')} className="btn btn-secondary">
      ✅ Desbloquear
    </button>
  </div>
);
```

### API Integration

**MAINTENANCE_SET Event:**
```json
{
  "eventType": "MAINTENANCE_SET",
  "maintenanceSubstatus": "FERTILIZANDO",
  "holdUntil": "2026-01-20"
}
```

**MAINTENANCE_CLEAR Event:**
```json
{
  "eventType": "MAINTENANCE_CLEAR"
}
```

### Validadores

```javascript
export const validateMaintenanceSet = (data) => {
  const errors = {};
  if (!data.holdUntil) errors.holdUntil = 'Fecha es requerida';
  if (new Date(data.holdUntil) < new Date()) 
    errors.holdUntil = 'Fecha debe ser futura';
  return errors;
};
```

### Testing Strategy

**Component Tests (RTL):**
```javascript
test('Modal muestra selector de modo', () => {
  render(<MaintenanceModal pasture={mock} isOpen={true} />);
  expect(screen.getByText(/bloquear/i)).toBeInTheDocument();
});

test('Modo SET muestra formulario de fecha', async () => {
  render(<MaintenanceModal pasture={mock} isOpen={true} />);
  fireEvent.click(screen.getByText(/bloquear/i));
  expect(screen.getByLabel(/fecha/i)).toBeInTheDocument();
});
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `MaintenanceModal.jsx` completamente implementado
- [ ] `ConfirmDialog.jsx` reutilizable
- [ ] `useMaintenanceEvent.js` hook con lógica
- [ ] `maintenanceReasons.js` constantes y mappings
- [ ] `maintenanceValidators.js` validadores
- [ ] Modo BLOQUEO (formulario) funcional
- [ ] Modo DESBLOQUEO (confirmación) funcional
- [ ] Validaciones: razón requerida, fecha válida
- [ ] Date picker restringe fechas (mañana a 365 días)
- [ ] POST MAINTENANCE_SET se envía correctamente
- [ ] POST MAINTENANCE_CLEAR se envía correctamente
- [ ] DetailPanel actualiza post-evento
- [ ] Tabla detrás se actualiza
- [ ] Toast notifications funcionan
- [ ] Responsivo: desktop (500px) + mobile (95vw)
- [ ] Estilos visuales limpios y profesionales
- [ ] Accesibilidad: role, aria labels, tab order
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests componentes: todos los ACs probados
- [ ] Tests E2E: flujos completos validados
- [ ] ESC cierra modal
- [ ] Click en overlay cierra modal
- [ ] Botones disabled en estado incorrecto
- [ ] Loading states y spinners
- [ ] Error messages vinculados a campos
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Documentación actualizada (JSDoc, comentarios)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
