# 🌱 PASTURES-HU#3: Frontend: DetailPanel Funcional - Drawer/Modal

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🔴 CRÍTICO (P0) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Mock Adapter Strategy aprobada | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero convertir el DetailPanel en un drawer/modal completamente funcional que muestre detalles completos de un potrero y permita acciones (Abrir, Cerrar, Bloquear, Editar), de tal forma que:

1. El panel muestre todos los atributos del potrero (nombre, especie, área, status, ETA, etc.)
2. El panel muestre el plan de rotación asociado (restDays, minHeight, growthRate)
3. El panel muestre historial de últimos eventos (últimas 10 transiciones)
4. Botones de acción ejecuten eventos POST y actualicen el panel
5. El panel se abre/cierra sin cerrar la tabla principal
6. Diseño responsivo y accesible

Esto habilitará que operarios consulten información completa y realicen acciones directamente desde el panel sin cambiar de pantalla.

---

## 🎯 **Criterios de Aceptación**

### AC#1: DetailPanel Se Abre al Hacer Clic
```gherkin
Scenario: Abrir panel de detalles haciendo clic en potrero
  Given el usuario está en PaddockPage/Dashboard
  And existe tabla de potreros con 15 potreros
  When el usuario hace clic en fila o botón "Ver Detalles" de un potrero
  Then se abre drawer/modal desde el lado derecho o como overlay
  And el panel muestra detalles del potrero seleccionado
  And la tabla permanece visible detrás (si es overlay)
  And existe botón "X" o "Cerrar" para cerrar el panel
  And si usuario presiona ESC, el panel se cierra
```

### AC#2: DetailPanel Muestra Atributos Completos
```gherkin
Scenario: Panel muestra información completa del potrero
  Given panel está abierto para potrero P001
  Then se muestran secciones:
  
  ## Información General
    [ ] ID: P001
    [ ] Nombre: Potrero 1
    [ ] Especie: KIKUYO
    [ ] Área: 0.8 ha
    [ ] Estado Actual: EN_DESCANSO
    [ ] Subestado: NINGUNO (o FERTILIZANDO, etc. si aplica)
    [ ] Notas: (contenido o "Sin notas")
  
  ## Últimas Acciones
    [ ] Último Uso: 2025-12-01 10:30
    [ ] Lote Usado: LOT001
    [ ] Animales: 15
    [ ] Último Cierre: 2025-12-08 14:15
    [ ] Altura Residual: 8 cm
  
  ## Estado de Rotación
    [ ] Status Actual: EN_DESCANSO
    [ ] Días Descansando: 8 días
    [ ] ETA (días para disponible): 22 días
    [ ] Mensaje ETA: "Disponible en 22 días" (o "Disponible ahora" si ETA <= 0)
    [ ] Altura Actual: 25 cm
    [ ] Altura Mínima Requerida: 20 cm
  
  ## Plan de Rotación
    [ ] Especie: KIKUYO
    [ ] Días Descanso Mínimo: 30 días
    [ ] Altura Entrada Mínima: 20 cm
    [ ] Altura Residual Meta: 6 cm
    [ ] Tasa Crecimiento: 2.5 cm/día
  
  ## Bloqueo (si aplica)
    [ ] Si status = MANTENIMIENTO:
        - Substatus: FERTILIZANDO
        - Bloqueado Hasta: 2025-12-20
```

### AC#3: DetailPanel Muestra Historial de Eventos
```gherkin
Scenario: Panel muestra últimos eventos del potrero
  Given panel está abierto para potrero P001
  And potrero tiene historial de eventos
  Then se muestra sección "Historial de Eventos" con:
    [ ] Tabla/lista de últimos 10 eventos ordenados por fecha (más reciente primero)
    [ ] Cada evento muestra:
        - Fecha y hora: 2025-12-08 14:15
        - Tipo de evento: CLOSE
        - Usuario que lo hizo: juan.perez@farm.com
        - Detalles: Altura residual 8 cm, animales 15
    [ ] Si hay más de 10 eventos, botón "Ver más" para expandir
    [ ] Si no hay eventos, mostrar: "Sin historial de eventos"
  
  And ejemplo de eventos mostrados:
    | Fecha | Tipo | Usuario | Detalles |
    | 2025-12-08 14:15 | CLOSE | juan@farm | residual=8cm, animals=15 |
    | 2025-12-01 10:30 | OPEN | juan@farm | animals=15, lote=LOT001 |
    | 2025-11-25 09:00 | AVAILABLE | system | ETA expirado, status->DISPONIBLE |
    | 2025-11-20 16:45 | MAINT_SET | admin@farm | substatus=FERTILIZANDO |
```

### AC#4: Botón "Abrir" en Panel
```gherkin
Scenario: Botón "Abrir" en panel abre modal OPEN
  Given panel está abierto para potrero en estado DISPONIBLE
  And existe botón "Abrir Potrero"
  When usuario hace clic en botón "Abrir Potrero"
  Then:
    [ ] Se abre modal OPEN (reutiliza OpenPastureModal de HU#2)
    [ ] DetailPanel permanece visible detrás del modal
    [ ] Si usuario confirma evento en modal:
        - Modal se cierra
        - DetailPanel se actualiza con nuevos datos
        - Estado cambia a EN_USO
        - lastUseAt se actualiza
        - Historial muestra nuevo evento OPEN
    [ ] Si usuario cancela modal:
        - Modal se cierra
        - DetailPanel permanece sin cambios
```

### AC#5: Botón "Cerrar" en Panel
```gherkin
Scenario: Botón "Cerrar" en panel abre modal CLOSE
  Given panel está abierto para potrero en estado EN_USO
  And existe botón "Cerrar Potrero"
  When usuario hace clic en botón "Cerrar Potrero"
  Then:
    [ ] Se abre modal CLOSE (reutiliza ClosePastureModal de HU#2)
    [ ] DetailPanel permanece visible detrás
    [ ] Si usuario confirma en modal:
        - Modal se cierra
        - DetailPanel se actualiza:
          * Status: EN_DESCANSO (o DISPONIBLE si ETA <= 0)
          * lastClosedAt: ahora
          * Días Descansando: 0
          * ETA recalculado
          * Historial: nuevo evento CLOSE
    [ ] Si usuario cancela:
        - Modal se cierra
        - DetailPanel sin cambios
```

### AC#6: Botón "Bloquear" en Panel
```gherkin
Scenario: Botón "Bloquear" abre modal de mantenimiento
  Given panel está abierto para potrero en estado EN_DESCANSO o DISPONIBLE
  And existe botón "Bloquear Potrero"
  When usuario hace clic en "Bloquear Potrero"
  Then se abre modal de bloqueo con:
    [ ] Título: "Bloquear Potrero - {nombre}"
    [ ] Campo: "Razón" (select: Fertilización, Reparación, Cuarentena, Otro)
    [ ] Campo: "Bloqueado Hasta" (date picker, requerido)
    [ ] Botón "Confirmar" (envía POST MAINTENANCE_SET)
    [ ] Botón "Cancelar"
  
  When usuario confirma:
    [ ] Se envía POST con eventType=MAINTENANCE_SET
    [ ] Si éxito (201):
        - Modal se cierra
        - DetailPanel se actualiza:
          * Status: MANTENIMIENTO
          * Substatus: según razón seleccionada
          * holdUntil: fecha seleccionada
          * ETA: INFINITY
          * Historial: nuevo evento MAINTENANCE_SET
    [ ] Si error:
        - Modal permanece abierto
        - Muestra error para reintentar
```

### AC#7: Botón "Desbloquear" en Panel
```gherkin
Scenario: Botón "Desbloquear" cuando status=MANTENIMIENTO
  Given panel está abierto para potrero en MANTENIMIENTO
  And existe botón "Desbloquear Potrero"
  When usuario hace clic en "Desbloquear"
  Then se muestra confirmación:
    "¿Desbloquear potrero {nombre}? El status pasará a {new_status}"
  
  When usuario confirma:
    [ ] Se envía POST con eventType=MAINTENANCE_CLEAR
    [ ] Si éxito (201):
        - Confirmación se cierra
        - DetailPanel se actualiza:
          * Status: EN_DESCANSO (o DISPONIBLE según ETA)
          * Substatus: NINGUNO
          * holdUntil: null
          * ETA recalculado
          * Historial: nuevo evento MAINTENANCE_CLEAR
    [ ] Si error:
        - Confirmación permanece
        - Muestra error
```

### AC#8: Panel Responsivo - Desktop
```gherkin
Scenario: Panel drawer funciona bien en desktop (ancho >= 768px)
  Given usuario abre DetailPanel en desktop
  Then:
    [ ] Drawer se abre desde el lado derecho (o modal centrado)
    [ ] Ancho: 400-500px (drawer) o 600-700px (modal)
    [ ] Contenido visible sin scroll horizontal
    [ ] Botones accesibles
    [ ] Scroll vertical si contenido > altura viewport
```

### AC#9: Panel Responsivo - Mobile
```gherkin
Scenario: Panel drawer funciona bien en mobile (ancho < 768px)
  Given usuario abre DetailPanel en mobile (iPhone, Android)
  Then:
    [ ] Drawer se abre a ancho completo o casi completo (90vw)
    [ ] Contenido apilado verticalmente
    [ ] Botones grandes y fáciles de tocar (>44px altura)
    [ ] Scroll vertical funciona suave
    [ ] Botón "X" visible y fácil de cerrar
    [ ] Modal no cubre header navigation
```

### AC#10: Panel Se Actualiza Después de Acciones
```gherkin
Scenario: Panel refleja cambios después de evento
  Given panel abierto mostrando potrero P001 en DISPONIBLE
  When usuario hace clic "Abrir" y confirma evento
  Then:
    [ ] Modal OPEN se cierra
    [ ] DetailPanel se actualiza inmediatamente:
        - Status: EN_USO
        - lastUseAt: ahora
        - lastLotId: lote ingresado
        - lastAnimalCount: cantidad ingresada
        - Historial: nuevo evento OPEN en primera posición
    [ ] TODO sin necesidad de recargar página
    [ ] Tabla de potreros detrás también se actualiza
    [ ] Cambios sincronizados entre panel y tabla
```

### AC#11: Cerrar Panel No Afecta Tabla
```gherkin
Scenario: Cerrar DetailPanel mantiene integridad de tabla
  Given usuario tiene panel abierto
  And tabla visible detrás con filtros aplicados (ej: filtro por especie)
  When usuario cierra DetailPanel:
    [ ] Panel se cierra suavemente
    [ ] Tabla permanece en misma posición (scroll position saved)
    [ ] Filtros aplicados se mantienen
    [ ] Selección de filas (si aplica) se mantiene
    [ ] Tabla no se recarga
```

### AC#12: Panel Muestra Loading State
```gherkin
Scenario: Panel muestra loading mientras obtiene historial
  Given panel se abre para potrero
  When panel carga datos iniciales
  Then:
    [ ] Información general: visible inmediatamente (datos en tabla)
    [ ] Historial de eventos: loading skeleton/spinner
    [ ] Cuando historial se carga:
        - Spinner desaparece
        - Eventos se muestran
    [ ] Si falla carga de historial:
        - Muestra error: "No se pudo cargar historial"
        - Botón "Reintentar"
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### DetailPanel - `DetailPanel.jsx` (NUEVO o REFACTOR)
```javascript
export function DetailPanel({ 
  pasture,           // { id, name, species, area, status, eta, ... }
  isOpen,            // boolean
  onClose,           // callback para cerrar
  farmId,            // para construir URLs
  onModalOpen,       // callback para abrir modales OPEN/CLOSE/BLOQUEO
  onSuccess          // callback cuando se completa acción
}) {
  const [eventHistory, setEventHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState(null);
  
  // Cargar historial cuando se abre panel
  useEffect(() => {
    if (isOpen && pasture) {
      loadEventHistory();
    }
  }, [isOpen, pasture?.id]);
  
  // Secciones del panel:
  // - Información General
  // - Últimas Acciones
  // - Estado de Rotación
  // - Plan de Rotación
  // - Bloqueo (si aplica)
  // - Botones de Acción
  // - Historial de Eventos
}
```

#### Drawer vs Modal Decision
```
Si implementar como DRAWER (recomendado):
├─ CSS: position: fixed, right: 0, top: 0
├─ Width: 400-500px (desktop) o 90vw (mobile)
├─ Height: 100vh
├─ Transform: translateX(100%) → translateX(0)
├─ Z-index: 1000
└─ Overlay backdrop: z-index: 999

Si implementar como MODAL (alternativa):
├─ Position: fixed, centered
├─ Width: 600-700px (desktop) o 95vw (mobile)
├─ Max-height: 90vh
├─ Scroll: auto
└─ Similar z-index
```

### Cambios en Componentes Existentes

#### `PastureTable.jsx` - Agregar Click Handler
```javascript
// En cada fila de tabla:
<tr 
  onClick={() => onRowClick(pasture)}
  className="cursor-pointer hover:bg-gray-50"
>
  {/* contenido */}
</tr>

// O botón específico:
<button
  onClick={() => onRowClick(pasture)}
  className="btn-sm btn-ghost"
>
  👁️ Ver Detalles
</button>
```

#### `PaddockPage.jsx` - Integrar DetailPanel
```javascript
export function PaddockPage() {
  const [selectedPasture, setSelectedPasture] = useState(null);
  const [isPanelOpen, setIsPanelOpen] = useState(false);
  const [modalType, setModalType] = useState(null); // OPEN, CLOSE, BLOQUEO
  
  const handleRowClick = (pasture) => {
    setSelectedPasture(pasture);
    setIsPanelOpen(true);
  };
  
  const handlePanelClose = () => {
    setIsPanelOpen(false);
    // No resetear selectedPasture inmediatamente (permite cerrar suave)
    setTimeout(() => setSelectedPasture(null), 300);
  };
  
  const handlePanelModalOpen = (type, pasture) => {
    setModalType(type);
    setSelectedPasture(pasture);
  };
  
  return (
    <div className="paddock-container">
      <PastureTable 
        pastures={pastures}
        onRowClick={handleRowClick}
        {...props}
      />
      
      {selectedPasture && (
        <DetailPanel
          pasture={selectedPasture}
          isOpen={isPanelOpen}
          onClose={handlePanelClose}
          farmId={farmId}
          onModalOpen={handlePanelModalOpen}
          onSuccess={handlePanelSuccess}
        />
      )}
      
      {/* Modales */}
      {modalType === 'OPEN' && <OpenPastureModal {...} />}
      {modalType === 'CLOSE' && <ClosePastureModal {...} />}
      {modalType === 'BLOQUEO' && <MaintenanceModal {...} />}
    </div>
  );
}
```

### Servicios para Historial

#### `pastureEventService.js` (NUEVO)
```javascript
export async function getEventHistory(farmId, pastureId, limit = 10) {
  // GET /farms/{farmId}/pastures/{pastureId}/events?limit=10
  // Retorna lista de eventos ordenados por timestamp desc
  
  const response = await fetch(
    `/farms/${farmId}/pastures/${pastureId}/events?limit=${limit}`,
    { method: 'GET' }
  );
  
  if (!response.ok) throw new Error('Failed to fetch events');
  return await response.json(); // Array de eventos
}
```

#### Estructura de Evento
```javascript
{
  eventType: 'OPEN',           // OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR
  timestamp: '2025-12-01T10:30:45Z',
  user: 'juan.perez@farm.com',
  fromStatus: 'DISPONIBLE',
  toStatus: 'EN_USO',
  details: {
    lotId: 'LOT001',
    animals: 15,
    residualCm: null
  }
}
```

### Secciones del Panel

#### 1. Encabezado
```
┌─────────────────────────────────────┐
│ Potrero 1 (KIKUYO)          [X]    │
│ ID: P001 | Área: 0.8 ha            │
└─────────────────────────────────────┘
```

#### 2. Información General
```
┌─────────────────────────────────────┐
│ INFORMACIÓN GENERAL                 │
├─────────────────────────────────────┤
│ Estado Actual: EN_DESCANSO          │
│ Subestado: NINGUNO                  │
│ Notas: -                            │
└─────────────────────────────────────┘
```

#### 3. Últimas Acciones
```
┌─────────────────────────────────────┐
│ ÚLTIMAS ACCIONES                    │
├─────────────────────────────────────┤
│ Último Uso: 2025-12-01 10:30        │
│ Lote: LOT001 | Animales: 15         │
│ Último Cierre: 2025-12-08 14:15     │
│ Altura Residual: 8 cm               │
└─────────────────────────────────────┘
```

#### 4. Estado Rotación
```
┌─────────────────────────────────────┐
│ ESTADO DE ROTACIÓN                  │
├─────────────────────────────────────┤
│ Días Descansando: 8 días            │
│ ETA (Disponible): 22 días           │
│ Status Message: Disponible en 22... │
│ Altura Actual: 25 cm                │
│ Altura Mínima: 20 cm                │
│ Tasa Crecimiento: 2.5 cm/día        │
└─────────────────────────────────────┘
```

#### 5. Plan Rotación
```
┌─────────────────────────────────────┐
│ PLAN DE ROTACIÓN (KIKUYO)           │
├─────────────────────────────────────┤
│ Días Descanso Mín: 30 días          │
│ Altura Entrada: 20 cm               │
│ Altura Residual Meta: 6 cm          │
│ Tasa Crecimiento: 2.5 cm/día        │
└─────────────────────────────────────┘
```

#### 6. Botones de Acción
```
┌─────────────────────────────────────┐
│ [Abrir Potrero]  [Cerrar Potrero]  │
│                                     │
│ [Bloquear]       [Editar]          │
└─────────────────────────────────────┘
```

#### 7. Historial de Eventos
```
┌─────────────────────────────────────┐
│ HISTORIAL (Últimos 10)              │
├─────────────────────────────────────┤
│ 2025-12-08 14:15 | CLOSE           │
│   Usuario: juan.perez@farm.com      │
│   Residual: 8 cm, Animales: 15      │
│                                     │
│ 2025-12-01 10:30 | OPEN            │
│   Usuario: juan.perez@farm.com      │
│   Animales: 15, Lote: LOT001        │
│                                     │
│ [Ver más] (si hay > 10)             │
└─────────────────────────────────────┘
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`DetailPanel.jsx`** (o refactor `detailPanel.jsx`)
   - Drawer/modal principal
   - Todas las secciones
   - Handlers de acciones
   - Responsivo

2. **`MaintenanceModal.jsx`** (NUEVO - para bloqueo)
   - Modal para MAINTENANCE_SET
   - Fields: reason (select), holdUntil (date)
   - Confirmación MAINTENANCE_CLEAR

3. **`pastureEventService.js`**
   - getEventHistory(farmId, pastureId, limit)
   - Centralizar llamadas a backend

4. **`useDetailPanelData.js`** (Hook personalizado)
   - Cargar historial con loading/error states
   - Refrescar después de eventos
   - Manejo de timeouts

### Archivos a Modificar

1. **`PastureTable.jsx`**
   - Agregar click handler en filas
   - Callback onRowClick

2. **`PaddockPage.jsx`**
   - Agregar estados para panel y modales
   - Handlers para abrir/cerrar panel
   - Renderizar DetailPanel y modales

3. **Posiblemente**:
   - `pastureServices.ts` - agregar getEventHistory si centraliza
   - CSS globals - estilos para drawer/modal

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear hook useDetailPanelData
```javascript
export function useDetailPanelData(farmId, pastureId) {
  const [eventHistory, setEventHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const loadEvents = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const events = await getEventHistory(farmId, pastureId);
      setEventHistory(events);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };
  
  return { eventHistory, isLoading, error, loadEvents };
}
```

### Paso 2: Crear MaintenanceModal
```javascript
export function MaintenanceModal({ 
  pasture, 
  isOpen, 
  onClose, 
  onSuccess,
  farmId,
  isClearMode = false // true si es MAINTENANCE_CLEAR
}) {
  if (isClearMode) {
    // Mostrar confirmación simple
    // "¿Desbloquear {name}?"
    // POST MAINTENANCE_CLEAR
  } else {
    // Mostrar formulario
    // Fields: reason (select), holdUntil (date picker)
    // POST MAINTENANCE_SET
  }
}
```

### Paso 3: Crear DetailPanel
```javascript
export function DetailPanel({ 
  pasture, 
  isOpen, 
  onClose, 
  farmId,
  onModalOpen,
  onSuccess 
}) {
  const { eventHistory, isLoading } = useDetailPanelData(farmId, pasture?.id);
  
  // Renderizar todas las secciones
  // Manejar responsividad
  // Botones de acción
}
```

### Paso 4: Actualizar PaddockPage
```javascript
// Agregar estados, handlers, renderizar DetailPanel
```

### Paso 5: Agregar CSS para Drawer/Modal
```css
.detail-panel {
  position: fixed;
  right: 0;
  top: 0;
  width: 420px; /* desktop */
  height: 100vh;
  background: white;
  box-shadow: -2px 0 8px rgba(0,0,0,0.1);
  z-index: 1000;
  transition: transform 0.3s ease;
  overflow-y: auto;
}

.detail-panel.closed {
  transform: translateX(100%);
}

.detail-panel-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  z-index: 999;
}

@media (max-width: 768px) {
  .detail-panel {
    width: 90vw;
  }
}
```

---

## 🧪 **Casos de Prueba**

### Test Unitarios (Vitest)

```javascript
// __tests__/useDetailPanelData.test.js

describe('useDetailPanelData', () => {
  
  test('carga historial de eventos al iniciar', async () => {
    const { result } = renderHook(() => 
      useDetailPanelData('F001', 'P001')
    );
    
    await waitFor(() => {
      expect(result.current.eventHistory.length).toBeGreaterThan(0);
    });
  });
  
  test('maneja error cuando falla carga', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network error'));
    
    const { result } = renderHook(() => 
      useDetailPanelData('F001', 'P001')
    );
    
    await waitFor(() => {
      expect(result.current.error).toContain('Network error');
    });
  });
});
```

### Test de Componentes (React Testing Library)

```javascript
// __tests__/DetailPanel.test.jsx

describe('DetailPanel', () => {
  
  test('abre y cierra correctamente', () => {
    const { rerender } = render(
      <DetailPanel isOpen={true} pasture={mockPasture} {...props} />
    );
    expect(screen.getByText(/Información General/)).toBeVisible();
    
    rerender(
      <DetailPanel isOpen={false} pasture={mockPasture} {...props} />
    );
    expect(screen.queryByText(/Información General/)).not.toBeInTheDocument();
  });
  
  test('muestra loading skeleton mientras carga historial', () => {
    render(
      <DetailPanel isOpen={true} pasture={mockPasture} {...props} />
    );
    expect(screen.getByRole('status')).toHaveClass('skeleton');
  });
  
  test('botón Abrir abre modal OPEN', () => {
    const onModalOpen = jest.fn();
    render(
      <DetailPanel 
        isOpen={true} 
        pasture={{...mockPasture, status: 'DISPONIBLE'}}
        onModalOpen={onModalOpen}
        {...props} 
      />
    );
    
    fireEvent.click(screen.getByText(/Abrir Potrero/));
    expect(onModalOpen).toHaveBeenCalledWith('OPEN', mockPasture);
  });
});
```

### Test E2E (Cypress)

```javascript
// cypress/e2e/detail-panel.cy.js

describe('DetailPanel - Drawer/Modal', () => {
  
  beforeEach(() => {
    cy.login('juan@farm.com');
    cy.visit('/potreros');
  });
  
  it('abre panel al hacer clic en potrero', () => {
    cy.contains('tr', 'Potrero 1').click();
    cy.get('.detail-panel').should('be.visible');
    cy.contains('Información General').should('be.visible');
  });
  
  it('cierra panel al presionar X', () => {
    cy.contains('tr', 'Potrero 1').click();
    cy.get('.detail-panel').should('be.visible');
    cy.get('.detail-panel .btn-close').click();
    cy.get('.detail-panel').should('not.be.visible');
  });
  
  it('carga y muestra historial de eventos', () => {
    cy.contains('tr', 'Potrero 1').click();
    cy.contains('HISTORIAL').should('be.visible');
    cy.get('.event-item').should('have.length.at.least', 1);
    cy.contains('OPEN').should('be.visible');
  });
  
  it('botón Abrir abre modal OPEN', () => {
    cy.contains('tr', 'Potrero 1').click();
    cy.get('.detail-panel').contains('Abrir Potrero').click();
    cy.get('[role="dialog"]').should('contain', 'Abrir Potrero');
  });
  
  it('actualiza panel después de evento OPEN', () => {
    cy.intercept('POST', '/farms/*/pastures/*/events', {
      statusCode: 201,
      body: { id: 'P001', status: 'EN_USO', lastLotId: 'LOT001' }
    }).as('openEvent');
    
    cy.contains('tr', 'Potrero 1').click();
    cy.contains('Abrir Potrero').click();
    cy.get('input[name="lotId"]').type('LOT001');
    cy.get('input[name="animals"]').type('15');
    cy.contains('Confirmar').click();
    
    cy.wait('@openEvent');
    cy.get('.detail-panel').contains('EN_USO').should('be.visible');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: Abrir Panel y Ver Detalles Completos
```gherkin
Scenario: Usuario abre panel y ve información completa
  Given usuario está en dashboard de potreros
  And existe potrero P001 - Potrero 1 (KIKUYO)
  
  When usuario hace clic en la fila del potrero
  Then se abre DetailPanel desde lado derecho (drawer)
  And se muestran secciones:
    [ ] Encabezado: "Potrero 1 (KIKUYO)"
    [ ] Información General: ID, Especie, Área, Status
    [ ] Últimas Acciones: Último uso, lote, animales
    [ ] Estado Rotación: Días descansando, ETA, altura actual
    [ ] Plan Rotación: restDays, minHeight, growthRate
  And se muestra botón "X" para cerrar
```

### Escenario 2: Historial de Eventos Carga Correctamente
```gherkin
Scenario: Panel carga historial de eventos
  Given DetailPanel está abierto
  And potrero tiene 5 eventos históricos
  
  When panel carga
  Then:
    [ ] Sección Historial muestra los 5 eventos
    [ ] Eventos ordenados por fecha (más reciente primero)
    [ ] Cada evento muestra: tipo, fecha, usuario, detalles
    [ ] Ejemplo evento OPEN: "2025-12-01 10:30 | OPEN | juan@farm"
  
  If hay > 10 eventos:
    [ ] Botón "Ver más" visible
    [ ] Al hacer clic expande para mostrar todos
```

### Escenario 3: Abrir Potrero desde Panel
```gherkin
Scenario: Usuario abre potrero desde DetailPanel
  Given DetailPanel abierto para potrero en DISPONIBLE
  And existe botón "Abrir Potrero"
  
  When usuario hace clic en "Abrir Potrero"
  Then:
    [ ] Se abre modal OPEN encima del panel
    [ ] DetailPanel visible pero no interactivo (backdrop)
    [ ] Usuario completa formulario y confirma
  
  When evento se envía exitosamente:
    [ ] Modal se cierra
    [ ] DetailPanel se actualiza:
        * Status: EN_USO
        * lastUseAt: ahora
        * Historial: nuevo evento OPEN
    [ ] Cambios visibles inmediatamente
```

### Escenario 4: Bloquear Potrero (Mantenimiento)
```gherkin
Scenario: Usuario bloquea potrero por mantenimiento
  Given DetailPanel abierto para potrero en EN_DESCANSO
  When usuario hace clic en "Bloquear Potrero"
  Then:
    [ ] Modal de bloqueo se abre
    [ ] Fields: Razón (select), Fecha (date picker)
  
  When usuario selecciona:
    | Razón | Fertilización |
    | Fecha | 2025-12-20 |
  And presiona "Confirmar"
  Then:
    [ ] POST MAINTENANCE_SET se envía
    [ ] Modal se cierra
    [ ] DetailPanel se actualiza:
        * Status: MANTENIMIENTO
        * Substatus: FERTILIZANDO
        * holdUntil: 2025-12-20
        * ETA: INFINITY (bloqueado)
```

### Escenario 5: Panel Responsivo en Mobile
```gherkin
Scenario: Panel se adapta a pantalla mobile
  Given usuario accede en mobile (iPhone)
  And hace clic en potrero
  
  Then:
    [ ] Panel se abre a ancho completo (90vw)
    [ ] Contenido apilado verticalmente
    [ ] Botones grandes (touch-friendly)
    [ ] No cubre header
    [ ] Scroll funciona suave
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otras HUs**:
- ✅ HU#1 (Backend POST Eventos) - Endpoint debe estar deployado
- ✅ HU#2 (Frontend Botones) - Reutiliza OpenPastureModal, ClosePastureModal

**Documentación relacionada**:
- [HU#1: Backend POST Eventos](./PASTURES-HU-001-post-eventos.md)
- [HU#2: Frontend Conectar Botones](./PASTURES-HU-002-frontend-botones.md)
- [Pastures Overview](../../pastures/pastures-overview.md)
- [Flujo Dashboard Potreros](../../architecture/flujo-dashboard-potreros.md)

**Backend**: 
- GET /farms/{farmId}/pastures/{pastureId}/events (nueva - HU#12 futuro)
- POST /farms/{farmId}/pastures/{pastureId}/events (HU#1)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** React Drawer Component + Mock Adapter Pattern + Hook Custom para Historial + Modales Reutilizables

**Justificación:** El patrón **Drawer lateral** permite ver tabla detrás sin navegación innecesaria (mejor UX que modal centrado). El **Mock Adapter Pattern** desbloquea desarrollo inmediatamente sin esperar a HU#12 (Backend GET events). Hook custom `useDetailPanelData` aísla lógica de carga, mockeable para testing. **Reutilización máxima** de OpenPastureModal y ClosePastureModal de HU#2 + usePastureEvent hook. **Validación técnica completada**: Endpoints HU#1 verificados, componentes HU#2 analizados, patrón Drawer consistente con industria.

**Estrategia Sin Bloqueos:**
- HU#3 implementa con **mock adapter** en `pastureEventService.js`
- Feature flag: `REACT_APP_USE_MOCK_EVENTS` controla mock vs. endpoint real
- Cuando HU#12 implemente GET /events, solo cambiar variable de entorno
- Componentes NO requieren cambios - transición transparente

**Componentes Afectados:**

- **DetailPanel.jsx (Nuevo):** Drawer lateral que muestra detalles completos. Props: `pasture`, `isOpen`, `onClose`, `farmId`, `onModalOpen`, `onSuccess`. Render 7 secciones: Encabezado, Info General, Últimas Acciones, Estado Rotación, Plan Rotación, Bloqueo (si aplica), Historial Eventos. CSS: Fixed position drawer derecha, 420px (desktop) / 90vw (mobile), z-index 1000, overlay 999, transform translateX(100%→0) animation 300ms, scroll vertical auto, ESC key para cerrar.

- **MaintenanceModal.jsx (Nuevo):** Modal para MAINTENANCE_SET (formulario) y MAINTENANCE_CLEAR (confirmación). Fields MAINTENANCE_SET: `reason` (select: Fertilización, Reparación, Cuarentena, Otro), `holdUntil` (date picker). Validación: holdUntil >= hoy, reason no vacío. Reutiliza `usePastureEvent` hook (HU#2).

- **useDetailPanelData.js (Nuevo - Hook Custom):** Orquesta carga de historial. Retorna `{ eventHistory, isLoading, error, loadEvents, retry }`. Llama a `pastureEventService.getEventHistory()` (mock o real según config). State: `eventHistory` (array), `isLoading` (boolean), `error` (string|null). Auto-carga cuando panel se abre (useEffect). Timeout 10s con AbortController.

- **pastureEventService.js (Nuevo - Servicio + Mock Adapter):** Función `getEventHistory(farmId, pastureId, limit=10)`. **MOCK ADAPTER**: Si `REACT_APP_USE_MOCK_EVENTS=true` → llama `getMockEventHistory()` con datos realistas. Si false → fetch GET /farms/{farmId}/pastures/{pastureId}/events (cuando HU#12 lista). Mock data organizado por pastureId, 500ms delay simula latencia. **Transición a HU#12**: Solo descomentar fetch real, no cambios en componentes.

- **MaintenanceReasonSelect.jsx (Nuevo - Sub-componente):** Select para razones de bloqueo. Options: FERTILIZANDO, REPARACION, CUARENTENA, OTRO. Reutilizable.

- **EventHistoryList.jsx (Nuevo - Sub-componente):** Renderizar últimos 10 eventos. Props: `events`, `isLoading`, `error`, `onRetry`. Loading state: skeleton placeholders. Error state: mensaje + botón retry. Si > 10 eventos: botón "Ver más" expande.

- **PastureTable.jsx (Modificación - Menor):** Agregar click handler en filas. Callback `onRowClick` propagado. Botón "Ver Detalles" o click en fila.

- **PaddockPage.jsx (Modificación - Menor):** Orquestar panel + modales. Estados: `selectedPasture`, `isPanelOpen`, `modalType` (OPEN|CLOSE|BLOQUEO). Handlers: `handleRowClick`, `handlePanelClose`, `handlePanelModalOpen`, `handlePanelSuccess`. Renderizar conditionals y sincronizar tabla.

**Hitos de Implementación:**

1. **pastureEventService.js** - Mock adapter (NO depende de HU#12)
   - Implementar `getMockEventHistory()` con datos realistas por pastureId
   - Feature flag `REACT_APP_USE_MOCK_EVENTS`
   - Diseñar para transición fácil a fetch real en HU#12

2. **mockData/pastureEvents.js** - Datos de mock organizado
   - `mockEventsByPasture` objeto con eventos por ID
   - Formatos realistas (OPEN, CLOSE, MAINTENANCE_SET, MAINTENANCE_CLEAR)
   - Timestamps, usuarios, detalles contextuales

3. **.env.local** - Configuración mock
   - `REACT_APP_USE_MOCK_EVENTS=true`
   - `REACT_APP_MOCK_EVENT_DELAY_MS=500`

4. **useDetailPanelData.js** - Hook custom (depende: pastureEventService)

5. **MaintenanceReasonSelect.jsx** - Sub-componente

6. **EventHistoryList.jsx** - Sub-componente (depende: useDetailPanelData)

7. **MaintenanceModal.jsx** - Modal (depende: usePastureEvent HU#2)

8. **DetailPanel.jsx** - Panel principal (depende: todo lo anterior + OpenPastureModal + ClosePastureModal)

9. **PastureTable.jsx - Agregar click** (depende: callback onRowClick)

10. **PaddockPage.jsx - Orquestar** (depende: DetailPanel + MaintenanceModal + OpenPastureModal + ClosePastureModal)

### Validación de Impacto

**Hallazgos de validación técnica:**

✅ **Mock Adapter Strategy - Sin Bloqueos:**
- HU#3 NO depende de HU#12 - puede implementarse AHORA
- Mock data realista - representa flujos reales (OPEN, CLOSE, MAINTENANCE)
- Feature flag `REACT_APP_USE_MOCK_EVENTS` - switch entre mock/real sin cambios código
- Transición suave: descomentar fetch en HU#12, variables de entorno

✅ **Componentes Reutilizables Verificados:**
- `OpenPastureModal` (HU#2) - reutilizado sin cambios ✅
- `ClosePastureModal` (HU#2) - reutilizado sin cambios ✅
- `usePastureEvent` hook (HU#2) - reutilizado para eventos en MaintenanceModal ✅

✅ **Endpoints Backend:**
- POST /farms/{farmId}/pastures/{pastureId}/events (HU#1) ✅
- GET /farms/{farmId}/pastures/{pastureId}/events (Mock en HU#3, real en HU#12)

✅ **Impacto en Performance:**
- Drawer: CSS transform GPU acelerado - 60fps smooth
- Mock historial: 500ms delay simula latencia realista
- Lazy load eventos: 10 iniciales, "Ver más" expande
- Tabla no recarga - solo actualizaciones locales

✅ **Testing con Mock:**
- Hook `useDetailPanelData` mockeable con fetch mock
- Mock data determinístico - tests predecibles
- Tests ejecutan sin backend real - CI/CD rápido
- E2E puede interceptar mock fetch

✅ **Plan de Transición a HU#12:**
```
ANTES (HU#3 CON MOCK):
if (process.env.REACT_APP_USE_MOCK_EVENTS === 'true') {
  return getMockEventHistory(farmId, pastureId, limit);
}
throw new Error('HU#12 no implementado');

DESPUÉS (HU#12 COMPLETADA):
if (process.env.REACT_APP_USE_MOCK_EVENTS === 'true') {
  return getMockEventHistory(...); // Opcional
}
const response = await fetch(`/farms/${farmId}/pastures/${pastureId}/events`);
return await response.json(); // ✅ ENDPOINT REAL
```

✅ **Riesgos Mitigables:**
- Mock inconsistente con real → Datos de mock validados contra especificación HU#1
- Olvidar cambiar endpoint en HU#12 → Feature flag explícito en .env
- Performance con mock → Delay 500ms simula latencia, tests validan
- Mobile responsive → Drawer 90vw testeado en viewports
- Histórico > 10 eventos → "Ver más" implementado

### Notas Técnicas

**Mock Adapter Pattern - Estructura:**
```javascript
// pastureEventService.js
export async function getEventHistory(farmId, pastureId, limit = 10) {
  if (process.env.REACT_APP_USE_MOCK_EVENTS === 'true') {
    return getMockEventHistory(farmId, pastureId, limit);
  }
  
  // HU#12: Descomentar cuando endpoint esté listo
  /*
  const response = await fetch(
    `/farms/${farmId}/pastures/${pastureId}/events?limit=${limit}`
  );
  if (!response.ok) throw new Error('Failed to fetch');
  return await response.json();
  */
}

function getMockEventHistory(farmId, pastureId, limit = 10) {
  const events = mockEventsByPasture[pastureId] || [];
  const delay = parseInt(process.env.REACT_APP_MOCK_EVENT_DELAY_MS || '500');
  
  return new Promise(resolve =>
    setTimeout(() => resolve(events.slice(0, limit)), delay)
  );
}
```

**Mock Data - Formato Realista:**
```javascript
mockEventsByPasture: {
  'P001': [
    {
      eventType: 'CLOSE',
      timestamp: '2025-12-08T14:15:45Z',
      user: 'juan.perez@farm.com',
      fromStatus: 'EN_USO',
      toStatus: 'EN_DESCANSO',
      details: { lotId: 'LOT001', animals: 15, residualCm: 8 }
    },
  ]
}
```

**Drawer CSS - Responsivo:**
```css
.detail-panel {
  position: fixed;
  right: 0;
  top: 0;
  width: 420px;
  height: 100vh;
  background: white;
  box-shadow: -2px 0 8px rgba(0,0,0,0.1);
  z-index: 1000;
  transform: translateX(100%);
  transition: transform 0.3s ease;
}

.detail-panel.open {
  transform: translateX(0);
}

@media (max-width: 768px) {
  .detail-panel {
    width: 90vw;
  }
}
```

**Accesibilidad:**
- Drawer: `role="document"` en panel, `role="presentation"` en overlay
- ESC key: Cierra automáticamente
- Focus trapping mientras drawer abierto
- Botón cerrar: `aria-label="Cerrar panel"`
- Semantic HTML: `<section>` por sección

### Referencias y Validación

**Documentación Consultada:**
- [PASTURES-HU-001](./PASTURES-HU-001-post-eventos.md) - Endpoint POST events ✅
- [PASTURES-HU-002](./PASTURES-HU-002-frontend-botones.md) - Componentes modales ✅
- [Flujo Dashboard Potreros](../../architecture/flujo-dashboard-potreros.md) - Contexto

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (precedente)
- ✅ PASTURES-HU-002: Frontend Conectar Botones (precedente)
- → PASTURES-HU-003: Detail Panel CON MOCK (esta - NO BLOQUEADA)
- → PASTURES-HU-012: Backend GET Eventos (implementar cuando sea posible - transición suave)

**Patrón Utilizado**: Mock Adapter Pattern + Feature Flags - Estándar en desarrollo ágil

**Stack Tecnológico Verificado:**
- React 19.1.0 - Hooks, Effect, Ref

---

## 🔧 **Refinamiento Técnico**

### Stack Tecnológico

**Frontend Stack:**
- React 18+ / React Hooks
- Axios (HTTP client)
- React Drawer (custom o Radix UI)
- TailwindCSS (estilos)

### Componentes - Mock Adapter

**DetailPanel.jsx:**
```javascript
export const DetailPanel = ({ pastureId, farmId, isOpen, onClose }) => {
  const { pasture, events, loading } = useDetailPanelData(farmId, pastureId);
  const [activeTab, setActiveTab] = useState('info');
  
  return (
    <Drawer isOpen={isOpen} onClose={onClose} position="right">
      <Tabs value={activeTab} onChange={setActiveTab}>
        <Tab label="Información" />
        <Tab label="Historial" />
        <Tab label="Acciones" />
      </Tabs>
      {activeTab === 'info' && <InfoTab pasture={pasture} />}
      {activeTab === 'historial' && <HistorialTab events={events} />}
      {activeTab === 'acciones' && <AccionesTab pasture={pasture} />}
    </Drawer>
  );
};
```

**useDetailPanelData.js - Mock Adapter Pattern:**
```javascript
export const useDetailPanelData = (farmId, pastureId) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        if (process.env.REACT_APP_USE_MOCK === 'true') {
          await new Promise(r => setTimeout(r, 500));
          setData(getMockPastureDetail(pastureId));
        } else {
          const response = await axios.get(
            `/farms/${farmId}/pastures/${pastureId}`
          );
          setData(response.data);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [farmId, pastureId]);
  
  return { ...data, loading };
};
```

**mockPastureData.js:**
```javascript
export const getMockPastureDetail = (pastureId) => ({
  id: 'P001',
  name: 'Potrero 1',
  status: 'EN_DESCANSO',
  eta: 22,
  events: [
    { type: 'CLOSE', timestamp: '2026-01-08T14:15:00Z', user: 'maria' },
    { type: 'OPEN', timestamp: '2026-01-05T10:30:00Z', user: 'juan' }
  ]
});
```

### Testing Strategy

**Unit Tests (Mock):**
```javascript
test('getMockPastureDetail retorna datos', () => {
  const data = getMockPastureDetail('P001');
  expect(data.name).toBe('Potrero 1');
});
```

**Component Tests (RTL):**
```javascript
test('DetailPanel muestra información', async () => {
  render(<DetailPanel pastureId="P001" isOpen={true} />);
  await waitFor(() => {
    expect(screen.getByText('Potrero 1')).toBeInTheDocument();
  });
});
```


- TailwindCSS 3.4.3 - Drawer responsivo
- Fetch API + AbortController - HTTP
- Environment variables - .env config

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Pragmático con Mock Adapter Strategy (sin bloqueos en HU#12)

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `DetailPanel.jsx` completamente implementado
- [ ] `MaintenanceModal.jsx` para bloqueo/desbloqueo
- [ ] `useDetailPanelData.js` hook personalizado
- [ ] `pastureEventService.js` con getEventHistory
- [ ] Todas las 7 secciones del panel mostradas correctamente
- [ ] PastureTable conecta con DetailPanel (onRowClick)
- [ ] PaddockPage orquesta panel + modales
- [ ] Responsivo: desktop (420px drawer) + mobile (90vw)
- [ ] Historial de eventos carga y muestra correctamente
- [ ] Botones de acción (Abrir, Cerrar, Bloquear, Desbloquear) funcionan
- [ ] Panel se actualiza post-evento sin recargar
- [ ] Modales se abren encima sin cerrar panel
- [ ] Cierre smooth con ESC key o click X
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests componentes: todos los AC probados
- [ ] Tests E2E: flujos completos validados
- [ ] Loading states para historial mostrados
- [ ] Error handling para historial (fallido, retry)
- [ ] Botones deshabilitados según status del potrero
- [ ] Tabla detrás se actualiza junto con panel
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Documentación actualizada (JSDoc, comentarios)
- [ ] Accesibilidad validada (role, labels, keyboard nav)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
