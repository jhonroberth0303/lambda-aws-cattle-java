# 🚀 Guía de Implementación: Milking

**Fecha**: 2026-01-09

## 🎯 Objetivo

Guía para mejorar y completar el módulo Milking con 15 tareas priorizadas.

---

## 📚 Tabla de Contenidos

1. [Estado Actual](#estado-actual)
2. [Tareas P0 (Críticas)](#tareas-p0-críticas)
3. [Tareas P1 (Importantes)](#tareas-p1-importantes)
4. [Tareas P2 (Deseables)](#tareas-p2-deseables)
5. [Roadmap](#roadmap)

---

## Estado Actual

### ✅ Implementado (70%)

| Componente | Status | Notas |
|-----------|--------|-------|
| DynamoDB | ✅ | Tabla correcta, claves correctas |
| Controller | ✅ | POST, GET funcionales |
| Service | ✅ | Básico pero funciona |
| Repository | ✅ | Query, save funcionales |
| Frontend page | ✅ | Estructura OK |
| Formulario | ✅ | Todos los campos presentes |
| Tabla | ✅ | Agrupa por fecha correctamente |
| Hooks | ✅ | useMilkingRecords, useMilkingForm |

### ⚠️ A Mejorar (20%)

- Validaciones incompletas
- Error handling básico
- Loading states
- Confirmaciones

### ❌ No Implementado (10%)

- Editar/eliminar registros
- Búsqueda avanzada
- Reportes
- Gráficos
- Alertas

---

## Tareas P0 (Críticas)

### P0#1: Mejorar Validaciones Backend

**Descripción**: Agregar validaciones completas en MilkingProcessor.

**Tiempo**: 1-2 horas

**Código**:

```java
// MilkingProcessor.java
private static void setPkSk(FarmMilking entity) {
    Integer bovineId = entity.getBovineId();
    String date = entity.getDate();
    String shift = entity.getShift();

    // ✅ Validar bovineId
    if (bovineId == null || bovineId <= 0) {
        throw new IllegalArgumentException(
            "El campo bovineId es obligatorio y debe ser mayor a 0."
        );
    }

    // ✅ Validar date
    if (date == null || date.isEmpty()) {
        throw new IllegalArgumentException(
            "El campo date es obligatorio."
        );
    }

    // ✅ Validar formato de date (YYYY-MM-DD)
    try {
        date = java.time.LocalDate.parse(
            date, 
            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        ).toString();
    } catch (java.time.format.DateTimeParseException e) {
        throw new IllegalArgumentException(
            "El campo date debe tener el formato YYYY-MM-DD (ej: 2025-12-10)."
        );
    }

    // ✅ Validar shift (AM o PM)
    if (shift == null || shift.isEmpty()) {
        throw new IllegalArgumentException(
            "El campo shift es obligatorio (AM o PM)."
        );
    }
    if (!shift.equals("AM") && !shift.equals("PM")) {
        throw new IllegalArgumentException(
            "El campo shift debe ser 'AM' o 'PM'."
        );
    }

    // ✅ NUEVO: Validar liters > 0 (si está presente)
    Double liters = entity.getLiters();
    if (liters != null && liters <= 0) {
        throw new IllegalArgumentException(
            "El campo liters debe ser mayor a 0 (ej: 15.5)."
        );
    }

    // ✅ NUEVO: Validar status en enum
    String status = entity.getStatus();
    if (status != null && !isValidStatus(status)) {
        throw new IllegalArgumentException(
            "El campo status debe ser 'completo', 'parcial' u 'omitido'."
        );
    }

    // Generar PK y SK
    entity.setPK(PK_PREFIX + bovineId);
    entity.setSK(SK_PREFIX + date + HASH_TAG + shift);
    entity.setCreatedAt(java.time.Instant.now().toString());
}

// ✅ NUEVO: Helper para validar status
private static boolean isValidStatus(String status) {
    return status.equals("completo") || 
           status.equals("parcial") || 
           status.equals("omitido");
}
```

### P0#2: Mostrar Errores en Frontend

**Descripción**: Capturar y mostrar errores de validación en la UI.

**Tiempo**: 1-2 horas

**Código**:

```jsx
// MilkingAdd.jsx
export default function MilkingAdd({ form, onChange, onSubmit }) {
  const [open, setOpen] = useState(false);
  const [error, setError] = useState(null);      // ✅ NUEVO
  const [loading, setLoading] = useState(false); // ✅ NUEVO

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      // Validaciones frontend
      if (!form.bovineId) {
        throw new Error("Selecciona un bovino");
      }
      if (!form.date) {
        throw new Error("Ingresa la fecha");
      }
      if (!form.liters) {
        throw new Error("Ingresa los litros");
      }
      if (parseFloat(form.liters) <= 0) {
        throw new Error("Los litros deben ser mayor a 0");
      }

      // Llamar onSubmit original
      await onSubmit(e);
      
      // Limpiar y contraer
      setOpen(false);
      
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="milkingRecord-add-accordion">
      <button className="button-milkingRecord" onClick={() => setOpen(!open)}>
        Nuevo registro
      </button>

      {/* ✅ NUEVO: Mostrar error */}
      {error && (
        <div className="error-banner">
          ⚠️ {error}
        </div>
      )}

      <div className={`milkingRecord-add-panel${open ? " open" : ""}`} ...>
        <form onSubmit={handleSubmit}>
          {/* ... campos del formulario ... */}
          
          {/* ✅ NUEVO: Desabilitar mientras se guarda */}
          <Button 
            variant="primary" 
            type="submit"
            disabled={loading}>
            {loading ? "Guardando..." : "Guardar"}
          </Button>
        </form>
      </div>
    </div>
  );
}
```

### ✅ Checklist P0

- [ ] Validar liters > 0 en backend
- [ ] Validar status enum en backend
- [ ] Mostrar errores en frontend
- [ ] Desabilitar botón mientras se guarda
- [ ] Loading state en formulario
- [ ] Tests de validación

---

## Tareas P1 (Importantes)

### P1#1: Implementar Editar Registro

**Descripción**: Agregar endpoint PUT para actualizar registros.

**Tiempo**: 3-4 horas

**Pasos**:

1. Crear `PUT /milkingRecord/{idBovine}/{date}/{shift}`
2. Actualizar MilkingController, Service, Repository
3. Conectar botón "Editar" en tabla
4. Crear modal/formulario de edición
5. Tests

**Código Backend Inicial**:

```java
// MilkingController.java
@PutMapping("/{idBovine}/{date}/{shift}")
public ResponseEntity<MilkingDTO> updateMilking(
    @PathVariable Integer idBovine,
    @PathVariable String date,
    @PathVariable String shift,
    @RequestBody MilkingDTO milkingDTO) {
    
    return milkingProcessor.updateMilking(idBovine, date, shift, milkingDTO)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

### P1#2: Implementar Eliminar Registro

**Descripción**: Agregar endpoint DELETE para eliminar registros.

**Tiempo**: 2-3 horas

**Pasos**:

1. Crear `DELETE /milkingRecord/{idBovine}/{date}/{shift}`
2. Agregar método a MilkingRepository (DeleteItem)
3. Conectar botón "Eliminar" en tabla
4. Pedir confirmación antes de eliminar
5. Tests

**Código Backend Inicial**:

```java
// MilkingController.java
@DeleteMapping("/{idBovine}/{date}/{shift}")
public ResponseEntity<Void> deleteMilking(
    @PathVariable Integer idBovine,
    @PathVariable String date,
    @PathVariable String shift) {
    
    milkingProcessor.deleteMilking(idBovine, date, shift);
    return ResponseEntity.noContent().build();
}
```

---

### P1#3: Mejorar Búsqueda (por rango de fechas)

**Descripción**: Agregar filtro por rango de fechas.

**Tiempo**: 2-3 horas

**Query Endpoint**:

```
GET /milkingRecord/5?fromDate=2025-12-01&toDate=2025-12-31&shift=AM
```

**Backend**:

```java
@GetMapping("/{idBovine}")
public ResponseEntity<List<MilkingDTO>> milkingData(
    @PathVariable Integer idBovine,
    @RequestParam(required = false) String fromDate,
    @RequestParam(required = false) String toDate,
    @RequestParam(required = false) String shift) {
    
    return milkingProcessor.getMilkingData(idBovine, fromDate, toDate, shift)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

### P1#4: Exportar CSV

**Descripción**: Generar archivo CSV con registros.

**Tiempo**: 3-4 horas

**Frontend**:

```jsx
// Botón en MilkingTable
<Button onClick={handleExportCSV}>Descargar CSV</Button>

// Handler
const handleExportCSV = () => {
  const csvContent = "data:text/csv;charset=utf-8," + 
    "Fecha,AM (L),Estado AM,PM (L),Estado PM,Total (L)\n" +
    rows.map(r => {
      const am = r.AM?.liters ?? "";
      const pm = r.PM?.liters ?? "";
      const total = (r.AM?.liters ?? 0) + (r.PM?.liters ?? 0);
      return `${r.date},${am},${r.AM?.status ?? ""},${pm},${r.PM?.status ?? ""},${total}`;
    }).join("\n");
  
  const link = document.createElement("a");
  link.href = encodeURI(csvContent);
  link.download = `milking_${bovineId}.csv`;
  link.click();
};
```

---

### P1#5: Tests Unitarios

**Descripción**: Agregar tests para validaciones.

**Tiempo**: 2-3 horas

**Ejemplos**:

```java
@Test
public void testCreateMilkingWithInvalidBovineId() {
    MilkingDTO dto = new MilkingDTO();
    dto.setBovineId(-1);  // ❌ Inválido
    
    assertThrows(IllegalArgumentException.class, () -> {
        milkingProcessor.createMilking(dto);
    });
}

@Test
public void testCreateMilkingWithInvalidDate() {
    MilkingDTO dto = new MilkingDTO();
    dto.setBovineId(5);
    dto.setDate("25/12/2025");  // ❌ Formato incorrecto
    
    assertThrows(IllegalArgumentException.class, () -> {
        milkingProcessor.createMilking(dto);
    });
}

@Test
public void testCreateMilkingWithInvalidShift() {
    MilkingDTO dto = new MilkingDTO();
    dto.setBovineId(5);
    dto.setDate("2025-12-10");
    dto.setShift("NOON");  // ❌ No es AM ni PM
    
    assertThrows(IllegalArgumentException.class, () -> {
        milkingProcessor.createMilking(dto);
    });
}
```

---

## Tareas P2 (Deseables)

### P2#1: Gráfico de Tendencia

**Descripción**: Mostrar gráfico de producción lechera por día.

**Tiempo**: 4-5 horas

**Herramientas**: Chart.js, Recharts, o similar

**Ejemplo**:

```jsx
import { LineChart, Line, XAxis, YAxis } from "recharts";

const MilkingChart = ({ records }) => {
  const data = records
    .reduce((acc, r) => {
      let day = acc.find(d => d.date === r.date);
      if (!day) {
        day = { date: r.date, total: 0 };
        acc.push(day);
      }
      day.total += r.liters || 0;
      return acc;
    }, [])
    .sort((a, b) => new Date(a.date) - new Date(b.date));

  return (
    <LineChart width={600} height={300} data={data}>
      <XAxis dataKey="date" />
      <YAxis />
      <Line type="monotone" dataKey="total" stroke="#8884d8" />
    </LineChart>
  );
};
```

---

### P2#2: Alertas Automáticas

**Descripción**: Mostrar alertas si producción es baja.

**Tiempo**: 2-3 horas

**Lógica**:

```javascript
function generateAlerts(records, bovineId) {
  const alerts = [];
  
  // Obtener últimos 7 días
  const last7days = records.slice(-14); // AM + PM = 14 registros
  
  // Calcular promedio diario
  const dailyTotals = groupByDate(last7days);
  const average = dailyTotals.reduce((sum, day) => {
    const total = (day.AM?.liters ?? 0) + (day.PM?.liters ?? 0);
    return sum + total;
  }, 0) / dailyTotals.length;
  
  // Última producción
  const lastDay = dailyTotals[dailyTotals.length - 1];
  const lastTotal = (lastDay.AM?.liters ?? 0) + (lastDay.PM?.liters ?? 0);
  
  // Reglas de alerta
  if (lastTotal < average * 0.8) {
    alerts.push({
      type: "warning",
      text: `⚠️ Bovino #${bovineId}: Producción ${(lastTotal / average * 100).toFixed(0)}% del promedio`
    });
  }
  
  if (lastTotal === 0) {
    alerts.push({
      type: "error",
      text: `🔴 Bovino #${bovineId}: Sin registro hoy`
    });
  }
  
  return alerts;
}
```

---

### P2#3: Reportes Diarios

**Descripción**: Reporte agregado de producción de todos los bovinos.

**Tiempo**: 5-6 horas

**Datos necesarios**:

- Total producción del día
- Por bovino
- Promedio
- Máximo/Mínimo

---

## Tareas P2 (Deseables)

### P2#4: Machine Learning para Predicción

**Descripción**: Predecir producción futura basado en histórico.

**Tiempo**: 8-10 horas

**Algoritmo simple**:

```javascript
function predictNextDay(records) {
  // Obtener últimos 30 días
  const last30 = records.slice(-60); // 30 días × 2 turnos
  
  // Calcular promedio móvil
  const movingAvg = calculateMovingAverage(last30, 7);
  
  // Predicción simple: promedio + tendencia
  const trend = movingAvg[movingAvg.length - 1] - movingAvg[0];
  const predicted = movingAvg[movingAvg.length - 1] + trend;
  
  return predicted;
}
```

---

## Roadmap

### Fase 1: Validaciones y Errores (Semana 1)

**Tareas**: P0#1, P0#2

**Horas**: 2-4

**Objetivo**: Backend robusto, UI con error handling

---

### Fase 2: CRUD Completo (Semanas 2-3)

**Tareas**: P1#1, P1#2

**Horas**: 6-8

**Objetivo**: Poder editar/eliminar registros

---

### Fase 3: Búsqueda y Exportación (Semana 3)

**Tareas**: P1#3, P1#4, P1#5

**Horas**: 7-9

**Objetivo**: Búsqueda avanzada, CSV, tests

---

### Fase 4: Reportes y Gráficos (Semanas 4-5)

**Tareas**: P2#1, P2#2, P2#3

**Horas**: 11-14

**Objetivo**: Dashboard con insights

---

### Fase 5: IA (Semana 6+)

**Tareas**: P2#4

**Horas**: 8-10

**Objetivo**: Predicciones automáticas

---

## 📊 Resumen de Esfuerzo

| Fase | Tareas | Horas | Semanas |
|------|--------|-------|---------|
| **P0** | #1, #2 | 2-4 | 1 |
| **P1** | #1-5 | 10-16 | 2-3 |
| **P2** | #1-3 | 11-14 | 2-3 |
| **Futuro** | #4 | 8-10 | 1+ |
| **TOTAL** | 15+ | 31-44 | 6-7 semanas |

---

**Generado**: 2026-01-09 | **Versión**: 1.0
