# 🌱 PASTURES-HU#21: Frontend: Exportar CSV/Excel

**Fecha**: 2026-01-09 | **Versión**: 1.0 | **Prioridad**: 🟢 BAJO (P3) | **Estado**: Analizado (Arquitecto) ✅

---

## 📊 **Registro de Cambios**

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-01-09 | 1.1 | Análisis arquitectónico completado - File Export + Data Transformation | jhon.fernandez | Arquitecto |
| 2026-01-09 | 1.0 | Historia creada por Product Owner | Technical Team | PO |

---

## 📝 **Descripción de la Historia**

Como **usuario operario o gerente**, quiero exportar datos de potreros a CSV y Excel, de tal forma que:

1. Pueda descargar listado de potreros como CSV
2. Pueda descargar listado como Excel (.xlsx)
3. Incluya filtros aplicados (fecha, estado, etc)
4. El archivo tenga formato profesional
5. Los nombres de columnas sean claros
6. Se puede compartir con otros usuarios
7. Se puede abrir en Excel, Google Sheets, etc

Esto habilitará que usuarios compartan datos, analicen en Excel, envíen reportes a gerencia, etc.

---

## 🎯 **Criterios de Aceptación**

### AC#1: Botón Descargar CSV
```gherkin
Scenario: Descargar datos como CSV
  Given usuario en página de potreros
  And lista de 25 potreros visible
  When hace click en "📥 Descargar CSV"
  Then:
    [ ] Se descarga archivo: pastures-2026-01-09.csv
    [ ] Archivo contiene todas las columnas
    [ ] Formato CSV válido (comma-separated)
    [ ] Encoding UTF-8
    [ ] Sin corrupción de caracteres españoles (ñ, á, é, etc)
    [ ] Tamaño razonable (< 1MB para 1000 registros)
```

### AC#2: Botón Descargar Excel
```gherkin
Scenario: Descargar datos como Excel
  Given usuario en página de potreros
  And lista de 25 potreros visible
  When hace click en "📊 Descargar Excel"
  Then:
    [ ] Se descarga archivo: pastures-2026-01-09.xlsx
    [ ] Archivo abre en Excel/LibreOffice/Google Sheets
    [ ] Formato profesional con colores de encabezado
    [ ] Ancho de columnas automático
    [ ] Sin errores
    [ ] Tamaño razonable (< 2MB para 1000 registros)
```

### AC#3: Columnas en Exportación
```gherkin
Scenario: Datos completos en archivo descargado
  Given exportación a CSV/Excel
  When se abre archivo
  Then contiene columnas:
    [ ] ID Potrero (P001, P002, etc)
    [ ] Nombre
    [ ] Descripción
    [ ] Área (hectáreas)
    [ ] Carga de Animales
    [ ] Estado (DISPONIBLE, EN_USO, etc)
    [ ] ETA (días)
    [ ] Último Cambio (fecha)
    [ ] Responsable (usuario)
    [ ] Fecha Creación
    [ ] Observaciones (si aplica)
```

### AC#4: Respetar Filtros
```gherkin
Scenario: Exportación incluye solo datos filtrados
  Given página con potreros filtrados
  When usuario aplica filtro: status=EN_DESCANSO
  And hace click "Descargar CSV"
  Then:
    [ ] CSV contiene solo potreros EN_DESCANSO
    [ ] No incluye otros estados
    [ ] Nota: "Filtro aplicado: EN_DESCANSO"
    [ ] Se conservan otros filtros (fechas, etc)
```

### AC#5: Incluir Rango de Fechas
```gherkin
Scenario: Exportación con rango de fechas
  Given filtro de fechas: 2026-01-01 a 2026-01-09
  When exporta
  Then:
    [ ] CSV/Excel solo contiene registros en ese rango
    [ ] Nombre archivo incluye rango: pastures-2026-01-01_to_2026-01-09.csv
    [ ] Encabezado indica: "Período: 2026-01-01 a 2026-01-09"
```

### AC#6: Nombre de Archivo Inteligente
```gherkin
Scenario: Nombre archivo descriptivo
  Given múltiples exportaciones en mismo día
  When descarga CSV
  Then:
    [ ] Nombre: pastures-{YYYY-MM-DD}_{HH-MM-SS}.csv
    [ ] Ejemplo: pastures-2026-01-09_15-30-45.csv
    [ ] Fácil de identificar qué descarga es cuál
    [ ] Cuando hay filtros: pastures-filtered-{date}.csv
```

### AC#7: Encabezado de Información
```gherkin
Scenario: Encabezado con metadatos
  Given archivo CSV/Excel descargado
  When se abre
  Then primera fila (o filas) contiene:
    [ ] Título: "Reporte de Potreros"
    [ ] Fecha descarga
    [ ] Finca/Usuario
    [ ] Filtros aplicados (si hay)
    [ ] Cantidad total de registros
    [ ] Línea en blanco antes de datos
```

### AC#8: Formato de Números
```gherkin
Scenario: Números formateados correctamente
  Given datos numéricos en exportación
  Then:
    [ ] Área: 5.5 ha (decimal con punto)
    [ ] Carga de animales: 20 (sin decimales)
    [ ] Porcentajes: 85.5% (con símbolo %)
    [ ] Fechas: 2026-01-09 (ISO 8601)
    [ ] Moneda (si aplica): $1,250.00
```

### AC#9: Formato Excel Profesional
```gherkin
Scenario: Excel con formato visual
  Given archivo Excel descargado
  When se abre en Excel
  Then:
    [ ] Encabezado con fondo gris y texto blanco
    [ ] Filas alternadas (blanco/gris claro)
    [ ] Ancho de columnas automático (auto-fit)
    [ ] Bordes en celdas
    [ ] Fuente legible (calibri o similar)
    [ ] Números alineados a derecha
    [ ] Texto alineado a izquierda
    [ ] Totales al final (si aplica)
```

### AC#10: Performance
```gherkin
Scenario: Exportación rápida
  Given 1000 potreros en lista
  When usuario hace click "Descargar"
  Then:
    [ ] Descarga comienza en < 1 segundo
    [ ] Archivo generado en < 500ms
    [ ] No bloquea UI
    [ ] Se muestra spinner/progreso
    [ ] Sin errores de memoria
```

### AC#11: Validación de Datos
```gherkin
Scenario: Sin datos corruptos en exportación
  Given potreros con caracteres especiales
  When se exporta
  Then:
    [ ] Caracteres españoles intactos (ñ, á, é, ü, ç)
    [ ] Comillas en descripciones manejadas
    [ ] Saltos de línea preservados (sin quebrar CSV)
    [ ] Caracteres Unicode funcionan
    [ ] Sin caracteres ocultos
```

### AC#12: Opción de Todas las Columnas
```gherkin
Scenario: Incluir/excluir columnas
  Given opciones de exportación
  When usuario puede seleccionar qué columnas incluir
  Then:
    [ ] Checkbox para cada columna
    [ ] Default: columnas principales
    [ ] Opción "Todas" y "Ninguna"
    [ ] Persistir selección
    [ ] Aplicar a CSV y Excel
```

### AC#13: Exportar Gráficos (Excel Bonus)
```gherkin
Scenario: Incluir gráficos en Excel
  Given datos de potreros
  When exporta a Excel
  Then:
    [ ] Hoja 1: datos tabulares
    [ ] Hoja 2: Gráfico de pastel (Estado)
    [ ] Hoja 3: Gráfico de barras (Utilización)
    [ ] Gráficos dinámicos y enlazados a datos
```

### AC#14: Móvil Compatible
```gherkin
Scenario: Descargar desde móvil
  Given usuario en móvil/tablet
  When hace click en "Descargar"
  Then:
    [ ] Descarga funciona
    [ ] Archivo se guarda en descargas
    [ ] Botón claramente visible
    [ ] Sin errores de responsividad
```

### AC#15: Accesibilidad
```gherkin
Scenario: Exportación accesible
  Given botones de descarga
  Then:
    [ ] Botones con ARIA labels
    [ ] Atajos de teclado (si aplica)
    [ ] Screen reader friendly
    [ ] Suficiente contraste
    [ ] Tamaño botón >= 44px (móvil)
```

---

## 📊 **Especificación Técnica**

### Instalación de Dependencias

#### package.json (Frontend)
```json
{
  "dependencies": {
    "papaparse": "^5.4.1",
    "xlsx": "^0.18.5",
    "file-saver": "^2.0.5"
  }
}
```

### Componente de Exportación

#### ExportButton.jsx
```javascript
import React, { useState } from 'react';
import Papa from 'papaparse';
import XLSX from 'xlsx';
import { saveAs } from 'file-saver';

export function ExportButton({ data, fileName = 'export' }) {
  const [isLoading, setIsLoading] = useState(false);
  const [selectedColumns, setSelectedColumns] = useState(
    ['id', 'name', 'status', 'eta', 'createdAt']
  );
  
  const columns = [
    { key: 'id', label: 'ID Potrero' },
    { key: 'name', label: 'Nombre' },
    { key: 'description', label: 'Descripción' },
    { key: 'areHa', label: 'Área (ha)' },
    { key: 'animalLoad', label: 'Carga Animales' },
    { key: 'status', label: 'Estado' },
    { key: 'eta', label: 'ETA (días)' },
    { key: 'updatedAt', label: 'Último Cambio' },
    { key: 'createdAt', label: 'Fecha Creación' },
  ];
  
  const formatValue = (value, key) => {
    if (value === null || value === undefined) return '';
    
    if (key === 'createdAt' || key === 'updatedAt') {
      return new Date(value).toLocaleDateString('es-ES');
    }
    if (key === 'areHa') {
      return parseFloat(value).toFixed(2);
    }
    if (key === 'status') {
      const statusMap = {
        'DISPONIBLE': 'Disponible',
        'EN_USO': 'En uso',
        'EN_DESCANSO': 'En descanso',
        'MANTENIMIENTO': 'Mantenimiento',
        'REMOVED': 'Eliminado'
      };
      return statusMap[value] || value;
    }
    
    return value;
  };
  
  const prepareData = () => {
    return data.map(item => {
      const row = {};
      selectedColumns.forEach(colKey => {
        const column = columns.find(c => c.key === colKey);
        if (column) {
          row[column.label] = formatValue(item[colKey], colKey);
        }
      });
      return row;
    });
  };
  
  const exportToCSV = () => {
    setIsLoading(true);
    try {
      const csvData = prepareData();
      const csv = Papa.unparse(csvData);
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      
      const timestamp = new Date().toISOString().split('T')[0];
      link.setAttribute('href', url);
      link.setAttribute('download', `${fileName}-${timestamp}.csv`);
      link.style.visibility = 'hidden';
      
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error('Error exporting CSV:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const exportToExcel = () => {
    setIsLoading(true);
    try {
      const excelData = prepareData();
      const worksheet = XLSX.utils.json_to_sheet(excelData);
      
      // Formatear ancho de columnas
      const columnWidths = selectedColumns.map(() => 15);
      worksheet['!cols'] = columnWidths.map(w => ({ wch: w }));
      
      // Formatear encabezado
      const range = XLSX.utils.decode_range(worksheet['!ref']);
      for (let col = range.s.c; col <= range.e.c; col++) {
        const cell = worksheet[XLSX.utils.encode_cell({ r: 0, c: col })];
        if (cell) {
          cell.s = {
            font: { bold: true, color: { rgb: 'FFFFFF' } },
            fill: { fgColor: { rgb: '366092' } },
            alignment: { horizontal: 'center', vertical: 'center' }
          };
        }
      }
      
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, 'Potreros');
      
      const timestamp = new Date().toISOString().split('T')[0];
      XLSX.writeFile(workbook, `${fileName}-${timestamp}.xlsx`);
    } catch (error) {
      console.error('Error exporting Excel:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <div className="export-buttons">
      <button
        onClick={exportToCSV}
        disabled={isLoading || !data.length}
        className="btn btn-primary"
      >
        {isLoading ? '⏳ Exportando...' : '📥 CSV'}
      </button>
      
      <button
        onClick={exportToExcel}
        disabled={isLoading || !data.length}
        className="btn btn-primary"
      >
        {isLoading ? '⏳ Exportando...' : '📊 Excel'}
      </button>
    </div>
  );
}
```

### Integración en PaddockPage

#### PaddockPage.jsx
```javascript
export function PaddockPage() {
  const [pastures, setPastures] = useState([]);
  const [filteredPastures, setFilteredPastures] = useState([]);
  const [filters, setFilters] = useState({});
  
  const handleExport = (format) => {
    const dataToExport = filteredPastures.length > 0 ? filteredPastures : pastures;
    return dataToExport;
  };
  
  return (
    <div className="paddock-page">
      <div className="toolbar">
        <h1>Potreros</h1>
        
        <div className="toolbar-actions">
          <FilterPanel
            onFilter={(newFilters) => {
              setFilters(newFilters);
              applyFilters(pastures, newFilters);
            }}
          />
          
          <ExportButton
            data={handleExport()}
            fileName="pastures"
          />
          
          <StatsButton />
        </div>
      </div>
      
      <PasturesList pastures={filteredPastures} />
    </div>
  );
}
```

### Estilos

#### export.css
```css
.export-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.export-buttons button {
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.export-buttons button:hover:not(:disabled) {
  background: #f0f0f0;
  border-color: #999;
}

.export-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.export-buttons button.btn-primary {
  background: #0066cc;
  color: white;
  border-color: #0066cc;
}

.export-buttons button.btn-primary:hover:not(:disabled) {
  background: #0052a3;
  border-color: #0052a3;
}

/* Responsive */
@media (max-width: 768px) {
  .export-buttons {
    flex-wrap: wrap;
  }
  
  .export-buttons button {
    padding: 8px 12px;
    font-size: 12px;
  }
}
```

### Servicio de Exportación

#### exportService.js
```javascript
import Papa from 'papaparse';
import XLSX from 'xlsx';
import { saveAs } from 'file-saver';

export const exportService = {
  /**
   * Exportar datos a CSV
   */
  exportToCSV(data, fileName = 'export', columns = null) {
    const csvData = columns ? filterColumns(data, columns) : data;
    const csv = Papa.unparse(csvData);
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    
    const timestamp = new Date().toISOString().split('T')[0];
    saveAs(blob, `${fileName}-${timestamp}.csv`);
  },
  
  /**
   * Exportar datos a Excel
   */
  exportToExcel(data, fileName = 'export', options = {}) {
    const {
      columns = null,
      sheetName = 'Sheet1',
      includeCharts = false,
      title = null
    } = options;
    
    const excelData = columns ? filterColumns(data, columns) : data;
    const worksheet = XLSX.utils.json_to_sheet(excelData);
    
    // Formatear ancho de columnas
    worksheet['!cols'] = Object.keys(excelData[0] || {}).map(() => ({ wch: 15 }));
    
    // Formatear encabezado
    formatExcelHeader(worksheet);
    
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);
    
    const timestamp = new Date().toISOString().split('T')[0];
    XLSX.writeFile(workbook, `${fileName}-${timestamp}.xlsx`);
  },
  
  /**
   * Exportar con filtros aplicados
   */
  exportFiltered(data, filters, fileName = 'export') {
    const filtered = applyFilters(data, filters);
    return {
      exportToCSV: () => this.exportToCSV(filtered, `${fileName}-filtered`),
      exportToExcel: () => this.exportToExcel(filtered, `${fileName}-filtered`)
    };
  }
};

function filterColumns(data, columns) {
  return data.map(item => {
    const filtered = {};
    columns.forEach(col => {
      filtered[col.label] = item[col.key];
    });
    return filtered;
  });
}

function formatExcelHeader(worksheet) {
  const range = XLSX.utils.decode_range(worksheet['!ref']);
  for (let col = range.s.c; col <= range.e.c; col++) {
    const cell = worksheet[XLSX.utils.encode_cell({ r: 0, c: col })];
    if (cell) {
      cell.s = {
        font: { bold: true, color: { rgb: 'FFFFFF' } },
        fill: { fgColor: { rgb: '366092' } },
        alignment: { horizontal: 'center' }
      };
    }
  }
}

function applyFilters(data, filters) {
  return data.filter(item => {
    for (const [key, value] of Object.entries(filters)) {
      if (value && item[key] !== value) {
        return false;
      }
    }
    return true;
  });
}
```

---

## 🛠️ **Componentes a Crear/Modificar**

### Nuevos Archivos

1. **`ExportButton.jsx`** - Componente de botones
2. **`exportService.js`** - Servicio de exportación
3. **`export.css`** - Estilos
4. **`ExportButton.test.jsx`** - Tests

### Archivos a Modificar

1. **`package.json`** - Agregar dependencias (papaparse, xlsx, file-saver)
2. **`PaddockPage.jsx`** - Integrar ExportButton
3. **`SearchBar.jsx`** o similar - Agregar botones de exportación

---

## 📋 **Lógica de Implementación Paso a Paso**

### Paso 1: Instalar Dependencias
```bash
npm install papaparse xlsx file-saver
```

### Paso 2: Crear ExportButton
- Componente React
- Estados y handlers
- Botones CSV y Excel

### Paso 3: Crear exportService
- Funciones de exportación
- Formateo de datos
- Manejo de archivos

### Paso 4: Integrar en PaddockPage
- Importar ExportButton
- Pasar datos (potreros)
- Manejar filtros

### Paso 5: Estilos
- CSS responsive
- Botones visibles
- Iconos/emojis

### Paso 6: Testing
- Tests unitarios
- Tests integración

---

## 🔧 **Refinamiento Técnico**

### CSV Exporter

```javascript
export const csvExporter = {
  export: (data, filename) => {
    const csv = convertToCSV(data);
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
  },
  
  convertToCSV: (data) => {
    const headers = Object.keys(data[0]);
    const rows = data.map(obj => 
      headers.map(h => `"${obj[h]}"`).join(',')
    );
    return [headers.join(','), ...rows].join('\n');
  }
};
```

### Excel Exporter (with Styling)

```javascript
import XLSX from 'xlsx';

export const excelExporter = {
  export: (data, filename) => {
    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    
    // Styling
    worksheet['!cols'] = [{wch: 15}, {wch: 25}, {wch: 10}];
    
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Potreros');
    XLSX.writeFile(workbook, filename);
  }
};
```

### ExportButton Component

```javascript
export const ExportButton = ({ data, filters }) => {
  const handleExportCSV = () => {
    const filename = `potreros-${new Date().toISOString().split('T')[0]}.csv`;
    csvExporter.export(data, filename);
  };
  
  const handleExportExcel = () => {
    const filename = `potreros-${new Date().toISOString().split('T')[0]}.xlsx`;
    excelExporter.export(data, filename);
  };
  
  return (
    <div className="flex gap-2">
      <button onClick={handleExportCSV}>📥 Descargar CSV</button>
      <button onClick={handleExportExcel}>📊 Descargar Excel</button>
    </div>
  );
};
```

### Testing Strategy

**Tests Críticos:**
- CSV generado correctamente
- Excel abre en Excel/Sheets
- Caracteres especiales (ñ, á) intactos
- Filtros respetados en export
- Performance: export < 1s

---

## ✅ **Definición de Completado**

Para marcar esta HU como **DONE**:

- [ ] Dependencias instaladas
- [ ] ExportButton.jsx creado
- [ ] Botón CSV funciona
- [ ] Botón Excel funciona
- [ ] Nombres de archivos correctos
- [ ] Formatos de números correctos
- [ ] Caracteres especiales intactos
- [ ] Encabezados profesionales
- [ ] Excel con formato visual
- [ ] Filtros respetados
- [ ] Performance optimizado
- [ ] Móvil compatible
- [ ] Accesibilidad OK
- [ ] Tests unitarios >= 80%

---

## Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** File Export Pattern + Data Transformation + Streaming

**Justificación:** **File Export**: Generar archivos descargables. **Data Transformation**: Potreros → CSV/Excel. **Professional Format**: Excel con estilos. **Filtering**: Respetar filtros. **Performance**: Manejo datasets grandes. **User-friendly**: Nombres descriptivos.

**Componentes Afectados:**

- **ExportButton.jsx (Nuevo):** UI botones. Props: `data`, `format` (csv/excel). Renderiza 2 botones. onClick ejecuta exporter.

- **csvExporter.js (Nuevo):** Genera CSV. Función: `exportToCSV(data, filename)`. Formato: UTF-8, comma-separated. Headers + metadata.

- **excelExporter.js (Nuevo):** Genera XLSX. Librería: xlsx. Estilos: encabezado gris, bordes, auto-fit. Formato profesional.

- **dataTransformer.js (Nuevo):** Transforma potreros. Filtra por filtros aplicados. Formatea fechas/números. Añade metadatos.

- **fileDownloader.js (Nuevo):** Descarga file. Blob → download link. Genera filename con timestamp.

- **exportFormatters.js (Nuevo):** Formatos. Números, fechas, textos. Según locale.

**Hitos:**
1. dataTransformer.js (transformación)
2. csvExporter.js + excelExporter.js (exporters)
3. fileDownloader.js (descarga)
4. ExportButton.jsx (UI)
5. Tests + accessibility

### Validación de Impacto

✅ **Professional Export**: CSV + XLSX formateos
✅ **Filtering**: Respeta filtros aplicados
✅ **Performance**: Eficiente para 1000+ registros
✅ **User-friendly**: Nombres claros, metadatos

### Referencias y Validación

**Validado por:** jhon.fernandez | **Fecha:** 2026-01-09 | **Enfoque:** File export + data transformation

---

## ✅ **Definición de Completado**
- [ ] Tests integración pasando
- [ ] Sin warnings de console
- [ ] Code review aprobado
- [ ] CI/CD green

---

**Generado**: 2026-01-09 | **Versión**: 1.0 | **Autor**: Technical Team
