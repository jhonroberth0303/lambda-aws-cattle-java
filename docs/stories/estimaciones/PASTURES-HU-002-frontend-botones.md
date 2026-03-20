# 🌱 PASTURES-HU#2: Frontend: Conectar Botones Abrir/Cerrar

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🔴 CRÍTICO (P0) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Propuesta aprobada | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero conectar los botones "Abrir" y "Cerrar" de la tabla de potreros al endpoint POST de eventos, de tal forma que:

1. Al hacer clic en "Abrir", se abre un modal/confirmación con campos necesarios
2. Al hacer clic en "Cerrar", se abre un modal/confirmación con campos necesarios
3. Se validan los datos localmente antes de enviar
4. Se muestra loading mientras se procesa en backend
5. Se actualiza la tabla al recibir respuesta exitosa
6. Se muestra error si falla

Esto habilitará que operarios registren acciones críticas de rotación directamente desde el dashboard.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Botón "Abrir" Abre Modal
```gherkin
Scenario: Click en botón "Abrir" abre modal con formulario
  Given el usuario está en PaddockPage/Dashboard
  And existe tabla de potreros con botón "Abrir" en cada fila
  And el potrero está en estado DISPONIBLE
  When el usuario hace clic en botón "Abrir" de un potrero
  Then se abre modal/dialog con:
    [ ] Título: "Abrir Potrero - {nombre_potrero}"
    [ ] Campo: "Lote ID" (text input, required)
    [ ] Campo: "Cantidad de Animales" (number input, required, min=1)
    [ ] Campo: "Usuario" (text, pre-llenado con usuario actual)
    [ ] Botón "Confirmar" (disabled hasta validar)
    [ ] Botón "Cancelar" (cierra modal sin hacer cambios)
    [ ] Datos por defecto: usuario actual en campo Usuario
```

### AC#2: Botón "Cerrar" Abre Modal
```gherkin
Scenario: Click en botón "Cerrar" abre modal con campos específicos
  Given el usuario está en PaddockPage/Dashboard
  And existe tabla de potreros con botón "Cerrar" en cada fila
  And el potrero está en estado EN_USO
  When el usuario hace clic en botón "Cerrar" de un potrero
  Then se abre modal/dialog con:
    [ ] Título: "Cerrar Potrero - {nombre_potrero}"
    [ ] Campo: "Lote ID" (text input, required)
    [ ] Campo: "Cantidad de Animales" (number input, required, min=1)
    [ ] Campo: "Altura Residual (cm)" (number input, required, min=1, max=50)
    [ ] Campo: "Usuario" (text, pre-llenado con usuario actual)
    [ ] Botón "Confirmar" (disabled hasta validar)
    [ ] Botón "Cancelar"
    [ ] Validación: residualCm <= altura actual del potrero (mostrar error si viola)
```

### AC#3: Validación Local - Campos Requeridos (OPEN)
```gherkin
Scenario: Validar campos requeridos en modal OPEN
  Given modal OPEN está abierto
  When usuario intenta confirmar sin completar campos:
    - Lote ID vacío
    - Cantidad de Animales = 0 o vacío
  Then se muestran errores inline:
    [ ] "Lote ID es requerido"
    [ ] "Cantidad de Animales debe ser mayor a 0"
  And botón "Confirmar" permanece disabled
  And NO se envía request al backend
```

### AC#4: Validación Local - Campos Requeridos (CLOSE)
```gherkin
Scenario: Validar campos requeridos en modal CLOSE
  Given modal CLOSE está abierto
  When usuario intenta confirmar sin completar campos:
    - Lote ID vacío
    - Cantidad de Animales = 0
    - Altura Residual = 0 o vacío
  Then se muestran errores inline:
    [ ] "Lote ID es requerido"
    [ ] "Cantidad de Animales debe ser mayor a 0"
    [ ] "Altura Residual debe ser mayor a 0"
  And botón "Confirmar" permanece disabled
  And NO se envía request
```

### AC#5: Validación Local - Altura Residual
```gherkin
Scenario: Validar altura residual no mayor a altura actual (CLOSE)
  Given modal CLOSE está abierto
  And potrero actual tiene altura = 25 cm
  When usuario ingresa residualCm = 30 (mayor a 25)
  And presiona "Confirmar"
  Then se muestra error:
    "Altura residual (30 cm) no puede ser mayor a altura actual (25 cm)"
  And NO se envía request
```

### AC#6: Envío de Evento OPEN - Happy Path
```gherkin
Scenario: Enviar evento OPEN al backend exitosamente
  Given modal OPEN está abierto con datos válidos:
    | Campo | Valor |
    | Lote ID | LOT001 |
    | Animales | 15 |
    | Usuario | juan.perez@farm.com |
  When usuario presiona "Confirmar"
  Then:
    [ ] Botón "Confirmar" muestra loading (disabled, spinner)
    [ ] Se envía POST /farms/{farmId}/pastures/{pastureId}/events
        {
          "eventType": "OPEN",
          "user": "juan.perez@farm.com",
          "lotId": "LOT001",
          "animals": 15
        }
    [ ] Se espera respuesta del backend (máx 30 segundos timeout)
  
  When respuesta es HTTP 201:
    [ ] Modal se cierra
    [ ] Tabla de potreros se actualiza:
        - status del potrero cambia a EN_USO
        - lastUseAt se actualiza a ahora
        - lastLotId muestra "LOT001"
        - lastAnimalCount muestra "15"
    [ ] Se muestra toast/notification: "Potrero abierto exitosamente"
    [ ] Toast desaparece en 3 segundos
```

### AC#7: Envío de Evento CLOSE - Happy Path
```gherkin
Scenario: Enviar evento CLOSE al backend exitosamente
  Given modal CLOSE está abierto con datos válidos:
    | Campo | Valor |
    | Lote ID | LOT001 |
    | Animales | 15 |
    | Altura Residual | 8 |
    | Usuario | juan.perez@farm.com |
  When usuario presiona "Confirmar"
  Then:
    [ ] Botón "Confirmar" muestra loading
    [ ] Se envía POST /farms/{farmId}/pastures/{pastureId}/events
        {
          "eventType": "CLOSE",
          "user": "juan.perez@farm.com",
          "lotId": "LOT001",
          "animals": 15,
          "residualCm": 8
        }
  
  When respuesta es HTTP 201:
    [ ] Modal se cierra
    [ ] Tabla de potreros se actualiza:
        - status del potrero cambia a EN_DESCANSO (o DISPONIBLE si ETA <= 0)
        - lastClosedAt se actualiza
        - residualHeightCm muestra "8"
        - ETA se recalcula y muestra
    [ ] Se muestra toast: "Potrero cerrado exitosamente"
```

### AC#8: Manejo de Error - Backend Retorna 400
```gherkin
Scenario: Backend valida y rechaza evento
  Given usuario intenta enviar evento
  When backend responde HTTP 400:
    {
      "status": 400,
      "message": "residualCm (50 cm) no puede ser mayor a altura actual (30 cm)"
    }
  Then:
    [ ] Modal permanece abierto
    [ ] Se muestra error en rojo bajo el botón "Confirmar":
        "Error: residualCm (50 cm) no puede ser mayor a altura actual (30 cm)"
    [ ] Usuario puede corregir campos e intentar de nuevo
    [ ] Botón "Confirmar" vuelve a estar activo (sin loading)
```

### AC#9: Manejo de Error - Backend Retorna 404
```gherkin
Scenario: Potrero no existe en backend
  Given usuario intenta abrir/cerrar potrero
  When backend responde HTTP 404:
    { "message": "Potrero P999 no encontrado" }
  Then:
    [ ] Modal se cierra automáticamente
    [ ] Se muestra toast error (rojo) con mensaje:
        "Error: Potrero no encontrado. Por favor, recarga la página."
    [ ] Tabla se actualiza con GET /farms/{farmId}/pastures
    [ ] Toast error persiste por 5 segundos
```

### AC#10: Manejo de Error - Timeout
```gherkin
Scenario: Timeout esperando respuesta del backend
  Given usuario presiona "Confirmar"
  When pasan 30 segundos sin respuesta del backend
  Then:
    [ ] Request se cancela (AbortController)
    [ ] Modal permanece abierto
    [ ] Se muestra error:
        "Tiempo límite excedido. Por favor, intenta de nuevo."
    [ ] Botón "Confirmar" vuelve a estar activo
    [ ] Usuario puede reintentar o cancelar
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### Modal OPEN - `OpenPastureModal.jsx` (NUEVO)
```javascript
export function OpenPastureModal({ 
  pasture,           // { id, name, species, status, areaHa, ... }
  isOpen,            // boolean
  onClose,           // callback to close modal
  onSuccess,         // callback after successful POST
  farmId             // string (para construir URL)
}) {
  const [formData, setFormData] = useState({
    lotId: '',
    animals: null,
    user: getCurrentUser()?.email || ''
  });
  
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  
  // Validar antes de enviar
  // POST a /farms/{farmId}/pastures/{pastureId}/events
  // Actualizar tabla en success
}
```

#### Modal CLOSE - `ClosePastureModal.jsx` (NUEVO)
```javascript
export function ClosePastureModal({ 
  pasture,           // { id, name, currentHeightCm, ... }
  isOpen, 
  onClose, 
  onSuccess,
  farmId 
}) {
  const [formData, setFormData] = useState({
    lotId: '',
    animals: null,
    residualCm: null,
    user: getCurrentUser()?.email || ''
  });
  
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
}
```

#### Hook Personalizado - `usePastureEvent.js` (NUEVO)
```javascript
export function usePastureEvent() {
  const [loading, setLoading] = useState(false);
  
  // POST event to backend
  // Handle 201, 400, 404, timeout
  // Return { isLoading, error, applyEvent }
  
  const applyEvent = async (farmId, pastureId, eventRequest) => {
    setLoading(true);
    try {
      const response = await fetch(
        `/farms/${farmId}/pastures/${pastureId}/events`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(eventRequest),
          signal: AbortSignal.timeout(30000) // 30s timeout
        }
      );
      
      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message);
      }
      
      return await response.json(); // Potrero actualizado
    } finally {
      setLoading(false);
    }
  };
  
  return { loading, applyEvent };
}
```

### Cambios en Componentes Existentes

#### `PastureTable.jsx` - Agregar Botones
```javascript
// En columna de acciones, después de botón "Ver Detalles":

<button
  onClick={() => {
    if (pasture.status === 'DISPONIBLE') {
      onOpenModal('OPEN', pasture);
    } else if (pasture.status === 'EN_USO') {
      onOpenModal('CLOSE', pasture);
    }
  }}
  className="btn-sm btn-primary"
  disabled={!['DISPONIBLE', 'EN_USO'].includes(pasture.status)}
>
  {pasture.status === 'DISPONIBLE' ? '📖 Abrir' : '🔒 Cerrar'}
</button>
```

#### `PaddockPage.jsx` - Orquestar Modales
```javascript
export function PaddockPage() {
  const [pastures, setPastures] = useState([]);
  const [selectedPasture, setSelectedPasture] = useState(null);
  const [modalType, setModalType] = useState(null); // 'OPEN' | 'CLOSE' | null
  
  const farmId = useParams().farmId;
  
  const handleOpenModal = (type, pasture) => {
    setModalType(type);
    setSelectedPasture(pasture);
  };
  
  const handleModalClose = () => {
    setModalType(null);
    setSelectedPasture(null);
  };
  
  const handleModalSuccess = (updatedPasture) => {
    // Actualizar tabla localmente
    setPastures(prev => 
      prev.map(p => p.id === updatedPasture.id ? updatedPasture : p)
    );
    handleModalClose();
    showToast('success', 'Potrero actualizado exitosamente');
  };
  
  return (
    <div>
      <PastureTable 
        pastures={pastures}
        onOpenModal={handleOpenModal}
        {...props}
      />
      
      {modalType === 'OPEN' && selectedPasture && (
        <OpenPastureModal
          pasture={selectedPasture}
          isOpen={true}
          onClose={handleModalClose}
          onSuccess={handleModalSuccess}
          farmId={farmId}
        />
      )}
      
      {modalType === 'CLOSE' && selectedPasture && (
        <ClosePastureModal
          pasture={selectedPasture}
          isOpen={true}
          onClose={handleModalClose}
          onSuccess={handleModalSuccess}
          farmId={farmId}
        />
      )}
    </div>
  );
}
```

### Validadores - `pastureEventValidators.js` (NUEVO)
```javascript
export const validateOpenEvent = (formData) => {
  const errors = {};
  
  if (!formData.lotId?.trim()) {
    errors.lotId = 'Lote ID es requerido';
  }
  
  if (!formData.animals || formData.animals <= 0) {
    errors.animals = 'Cantidad de animales debe ser mayor a 0';
  }
  
  return errors;
};

export const validateCloseEvent = (formData, currentHeightCm) => {
  const errors = { ...validateOpenEvent(formData) };
  
  if (!formData.residualCm || formData.residualCm <= 0) {
    errors.residualCm = 'Altura residual debe ser mayor a 0';
  }
  
  if (formData.residualCm > currentHeightCm) {
    errors.residualCm = 
      `Altura residual (${formData.residualCm} cm) no puede ser mayor a altura actual (${currentHeightCm} cm)`;
  }
  
  return errors;
};
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`OpenPastureModal.jsx`**
   - Componente modal para evento OPEN
   - Campos: lotId, animals, user
   - Validación local
   - Llamar usePastureEvent
   - Manejo de errores

2. **`ClosePastureModal.jsx`**
   - Componente modal para evento CLOSE
   - Campos: lotId, animals, residualCm, user
   - Validación local + altura residual
   - Llamar usePastureEvent
   - Manejo de errores

3. **`usePastureEvent.js`** (Hook Personalizado)
   - POST /farms/{farmId}/pastures/{pastureId}/events
   - Timeout 30s con AbortController
   - Return { loading, error, applyEvent }

4. **`pastureEventValidators.js`**
   - validateOpenEvent()
   - validateCloseEvent()

### Archivos a Modificar

1. **`PastureTable.jsx`**
   - Agregar columna o botones de acción
   - Botón "Abrir" si status = DISPONIBLE
   - Botón "Cerrar" si status = EN_USO
   - Callback onOpenModal

2. **`PaddockPage.jsx`**
   - Agregar estado: modalType, selectedPasture
   - Handlers: handleOpenModal, handleModalClose, handleModalSuccess
   - Renderizar <OpenPastureModal /> y <ClosePastureModal />
   - Actualizar tabla post-success

3. **`pastureServices.ts`** (opcional)
   - O usar fetch directo desde usePastureEvent
   - Si existe pastureService, centralizar ahí

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear validadores
```javascript
// archivo: cattle-front/src/components/Paddock/validators/pastureEventValidators.js

export const validateOpenEvent = (formData) => {
  const errors = {};
  if (!formData.lotId?.trim()) errors.lotId = 'Lote ID requerido';
  if (!formData.animals || formData.animals <= 0) errors.animals = 'Animales > 0';
  return errors;
};

export const validateCloseEvent = (formData, currentHeightCm) => {
  const errors = validateOpenEvent(formData);
  if (!formData.residualCm || formData.residualCm <= 0) {
    errors.residualCm = 'Altura residual > 0';
  }
  if (formData.residualCm > currentHeightCm) {
    errors.residualCm = `No puede ser mayor a ${currentHeightCm} cm`;
  }
  return errors;
};
```

### Paso 2: Crear hook usePastureEvent
```javascript
// archivo: cattle-front/src/components/Paddock/hooks/usePastureEvent.js

export function usePastureEvent() {
  const [loading, setLoading] = useState(false);
  
  const applyEvent = async (farmId, pastureId, eventRequest) => {
    setLoading(true);
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000);
    
    try {
      const response = await fetch(
        `/farms/${farmId}/pastures/${pastureId}/events`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(eventRequest),
          signal: controller.signal
        }
      );
      
      clearTimeout(timeoutId);
      
      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Error desconocido');
      }
      
      return await response.json();
    } catch (error) {
      if (error.name === 'AbortError') {
        throw new Error('Tiempo límite excedido (30s)');
      }
      throw error;
    } finally {
      setLoading(false);
      clearTimeout(timeoutId);
    }
  };
  
  return { loading, applyEvent };
}
```

### Paso 3: Crear OpenPastureModal
```javascript
// archivo: cattle-front/src/components/Paddock/modals/OpenPastureModal.jsx

import { validateOpenEvent } from '../validators/pastureEventValidators';
import { usePastureEvent } from '../hooks/usePastureEvent';

export function OpenPastureModal({ pasture, isOpen, onClose, onSuccess, farmId }) {
  const [formData, setFormData] = useState({
    lotId: '',
    animals: null,
    user: getCurrentUser()?.email || ''
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState(null);
  const { loading, applyEvent } = usePastureEvent();
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Limpiar error cuando usuario edita
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validateOpenEvent(formData);
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    
    setServerError(null);
    
    try {
      const updated = await applyEvent(farmId, pasture.id, {
        eventType: 'OPEN',
        user: formData.user,
        lotId: formData.lotId,
        animals: parseInt(formData.animals)
      });
      
      onSuccess(updated);
    } catch (error) {
      setServerError(error.message);
    }
  };
  
  if (!isOpen) return null;
  
  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>Abrir Potrero - {pasture.name}</h2>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Lote ID *</label>
            <input
              type="text"
              name="lotId"
              value={formData.lotId}
              onChange={handleChange}
              className={errors.lotId ? 'error' : ''}
            />
            {errors.lotId && <span className="error-msg">{errors.lotId}</span>}
          </div>
          
          <div className="form-group">
            <label>Cantidad de Animales *</label>
            <input
              type="number"
              name="animals"
              min="1"
              value={formData.animals || ''}
              onChange={handleChange}
              className={errors.animals ? 'error' : ''}
            />
            {errors.animals && <span className="error-msg">{errors.animals}</span>}
          </div>
          
          <div className="form-group">
            <label>Usuario</label>
            <input type="text" value={formData.user} disabled />
          </div>
          
          {serverError && (
            <div className="error-alert">{serverError}</div>
          )}
          
          <div className="modal-actions">
            <button 
              type="button" 
              onClick={onClose}
              className="btn-secondary"
              disabled={loading}
            >
              Cancelar
            </button>
            <button 
              type="submit"
              className="btn-primary"
              disabled={loading || Object.keys(errors).length > 0}
            >
              {loading ? 'Procesando...' : 'Confirmar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
```

### Paso 4: Crear ClosePastureModal (similar a OpenPastureModal)
```javascript
// Mismo patrón que OpenPastureModal
// Pero con validateCloseEvent(formData, pasture.currentHeightCm)
// Y campos adicionales: residualCm
```

### Paso 5: Modificar PaddockPage.jsx
```javascript
// Agregar estado
const [modalType, setModalType] = useState(null);
const [selectedPasture, setSelectedPasture] = useState(null);

// Agregar handlers
const handleOpenModal = (type, pasture) => {
  setModalType(type);
  setSelectedPasture(pasture);
};

const handleModalSuccess = (updatedPasture) => {
  setPastures(prev => 
    prev.map(p => p.id === updatedPasture.id ? updatedPasture : p)
  );
  setModalType(null);
  setSelectedPasture(null);
  showToast('success', 'Potrero actualizado');
};

// Renderizar modales
{modalType === 'OPEN' && selectedPasture && (
  <OpenPastureModal {...props} />
)}
```

---

## 🧪 **Casos de Prueba**

### Test Unitarios (Vitest)

```javascript
// __tests__/pastureEventValidators.test.js

describe('pastureEventValidators', () => {
  
  test('validateOpenEvent: rechaza lotId vacío', () => {
    const errors = validateOpenEvent({ lotId: '', animals: 5 });
    expect(errors.lotId).toBe('Lote ID es requerido');
  });
  
  test('validateOpenEvent: rechaza animals <= 0', () => {
    const errors = validateOpenEvent({ lotId: 'LOT1', animals: 0 });
    expect(errors.animals).toBe('Cantidad de animales debe ser mayor a 0');
  });
  
  test('validateCloseEvent: rechaza residualCm > currentHeight', () => {
    const errors = validateCloseEvent(
      { lotId: 'LOT1', animals: 5, residualCm: 30 },
      25 // currentHeightCm
    );
    expect(errors.residualCm).toContain('no puede ser mayor');
  });
});
```

### Test de Componentes (React Testing Library)

```javascript
// __tests__/OpenPastureModal.test.jsx

describe('OpenPastureModal', () => {
  
  test('abre modal cuando isOpen=true', () => {
    const { getByText } = render(
      <OpenPastureModal isOpen={true} pasture={mockPasture} {...props} />
    );
    expect(getByText(/Abrir Potrero/i)).toBeInTheDocument();
  });
  
  test('deshabilita botón si campos requeridos vacíos', () => {
    const { getByRole } = render(
      <OpenPastureModal isOpen={true} pasture={mockPasture} {...props} />
    );
    const confirmBtn = getByRole('button', { name: /Confirmar/ });
    expect(confirmBtn).toBeDisabled();
  });
  
  test('envía POST cuando submit es válido', async () => {
    const mockApplyEvent = jest.fn().mockResolvedValue(updatedPasture);
    const { getByRole, getByLabelText } = render(
      <OpenPastureModal isOpen={true} pasture={mockPasture} {...props} />
    );
    
    fireEvent.change(getByLabelText('Lote ID'), { target: { value: 'LOT001' } });
    fireEvent.change(getByLabelText('Cantidad'), { target: { value: '15' } });
    fireEvent.click(getByRole('button', { name: /Confirmar/ }));
    
    await waitFor(() => {
      expect(mockApplyEvent).toHaveBeenCalledWith({
        eventType: 'OPEN',
        lotId: 'LOT001',
        animals: 15
      });
    });
  });
});
```

### Test de Integración E2E (Cypress)

```javascript
// cypress/e2e/pasture-open-close.cy.js

describe('Abrir y Cerrar Potreros', () => {
  
  beforeEach(() => {
    cy.login('juan@farm.com');
    cy.visit('/potreros');
  });
  
  it('debe abrir potrero exitosamente', () => {
    // Arrange
    cy.intercept('POST', '/farms/*/pastures/*/events', {
      statusCode: 201,
      body: { id: 'P001', status: 'EN_USO', lastLotId: 'LOT001' }
    }).as('openEvent');
    
    // Act
    cy.contains('button', 'Abrir').first().click();
    cy.get('input[name="lotId"]').type('LOT001');
    cy.get('input[name="animals"]').type('15');
    cy.contains('button', 'Confirmar').click();
    
    // Assert
    cy.wait('@openEvent');
    cy.contains('Potrero abierto exitosamente').should('be.visible');
    cy.contains('EN_USO').should('be.visible');
  });
  
  it('debe rechazar formulario incompleto', () => {
    // Act
    cy.contains('button', 'Abrir').first().click();
    cy.contains('button', 'Confirmar').should('be.disabled');
    
    // Assert
    cy.get('input[name="lotId"]').type('LOT001');
    // animals aún vacío, botón debe seguir disabled
    cy.contains('button', 'Confirmar').should('be.disabled');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: Abrir Potrero Exitosamente
```gherkin
Scenario: Usuario abre potrero disponible exitosamente
  Given el usuario está en el dashboard de potreros
  And existe potrero "P001 - Potrero 1" en estado DISPONIBLE
  And el backend endpoint POST .../events está disponible
  
  When el usuario hace clic en botón "Abrir" del potrero
  Then se abre modal con título "Abrir Potrero - Potrero 1"
  
  When el usuario completa:
    | Campo | Valor |
    | Lote ID | LOT001 |
    | Animales | 15 |
    | Usuario | juan.perez@farm.com (pre-llenado) |
  
  And presiona "Confirmar"
  Then:
    [ ] Se envía POST /farms/F001/pastures/P001/events
    [ ] Body contiene: eventType=OPEN, lotId=LOT001, animals=15
    [ ] Botón muestra loading state
    [ ] Backend responde HTTP 201
    [ ] Modal se cierra automáticamente
    [ ] Tabla se actualiza: P001 ahora muestra status=EN_USO
    [ ] Se muestra toast: "Potrero abierto exitosamente"
    [ ] Toast desaparece en 3 segundos
```

### Escenario 2: Cerrar Potrero con Validación de Altura
```gherkin
Scenario: Usuario cierra potrero con altura residual validada
  Given potrero "P002" está en estado EN_USO con altura actual = 25 cm
  And usuario hace clic en "Cerrar"
  
  When el usuario completa:
    | Campo | Valor |
    | Lote ID | LOT001 |
    | Animales | 15 |
    | Altura Residual | 30 (INVÁLIDO: mayor a 25) |
  
  Then se muestra error inline:
    "Altura residual (30 cm) no puede ser mayor a altura actual (25 cm)"
  And botón "Confirmar" permanece disabled
  And NO se envía request
  
  When usuario corrige a 8:
    | Altura Residual | 8 (VÁLIDO) |
  Then error desaparece
  And botón "Confirmar" se habilita
  
  When presiona "Confirmar":
  Then se envía POST con residualCm=8
  And respuesta 201
  And tabla muestra: status=EN_DESCANSO, lastClosedAt=ahora
```

### Escenario 3: Manejo de Error Backend 404
```gherkin
Scenario: Potrero fue eliminado entre cargas
  Given usuario abre modal para potrero P999
  When presiona "Confirmar"
  And backend responde HTTP 404: "Potrero P999 no encontrado"
  
  Then:
    [ ] Modal se cierra automáticamente
    [ ] Se muestra toast error (rojo): "Potrero no encontrado"
    [ ] Tabla se recarga con GET /farms/F001/pastures
    [ ] Potrero P999 ya no aparece en tabla
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otras HUs**:
- ✅ HU#1 (Backend POST Eventos) - Endpoint debe estar deployado

**Documentación relacionada**:
- [HU#1: Backend POST Eventos](./PASTURES-HU-001-post-eventos.md)
- [HU#3: DetailPanel Funcional](./PASTURES-HU-003-detailpanel.md) (próxima)
- [Pastures Overview](../../pastures/pastures-overview.md)
- [Flujo Dashboard Potreros](../../architecture/flujo-dashboard-potreros.md)

**Componentes existentes**:
- `PastureTable.jsx` - Tabla donde van los botones
- `PaddockPage.jsx` - Página que orquesta todo
- `useAuth()` - Para obtener usuario actual

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** React Hooks + Modales Desacoplados + Validación Local + Hook Custom para HTTP

**Justificación:** El patrón de **Hooks + Modales** ya está consolidado en BovineForm/BovineList del proyecto. El hook personalizado `usePastureEvent` permite compartir lógica HTTP entre ambos modales (OpenPastureModal, ClosePastureModal) sin duplicación. La **validación local** con funciones puras (pastureEventValidators.js) previene requests inválidos y proporciona feedback instantáneo. La **separación de concerns** permite testing independiente: validadores puros (100% testeable), hook mocable con fetch, componentes reutilizables. **Validación técnica completada**: Arquitectura frontend revisada (BovineList, BovineForm), endpoint backend disponible (HU#1), useAuth() accesible, TailwindCSS configurado.

**Componentes Afectados:**

- **OpenPastureModal.jsx (Nuevo):** Componente modal para evento OPEN. Props: `pasture`, `isOpen`, `onClose`, `onSuccess`, `farmId`. Estado: `formData` (lotId, animals, user), `errors`, `serverError`, `loading`. Validación con `validateOpenEvent()`. HTTP con `usePastureEvent.applyEvent()` y eventType=OPEN. Manejo de errores: 400 → error inline, 404 → cerrar + toast, timeout → reintentar. UI: formulario con 3 campos (2 editable, 1 disabled), botones Cancelar/Confirmar con loading state.

- **ClosePastureModal.jsx (Nuevo):** Estructura idéntica a OpenPastureModal. Campos adicionales: `residualCm`. Validación con `validateCloseEvent(formData, pasture.currentHeightCm)`. HTTP con eventType=CLOSE. Validación especial: `residualCm <= pasture.currentHeightCm`.

- **usePastureEvent.js (Nuevo - Hook Custom):** Orquesta POST HTTP. Retorna `{ loading, applyEvent }`. `applyEvent(farmId, pastureId, eventRequest)` → Promise<Pasture actualizado>. AbortController para timeout de 30 segundos. Error handling: AbortError → "Tiempo límite", JSON responses (201 OK, 400 error, 404 not found). State: `loading` (boolean). Sin lógica UI, solo HTTP + parsing.

- **pastureEventValidators.js (Nuevo - Utilidad):** Funciones puras de validación. `validateOpenEvent(formData)` → objeto errors. `validateCloseEvent(formData, currentHeightCm)` → objeto errors. Valida campos vacíos, valores numéricos, altura residual <= altura actual. Retorna objeto keyed por nombre de campo. Sin efectos secundarios.

- **PaddockPage.jsx (Modificación - Menor):** Orquestar tabla + modales. Estado: `modalType` (null|'OPEN'|'CLOSE'), `selectedPasture`. Handlers: `handleOpenModal(type, pasture)`, `handleModalClose()`, `handleModalSuccess(updatedPasture)`. Renderizar conditionals para ambos modales. Actualizar estado local sin refetch.

- **PastureTable.jsx (Modificación - Menor):** Agregar columna "Acciones". Botón "📖 Abrir" si status=DISPONIBLE. Botón "🔒 Cerrar" si status=EN_USO. Deshabilitado para otros estados. Click → callback `onOpenModal(type, pasture)`. Estilos TailwindCSS reutilizables.

- **useAuth() Hook (Existente - Usado):** Obtener usuario actual para pre-llenar campo "user". `const currentUser = useAuth(); formData.user = currentUser?.email`.

**Hitos de Implementación:**

1. **pastureEventValidators.js** - Funciones puras (sin dependencias)
2. **usePastureEvent.js** - Hook custom para HTTP (sin dependencias externas)
3. **OpenPastureModal.jsx** - Modal OPEN (depende: usePastureEvent, validateOpenEvent, useAuth)
4. **ClosePastureModal.jsx** - Modal CLOSE (depende: usePastureEvent, validateCloseEvent, useAuth)
5. **PastureTable.jsx - Agregar botones** (depende: callback onOpenModal)
6. **PaddockPage.jsx - Orquestar modales** (depende: OpenPastureModal, ClosePastureModal, PastureTable)

### Validación de Impacto

**Hallazgos de validación técnica:**

✅ **Arquitectura Existente Revisada:**
- `BovineList.jsx` usa axios para GET - Pattern similar reutilizable
- `BovineForm.jsx` implementa modales con validaciones locales - Pattern a replicar
- TailwindCSS configurado - Estilos reutilizables
- `useAuth()` hook disponible - Para obtener usuario actual

✅ **Endpoint Backend Disponible**:
- POST /farms/{farmId}/pastures/{pastureId}/events (HU#1) ✅
- Response: HTTP 201 + PastureDTO actualizado ✅
- Error responses: 400 (validación), 404 (not found) ✅

✅ **Impacto en Performance**:
- Fetch HTTP con 30s timeout - Estándar React
- Validación local previene requests inválidos
- Actualización local del estado - No requiere refetch
- Modales lazy-loaded - No impacta carga inicial

✅ **Testing**:
- Validadores: Funciones puras → fácil testear (100% coverage)
- Hook: Mockeable con jest.mock fetch
- Componentes: React Testing Library estándar
- E2E: Cypress con intercept de requests

✅ **Flujo de Usuario Verificado**:
```
Usuario → Tabla → Botón "Abrir/Cerrar"
  ↓ PastureTable.handleOpenModal
  ↓ PaddockPage.handleOpenModal(type, pasture)
  ↓ Renderizar Modal (OpenPastureModal / ClosePastureModal)
  ↓ Usuario completa formulario
  ↓ Frontend valida: validateOpenEvent/validateCloseEvent
  ↓ Si válido: habilitar botón "Confirmar"
  ↓ Usuario presiona "Confirmar"
  ↓ usePastureEvent.applyEvent() → POST HTTP
  ↓ Backend procesa (HU#1)
  ↓ Si 201: Modal cierra, tabla actualiza, toast éxito
  ↓ Si 400/404: Error inline, permitir reintentar
```

✅ **Integraciones Requeridas**:
- Backend (HU#1): POST /farms/{farmId}/pastures/{pastureId}/events
- Frontend existente: PaddockPage, PastureTable, useAuth, TailwindCSS
- Error handling integrado (400, 404, timeout)

✅ **Riesgos Mitigables**:
- Usuario envía incompleto → Button disabled hasta pasar validación local
- Altura residual inválida → Validación local antes (residualCm <= currentHeightCm)
- Timeout → AbortController + setTimeout 30s con try/catch
- Respuesta 404 → Cerrar modal + toast error + refetch tabla
- User spam botón → Button disabled mientras loading=true

### Notas Técnicas

**Hook usePastureEvent - Patrón de Error Handling:**
- AbortController para cancelar si excede 30s
- Try/catch captura AbortError → mensajes específicos
- Fetch retorna JSON en response.json() - Si error, extraer message
- Finalmente: limpiar timeout + setLoading(false)

**Validadores - Funciones Puras:**
- validateOpenEvent: Checkea lotId, animals > 0
- validateCloseEvent: Extiende validador OPEN + checkea residualCm vs currentHeight
- Retorna objeto errors keyed por campo → fácil renderizar inline

**Modales - Actualización Optimista:**
- handleModalSuccess(updatedPasture) actualiza tabla localmente
- setPastures(prev => prev.map(p => p.id === updatedPasture.id ? updatedPasture : p))
- Sin refetch completo - Respuesta del backend es la fuente de verdad

**Accesibilidad:**
- Botones con `aria-label` descriptivos
- Modal con `role="dialog"` y `aria-labelledby`
- Campos con `<label htmlFor="">` explícitos
- Errores anunciados con `aria-live="polite"`

### Referencias y Validación

**Documentación Consultada:**
- [cattle-front/src/components/BovineList.jsx](../../../../../cattle-front/src/components/BovineList.jsx) - Patrón HTTP
- [cattle-front/src/App.jsx](../../../../../cattle-front/src/App.jsx) - Estructura React
- [PASTURES-HU-001](./PASTURES-HU-001-post-eventos.md) - Endpoint backend
- [Flujo Dashboard Potreros](../../architecture/flujo-dashboard-potreros.md) - Contexto

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (precedente, aprobado)
- → PASTURES-HU-003: Detail Panel (consume actualizaciones)

**Stack Tecnológico Verificado:**
- React 19.1.0 - Hooks (useState, useEffect)
- TailwindCSS 3.4.3 - Estilos modales
- Fetch API nativo - HTTP client

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Exploratorio (análisis fundamentado en arquitectura existente verificada)

---

## 🔧 **Refinamiento Técnico**

### Stack Tecnológico

**Frontend Stack:**
- React 18+ / React Hooks
- Axios (HTTP client)
- React Router (navigation)
- TailwindCSS (estilos)
- React Context (state management)
- React Testing Library (tests)

### Estructura de Componentes

**usePastureEvent.js - Custom Hook:**
```javascript
// Hook reutilizable para operaciones de eventos
const usePastureEvent = (farmId) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { user } = useAuth();
  
  const applyEvent = async (pastureId, eventType, payload) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.post(
        `/farms/${farmId}/pastures/${pastureId}/events`,
        { eventType, user: user.email, ...payload },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      return response.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Error');
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  return { applyEvent, loading, error };
};
```

**OpenPastureModal.jsx - Component:**
```javascript
export const OpenPastureModal = ({ pasture, isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({ lotId: '', animalCount: '' });
  const [errors, setErrors] = useState({});
  const { applyEvent, loading } = usePastureEvent(farmId);
  
  const handleConfirm = async () => {
    const newErrors = validateOpenEvent(formData);
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    try {
      const updated = await applyEvent(pasture.id, 'OPEN', formData);
      onSuccess(updated);
      onClose();
    } catch (err) {
      setErrors({ api: err.message });
    }
  };
  
  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <h2>Abrir Potrero - {pasture.name}</h2>
      <FormField label="Lote ID" value={formData.lotId} error={errors.lotId} 
        onChange={(e) => setFormData({...formData, lotId: e.target.value})} />
      <button onClick={handleConfirm} disabled={loading}>
        {loading ? 'Guardando...' : 'Confirmar'}
      </button>
    </Modal>
  );
};
```

### Validadores Locales

```javascript
export const validateOpenEvent = (data) => {
  const errors = {};
  if (!data.lotId?.trim()) errors.lotId = 'Lote ID es requerido';
  if (!data.animalCount || data.animalCount < 1) 
    errors.animalCount = 'Animales debe ser > 0';
  return errors;
};

export const validateCloseEvent = (data) => {
  const errors = {};
  if (!data.residualHeight || data.residualHeight < 1)
    errors.residualHeight = 'Altura residual debe ser > 0';
  return errors;
};
```

### API Integration

**Endpoint consumido:**
```
POST /farms/{farmId}/pastures/{pastureId}/events

Request:
{
  "eventType": "OPEN",
  "lotId": "LOT001",
  "animalCount": 15,
  "user": "juan.perez@farm.com"
}

Response (201):
{
  "id": "P001",
  "status": "EN_USO",
  "lastUseAt": "2026-01-09T10:30:45Z",
  "lastLotId": "LOT001"
}
```

### Error Handling

```javascript
try {
  await applyEvent(pastureId, eventType, payload);
} catch (err) {
  if (err.response?.status === 400) {
    showError('Datos inválidos');
  } else if (err.response?.status === 409) {
    showError('Operación no permitida en este estado');
  } else {
    showError('Error - intenta de nuevo');
  }
}
```

### Dependencias NPM

```json
{
  "dependencies": {
    "react": "^18.x",
    "axios": "^1.x",
    "tailwindcss": "^3.x"
  }
}
```

### Testing Strategy

**Unit Tests:**
```javascript
test('validateOpenEvent requiere lotId', () => {
  const errors = validateOpenEvent({ lotId: '', animalCount: 10 });
  expect(errors.lotId).toBeDefined();
});
```

**Component Tests (RTL):**
```javascript
test('OpenPastureModal muestra campos correctos', () => {
  render(<OpenPastureModal pasture={mockPasture} isOpen={true} />);
  expect(screen.getByLabel(/lote id/i)).toBeInTheDocument();
});
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `OpenPastureModal.jsx` implementado y funcional
- [ ] `ClosePastureModal.jsx` implementado y funcional
- [ ] `usePastureEvent.js` hook personalizado completado
- [ ] `pastureEventValidators.js` con todas las validaciones
- [ ] Botones "Abrir" y "Cerrar" en PastureTable conectados
- [ ] `PaddockPage.jsx` orquesta modales correctamente
- [ ] Tests unitarios: cobertura >= 80% (validadores + hook)
- [ ] Tests componentes: todos los AC probados
- [ ] Tests E2E: flujos happy path + error scenarios
- [ ] Validaciones locales funcionan antes de enviar
- [ ] Mensajes de error claros y visibles
- [ ] Loading state visible mientras se procesa
- [ ] Timeout a 30 segundos implementado
- [ ] Tabla se actualiza post-éxito sin reload
- [ ] Toast notifications mostradas apropiadamente
- [ ] Manual testing en Postman + navegador: OPEN y CLOSE exitosos
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Documentación actualizada (JSDoc, comentarios)
- [ ] Accesibilidad validada (botones, labels, modal role)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
