# 🌱 PASTURES-HU#11: Frontend: AlertCenter con Datos Reales

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Alert Manager + Type-based Filtering | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero implementar un AlertCenter (componente de alertas/notificaciones) que muestre datos reales del backend sobre potreros en estado crítico, de tal forma que:

1. El AlertCenter muestre alertas dinámicas basadas en datos reales
2. Las alertas se actualicen automáticamente (polling o WebSocket)
3. Se muestren diferentes tipos de alertas (crítica, advertencia, info)
4. Los operarios vean qué potreros necesitan atención urgente
5. Las alertas sean accesibles y clickeables para ver detalles

Esto habilitará que operarios sepan de un vistazo qué potreros necesitan atención (ETA expirado, mantenimiento requerido, etc.).

---

## 🎯 **Criterios de Aceptación**

### AC#1: AlertCenter Se Renderiza Correctamente
```gherkin
Scenario: AlertCenter se muestra en dashboard
  Given el usuario está en PaddockPage/Dashboard
  And existe componente AlertCenter en el layout
  When página carga
  Then se renderiza AlertCenter con:
    [ ] Título: "Alertas de Potreros" o similar
    [ ] Contador de alertas: "3 alertas"
    [ ] Lista de alertas (máximo 5 visibles)
    [ ] Si hay más de 5: botón "Ver todas" o expandible
    [ ] Icono de alerta (❌, ⚠️, ℹ️)
    [ ] Estilos visuales diferenciados por tipo
```

### AC#2: Tipos de Alertas - ETA Expirado (Crítica)
```gherkin
Scenario: Mostrar alerta cuando ETA <= 0
  Given potrero P001 en estado EN_DESCANSO
  And ETA = -2 días (expirado)
  When AlertCenter obtiene datos de backend
  Then se muestra alerta CRÍTICA (rojo) con:
    [ ] Tipo: "❌ Crítica"
    [ ] Mensaje: "Potrero 1 disponible para uso"
    [ ] Detalles: "ETA expirado hace 2 días"
    [ ] Botón: "Abrir Potrero" (abre modal)
    [ ] Color: Rojo/Red
    [ ] Clase CSS: alert-critical
```

### AC#3: Tipos de Alertas - Próximo a Vencer (Advertencia)
```gherkin
Scenario: Mostrar alerta cuando ETA está próximo (3-7 días)
  Given potrero P002 en EN_DESCANSO
  And ETA = 3 días
  When AlertCenter obtiene datos
  Then se muestra alerta ADVERTENCIA (amarilla) con:
    [ ] Tipo: "⚠️ Advertencia"
    [ ] Mensaje: "Potrero 2 próximo a disponible"
    [ ] Detalles: "Disponible en 3 días"
    [ ] Color: Amarillo/Orange
    [ ] Clase CSS: alert-warning
```

### AC#4: Tipos de Alertas - Mantenimiento Requerido (Info)
```gherkin
Scenario: Mostrar alerta cuando potrero está bloqueado
  Given potrero P003 en estado MANTENIMIENTO
  And substatus = FERTILIZANDO
  And holdUntil = 2025-12-20
  When AlertCenter obtiene datos
  Then se muestra alerta INFO (azul) con:
    [ ] Tipo: "ℹ️ Mantenimiento"
    [ ] Mensaje: "Potrero 3 en mantenimiento"
    [ ] Detalles: "Fertilizando, bloqueado hasta 2025-12-20"
    [ ] Color: Azul/Blue
    [ ] Clase CSS: alert-info
```

### AC#5: Tipos de Alertas - Anómala (Crítica)
```gherkin
Scenario: Mostrar alerta si altura anómala (muy baja/alta)
  Given potrero P004 con altura actual = 5 cm
  And altura mínima requerida = 20 cm
  And está EN_USO (debería tener más altura)
  When AlertCenter obtiene datos
  Then se muestra alerta CRÍTICA (rojo) con:
    [ ] Tipo: "❌ Anómala"
    [ ] Mensaje: "Potrero 4 con altura insuficiente"
    [ ] Detalles: "Altura actual: 5 cm (mínimo: 20 cm)"
    [ ] Color: Rojo
```

### AC#6: Filtrar Alertas por Tipo
```gherkin
Scenario: Filtros para mostrar tipos específicos de alertas
  Given AlertCenter muestra 10 alertas (5 críticas, 3 advertencias, 2 info)
  And existen pestañas/filtros:
    - "Todas" (default)
    - "Críticas"
    - "Advertencias"
    - "Mantenimiento"
  
  When usuario hace clic en "Críticas"
  Then:
    [ ] Solo se muestran 5 alertas críticas
    [ ] Contador actualiza: "5 críticas"
  
  When usuario hace clic en "Todas"
  Then:
    [ ] Se muestran todas las 10 alertas
    [ ] Ordenadas por prioridad (crítica primero)
```

### AC#7: Actualización Automática - Polling
```gherkin
Scenario: AlertCenter se actualiza automáticamente cada minuto
  Given AlertCenter está renderizado
  When pasan 60 segundos
  Then:
    [ ] GET /farms/{farmId}/alerts se ejecuta automáticamente
    [ ] AlertCenter se re-renderiza con datos nuevos
    [ ] Si hay nuevas alertas, se muestran
    [ ] Si una alerta se resolvió, desaparece
    [ ] User experience: transparente (sin parpadeos)
  
  And si usuario hace clic en una alerta:
    [ ] Intervalo no se reinicia (sigue cada 60s)
  
  And si usuario cierra la página:
    [ ] Se limpia el intervalo (no memory leak)
```

### AC#8: Click en Alerta Abre Panel/Modal
```gherkin
Scenario: Hacer clic en una alerta abre detalles
  Given se muestra alerta: "Potrero 1 disponible para uso"
  When usuario hace clic en la alerta
  Then:
    [ ] Se abre DetailPanel o modal con detalles del potrero
    [ ] Se muestra nombre, especie, ETA, altura, status
    [ ] Se ofrece acción: "Abrir Potrero" (si aplica)
    [ ] AlertCenter permanece visible
```

### AC#9: AlertCenter Responsivo
```gherkin
Scenario: AlertCenter se adapta a diferentes tamaños de pantalla
  Given usuario en desktop (1920px)
  When AlertCenter se renderiza
  Then:
    [ ] Se muestra como componente lateral o superior
    [ ] Máximo 5 alertas visibles, scroll si hay más
    [ ] Ancho: 400-500px
  
  Given usuario en tablet (768px)
  When AlertCenter se renderiza
  Then:
    [ ] Se comprime a 350px
    [ ] Texto más pequeño pero legible
  
  Given usuario en mobile (375px)
  When AlertCenter se renderiza
  Then:
    [ ] Se muestra como bottom sheet o modal
    [ ] Ancho: 95vw
    [ ] Botón flotante (FAB) con contador: "3"
    [ ] Al hacer clic en FAB, abre AlertCenter
```

### AC#10: Estilos Visuales Claros
```gherkin
Scenario: Alertas tienen estilos diferenciados por tipo
  Given AlertCenter muestra diferentes tipos
  Then cada tipo tiene:
    [ ] CRÍTICA: Rojo (#FF0000), icono ❌, negrita
    [ ] ADVERTENCIA: Naranja (#FFA500), icono ⚠️
    [ ] INFO: Azul (#0066FF), icono ℹ️
    [ ] MANTENIMIENTO: Morado (#9933FF)
  
  And cada alerta tiene:
    [ ] Borde izquierdo de color (4-5px)
    [ ] Icono alineado a la izquierda
    [ ] Texto principal (mensaje corto)
    [ ] Texto secundario (detalles, más pequeño)
    [ ] Hover: fondo ligeramente más oscuro
    [ ] Cursor: pointer
```

### AC#11: Sin Datos de Alerta (Happy Case)
```gherkin
Scenario: No hay alertas, mostrar mensaje positivo
  Given todos los potreros están en estado óptimo
  And no hay alertas críticas
  When AlertCenter se renderiza
  Then:
    [ ] Muestra mensaje: "✅ Todo está en orden"
    [ ] O "0 alertas activas"
    [ ] Icono verde
    [ ] Texto pequeño: "Próximas 24 horas sin problemas"
```

### AC#12: Error en Obtener Alertas - Graceful Fallback
```gherkin
Scenario: Si GET /farms/{farmId}/alerts falla
  When endpoint retorna error (500, timeout, etc.)
  Then:
    [ ] AlertCenter muestra: "Error al cargar alertas"
    [ ] Botón "Reintentar" (dispara GET nuevamente)
    [ ] Icono de error (❌)
    [ ] No rompe la página
    [ ] Próximo polling en 30s intenta de nuevo
```

### AC#13: Loading State
```gherkin
Scenario: Mostrar loading mientras obtiene alertas
  Given AlertCenter está montado en página
  When GET /farms/{farmId}/alerts está en progreso
  Then:
    [ ] Muestra skeleton loaders o spinner
    [ ] Placeholder: "Cargando alertas..."
    [ ] Una vez datos llegan: desaparece spinner, muestra alertas
    [ ] Transición suave
```

### AC#14: Badge de Contador en Header
```gherkin
Scenario: Badge rojo con contador en header del dashboard
  Given AlertCenter tiene 5 alertas críticas
  When página carga
  Then en header/topbar, existe:
    [ ] Badge rojo: "5"
    [ ] Siguiente al icono de campana o "Alertas"
    [ ] Si hay alertas, badge visible y en rojo
    [ ] Si no hay alertas, badge oculto o gris con "0"
    [ ] Click en badge abre AlertCenter (o scroll hacia él)
```

### AC#15: Persistencia Mínima (localStorage)
```gherkin
Scenario: AlertCenter recuerda estado de filtro
  Given usuario cambió filtro a "Críticas"
  When usuario navega a otra página
  And vuelve al dashboard
  Then:
    [ ] Filtro "Críticas" permanece seleccionado
    [ ] Guardado en localStorage: alertFilter=CRITICAL
    [ ] Si localStorage vacío: default es "Todas"
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### AlertCenter - `AlertCenter.jsx` (NUEVO)
```javascript
export function AlertCenter({ 
  farmId,              // para construir URLs
  refreshInterval = 60000 // ms (default 1 minuto)
}) {
  const [alerts, setAlerts] = useState([]);
  const [filteredAlerts, setFilteredAlerts] = useState([]);
  const [filterType, setFilterType] = useState('ALL'); // ALL, CRITICAL, WARNING, INFO, MAINTENANCE
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedAlert, setSelectedAlert] = useState(null);
  
  // Cargar alertas inicialmente y con polling
  useEffect(() => {
    loadAlerts();
    const interval = setInterval(loadAlerts, refreshInterval);
    return () => clearInterval(interval);
  }, [farmId, refreshInterval]);
  
  // Actualizar filtro cuando cambian alerts o filterType
  useEffect(() => {
    applyFilter();
  }, [alerts, filterType]);
  
  const loadAlerts = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`/farms/${farmId}/alerts`);
      if (!response.ok) throw new Error('Failed to fetch alerts');
      const data = await response.json();
      setAlerts(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };
  
  const applyFilter = () => {
    let filtered = alerts;
    if (filterType !== 'ALL') {
      filtered = alerts.filter(a => a.type === filterType);
    }
    setFilteredAlerts(filtered);
  };
  
  // Renderizar AlertCenter con:
  // - Título + contador
  // - Filtros (pestañas/botones)
  // - Lista de alertas (máx 5 visibles)
  // - Loading/error states
}
```

#### Alert Item - `AlertItem.jsx` (NUEVO)
```javascript
export function AlertItem({ 
  alert,           // { id, type, message, details, pastureId, ... }
  onClick          // callback cuando hace clic
}) {
  // Renderizar alerta individual con:
  // - Icono según tipo
  // - Mensaje principal
  // - Detalles
  // - Color según tipo
  // - Borde izquierdo
  // - Hover effects
}
```

#### Hook - `useAlerts.js` (NUEVO)
```javascript
export function useAlerts(farmId, refreshInterval = 60000) {
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Lógica de carga con polling
  // Return { alerts, isLoading, error, refetch }
}
```

### Estructura de Alertas (Backend Response)

#### GET /farms/{farmId}/alerts
```json
{
  "alerts": [
    {
      "id": "ALERT_001",
      "type": "CRITICAL",     // CRITICAL, WARNING, INFO, MAINTENANCE, ANOMALY
      "pastureId": "P001",
      "pastureName": "Potrero 1",
      "message": "Potrero disponible para uso",
      "details": "ETA expirado hace 2 días",
      "severity": 1,          // 1=crítica, 2=advertencia, 3=info
      "actionRequired": true,
      "suggestedAction": "OPEN_PASTURE",
      "suggestedActionLabel": "Abrir Potrero",
      "timestamp": "2025-12-09T10:30:45Z"
    },
    {
      "id": "ALERT_002",
      "type": "WARNING",
      "pastureId": "P002",
      "pastureName": "Potrero 2",
      "message": "Próximo a disponible",
      "details": "Disponible en 3 días",
      "severity": 2,
      "timestamp": "2025-12-09T10:30:45Z"
    },
    {
      "id": "ALERT_003",
      "type": "MAINTENANCE",
      "pastureId": "P003",
      "pastureName": "Potrero 3",
      "message": "En mantenimiento",
      "details": "Fertilizando, bloqueado hasta 2025-12-20",
      "severity": 3,
      "timestamp": "2025-12-09T10:30:45Z"
    }
  ],
  "summary": {
    "total": 3,
    "critical": 1,
    "warning": 1,
    "info": 1,
    "maintenance": 0
  }
}
```

### Cambios en Componentes Existentes

#### `DashboardLayout.jsx` - Integrar AlertCenter
```javascript
<div className="dashboard-layout">
  <Topbar alerts={alertCount} />
  <Sidebar />
  
  {/* AlertCenter como componente principal */}
  <AlertCenter 
    farmId={farmId}
    onAlertClick={handleAlertClick}
    onOpen={() => setIsAlertCenterOpen(true)}
  />
  
  {/* Contenido principal */}
  <main className="main-content">
    <PaddockPage />
  </main>
</div>
```

#### `Topbar.jsx` - Agregar Badge de Alertas
```javascript
// En Topbar, agregar:
<div className="topbar-right">
  {/* Badge rojo con contador */}
  {alertCount > 0 && (
    <span className="alert-badge">{alertCount}</span>
  )}
  
  {/* Icono de campana/alerta */}
  <button onClick={() => scrollToAlertCenter()}>
    🔔 Alertas
  </button>
</div>
```

### Servicios

#### `alertService.js` (NUEVO)
```javascript
export async function getAlerts(farmId) {
  // GET /farms/{farmId}/alerts
  const response = await fetch(`/farms/${farmId}/alerts`);
  if (!response.ok) throw new Error('Failed to fetch alerts');
  return await response.json();
}
```

### Estilos CSS

```css
.alert-center {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  background: #f9f9f9;
  max-height: 600px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  border-left: 4px solid;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.alert-item:hover {
  background-color: #f0f0f0;
}

.alert-item.critical {
  border-left-color: #ff0000;
  background-color: #ffebee;
}

.alert-item.warning {
  border-left-color: #ffa500;
  background-color: #fff3e0;
}

.alert-item.info {
  border-left-color: #0066ff;
  background-color: #e3f2fd;
}

.alert-item.maintenance {
  border-left-color: #9933ff;
  background-color: #f3e5f5;
}

.alert-icon {
  font-size: 20px;
  min-width: 24px;
}

.alert-content {
  flex: 1;
}

.alert-message {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.alert-details {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.alert-badge {
  background-color: #ff0000;
  color: white;
  border-radius: 50%;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: bold;
  margin-left: 8px;
}

@media (max-width: 768px) {
  .alert-center {
    max-height: 400px;
  }
  
  .alert-item {
    padding: 10px;
    font-size: 12px;
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`AlertCenter.jsx`**
   - Componente principal
   - Estados: alerts, filter, loading, error
   - Polling cada 60s
   - Filtros por tipo

2. **`AlertItem.jsx`**
   - Componente para cada alerta
   - Estilos diferenciados
   - Click handler

3. **`useAlerts.js`** (Hook personalizado)
   - Lógica de carga con polling
   - Cleanup de intervalo
   - Error handling

4. **`alertService.js`**
   - getAlerts(farmId)
   - Centralizar llamadas HTTP

5. **`alertTypes.js`** o constantes
   - ALERT_TYPE.CRITICAL, WARNING, INFO, MAINTENANCE, ANOMALY
   - Mappings de icono, color, severidad

### Archivos a Modificar

1. **`DashboardLayout.jsx`**
   - Agregar AlertCenter al layout
   - Pasar farmId y callbacks

2. **`Topbar.jsx`**
   - Agregar badge con contador
   - Click abre/scroll a AlertCenter

3. **`PaddockPage.jsx`** (opcional)
   - Si AlertCenter está aquí en lugar de DashboardLayout

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear constantes de tipos de alerta
```javascript
export const ALERT_TYPES = {
  CRITICAL: 'CRITICAL',      // ETA expirado, altura anómala
  WARNING: 'WARNING',         // ETA próximo a vencer
  INFO: 'INFO',              // Información general
  MAINTENANCE: 'MAINTENANCE', // Potrero en mantenimiento
  ANOMALY: 'ANOMALY'         // Altura anómala
};

export const ALERT_CONFIG = {
  [ALERT_TYPES.CRITICAL]: {
    icon: '❌',
    color: '#ff0000',
    bgColor: '#ffebee',
    severity: 1
  },
  [ALERT_TYPES.WARNING]: {
    icon: '⚠️',
    color: '#ffa500',
    bgColor: '#fff3e0',
    severity: 2
  }
  // ...
};
```

### Paso 2: Crear hook useAlerts
```javascript
export function useAlerts(farmId, refreshInterval = 60000) {
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const loadAlerts = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await getAlerts(farmId);
      setAlerts(data.alerts || []);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, [farmId]);
  
  useEffect(() => {
    loadAlerts(); // Cargar inmediatamente
    const interval = setInterval(loadAlerts, refreshInterval);
    return () => clearInterval(interval);
  }, [loadAlerts, refreshInterval]);
  
  return { alerts, isLoading, error, refetch: loadAlerts };
}
```

### Paso 3: Crear AlertItem
```javascript
export function AlertItem({ alert, onClick }) {
  const config = ALERT_CONFIG[alert.type];
  
  return (
    <div 
      className={`alert-item ${alert.type.toLowerCase()}`}
      onClick={onClick}
      style={{ borderLeftColor: config.color }}
    >
      <div className="alert-icon">{config.icon}</div>
      <div className="alert-content">
        <div className="alert-message">{alert.message}</div>
        {alert.details && (
          <div className="alert-details">{alert.details}</div>
        )}
      </div>
    </div>
  );
}
```

### Paso 4: Crear AlertCenter
```javascript
export function AlertCenter({ farmId, onAlertClick, refreshInterval = 60000 }) {
  const { alerts, isLoading, error, refetch } = useAlerts(farmId, refreshInterval);
  const [filterType, setFilterType] = useState('ALL');
  const [selectedAlert, setSelectedAlert] = useState(null);
  
  const filteredAlerts = filterType === 'ALL'
    ? alerts
    : alerts.filter(a => a.type === filterType);
  
  const handleAlertClick = (alert) => {
    setSelectedAlert(alert);
    onAlertClick?.(alert);
  };
  
  return (
    <div className="alert-center">
      <div className="alert-header">
        <h2>Alertas de Potreros</h2>
        <span className="alert-counter">{filteredAlerts.length}</span>
      </div>
      
      {/* Filtros */}
      <div className="alert-filters">
        <button 
          className={filterType === 'ALL' ? 'active' : ''}
          onClick={() => setFilterType('ALL')}
        >
          Todas ({alerts.length})
        </button>
        <button 
          className={filterType === 'CRITICAL' ? 'active' : ''}
          onClick={() => setFilterType('CRITICAL')}
        >
          Críticas
        </button>
        {/* más filtros */}
      </div>
      
      {/* Estados */}
      {isLoading && <Spinner />}
      {error && (
        <div className="alert-error">
          Error: {error}
          <button onClick={refetch}>Reintentar</button>
        </div>
      )}
      {alerts.length === 0 && !isLoading && (
        <div className="alert-empty">
          ✅ Todo está en orden. Sin alertas activas.
        </div>
      )}
      
      {/* Lista de alertas */}
      <div className="alert-list">
        {filteredAlerts.slice(0, 5).map(alert => (
          <AlertItem
            key={alert.id}
            alert={alert}
            onClick={() => handleAlertClick(alert)}
          />
        ))}
        {filteredAlerts.length > 5 && (
          <button className="btn-view-all">
            Ver todas ({filteredAlerts.length})
          </button>
        )}
      </div>
    </div>
  );
}
```

### Paso 5: Integrar en DashboardLayout
```javascript
// En DashboardLayout.jsx
<div className="dashboard-layout">
  <Topbar alertCount={alertCriticalCount} />
  <Sidebar />
  
  <div className="dashboard-content">
    <AlertCenter 
      farmId={farmId}
      onAlertClick={handleAlertClick}
    />
    
    <div className="main-content">
      <PaddockPage />
    </div>
  </div>
</div>
```

---

## 🧪 **Casos de Prueba**

### Test Unitarios (Vitest)

```javascript
describe('AlertCenter', () => {
  
  test('carga alertas al montar', async () => {
    render(<AlertCenter farmId="F001" />);
    await waitFor(() => {
      expect(screen.getByText(/Alertas de Potreros/)).toBeInTheDocument();
    });
  });
  
  test('filtra alertas por tipo', async () => {
    render(<AlertCenter farmId="F001" />);
    const criticalBtn = screen.getByText('Críticas');
    fireEvent.click(criticalBtn);
    
    const alertItems = screen.getAllByRole('button', { name: /Potrero/ });
    expect(alertItems.every(a => a.textContent.includes('❌'))).toBe(true);
  });
  
  test('actualiza alertas cada 60 segundos', async () => {
    jest.useFakeTimers();
    const { rerender } = render(<AlertCenter farmId="F001" />);
    
    jest.advanceTimersByTime(60000);
    expect(mockFetch).toHaveBeenCalledTimes(2); // inicial + después de 60s
  });
  
  test('muestra error si GET falla', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network error'));
    render(<AlertCenter farmId="F001" />);
    
    await waitFor(() => {
      expect(screen.getByText(/Error/)).toBeInTheDocument();
    });
  });
});
```

### Test de Componentes (React Testing Library)

```javascript
describe('AlertItem', () => {
  
  test('renderiza alerta crítica con estilos correctos', () => {
    const alert = {
      type: 'CRITICAL',
      message: 'Potrero disponible',
      details: 'ETA expirado hace 2 días'
    };
    
    const { container } = render(<AlertItem alert={alert} />);
    const item = container.querySelector('.alert-item.critical');
    
    expect(item).toHaveClass('critical');
    expect(screen.getByText(/Potrero disponible/)).toBeInTheDocument();
  });
  
  test('click en alerta dispara callback', () => {
    const onClick = jest.fn();
    const alert = { type: 'CRITICAL', message: 'Test' };
    
    render(<AlertItem alert={alert} onClick={onClick} />);
    fireEvent.click(screen.getByText('Test'));
    
    expect(onClick).toHaveBeenCalledWith(alert);
  });
});
```

### Test E2E (Cypress)

```javascript
describe('AlertCenter - E2E', () => {
  
  beforeEach(() => {
    cy.login('juan@farm.com');
    cy.visit('/potreros');
  });
  
  it('muestra alertas críticas en rojo', () => {
    cy.intercept('GET', '/farms/*/alerts', {
      statusCode: 200,
      body: {
        alerts: [
          {
            id: 'A1',
            type: 'CRITICAL',
            message: 'Potrero disponible',
            details: 'ETA expirado'
          }
        ],
        summary: { total: 1, critical: 1 }
      }
    }).as('getAlerts');
    
    cy.wait('@getAlerts');
    cy.contains('Alertas de Potreros').should('be.visible');
    cy.contains('Potrero disponible')
      .parent()
      .should('have.class', 'critical');
  });
  
  it('click en alerta abre detalles', () => {
    cy.contains('.alert-item', 'Potrero 1').click();
    cy.get('[role="dialog"]').should('contain', 'Potrero 1');
  });
  
  it('filtro funciona correctamente', () => {
    cy.contains('button', 'Críticas').click();
    cy.get('.alert-item').should('have.length', 1);
    cy.get('.alert-item').should('have.class', 'critical');
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD con Gherkin)**

### Escenario 1: AlertCenter Muestra Alertas Críticas
```gherkin
Scenario: Mostrar alertas críticas en color rojo
  Given existen 3 potreros con ETA expirado
  When usuario carga dashboard
  Then AlertCenter muestra:
    [ ] "3 alertas críticas"
    [ ] 3 items en rojo con icono ❌
    [ ] Mensaje: "Potrero X disponible para uso"
    [ ] Detalles: "ETA expirado hace Y días"
    [ ] Botón: "Abrir Potrero" visible
```

### Escenario 2: Filtrar Alertas
```gherkin
Scenario: Usuario filtra alertas por tipo
  Given AlertCenter muestra 5 alertas (2 críticas, 2 advertencias, 1 info)
  When usuario hace clic en filtro "Críticas"
  Then se muestran solo 2 alertas críticas
  And contador actualiza: "2 críticas"
```

### Escenario 3: Actualización Automática
```gherkin
Scenario: AlertCenter se actualiza automáticamente cada minuto
  Given AlertCenter está visible
  When pasan 60 segundos
  Then:
    [ ] GET /farms/F001/alerts se ejecuta
    [ ] AlertCenter re-renderiza con datos nuevos
    [ ] Si nueva alerta: aparece en la lista
    [ ] Si alerta resuelta: desaparece
```

### Escenario 4: Click en Alerta Abre Panel
```gherkin
Scenario: Click en alerta abre DetailPanel
  Given se muestra alerta para "Potrero 1"
  When usuario hace clic en la alerta
  Then:
    [ ] DetailPanel se abre mostrando Potrero 1
    [ ] AlertCenter permanece visible
    [ ] Usuario puede abrir el potrero desde panel
```

### Escenario 5: Sin Alertas - Happy Case
```gherkin
Scenario: Todos los potreros están en orden
  Given todos los potreros en estado óptimo
  When usuario carga dashboard
  Then AlertCenter muestra:
    [ ] "✅ Todo está en orden"
    [ ] "0 alertas activas"
    [ ] Ícono verde
```

---

## 📚 **Referencias y Dependencias**

**Dependencias de otros componentes**:
- ✅ Backend /farms/{farmId}/alerts (necesita ser creado en HU#12)
- ✅ DetailPanel (HU#3)
- ✅ DashboardLayout (existente)

**Documentación relacionada**:
- [HU#3: Frontend DetailPanel](../P0/PASTURES-HU-003-detailpanel.md)
- [HU#12: Backend GET Historial Eventos](./PASTURES-HU-012-get-historial.md) (próxima)
- [Pastures Overview](../../pastures/pastures-overview.md)
- [Flujo Dashboard Potreros](../../architecture/flujo-dashboard-potreros.md)

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Real-time Alert Manager + Type-based Filtering + Polling/WebSocket

**Justificación:** **Alert Manager**: Sistema centralizado de alertas (como notificaciones). **Type-based Filtering**: Categorías (CRÍTICA, ADVERTENCIA, INFO) con estilos diferentes. **Polling**: Actualización cada 60s (simple) o WebSocket (avanzado). **Responsive**: FAB en mobile, panel lateral desktop. **Accesibilidad**: aria-live para anuncios nuevas alertas. **Performance**: Máximo 5 visibles, sin sobrecarga servidor.

**Componentes Afectados:**

- **AlertCenter.jsx (Nuevo):** Componente principal. Props: `farmId`. State: `alerts`, `filter`, `loading`. Renderiza: título + contador + filtros + lista alerts. Polling cada 60s.

- **Alert.jsx (Nuevo):** Alerta individual. Props: `alert`, `onClick`. Renderiza: icono + tipo + mensaje + detalles. Estilos por tipo (rojo/naranja/azul).

- **AlertFilter.jsx (Nuevo):** Pestañas/filtros. Props: `onFilter`. Opciones: Todas, Críticas, Advertencias, Mantenimiento.

- **useAlerts.js (Nuevo - Hook):** Polling + estado. Retorna: `{ alerts, filter, setFilter, loading, error }`. GET `/farms/{farmId}/alerts` cada 60s. Cleanup en unmount.

- **alertTypeMap.js (Nuevo):** Tipos y colores. CRITICAL (rojo), WARNING (naranja), INFO (azul), MAINTENANCE (morado). Icons y mensajes.

- **alertGenerator.js (Nuevo):** Lógica para generar alertas desde datos pasturas. ETA<=0→CRÍTICA, ETA<=7→ADVERTENCIA, MANTENIMIENTO→INFO, altura anómala→CRÍTICA.

**Hitos:**
1. alertTypeMap.js (sin dependencias)
2. Alert.jsx (depende: alertTypeMap)
3. AlertFilter.jsx (sin dependencias)
4. useAlerts.js (depende: API)
5. AlertCenter.jsx (depende: Alert, AlertFilter, useAlerts)

### Validación de Impacto

✅ **Alert Manager**: Centralizado, reutilizable para futuras notificaciones
✅ **Type-based Filtering**: Categorización clara
✅ **Polling**: Simple, eficiente para 60s
✅ **Responsive**: Desktop/Tablet/Mobile
✅ **Accesibilidad**: aria-live para screen readers
✅ **Performance**: Máximo 5 visibles + scroll

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-001: Backend POST Eventos (genera datos)
- ✅ PASTURES-HU-003: Detail Panel (integración)
- → PASTURES-HU-011: AlertCenter (esta)
- → PASTURES-HU-012: Historial Eventos

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Alert Manager + Polling (real-time monitoring)

---

## 🔧 **Refinamiento Técnico**

### Alert Manager Architecture

**AlertCenter.jsx:**
```javascript
export const AlertCenter = ({ farmId }) => {
  const { alerts, loading } = useAlerts(farmId);
  const [dismissedIds, setDismissedIds] = useState(new Set());
  
  const visibleAlerts = alerts.filter(a => !dismissedIds.has(a.id));
  
  const handleDismiss = (id) => {
    setDismissedIds(prev => new Set([...prev, id]));
  };
  
  return (
    <div className="fixed bottom-4 right-4 space-y-2 max-w-md z-50">
      {visibleAlerts.map(alert => (
        <AlertItem key={alert.id} alert={alert} onDismiss={handleDismiss} />
      ))}
    </div>
  );
};
```

### useAlerts Hook - Polling

```javascript
export const useAlerts = (farmId, pollInterval = 5000) => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    // Initial fetch
    fetchAlerts();
    
    // Setup polling
    const interval = setInterval(fetchAlerts, pollInterval);
    
    const fetchAlerts = async () => {
      try {
        const response = await axios.get(`/farms/${farmId}/alerts`);
        setAlerts(response.data);
      } catch (err) {
        console.error('Failed to fetch alerts', err);
      } finally {
        setLoading(false);
      }
    };
    
    return () => clearInterval(interval);
  }, [farmId, pollInterval]);
  
  return { alerts, loading };
};
```

### Alert Item Types

```javascript
const alertTypeConfig = {
  'PASTURE_AVAILABLE': {
    icon: '✅',
    bgColor: 'bg-green-100',
    textColor: 'text-green-800',
    duration: 5000
  },
  'PASTURE_ETA_SOON': {
    icon: '⏰',
    bgColor: 'bg-yellow-100',
    textColor: 'text-yellow-800',
    duration: 8000
  },
  'PASTURE_VENCIDO': {
    icon: '⚠️',
    bgColor: 'bg-orange-100',
    textColor: 'text-orange-800',
    duration: 10000
  },
  'ERROR': {
    icon: '❌',
    bgColor: 'bg-red-100',
    textColor: 'text-red-800',
    duration: 15000
  }
};

export const AlertItem = ({ alert, onDismiss }) => {
  const config = alertTypeConfig[alert.type];
  
  useEffect(() => {
    const timer = setTimeout(() => onDismiss(alert.id), config.duration);
    return () => clearTimeout(timer);
  }, [alert.id, config.duration]);
  
  return (
    <div className={`${config.bgColor} ${config.textColor} p-4 rounded shadow-lg`}>
      <span>{config.icon}</span>
      <p>{alert.message}</p>
    </div>
  );
};
```

### Testing Strategy

**Component Tests:**
```javascript
test('AlertCenter muestra alertas', async () => {
  render(<AlertCenter farmId="F001" />);
  await waitFor(() => {
    expect(screen.getByText(/disponible/i)).toBeInTheDocument();
  });
});

test('Alert se auto-cierra después de timeout', async () => {
  jest.useFakeTimers();
  render(<AlertCenter farmId="F001" />);
  jest.advanceTimersByTime(5000);
  expect(screen.queryByText(/disponible/i)).not.toBeInTheDocument();
});
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] `AlertCenter.jsx` completamente implementado
- [ ] `AlertItem.jsx` con estilos diferenciados
- [ ] `useAlerts.js` hook con polling
- [ ] `alertService.js` con getAlerts()
- [ ] `alertTypes.js` con constantes y mappings
- [ ] Todos los 5 tipos de alerta implementados
- [ ] Filtros funcionan correctamente (ALL, CRITICAL, WARNING, INFO, MAINTENANCE)
- [ ] Polling cada 60 segundos funciona sin memory leaks
- [ ] Click en alerta abre DetailPanel
- [ ] Responsive: desktop (400-500px) + mobile (bottom sheet)
- [ ] Badge con contador en Topbar
- [ ] Loading states y error handling
- [ ] localStorage para guardar filtro seleccionado
- [ ] Tests unitarios: cobertura >= 80%
- [ ] Tests componentes: todos los ACs probados
- [ ] Tests E2E: flujos completos validados
- [ ] Estilos CSS con hover effects
- [ ] Sin memory leaks (cleanup de intervalos)
- [ ] Code review aprobado (2 approvals)
- [ ] Sin warnings de linting
- [ ] CI/CD green (build, test, coverage)
- [ ] Demostrable en staging environment
- [ ] Documentación actualizada (JSDoc, comentarios)
- [ ] Accesibilidad validada (roles, labels, keyboard nav)

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
