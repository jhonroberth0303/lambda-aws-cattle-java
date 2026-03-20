# 🌱 PASTURES-HU#18: Frontend: Estadísticas y Reportes

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟡 MEDIO (P2) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - Dashboard + Recharts Visualization | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **frontend developer**, quiero crear un Dashboard de Estadísticas y Reportes para visualizar el desempeño de potreros, de tal forma que:

1. Se muestren gráficos de utilización de potreros (porcentaje, tiempo)
2. Se visualice la disponibilidad promedio a lo largo del tiempo
3. Se muestren operaciones por usuario (quién abre/cierra más)
4. Se muestre distribución de ETA (cuánto tiempo en descanso)
5. Se listen los top potreros (más usados, más disponibles, etc.)
6. Se pueda filtrar por rango de fechas
7. Se pueda exportar reportes a PDF y CSV

Esto habilitará que gerentes analicen el desempeño de potreros, detecten ineficiencias, y tomen decisiones basadas en datos.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Dashboard Renderiza Correctamente
```gherkin
Scenario: Mostrar dashboard de estadísticas
  Given usuario en PaddockPage
  And hace click en "Reportes" o "Estadísticas"
  When se carga el dashboard
  Then:
    [ ] Se muestra en panel o página nueva
    [ ] Gráficos se cargan exitosamente
    [ ] Datos actualizados del backend
    [ ] Sin errores o crashes
```

### AC#2: Gráfico de Utilización por Potrero
```gherkin
Scenario: Mostrar utilización en gráfico de barras
  Given dashboard con datos de enero 2026
  When se muestra gráfico "Utilización por Potrero"
  Then:
    [ ] Gráfico tipo barra horizontal
    [ ] Eje X: porcentaje (0-100%)
    [ ] Eje Y: potreros (P001, P002, P003, ...)
    [ ] Cada barra coloreada: verde (>80%), naranja (50-80%), rojo (<50%)
    [ ] Valores en barras (ej: "85%")
    [ ] Top 10 potreros ordenados por uso
    [ ] Leyenda con colores
```

### AC#3: Gráfico de Disponibilidad Temporal
```gherkin
Scenario: Mostrar disponibilidad a lo largo del tiempo
  Given dashboard con datos de 30 días
  When se muestra gráfico "Disponibilidad Promedio"
  Then:
    [ ] Gráfico tipo línea
    [ ] Eje X: fechas (día 1, 2, 3, ..., 30)
    [ ] Eje Y: porcentaje disponible (0-100%)
    [ ] Línea azul suave
    [ ] Puntos en cada día
    [ ] Valores en tooltip al hover
    [ ] Media general en línea roja punteada
    [ ] Útil para ver tendencias
```

### AC#4: Gráfico de Operaciones por Usuario
```gherkin
Scenario: Mostrar quién realiza más operaciones
  Given dashboard con historial de cambios
  When se muestra gráfico "Operaciones por Usuario"
  Then:
    [ ] Gráfico tipo barra vertical
    [ ] Eje X: usuarios (Carlos, Ana, Juan, ...)
    [ ] Eje Y: cantidad de operaciones
    [ ] Barras coloreadas por tipo: OPEN (verde), CLOSE (rojo), EDIT (azul)
    [ ] Stacked bars (acumuladas)
    [ ] Valores en barras
    [ ] Útil para auditoría
```

### AC#5: Gráfico de Distribución ETA
```gherkin
Scenario: Mostrar distribución de ETA
  Given potreros en descanso con diferentes ETAs
  When se muestra gráfico "Distribución ETA"
  Then:
    [ ] Gráfico tipo histograma o pie
    [ ] Categorías: 1-7 días, 8-14 días, 15-21 días, 22+ días
    [ ] O histograma con número exacto de días
    [ ] Colores: de rojo (pronto) a verde (lejano)
    [ ] Cantidad de potreros en cada rango
    [ ] Útil para planificar disponibilidad
```

### AC#6: Tabla de Top Potreros
```gherkin
Scenario: Listar potreros por métrica
  Given dashboard con múltiples potreros
  When se muestran tablas "Top Potreros"
  Then:
    [ ] Tabla con columnas: Potrero, Métrica, Valor
    [ ] Tabs o pestañas para diferentes ordenamientos:
        * Más utilizados (cantidad de usos)
        * Más disponibles (% disponible)
        * Mayor ETA promedio (días en descanso)
        * Más editados (cambios)
    [ ] Top 10 de cada categoría
    [ ] Ordenado descendente
```

### AC#7: Filtro por Rango de Fechas
```gherkin
Scenario: Filtrar datos por período
  Given dashboard mostrando enero 2026
  When usuario selecciona rango de fechas
  And selecciona: 2026-01-01 a 2026-01-15
  Then:
    [ ] Todos los gráficos se actualizan
    [ ] Datos solo para período seleccionado
    [ ] Loading spinner durante recarga
    [ ] Transición suave
```

### AC#8: Seleccionar Potrero para Detallar
```gherkin
Scenario: Ver detalles de un potrero específico
  Given usuario viendo gráfico de utilización
  When hace click en una barra (ej: P001)
  Then:
    [ ] Se abre panel lateral con detalles de P001
    [ ] Historial de cambios de P001
    [ ] ETA actual
    [ ] Última operación
    [ ] O abre DetailPanel (HU#3)
    [ ] Gráfico mantiene contexto
```

### AC#9: Estadísticas Numéricas
```gherkin
Scenario: Mostrar métricas clave en números
  Given dashboard cargado
  Then muestra en tarjetas o KPI:
    [ ] Total de potreros: 25
    [ ] Disponibles ahora: 12 (48%)
    [ ] En descanso: 10 (40%)
    [ ] En uso: 3 (12%)
    [ ] Disponibilidad promedio: 65%
    [ ] ETA promedio: 18 días
    [ ] Total de cambios en período: 456
    [ ] Potrero más usado: P005 (95 cambios)
    [ ] Operario más activo: Carlos (156 ops)
```

### AC#10: Exportar a CSV
```gherkin
Scenario: Descargar reporte como CSV
  Given dashboard visible con datos
  When usuario hace click "Descargar CSV"
  Then:
    [ ] Se genera archivo CSV
    [ ] Contiene: potrero, utilización, disponibilidad, ETA, cambios
    [ ] Formato: comma-separated, encoding UTF-8
    [ ] Archivo: pasture-report-2026-01-09.csv
    [ ] Se descarga automáticamente
    [ ] Abre en Excel correctamente
```

### AC#11: Exportar a PDF
```gherkin
Scenario: Descargar reporte como PDF
  Given dashboard visible
  When usuario hace click "Descargar PDF"
  Then:
    [ ] Se genera PDF con:
        * Título: "Reporte de Potreros - Enero 2026"
        * Fecha del reporte
        * Gráficos (imagen de alta calidad)
        * Tabla de datos
        * Pie de página con metadatos
    [ ] Archivo: pasture-report-2026-01-09.pdf
    [ ] Se descarga automáticamente
    [ ] Legible en cualquier PDF viewer
```

### AC#12: Responsive Design
```gherkin
Scenario: Dashboard adaptado a pantallas
  Given usuario en desktop (1920px)
  When dashboard se carga
  Then:
    [ ] Gráficos en grid 2x2 o 3x2
    [ ] Amplio espaciado
    [ ] Todos visibles sin scroll
  
  Given usuario en tablet (768px)
  When dashboard se carga
  Then:
    [ ] Gráficos apilados verticalmente (2 por fila)
    [ ] Scroll vertical necesario
  
  Given usuario en mobile (375px)
  When dashboard se carga
  Then:
    [ ] Gráficos uno por uno (full width)
    [ ] Scroll vertical para ver todos
    [ ] Toque para interactuar
    [ ] Tooltips en mobile funcionan
```

### AC#13: Performance
```gherkin
Scenario: Dashboard carga rápido
  Given usuario abre dashboard
  When página carga
  Then:
    [ ] Renderizado inicial en < 2 segundos
    [ ] Gráficos en < 3 segundos total
    [ ] Sin bloqueos (UI responsive)
    [ ] Scroll smooth
    [ ] Cambio de fechas: < 1 segundo
```

### AC#14: Actualización Automática (Opcional)
```gherkin
Scenario: Datos se actualizan automáticamente
  Given dashboard abierto
  And usuario A realiza cambios en otro navegador
  When pasan 30-60 segundos
  Then:
    [ ] Datos se refrescan automáticamente
    [ ] O botón "Actualizar" visible
    [ ] Gráficos se recalculan
    [ ] Sin afectar interacción del usuario
```

### AC#15: Accesibilidad
```gherkin
Scenario: Dashboard es accesible
  Given dashboard visible
  Then:
    [ ] Colores con suficiente contraste
    [ ] Labels claros para gráficos
    [ ] Datos en tablas (no solo gráficos)
    [ ] Navegación con Tab
    [ ] Alt text en imágenes
    [ ] ARIA labels en gráficos
    [ ] Screen reader compatible
```

---

## 📊 **Especificación Técnica**

### Estructura de Componentes

#### Dashboard Principal - `StatsAndReportsPage.jsx`

```javascript
export function StatsAndReportsPage({ farmId }) {
  const [dateRange, setDateRange] = useState({
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), // 30 días atrás
    endDate: new Date()
  });
  
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    fetchReportData(farmId, dateRange);
  }, [farmId, dateRange]);
  
  const fetchReportData = async (farmId, range) => {
    setLoading(true);
    try {
      const data = await getReportData(
        farmId,
        range.startDate,
        range.endDate
      );
      setReportData(data);
    } catch (error) {
      console.error('Error cargando reportes:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const handleExportCSV = () => {
    exportToCSV(reportData, dateRange);
  };
  
  const handleExportPDF = () => {
    exportToPDF(reportData, dateRange);
  };
  
  if (loading) return <LoadingSpinner />;
  
  return (
    <div className="stats-page">
      <div className="stats-header">
        <h1>Estadísticas y Reportes</h1>
        
        <div className="stats-controls">
          <DateRangePicker
            startDate={dateRange.startDate}
            endDate={dateRange.endDate}
            onChange={(range) => setDateRange(range)}
          />
          
          <button onClick={() => fetchReportData(farmId, dateRange)}>
            🔄 Actualizar
          </button>
          
          <button onClick={handleExportCSV}>
            📥 CSV
          </button>
          
          <button onClick={handleExportPDF}>
            📄 PDF
          </button>
        </div>
      </div>
      
      <div className="stats-kpis">
        <KPICard
          title="Potreros Disponibles"
          value={reportData?.availableCount || 0}
          percentage={reportData?.availablePercentage || 0}
          color="green"
        />
        <KPICard
          title="En Descanso"
          value={reportData?.restingCount || 0}
          percentage={reportData?.restingPercentage || 0}
          color="orange"
        />
        <KPICard
          title="En Uso"
          value={reportData?.inUseCount || 0}
          percentage={reportData?.inUsePercentage || 0}
          color="yellow"
        />
        <KPICard
          title="Disponibilidad Promedio"
          value={`${reportData?.avgAvailability || 0}%`}
          color="blue"
        />
      </div>
      
      <div className="stats-charts">
        <div className="chart-container">
          <h3>Utilización por Potrero</h3>
          <UtilizationChart data={reportData?.utilizationByPasture} />
        </div>
        
        <div className="chart-container">
          <h3>Disponibilidad Temporal</h3>
          <AvailabilityChart data={reportData?.availabilityTrend} />
        </div>
        
        <div className="chart-container">
          <h3>Operaciones por Usuario</h3>
          <OperationsByUserChart data={reportData?.operationsByUser} />
        </div>
        
        <div className="chart-container">
          <h3>Distribución ETA</h3>
          <ETADistributionChart data={reportData?.etaDistribution} />
        </div>
      </div>
      
      <div className="stats-tables">
        <TopPasturesTable data={reportData?.topPastures} />
      </div>
    </div>
  );
}
```

#### Componente KPI - `KPICard.jsx`

```javascript
export function KPICard({ title, value, percentage, color }) {
  return (
    <div className={`kpi-card kpi-${color}`}>
      <h4>{title}</h4>
      <div className="kpi-value">{value}</div>
      {percentage !== undefined && (
        <div className="kpi-percentage">{percentage}%</div>
      )}
    </div>
  );
}
```

#### Componente Gráfico - Utilización

```javascript
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export function UtilizationChart({ data }) {
  return (
    <ResponsiveContainer width="100%" height={400}>
      <BarChart
        data={data}
        layout="vertical"
        margin={{ top: 5, right: 30, left: 100, bottom: 5 }}
      >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis type="number" domain={[0, 100]} />
        <YAxis dataKey="name" type="category" width={100} />
        <Tooltip formatter={(value) => `${value}%`} />
        <Bar dataKey="utilization" fill="#0066cc" />
      </BarChart>
    </ResponsiveContainer>
  );
}
```

#### Componente Gráfico - Disponibilidad

```javascript
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export function AvailabilityChart({ data }) {
  return (
    <ResponsiveContainer width="100%" height={400}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="date" />
        <YAxis domain={[0, 100]} />
        <Tooltip formatter={(value) => `${value}%`} />
        <Legend />
        <Line type="monotone" dataKey="availability" stroke="#0066cc" />
        <Line type="monotone" dataKey="average" stroke="#ff0000" strokeDasharray="5 5" />
      </LineChart>
    </ResponsiveContainer>
  );
}
```

#### Componente Gráfico - Operaciones

```javascript
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';

export function OperationsByUserChart({ data }) {
  const COLORS = {
    OPEN: '#00aa00',
    CLOSE: '#ff0000',
    EDIT: '#0066cc',
    MAINTENANCE_SET: '#ff8800'
  };
  
  return (
    <ResponsiveContainer width="100%" height={400}>
      <BarChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="userName" />
        <YAxis />
        <Tooltip />
        <Legend />
        <Bar dataKey="OPEN" stackId="a" fill={COLORS.OPEN} />
        <Bar dataKey="CLOSE" stackId="a" fill={COLORS.CLOSE} />
        <Bar dataKey="EDIT" stackId="a" fill={COLORS.EDIT} />
        <Bar dataKey="MAINTENANCE_SET" stackId="a" fill={COLORS.MAINTENANCE_SET} />
      </BarChart>
    </ResponsiveContainer>
  );
}
```

#### Componente Gráfico - ETA

```javascript
import { PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer } from 'recharts';

export function ETADistributionChart({ data }) {
  const COLORS = ['#ff0000', '#ff8800', '#ffff00', '#00aa00'];
  
  return (
    <ResponsiveContainer width="100%" height={400}>
      <PieChart>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          labelLine={false}
          label={({ name, value }) => `${name}: ${value}`}
          outerRadius={120}
          fill="#8884d8"
          dataKey="value"
        >
          {data.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
}
```

#### Componente Tabla - Top Potreros

```javascript
export function TopPasturesTable({ data }) {
  const [sortBy, setSortBy] = useState('mostUsed');
  
  const tabs = [
    { id: 'mostUsed', label: 'Más Utilizados' },
    { id: 'mostAvailable', label: 'Más Disponibles' },
    { id: 'highestETA', label: 'Mayor ETA Promedio' },
    { id: 'mostEdited', label: 'Más Editados' }
  ];
  
  const currentData = data?.[sortBy] || [];
  
  return (
    <div className="top-pastures">
      <h3>Top Potreros</h3>
      
      <div className="tabs">
        {tabs.map(tab => (
          <button
            key={tab.id}
            className={`tab ${sortBy === tab.id ? 'active' : ''}`}
            onClick={() => setSortBy(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      
      <table className="pastures-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Potrero</th>
            <th>Valor</th>
            <th>Cambio</th>
          </tr>
        </thead>
        <tbody>
          {currentData.map((row, idx) => (
            <tr key={row.pastureId}>
              <td>{idx + 1}</td>
              <td className="clickable" onClick={() => openPastureDetail(row.pastureId)}>
                {row.pastureId}
              </td>
              <td>{row.value}</td>
              <td className={row.change > 0 ? 'positive' : 'negative'}>
                {row.change > 0 ? '↑' : '↓'} {Math.abs(row.change)}%
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

### Servicios

#### `reportService.js`

```javascript
export async function getReportData(farmId, startDate, endDate) {
  const params = new URLSearchParams({
    startDate: startDate.toISOString().split('T')[0],
    endDate: endDate.toISOString().split('T')[0]
  });
  
  const response = await fetch(
    `/api/farms/${farmId}/reports?${params}`
  );
  
  if (!response.ok) {
    throw new Error('Error cargando reportes');
  }
  
  return response.json();
}

export function exportToCSV(reportData, dateRange) {
  let csv = 'Reporte de Potreros\n';
  csv += `Período: ${dateRange.startDate.toLocaleDateString()} - ${dateRange.endDate.toLocaleDateString()}\n\n`;
  
  csv += 'Potrero,Utilización,Disponibilidad,ETA Promedio,Cambios\n';
  reportData.utilizationByPasture.forEach(row => {
    csv += `${row.name},${row.utilization}%,${row.availability}%,${row.eta},${row.changes}\n`;
  });
  
  // Descargar archivo
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `pasture-report-${new Date().toISOString().split('T')[0]}.csv`;
  a.click();
  window.URL.revokeObjectURL(url);
}

export async function exportToPDF(reportData, dateRange) {
  // Usar librería como jsPDF + html2canvas
  const doc = new jsPDF();
  
  doc.setFontSize(16);
  doc.text('Reporte de Potreros', 20, 20);
  
  doc.setFontSize(10);
  doc.text(
    `Período: ${dateRange.startDate.toLocaleDateString()} - ${dateRange.endDate.toLocaleDateString()}`,
    20,
    30
  );
  
  // Agregar imágenes de gráficos
  // Agregar tabla de datos
  
  doc.save(`pasture-report-${new Date().toISOString().split('T')[0]}.pdf`);
}
```

### Estilos CSS

```css
.stats-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.stats-header h1 {
  font-size: 28px;
  margin: 0;
}

.stats-controls {
  display: flex;
  gap: 12px;
}

.stats-controls button {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  background: white;
  color: #333;
  cursor: pointer;
  font-weight: 500;
  border: 1px solid #ddd;
}

.stats-controls button:hover {
  background: #f0f0f0;
}

.stats-kpis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

.kpi-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border-left: 4px solid;
}

.kpi-card.kpi-green {
  border-left-color: #00aa00;
}

.kpi-card.kpi-orange {
  border-left-color: #ff8800;
}

.kpi-card.kpi-yellow {
  border-left-color: #ffff00;
}

.kpi-card.kpi-blue {
  border-left-color: #0066cc;
}

.kpi-card h4 {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #666;
  text-transform: uppercase;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.kpi-percentage {
  font-size: 14px;
  color: #666;
}

.stats-charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-container h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.stats-tables {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tab {
  padding: 8px 16px;
  border: none;
  background: none;
  color: #666;
  cursor: pointer;
  font-weight: 500;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #0066cc;
  border-bottom-color: #0066cc;
}

.pastures-table {
  width: 100%;
  border-collapse: collapse;
}

.pastures-table thead {
  background: #f9f9f9;
}

.pastures-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  font-size: 12px;
  color: #666;
  text-transform: uppercase;
}

.pastures-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.pastures-table .clickable {
  color: #0066cc;
  cursor: pointer;
  font-weight: 500;
}

.pastures-table .clickable:hover {
  text-decoration: underline;
}

.pastures-table .positive {
  color: #00aa00;
}

.pastures-table .negative {
  color: #ff0000;
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-charts {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .stats-controls {
    flex-wrap: wrap;
  }
  
  .stats-kpis {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-container {
    min-height: 300px;
  }
}

@media (max-width: 480px) {
  .stats-kpis {
    grid-template-columns: 1fr;
  }
  
  .tabs {
    flex-wrap: wrap;
  }
  
  .pastures-table {
    font-size: 12px;
  }
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`StatsAndReportsPage.jsx`** - Página principal
2. **`KPICard.jsx`** - Tarjeta KPI
3. **`UtilizationChart.jsx`** - Gráfico utilización
4. **`AvailabilityChart.jsx`** - Gráfico disponibilidad
5. **`OperationsByUserChart.jsx`** - Gráfico operaciones
6. **`ETADistributionChart.jsx`** - Gráfico ETA
7. **`TopPasturesTable.jsx`** - Tabla top potreros
8. **`DateRangePicker.jsx`** - Selector fechas
9. **`reportService.js`** - Servicios
10. **`stats.css`** - Estilos
11. **`StatsAndReportsPage.test.jsx`** - Tests

### Archivos a Modificar

1. **`PaddockPage.jsx`** - Link a reportes
2. **`Topbar.jsx`** - Link a reportes (bonus)
3. **`package.json`** - Agregar recharts, date-fns-format

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Crear estructura base
- StatsAndReportsPage.jsx
- Componentes KPI
- CSS básico

### Paso 2: Agregar gráficos
- Instalar recharts
- Componentes de gráficos
- Integrar con datos

### Paso 3: Agregar tabla
- TopPasturesTable
- Tabs para diferentes ordenamientos

### Paso 4: Integrar servicios
- fetchReportData()
- Filtros por fecha
- Actualización automática

### Paso 5: Agregar exportación
- Export CSV
- Export PDF (jsPDF)

### Paso 6: Testing
- Tests unitarios
- Tests E2E

---

## 🧪 **Casos de Prueba**

### Test Unitarios

```javascript
describe('StatsAndReportsPage', () => {
  
  test('renderiza KPIs', () => {
    render(<StatsAndReportsPage farmId="F001" />);
    expect(screen.getByText(/Potreros Disponibles/i)).toBeInTheDocument();
  });
  
  test('carga datos al montar', async () => {
    render(<StatsAndReportsPage farmId="F001" />);
    await waitFor(() => {
      expect(screen.queryByText(/Cargando/i)).not.toBeInTheDocument();
    });
  });
});
```

---

## 🔄 **Escenarios de Prueba (BDD)**

### Escenario 1: Ver Dashboard
```gherkin
Scenario: Mostrar estadísticas
  Given usuario en PaddockPage
  When hace click en "Reportes"
  Then se muestra dashboard con gráficos
```

### Escenario 2: Filtrar por Fecha
```gherkin
Scenario: Cambiar período
  Given dashboard visible
  When selecciona rango 2026-01-01 a 2026-01-15
  Then gráficos se actualizan
```

### Escenario 3: Exportar Datos
```gherkin
Scenario: Descargar reporte
  Given dashboard visible
  When hace click "CSV"
  Then se descarga archivo CSV
```

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] StatsAndReportsPage.jsx creado
- [ ] Gráfico utilización funciona
- [ ] Gráfico disponibilidad funciona
- [ ] Gráfico operaciones funciona
- [ ] Gráfico ETA funciona
- [ ] KPIs muestran valores correctos
- [ ] Filtro por fecha funciona
- [ ] Click en elemento abre detalle
- [ ] Export CSV funciona
- [ ] Export PDF funciona
- [ ] Responsive: desktop, tablet, mobile
- [ ] Performance: < 2s carga
- [ ] Recharts integrado
- [ ] Colores consistentes
- [ ] Tests unitarios: >= 80%

---

## 🔧 **Refinamiento Técnico**

### Dashboard Layout

```javascript
export const Dashboard = ({ farmId, startDate, endDate }) => {
  const { stats, loading } = useStatistics(farmId, startDate, endDate);
  
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 p-6">
      <KPICard label="% Utilización" value={stats.utilizationPercentage} />
      <KPICard label="Disponible Ahora" value={stats.availableNow} />
      <KPICard label="En Descanso" value={stats.resting} />
      <KPICard label="Mantenimiento" value={stats.maintenance} />
      
      <div className="col-span-full">
        <UtilizationChart data={stats.utilizationData} />
      </div>
      
      <div className="col-span-full">
        <AvailabilityChart data={stats.availabilityData} />
      </div>
      
      <div className="col-span-full flex gap-4">
        <button onClick={() => exportCSV(stats)}>📥 CSV</button>
        <button onClick={() => exportPDF(stats)}>📊 PDF</button>
      </div>
    </div>
  );
};
```

### Recharts Components

```javascript
export const UtilizationChart = ({ data }) => (
  <BarChart data={data} width={500} height={300}>
    <CartesianGrid strokeDasharray="3 3" />
    <XAxis dataKey="name" />
    <YAxis />
    <Bar dataKey="value" fill="#8884d8" />
  </BarChart>
);

export const AvailabilityChart = ({ data }) => (
  <LineChart data={data} width={500} height={300}>
    <CartesianGrid strokeDasharray="3 3" />
    <XAxis dataKey="date" />
    <YAxis />
    <Line type="monotone" dataKey="percentage" stroke="#8884d8" />
    <ReferenceLine y={50} stroke="#82ca9d" />
  </LineChart>
);
```

### Export Functions

```javascript
export const exportCSV = (stats) => {
  const csv = [
    ['Métrica', 'Valor'],
    ['Utilización', stats.utilizationPercentage],
    ['Disponible', stats.availableNow],
    ['En Descanso', stats.resting]
  ];
  
  downloadCSV(csv, `stats-${new Date().toISOString()}.csv`);
};

export const exportPDF = (stats) => {
  const doc = new jsPDF();
  doc.text('Reporte de Estadísticas', 10, 10);
  doc.text(`Utilización: ${stats.utilizationPercentage}%`, 10, 20);
  // Más contenido...
  doc.save('report.pdf');
};
```

### Testing Strategy

**Component Tests:**
- KPI cards muestran valores correctos
- Gráficos se renderizan
- Filtro de fechas funciona
- Export CSV/PDF funciona

---

## Análisis Arquitectónico (Arquitecto)

<!-- ============================================================================ -->
<!-- SECCIÓN AGREGADA POR: Workflow analizar-disenar-historia-usuario            -->
<!-- ETAPA: Análisis Arquitectónico                                              -->
<!-- RESPONSABLE: Arquitecto                                                     -->
<!-- ============================================================================ -->

### Decisiones de Diseño

**Patrón Arquitectónico:** Dashboard Pattern + Recharts Visualization + Report Generation

**Justificación:** **Dashboard Layout**: Grid de múltiples gráficos. **Recharts**: Librería gráficos responsivos. **Multiple Metrics**: KPI cards + charts. **Filtering**: Por rango fechas. **Export**: PDF y CSV. **Real-time**: Datos backend actualizados.

**Componentes Afectados:**

- **Dashboard.jsx (Nuevo):** Layout principal. Grid de componentes. Renderiza: KPI cards, gráficos, tabla top potreros. DateRange filter en top.

- **UtilizationChart.jsx (Nuevo):** Gráfico barras horizontales. Recharts BarChart. X: % utilización, Y: potreros. Colores por uso. Top 10.

- **AvailabilityChart.jsx (Nuevo):** Gráfico línea temporal. Recharts LineChart. X: fechas, Y: % disponible. Media en línea punteada.

- **OperationsChart.jsx (Nuevo):** Gráfico barras apiladas. Usuarios vs operaciones por tipo. Recharts BarChart stacked.

- **ETADistributionChart.jsx (Nuevo):** Gráfico distribución ETA. Histograma o pie. Rangos: 1-7d, 8-14d, etc.

- **ReportExporter.js (Nuevo):** PDF y CSV generation. jsPDF para PDF. Papa Parse para CSV. Exporta tablas y gráficos.

- **useStatistics.js (Nuevo - Hook):** Obtiene datos. Filtro por fecha. GET `/farms/{farmId}/statistics?startDate=...&endDate=...`. Retorna: metrics, charts data.

**Hitos:**
1. useStatistics.js (data fetching)
2. UtilizationChart + AvailabilityChart + OperationsChart (charts)
3. ReportExporter.js (export)
4. Dashboard.jsx (main layout)
5. Tests + performance

### Validación de Impacto

✅ **Dashboard Pattern**: Standard UI layout
✅ **Multiple Metrics**: Visibilidad completa
✅ **Filtering**: Análisis flexible
✅ **Export**: CSV + PDF
✅ **Real-time**: Datos backend

### Referencias y Validación

**Historias Relacionadas:**
- ✅ PASTURES-HU-012: Eventos (datos)
- ✅ PASTURES-HU-013: Auditoría (reportes auditoría)
- → PASTURES-HU-016: Estadísticas (esta)

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** Dashboard analytics + export (business intelligence)

---

## ✅ **Definición de Completado**
- [ ] Tests componentes: todos ACs
- [ ] Accesibilidad completa
- [ ] Code review aprobado
- [ ] CI/CD green

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
