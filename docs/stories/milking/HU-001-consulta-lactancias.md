# HU-001: Consulta de Registros de Ordeño por Lactancia

## 📋 Información General

| Campo | Valor |
|-------|-------|
| **ID** | HU-001 |
| **Título** | Consulta de Registros de Ordeño por Lactancia |
| **Módulo** | Milking (Ordeño) |
| **Prioridad** | Alta |
| **Fecha Creación** | 2026-02-03 |
| **Estado** | Lista para Revisión |

---

## 📖 Historia de Usuario

**Como** productor ganadero  
**Quiero** consultar los registros de ordeño filtrados por número de lactancia  
**Para** analizar la producción de leche de cada lactancia de forma independiente y comparar el rendimiento entre lactancias

---

## 🎯 Criterios de Aceptación

### AC-01: Consulta de vacas con lactancias
- **Dado** que existen vacas con al menos una lactancia registrada
- **Cuando** el usuario accede al módulo de ordeño
- **Entonces** el sistema muestra un selector con las vacas que tienen lactancias

### AC-02: Consulta de lactancias por vaca
- **Dado** que el usuario selecciona una vaca
- **Cuando** se carga la información
- **Entonces** el sistema muestra un segundo selector con las lactancias disponibles (LACT#01, LACT#02, etc.)

### AC-03: Consulta de registros de ordeño filtrados
- **Dado** que el usuario selecciona una vaca y una lactancia
- **Cuando** ejecuta la consulta
- **Entonces** el sistema muestra solo los registros de ordeño correspondientes a esa lactancia

---

## 🔍 Análisis del Problema

### Situación Actual

1. **Modelo `bovineIdentityItem-lact`** (Lactancias):
```json
{
  "PK": "BOVINE#172",
  "SK": "LACT#01",
  "lactationNumber": 1,
  "startDate": "2025-11-27",
  "status": "OPEN",
  "GSI1PK": "LACT#OPEN",
  "GSI1SK": "2025-11-27#BOVINE#172"
}
```

2. **Modelo `milking-records`** (Registros de ordeño):
```json
{
  "PK": "BOVINE#172",
  "SK": "MILKING#2025-11-27#AM",
  "bovineId": 172,
  "date": "2025-11-27",
  "shift": "AM",
  "liters": 4.3
}
```

### Problema Identificado

- No existe relación entre `milking-records` y `bovineIdentityItem-lact`
- La consulta actual trae TODOS los registros de ordeño sin discriminar por lactancia
- El frontend no conoce las vacas con lactancias activas

---

## 📐 Diseño de Solución (Propuesta C: GSI)

### Nuevo Modelo de Datos

Agregar campos GSI al `milking-records`:

```json
{
  "PK": "BOVINE#172",
  "SK": "MILKING#2025-11-27#AM",
  "bovineId": 172,
  "lactationNumber": 1,
  "date": "2025-11-27",
  "shift": "AM",
  "liters": 4.3,
  "GSI2PK": "BOVINE#172#LACT#01",
  "GSI2SK": "2025-11-27#AM"
}
```

### Nuevos Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/milking/cows` | Lista vacas con lactancias activas + sus lactancias |
| GET | `/milking/{idBovine}/lactation/{lactationNumber}` | Registros de ordeño por lactancia |

---

## 📝 Tareas Técnicas

### FASE 1: Infraestructura DynamoDB

#### Tarea 1.1: Script de Actualización de Tabla
**Archivo:** `docs/scripts/update-milking-gsi.sh`

```bash
#!/bin/bash
# Script para agregar GSI2 a la tabla de milking-records

TABLE_NAME="cattle-milking-records"

aws dynamodb update-table \
  --table-name $TABLE_NAME \
  --attribute-definitions \
    AttributeName=GSI2PK,AttributeType=S \
    AttributeName=GSI2SK,AttributeType=S \
  --global-secondary-index-updates \
    "[{
      \"Create\": {
        \"IndexName\": \"GSI2-bovineIdentityItem-lactation-index\",
        \"KeySchema\": [
          {\"AttributeName\": \"GSI2PK\", \"KeyType\": \"HASH\"},
          {\"AttributeName\": \"GSI2SK\", \"KeyType\": \"RANGE\"}
        ],
        \"Projection\": {\"ProjectionType\": \"ALL\"},
        \"ProvisionedThroughput\": {
          \"ReadCapacityUnits\": 5,
          \"WriteCapacityUnits\": 5
        }
      }
    }]"

echo "GSI2 creado exitosamente"
```

#### Tarea 1.2: Script de Migración de Datos Existentes
**Archivo:** `docs/scripts/migrate-milking-records.py`

```python
# Script para agregar GSI2PK y GSI2SK a registros existentes
# Requiere: boto3, obtener lactationNumber del modelo bovineIdentityItem-lact

import boto3
from datetime import datetime

dynamodb = boto3.resource('dynamodb')
milking_table = dynamodb.Table('cattle-milking-records')
lact_table = dynamodb.Table('cattle-bovineIdentityItems')  # Donde están las lactancias

def get_lactation_for_date(bovine_id, date):
    """
    Obtiene el número de lactancia activa para un bovino en una fecha específica
    Consulta bovineIdentityItem-lact para encontrar la lactancia que cubra esa fecha
    """
    # Lógica: buscar lactancia donde startDate <= date y (endDate >= date OR status = OPEN)
    response = lact_table.query(
        KeyConditionExpression='PK = :pk AND begins_with(SK, :sk_prefix)',
        ExpressionAttributeValues={
            ':pk': f'BOVINE#{bovine_id}',
            ':sk_prefix': 'LACT#'
        }
    )
    
    for lact in response.get('Items', []):
        start_date = lact.get('startDate')
        end_date = lact.get('endDate')
        status = lact.get('status')
        
        if start_date <= date:
            if status == 'OPEN' or (end_date and end_date >= date):
                return lact.get('lactationNumber')
    
    return None

def migrate_records():
    """Migra todos los registros existentes agregando GSI2PK y GSI2SK"""
    scan_response = milking_table.scan()
    
    for item in scan_response.get('Items', []):
        bovine_id = item.get('bovineId')
        date = item.get('date')
        shift = item.get('shift')
        
        lact_number = get_lactation_for_date(bovine_id, date)
        
        if lact_number:
            lact_str = str(lact_number).zfill(2)
            gsi2pk = f"BOVINE#{bovine_id}#LACT#{lact_str}"
            gsi2sk = f"{date}#{shift}"
            
            milking_table.update_item(
                Key={'PK': item['PK'], 'SK': item['SK']},
                UpdateExpression='SET lactationNumber = :ln, GSI2PK = :gsi2pk, GSI2SK = :gsi2sk',
                ExpressionAttributeValues={
                    ':ln': lact_number,
                    ':gsi2pk': gsi2pk,
                    ':gsi2sk': gsi2sk
                }
            )
            print(f"Migrado: {item['PK']} - {item['SK']} -> Lactancia {lact_number}")

if __name__ == '__main__':
    migrate_records()
```

---

### FASE 2: Backend - Entidades y DTOs

#### Tarea 2.1: Actualizar Entidad MilkingRecord
**Archivo:** `src/main/java/com/cattle/entities/MilkingRecord.java`

**Cambios:**
- Agregar campo `lactationNumber` (Integer)
- Agregar campo `GSI2PK` (String)
- Agregar campo `GSI2SK` (String)
- Agregar anotaciones para el GSI

```java
@DynamoDbBean
public class MilkingRecord {
    // ... campos existentes ...
    
    private Integer lactationNumber;  // NUEVO: Número de lactancia (1, 2, 3...)
    private String GSI2PK;            // NUEVO: BOVINE#<id>#LACT#<nn>
    private String GSI2SK;            // NUEVO: YYYY-MM-DD#AM|PM
    
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI2-bovineIdentityItem-lactation-index")
    public String getGSI2PK() { return GSI2PK; }
    
    @DynamoDbSecondarySortKey(indexNames = "GSI2-bovineIdentityItem-lactation-index")
    public String getGSI2SK() { return GSI2SK; }
}
```

#### Tarea 2.2: Crear DTO CowWithLactationsDTO
**Archivo:** `src/main/java/com/cattle/dtos/CowWithLactationsDTO.java`

```java
package com.cattle.dtos;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class CowWithLactationsDTO {
    private Integer bovineId;
    private String bovineName;       // Opcional: nombre de la vaca
    private List<LactationSummaryDTO> lactations;
}
```

#### Tarea 2.3: Crear DTO LactationSummaryDTO
**Archivo:** `src/main/java/com/cattle/dtos/LactationSummaryDTO.java`

```java
package com.cattle.dtos;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class LactationSummaryDTO {
    private Integer lactationNumber;
    private String startDate;
    private String endDate;          // null si está OPEN
    private String status;           // OPEN | CLOSED
}
```

#### Tarea 2.4: Actualizar MilkingDTO
**Archivo:** `src/main/java/com/cattle/dtos/MilkingDTO.java`

**Cambios:**
- Agregar campo `lactationNumber` (Integer)

---

### FASE 3: Backend - Repository

#### Tarea 3.1: Crear Entidad BovineLactation
**Archivo:** `src/main/java/com/cattle/entities/BovineLactation.java`

```java
package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class BovineLactation {
    
    private String PK;              // BOVINE#<id>
    private String SK;              // LACT#<nn>
    private Integer lactationNumber;
    private String startDate;
    private String endDate;
    private String status;          // OPEN | CLOSED
    private String notes;
    private String GSI1PK;          // LACT#OPEN o LACT#CLOSED
    private String GSI1SK;          // startDate#BOVINE#<id>
    private String createdAt;
    private String updatedAt;
    
    @DynamoDbPartitionKey
    public String getPK() { return PK; }
    
    @DynamoDbSortKey
    public String getSK() { return SK; }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI1-lactation-status-index")
    public String getGSI1PK() { return GSI1PK; }
    
    @DynamoDbSecondarySortKey(indexNames = "GSI1-lactation-status-index")
    public String getGSI1SK() { return GSI1SK; }
}
```

#### Tarea 3.2: Crear LactationRepository
**Archivo:** `src/main/java/com/cattle/repository/LactationRepository.java`

```java
package com.cattle.repository;

public class LactationRepository {
    
    /**
     * Obtiene todas las lactancias con status OPEN usando GSI1
     * GSI1PK = "LACT#OPEN"
     */
    public Optional<List<BovineLactation>> getOpenLactations();
    
    /**
     * Obtiene todas las lactancias de un bovino
     * PK = BOVINE#<id>, SK begins_with "LACT#"
     */
    public Optional<List<BovineLactation>> getLactationsByBovine(Integer bovineId);
    
    /**
     * Obtiene una lactancia específica
     * PK = BOVINE#<id>, SK = LACT#<nn>
     */
    public Optional<BovineLactation> getLactation(Integer bovineId, Integer lactationNumber);
}
```

#### Tarea 3.3: Actualizar MilkingRepository
**Archivo:** `src/main/java/com/cattle/repository/MilkingRepository.java`

**Nuevo método:**
```java
/**
 * Obtiene registros de ordeño por bovino y lactancia usando GSI2
 * GSI2PK = BOVINE#<id>#LACT#<nn>
 */
public Optional<List<MilkingRecord>> getMilkingByBovineAndLactation(
    Integer bovineId, 
    Integer lactationNumber
) throws RepositoryException {
    String gsi2pk = String.format("BOVINE#%d#LACT#%02d", bovineId, lactationNumber);
    
    QueryConditional queryConditional = QueryConditional.keyEqualTo(
        Key.builder().partitionValue(gsi2pk).build()
    );
    
    DynamoDbIndex<MilkingRecord> index = table.index("GSI2-bovineIdentityItem-lactation-index");
    
    List<MilkingRecord> records = new ArrayList<>();
    for (MilkingRecord record : index.query(r -> r.queryConditional(queryConditional)).items()) {
        records.add(record);
    }
    
    return Optional.of(records);
}
```

---

### FASE 4: Backend - Services y Processors

#### Tarea 4.1: Crear LactationService
**Archivo:** `src/main/java/com/cattle/services/LactationService.java`

```java
package com.cattle.services;

@Service
public class LactationService {
    
    private final LactationRepository lactationRepository;
    
    /**
     * Obtiene vacas con lactancias activas (OPEN)
     */
    public Optional<List<BovineLactation>> getCowsWithOpenLactations();
    
    /**
     * Obtiene todas las lactancias de un bovino
     */
    public Optional<List<BovineLactation>> getLactationsByBovine(Integer bovineId);
    
    /**
     * Obtiene la lactancia activa de un bovino
     */
    public Optional<BovineLactation> getActiveLactation(Integer bovineId);
}
```

#### Tarea 4.2: Actualizar MilkingService
**Archivo:** `src/main/java/com/cattle/services/MilkingService.java`

**Nuevo método:**
```java
public Optional<List<MilkingRecord>> getMilkingByBovineAndLactation(
    Integer bovineId, 
    Integer lactationNumber
) {
    return milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber);
}
```

#### Tarea 4.3: Actualizar MilkingProcessor
**Archivo:** `src/main/java/com/cattle/processor/MilkingProcessor.java`

**Cambios:**
1. Inyectar `LactationService`
2. Actualizar `createMilking()` para incluir `lactationNumber` y generar GSI2
3. Agregar método `getCowsWithLactations()`
4. Agregar método `getLactationsByBovine(Integer bovineId)`
5. Agregar método `getMilkingByLactation(Integer bovineId, Integer lactationNumber)`

```java
// En setPkSk(), agregar:
private void setPkSk(MilkingRecord entity) {
    // ... código existente ...
    
    // NUEVO: Obtener lactancia activa y generar GSI2
    Integer lactNumber = entity.getLactationNumber();
    if (lactNumber == null) {
        // Obtener lactancia activa del bovino
        lactNumber = lactationService.getActiveLactation(bovineId)
            .map(BovineLactation::getLactationNumber)
            .orElseThrow(() -> new IllegalArgumentException(
                "El bovino no tiene una lactancia activa"));
        entity.setLactationNumber(lactNumber);
    }
    
    String lactStr = String.format("%02d", lactNumber);
    entity.setGSI2PK(PK_PREFIX + bovineId + "#LACT#" + lactStr);
    entity.setGSI2SK(date + HASH_TAG + shift);
}
```

---

### FASE 5: Backend - Controller

#### Tarea 5.1: Actualizar MilkingController
**Archivo:** `src/main/java/com/cattle/controller/MilkingController.java`

**Nuevos endpoints:**

```java
@Operation(
    summary = "Listar vacas con lactancias",
    description = "Obtiene la lista de vacas con lactancias activas, incluyendo el historial de lactancias de cada una"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Lista de vacas con lactancias obtenida exitosamente",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CowWithLactationsDTO.class))),
    @ApiResponse(responseCode = "404", description = "No se encontraron vacas con lactancias", content = @Content)
})
@GetMapping("/cows")
public ResponseEntity<List<CowWithLactationsDTO>> getCowsWithLactations() {
    return milkingProcessor.getCowsWithLactations()
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}

@Operation(
    summary = "Consultar ordeños por lactancia",
    description = "Obtiene los registros de ordeño de un bovino filtrados por número de lactancia"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registros de ordeño obtenidos exitosamente",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = MilkingDTO.class))),
    @ApiResponse(responseCode = "404", description = "No se encontraron registros de ordeño", content = @Content)
})
@GetMapping("/{idBovine}/lactation/{lactationNumber}")
public ResponseEntity<List<MilkingDTO>> getMilkingByLactation(
    @Parameter(description = "ID del bovino", required = true, example = "172")
    @PathVariable("idBovine") Integer idBovine,
    @Parameter(description = "Número de lactancia", required = true, example = "1")
    @PathVariable("lactationNumber") Integer lactationNumber,
    @Parameter(description = "Turno de ordeño (AM o PM)", required = false, example = "AM")
    @RequestParam(value = "shift", required = false) String shift
) {
    return milkingProcessor.getMilkingByLactation(idBovine, lactationNumber, shift)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

### FASE 6: Frontend

#### Tarea 6.1: Crear Servicio de Lactancias
**Archivo:** `cattle-front/src/services/lactationService.js`

```javascript
const BASE_URL = "https://44xpamzadd.execute-api.us-east-1.amazonaws.com/Prod";

/**
 * Obtiene vacas con lactancias activas y su historial de lactancias
 * @returns {Promise<Array<{bovineId, bovineName, lactations: Array<{lactationNumber, startDate, endDate, status}>}>>}
 */
export async function getCowsWithLactations() {
  const res = await fetch(`${BASE_URL}/milking/cows`);
  if (!res.ok) throw new Error("Error fetching cows with lactations");
  return res.json();
}

/**
 * Obtiene registros de ordeño por lactancia
 * @param {number} bovineId 
 * @param {number} lactationNumber 
 * @returns {Promise<Array>}
 */
export async function getMilkingByLactation(bovineId, lactationNumber) {
  const res = await fetch(`${BASE_URL}/milking/${bovineId}/lactation/${lactationNumber}`);
  if (!res.ok) throw new Error("Error fetching milking records");
  return res.json();
}
```

#### Tarea 6.2: Actualizar BovineSelect
**Archivo:** `cattle-front/src/components/Milking/BovineSelect.jsx`

```jsx
import React from "react";
import "./BovineSelect.css";

/**
 * Selector de vacas - recibe la lista de vacas desde el componente padre
 * Las vacas vienen del endpoint GET /milking/cows
 */
export default function BovineSelect({ 
  cows = [],
  value, 
  onChange, 
  name = "bovineId", 
  id = "bovineId",
  loading = false
}) {
  if (loading) {
    return (
      <select id={id} name={name} disabled className="bovineIdentityItem-select">
        <option>Cargando vacas...</option>
      </select>
    );
  }

  return (
    <select
      id={id}
      name={name}
      value={value}
      onChange={onChange}
      className="bovineIdentityItem-select"
    >
      <option value="">Seleccione vaca</option>
      {cows.map((cow) => (
        <option key={cow.bovineId} value={cow.bovineId}>
          {cow.bovineId} - {cow.bovineName || `Vaca ${cow.bovineId}`}
        </option>
      ))}
    </select>
  );
}
```

#### Tarea 6.3: Crear LactationSelect
**Archivo:** `cattle-front/src/components/Milking/LactationSelect.jsx`

```jsx
import React from "react";
import "./BovineSelect.css";

/**
 * Selector de lactancias - recibe lactancias desde el componente padre
 * Las lactancias vienen del endpoint GET /milking/cows junto con la vaca seleccionada
 */
export default function LactationSelect({ 
  lactations = [],
  value, 
  onChange, 
  name = "lactationNumber", 
  id = "lactationNumber",
  disabled = false
}) {
  if (disabled || lactations.length === 0) {
    return (
      <select id={id} name={name} disabled className="bovineIdentityItem-select">
        <option>{disabled ? "Seleccione vaca primero" : "Sin lactancias"}</option>
      </select>
    );
  }

  return (
    <select
      id={id}
      name={name}
      value={value}
      onChange={onChange}
      className="bovineIdentityItem-select"
    >
      <option value="">Todas las lactancias</option>
      {lactations.map((lact) => (
        <option key={lact.lactationNumber} value={lact.lactationNumber}>
          Lactancia {lact.lactationNumber} ({lact.status}) - {lact.startDate}
        </option>
      ))}
    </select>
  );
}
```

#### Tarea 6.4: Actualizar MilkingHooks.js
**Archivo:** `cattle-front/src/components/Milking/hooks/MilkingHooks.js`

```javascript
import { useState, useEffect } from "react";
import { getMilkingByBovineId, addMilkingRecord } from "../../../services/milkingService";
import { getCowsWithLactations, getMilkingByLactation } from "../../../services/lactationService";

/**
 * Hook para cargar vacas con lactancias (una sola llamada al endpoint)
 */
export function useCowsWithLactations() {
  const [cows, setCows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchCows() {
      try {
        const data = await getCowsWithLactations();
        setCows(data);
      } catch (err) {
        console.error("Error cargando vacas:", err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    fetchCows();
  }, []);

  return { cows, loading, error };
}

/**
 * Hook principal para manejar registros de ordeño
 */
export function useMilkingRecords(bovineIdFromProp) {
  const [bovineId, setBovineId] = useState(bovineIdFromProp || "");
  const [lactationNumber, setLactationNumber] = useState("");
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchData = async (id, lactNum = null) => {
    if (!id) return;
    setLoading(true);
    try {
      let data;
      if (lactNum) {
        // Consulta filtrada por lactancia
        data = await getMilkingByLactation(id, lactNum);
      } else {
        // Consulta todos los registros del bovino
        data = await getMilkingByBovineId(id);
      }
      setRecords(data);
    } catch (e) {
      console.error(e);
      alert("Error cargando registros de milking");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (bovineIdFromProp) {
      setBovineId(bovineIdFromProp);
      fetchData(bovineIdFromProp);
    }
  }, [bovineIdFromProp]);

  const onQuery = () => {
    if (!bovineId) return alert("Seleccione una vaca");
    fetchData(bovineId, lactationNumber || null);
  };

  return { 
    bovineId, 
    setBovineId, 
    lactationNumber,
    setLactationNumber,
    records, 
    loading, 
    fetchData, 
    onQuery 
  };
}

// useMilkingForm sin cambios...
export function useMilkingForm(bovineId, fetchData) {
  const [form, setForm] = useState({
    bovineId: "",
    date: "",
    shift: "AM",
    liters: "",
    status: "completo",
    observations: "",
    recordedBy: "jhonroberth"
  });

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      bovineId: Number(form.bovineId || bovineId)
    };
    if (!payload.bovineId || !payload.date || !payload.shift) {
      return alert("bovineId, date y shift son obligatorios");
    }
    try {
      await addMilkingRecord(payload);
      alert("Registro creado");
      fetchData(payload.bovineId);
      setForm(prev => ({ ...prev, liters: "", observations: "" }));
    } catch (err) {
      console.error(err);
      alert("Error creando registro");
    }
  };

  return { form, setForm, onChange, onSubmit };
}
```

#### Tarea 6.5: Actualizar MilkingTable.jsx
**Archivo:** `cattle-front/src/components/Milking/table/MilkingTable.jsx`

```jsx
import React, { useMemo } from "react";
import { groupByDate } from "../../../utils/milkingUtils";
import Button from "../../Shared/Button";
import BovineSelect from "../BovineSelect";
import LactationSelect from "../LactationSelect";
import "./MilkingTable.css";

export default function MilkingTable({
  records = [],
  cows = [],
  cowsLoading = false,
  bovineId,
  setBovineId,
  lactationNumber,
  setLactationNumber,
  onQuery,
  loading
}) {
  const rows = useMemo(() => groupByDate(records), [records]);

  // Obtener lactancias de la vaca seleccionada
  const selectedCow = cows.find(c => String(c.bovineId) === String(bovineId));
  const lactations = selectedCow?.lactations || [];

  const handleBovineChange = (e) => {
    setBovineId && setBovineId(e.target.value);
    setLactationNumber && setLactationNumber("");  // Reset lactancia al cambiar vaca
  };

  return (
    <div>
      <div className="milking-filter-row">
        <BovineSelect
          cows={cows}
          loading={cowsLoading}
          value={bovineId}
          onChange={handleBovineChange}
        />
        <LactationSelect
          lactations={lactations}
          value={lactationNumber}
          onChange={e => setLactationNumber && setLactationNumber(e.target.value)}
          disabled={!bovineId}
        />
        <Button onClick={onQuery} disabled={!bovineId}>Buscar</Button>
      </div>

      {loading ? (
        <p>Cargando…</p>
      ) : (
        <div className="milking-table-container">
          <table className="milking-table">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Mañana (AM)</th>
                <th>Obs AM</th>
                <th>Tarde (PM)</th>
                <th>Obs PM</th>
                <th>Total día</th>
              </tr>
            </thead>
            <tbody>
              {rows.map(({ date, AM, PM }) => {
                const amLiters = AM?.liters ?? null;
                const pmLiters = PM?.liters ?? null;
                const total = (amLiters ?? 0) + (pmLiters ?? 0);
                return (
                  <tr key={date}>
                    <td>{date}</td>
                    <td>{amLiters != null ? `${amLiters} L` : "—"}</td>
                    <td title={AM?.observations || ""}>
                      {AM?.status ? AM.status : "—"}
                    </td>
                    <td>{pmLiters != null ? `${pmLiters} L` : "—"}</td>
                    <td title={PM?.observations || ""}>
                      {PM?.status ? PM.status : "—"}
                    </td>
                    <td><strong>{total.toFixed(2)} L</strong></td>
                  </tr>
                );
              })}
              {rows.length === 0 && (
                <tr><td colSpan="6" style={{textAlign:"center", padding:12}}>Sin registros</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
```

#### Tarea 6.6: Actualizar MilkingPage.jsx
**Archivo:** `cattle-front/src/components/Milking/page/MilkingPage.jsx`

```jsx
import React from "react";
import { useCowsWithLactations, useMilkingRecords, useMilkingForm } from "../hooks/MilkingHooks";
import MilkingTable from "../table/MilkingTable";
import MilkingAdd from "../add/MilkingAdd";
import "./MilkingPage.css";

export default function MilkingPage({ bovineIdFromProp }) {
  // Hook para cargar vacas con lactancias (una sola llamada al backend)
  const { cows, loading: cowsLoading } = useCowsWithLactations();

  const {
    bovineId,
    setBovineId,
    lactationNumber,
    setLactationNumber,
    records,
    loading,
    fetchData,
    onQuery,
  } = useMilkingRecords(bovineIdFromProp);

  const {
    form,
    setForm,
    onChange,
    onSubmit,
  } = useMilkingForm(bovineId, fetchData);

  return (
    <div>
      <section>
        <h2>Lactancia</h2>
        <MilkingAdd form={form} onChange={onChange} onSubmit={onSubmit} />
      </section>

      <section>        
        <MilkingTable
          records={records}
          cows={cows}
          cowsLoading={cowsLoading}
          bovineId={bovineId}
          setBovineId={setBovineId}
          lactationNumber={lactationNumber}
          setLactationNumber={setLactationNumber}
          onQuery={onQuery}
          loading={loading}
        />
      </section>
    </div>
  );
}
```

---

## 📊 Diagrama de Flujo

```
┌─────────────────────────────────────────────────────────────────┐
│                      FRONTEND (React)                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   MilkingPage (carga vacas una sola vez)                       │
│   └── useCowsWithLactations() ──▶ GET /milking/cows            │
│                                                                 │
│   ┌──────────────────┐    ┌───────────────────┐                │
│   │  BovineSelect    │    │  LactationSelect  │                │
│   │  (props: cows)   │───▶│  (props: vaca     │                │
│   │                  │    │   seleccionada.   │                │
│   │                  │    │   lactations)     │                │
│   └──────────────────┘    └───────────────────┘                │
│            │                       │                           │
│            └───────────┬───────────┘                           │
│                        ▼                                       │
│   ┌─────────────────────────────────────────┐                  │
│   │           MilkingTable                  │                  │
│   │  GET /milking/{id}/lactation/{number}   │                  │
│   └─────────────────────────────────────────┘                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     BACKEND (Spring Boot)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   MilkingController                                             │
│   ├── GET /milking/cows  ──────────────────────────────────┐   │
│   │   └─▶ Vacas con lactancias activas + historial         │   │
│   │                                                         │   │
│   └── GET /milking/{id}/lactation/{number}  ───────────────┤   │
│       └─▶ Registros de ordeño filtrados por lactancia      │   │
│                     │                                       │   │
│                     ▼                                       │   │
│   MilkingProcessor + LactationService                          │
│                     │                                           │
│                     ▼                                           │
│   MilkingRepository + LactationRepository                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DYNAMODB                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────────────────┐    ┌─────────────────────┐           │
│   │   bovineIdentityItems-table     │    │  milking-records    │           │
│   │   (bovineIdentityItem-lact)     │    │                     │           │
│   ├─────────────────────┤    ├─────────────────────┤           │
│   │ PK: BOVINE#172      │    │ PK: BOVINE#172      │           │
│   │ SK: LACT#01         │    │ SK: MILKING#...     │           │
│   │ GSI1PK: LACT#OPEN   │    │ GSI2PK: BOVINE#172  │           │
│   │ GSI1SK: date#id     │    │         #LACT#01    │           │
│   └─────────────────────┘    │ GSI2SK: date#shift  │           │
│                              └─────────────────────┘           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Respuesta Endpoint GET /milking/cows

```json
[
  {
    "bovineId": 172,
    "bovineName": "Rosa",
    "lactations": [
      {
        "lactationNumber": 1,
        "startDate": "2025-11-27",
        "endDate": null,
        "status": "OPEN"
      }
    ]
  },
  {
    "bovineId": 167,
    "bovineName": "Tormento",
    "lactations": [
      {
        "lactationNumber": 1,
        "startDate": "2025-01-15",
        "endDate": "2025-09-20",
        "status": "CLOSED"
      },
      {
        "lactationNumber": 2,
        "startDate": "2025-12-01",
        "endDate": null,
        "status": "OPEN"
      }
    ]
  }
]
```

---

## 🏗️ Análisis Arquitectónico (Arquitecto)

### Decisiones de Diseño

**Patrón Arquitectónico:** Layered Architecture + GSI Pattern (DynamoDB)

**Justificación:** 
1. Coherencia con arquitectura existente: El sistema ya usa capas Controller → Processor → Service → Repository
2. Reutilización de componentes: Ya existe `ProfileLactancy` y `ProfileLactancyRepository` que manejan lactancias
3. Eficiencia de consultas: El GSI2 permite filtrar milking records por lactancia sin full scan
4. Mínimo impacto: Extiende componentes existentes en lugar de crear nuevos

**Componentes Afectados:**

- **MilkingRecord.java (Modificación Mayor):** Agregar campos `lactationNumber`, `GSI2PK`, `GSI2SK` con anotaciones DynamoDB para el nuevo índice secundario
- **MilkingDTO.java (Modificación Menor):** Agregar campo `lactationNumber` para exponer en API
- **MilkingRepository.java (Modificación Mayor):** Nuevo método `getMilkingByGSI2()` para query por bovino+lactancia usando el índice GSI2
- **ProfileLactancyRepository.java (Modificación Menor):** Nuevo método `findOpenLactations()` usando GSI1PK="LACT#OPEN" para obtener vacas con lactancias activas
- **MilkingService.java (Modificación Menor):** Exponer nuevo método de consulta por lactancia
- **MilkingProcessor.java (Modificación Mayor):** Orquestar `getCowsWithLactations()` + `getMilkingByLactation()` + modificar `createMilking()` para asignar lactancia automáticamente
- **MilkingController.java (Modificación Mayor):** 2 nuevos endpoints REST: `GET /milking/cows` y `GET /milking/{id}/lactation/{number}`
- **CowWithLactationsDTO.java (Nuevo):** DTO para respuesta de vacas con sus lactancias embebidas
- **LactationSummaryDTO.java (Nuevo):** DTO resumen de lactancia (número, fechas, estado)
- **lactationService.js (Nuevo):** Cliente HTTP para los 2 nuevos endpoints
- **LactationSelect.jsx (Nuevo):** Componente selector de lactancias
- **BovineSelect.jsx (Modificación Menor):** Recibir lista de vacas desde props en lugar de hardcoded
- **MilkingHooks.js (Modificación Mayor):** Nuevo hook `useCowsWithLactations()` + actualizar `useMilkingRecords` para soportar filtro por lactancia
- **MilkingTable.jsx (Modificación Mayor):** Integrar ambos selectores (vaca + lactancia)
- **MilkingPage.jsx (Modificación Menor):** Orquestar hooks y pasar props a componentes hijos

**Hitos de Implementación:**

1. **Infraestructura DynamoDB** - Crear GSI2 en tabla milking-records + script migración
   - Dependencias: Ninguna

2. **MilkingRecord + MilkingDTO** - Agregar campos lactationNumber, GSI2PK, GSI2SK
   - Dependencias: Hito 1

3. **DTOs nuevos** - Crear CowWithLactationsDTO, LactationSummaryDTO
   - Dependencias: Ninguna

4. **ProfileLactancyRepository** - Método findOpenLactations() usando GSI1
   - Dependencias: Ninguna

5. **MilkingRepository** - Método getMilkingByGSI2()
   - Dependencias: Hito 1, 2

6. **MilkingService** - Exponer nuevos métodos de consulta
   - Dependencias: Hito 4, 5

7. **MilkingProcessor** - Orquestar getCowsWithLactations + getMilkingByLactation + modificar createMilking
   - Dependencias: Hito 6

8. **MilkingController** - 2 nuevos endpoints REST
   - Dependencias: Hito 7

9. **Frontend - Service** - lactationService.js
   - Dependencias: Hito 8

10. **Frontend - Componentes** - BovineSelect, LactationSelect, MilkingHooks, MilkingTable, MilkingPage
    - Dependencias: Hito 9

### Validación de Impacto

**Código Real Verificado:**

| Componente | Estado Actual | Acción Requerida |
|------------|---------------|------------------|
| `ProfileLactancy.java` | ✅ Ya existe con GSI1 | Reutilizar |
| `ProfileLactancyRepository.java` | ✅ Ya existe | Agregar `findOpenLactations()` |
| `MilkingRecord.java` | ⚠️ Sin campos GSI2 | Modificar |
| `MilkingRepository.java` | ⚠️ Solo `getMilkingByPk` | Agregar query por GSI2 |
| `MilkingProcessor.java` | ⚠️ Sin servicio lactancias | Inyectar + modificar |
| `MilkingController.java` | ⚠️ Solo 2 endpoints | Agregar 2 endpoints |

**Riesgos y Mitigaciones:**

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Migración de datos existentes | Medio | Script Python para backfill de GSI2PK/GSI2SK en milking-records existentes |
| Creación de GSI en DynamoDB | Bajo | El GSI se crea sin downtime, indexación tarda ~minutos |
| Registros sin lactancia asociada | Medio | Validar en `createMilking()` que el bovino tenga lactancia OPEN; sino rechazar con error claro |

### Referencias y Validación

**Documentación consultada:**
- [index.md](../arquitectura/index.md) - GPS Arquitectónico del sistema
- [architecture-cattle-lambda-function.md](../arquitectura/architecture-cattle-lambda-function.md) - Documentación del componente backend

**Código fuente verificado:**
- [ProfileLactancy.java](../../src/main/java/com/cattle/entities/ProfileLactancy.java) - Entidad existente de lactancias
- [ProfileLactancyRepository.java](../../src/main/java/com/cattle/repository/ProfileLactancyRepository.java) - Repositorio existente
- [MilkingRecord.java](../../src/main/java/com/cattle/entities/MilkingRecord.java) - Entidad actual sin GSI2
- [MilkingRepository.java](../../src/main/java/com/cattle/repository/MilkingRepository.java) - Repositorio actual
- [MilkingProcessor.java](../../src/main/java/com/cattle/processor/MilkingProcessor.java) - Procesador actual
- [Pasture.java](../../src/main/java/com/cattle/entities/Pasture.java) - Referencia de implementación GSI existente

**Historias relacionadas:**
- Historia #HU-001: Primera historia del módulo Milking con enfoque en lactancias

**Validado por:** jhon.fernandez | **Fecha:** 2026-02-04 | **Enfoque:** Exploratorio

---

## 💻 Refinamiento Técnico (Developer)

### Consideraciones Generales

**Basado en análisis arquitectónico:**
Sección "Análisis Arquitectónico (Arquitecto)" de esta historia - patrón Layered Architecture + GSI Pattern validado

**Nivel de complejidad:**
Media - Modificación de múltiples capas (Entity, Repository, Service, Processor, Controller) + creación de GSI en DynamoDB + integración frontend

**Riesgos técnicos conocidos:**
1. Migración de datos existentes: Script Python para backfill - ejecutar en horario de bajo tráfico
2. GSI creation time: La indexación puede tardar minutos dependiendo del volumen de datos
3. Registros sin lactancia: Validación en `createMilking()` para rechazar registros si no hay lactancia OPEN

**Patrones y convenciones del equipo:**
- Sufijos: Controller, Service, Repository, Processor, DTO, Mapper
- Tests: JUnit 5 + Mockito, ubicación `src/test/java/com/cattle/{layer}/`
- Naming: camelCase para métodos, PascalCase para clases
- GSI: Prefijo `GSI{N}PK`, `GSI{N}SK` con anotaciones `@DynamoDbSecondaryPartitionKey`

**Dependencias nuevas a instalar:**
Ninguna - todas las dependencias ya existen en el proyecto

**Estrategia de testing:**
JUnit 5 + Mockito | Tests unitarios por capa (Repository, Service, Processor, Controller) | Cobertura: 75% mínimo | Builders: Reutilizar `TestDataBuilder.java`, crear métodos para `MilkingRecord` y `ProfileLactancy`

### Historias Relacionadas Consultadas

**Implementaciones similares analizadas:**
- `Pasture.java` - Implementación de GSI2 como referencia (gsi2pk, gsi2sk con anotaciones)
- `MilkingRecordRepositoryTest.java` - Patrón de tests para repositorio con mocks de DynamoDB
- `MilkingRecordProcessorTest.java` - Patrón de tests para processor con mocks de service

**Patrones de código reutilizados:**
- Query por GSI: Referencia en `PastureRepository.java` para consultas con índices secundarios
- DTO mapping: Usar `MilkingMapper.java` para agregar mapeo de `lactationNumber`
- Test Data Builder: Extender `TestDataBuilder.java` con métodos para `MilkingRecord` y `ProfileLactancy`

**Mejores prácticas aplicadas:**
- Inyección de dependencias via constructor (no @Autowired en campo)
- Optional para retornos que pueden ser vacíos
- Validación de inputs en Processor antes de llamar a Service
- Logs con `LambdaContext.logInfo()` en Repository

---

## 📋 Tareas de Implementación (Developer)

### Fase 0: Infraestructura y Setup

#### 🗄️ DynamoDB

- [ ] **Crear script GSI2 para milking-records** (AC: 3)
  - [ ] Crear archivo `docs/scripts/update-milking-gsi.sh`
  - [ ] Definir GSI2-bovineIdentityItem-lactation-index con GSI2PK (Hash) y GSI2SK (Range)
  - [ ] Ejecutar en ambiente desarrollo
  - [ ] Validar GSI creado con `aws dynamodb describe-table`

- [ ] **Crear script migración de datos existentes** (AC: 3)
  - [ ] Crear archivo `docs/scripts/migrate-milking-records.py`
  - [ ] Lógica: Para cada milking-record, buscar lactancia correspondiente por fecha
  - [ ] Generar GSI2PK = `BOVINE#<id>#LACT#<nn>` y GSI2SK = `<date>#<shift>`
  - [ ] Ejecutar en ambiente desarrollo
  - [ ] Validar registros migrados con scan

---

### Fase 1: Entidades y DTOs

#### 📦 cattle-lambda-function (Backend)

- [ ] **Actualizar MilkingRecord.java** (AC: 3)
  - [ ] Agregar campo `lactationNumber` (Integer) - Archivo: `src/main/java/com/cattle/entities/MilkingRecord.java`
  - [ ] Agregar campo `GSI2PK` (String) con `@DynamoDbSecondaryPartitionKey(indexNames = "GSI2-bovineIdentityItem-lactation-index")`
  - [ ] Agregar campo `GSI2SK` (String) con `@DynamoDbSecondarySortKey(indexNames = "GSI2-bovineIdentityItem-lactation-index")`
  - [ ] Agregar getters/setters con anotaciones

- [ ] **Crear CowWithLactationsDTO.java** (AC: 1, 2)
  - [ ] Crear archivo `src/main/java/com/cattle/dtos/CowWithLactationsDTO.java`
  - [ ] Campos: `bovineId` (Integer), `bovineName` (String), `lactations` (List<LactationSummaryDTO>)
  - [ ] Anotaciones Lombok: @Getter, @Setter, @Builder, @AllArgsConstructor, @NoArgsConstructor

- [ ] **Crear LactationSummaryDTO.java** (AC: 2)
  - [ ] Crear archivo `src/main/java/com/cattle/dtos/LactationSummaryDTO.java`
  - [ ] Campos: `lactationNumber` (Integer), `startDate` (String), `endDate` (String), `status` (String)
  - [ ] Anotaciones Lombok

- [ ] **Actualizar MilkingDTO.java** (AC: 3)
  - [ ] Agregar campo `lactationNumber` (Integer) - Archivo: `src/main/java/com/cattle/dtos/MilkingDTO.java`
  - [ ] Sin validación @NotNull (puede ser null en registros legacy)

- [ ] **Actualizar MilkingMapper.java** (AC: 3)
  - [ ] Agregar mapeo de `lactationNumber` en toDTO y toEntity - Archivo: `src/main/java/com/cattle/mapper/MilkingMapper.java`

---

### Fase 2: Repository

#### 📦 cattle-lambda-function (Backend)

- [ ] **Agregar findOpenLactations() en ProfileLactancyRepository** (AC: 1)
  - [ ] Archivo: `src/main/java/com/cattle/repository/ProfileLactancyRepository.java`
  - [ ] Método: `Optional<List<ProfileLactancy>> findOpenLactations()`
  - [ ] Query: GSI1PK = "LACT#OPEN"
  - [ ] Test unitario en `src/test/java/com/cattle/repository/ProfileLactancyRepositoryTest.java`

- [ ] **Agregar findAllLactationsByBovine() en ProfileLactancyRepository** (AC: 2)
  - [ ] Archivo: `src/main/java/com/cattle/repository/ProfileLactancyRepository.java`
  - [ ] Método: `Optional<List<ProfileLactancy>> findAllLactationsByBovine(String pk)`
  - [ ] Query: PK = BOVINE#<id>, SK begins_with "LACT#"
  - [ ] Test unitario

- [ ] **Agregar getMilkingByGSI2() en MilkingRepository** (AC: 3)
  - [ ] Archivo: `src/main/java/com/cattle/repository/MilkingRepository.java`
  - [ ] Método: `Optional<List<MilkingRecord>> getMilkingByBovineAndLactation(Integer bovineId, Integer lactationNumber)`
  - [ ] Query: GSI2PK = `BOVINE#<id>#LACT#<nn>`
  - [ ] Test unitario en `src/test/java/com/cattle/repository/MilkingRecordRepositoryTest.java`

---

### Fase 3: Services

#### 📦 cattle-lambda-function (Backend)

- [ ] **Agregar métodos en MilkingService** (AC: 3)
  - [ ] Archivo: `src/main/java/com/cattle/services/MilkingService.java`
  - [ ] Método: `getMilkingByBovineAndLactation(Integer bovineId, Integer lactationNumber)`
  - [ ] Delegar a MilkingRepository.getMilkingByBovineAndLactation()
  - [ ] Test unitario en `src/test/java/com/cattle/services/MilkingRecordServiceTest.java`

---

### Fase 4: Processor

#### 📦 cattle-lambda-function (Backend)

- [ ] **Actualizar MilkingProcessor - Inyectar dependencias** (AC: 1, 2, 3)
  - [ ] Archivo: `src/main/java/com/cattle/processor/MilkingProcessor.java`
  - [ ] Inyectar `ProfileLactancyRepository` en constructor
  - [ ] Inyectar `BovineRepository` para obtener nombre de vaca

- [ ] **Implementar getCowsWithLactations()** (AC: 1, 2)
  - [ ] Archivo: `src/main/java/com/cattle/processor/MilkingProcessor.java`
  - [ ] Método: `Optional<List<CowWithLactationsDTO>> getCowsWithLactations()`
  - [ ] Paso 1: Obtener lactancias OPEN desde ProfileLactancyRepository.findOpenLactations()
  - [ ] Paso 2: Extraer bovineIds únicos
  - [ ] Paso 3: Para cada bovineId, obtener TODAS las lactancias (findAllLactationsByBovine)
  - [ ] Paso 4: Mapear a CowWithLactationsDTO
  - [ ] Test unitario en `src/test/java/com/cattle/processor/MilkingRecordProcessorTest.java`

- [ ] **Implementar getMilkingByLactation()** (AC: 3)
  - [ ] Archivo: `src/main/java/com/cattle/processor/MilkingProcessor.java`
  - [ ] Método: `Optional<List<MilkingDTO>> getMilkingByLactation(Integer bovineId, Integer lactationNumber, String shift)`
  - [ ] Delegar a MilkingService.getMilkingByBovineAndLactation()
  - [ ] Filtrar por shift si se proporciona
  - [ ] Mapear a MilkingDTO
  - [ ] Test unitario

- [ ] **Modificar createMilking() para asignar lactancia** (AC: 3)
  - [ ] Archivo: `src/main/java/com/cattle/processor/MilkingProcessor.java`
  - [ ] En setPkSk(): Obtener lactancia OPEN del bovino
  - [ ] Si no hay lactancia OPEN: throw IllegalArgumentException("El bovino no tiene lactancia activa")
  - [ ] Generar GSI2PK y GSI2SK con el número de lactancia
  - [ ] Test unitario para caso éxito y caso error

---

### Fase 5: Controller

#### 📦 cattle-lambda-function (Backend)

- [ ] **Agregar endpoint GET /milking/cows** (AC: 1, 2)
  - [ ] Archivo: `src/main/java/com/cattle/controller/MilkingController.java`
  - [ ] Anotaciones: @Operation, @ApiResponses, @GetMapping("/cows")
  - [ ] Retorno: `ResponseEntity<List<CowWithLactationsDTO>>`
  - [ ] Delegar a MilkingProcessor.getCowsWithLactations()
  - [ ] Test unitario en `src/test/java/com/cattle/controller/MilkingRecordControllerTest.java`

- [ ] **Agregar endpoint GET /milking/{idBovine}/lactation/{lactationNumber}** (AC: 3)
  - [ ] Archivo: `src/main/java/com/cattle/controller/MilkingController.java`
  - [ ] Anotaciones: @Operation, @ApiResponses, @GetMapping("/{idBovine}/lactation/{lactationNumber}")
  - [ ] Parámetros: @PathVariable idBovine, @PathVariable lactationNumber, @RequestParam(required=false) shift
  - [ ] Retorno: `ResponseEntity<List<MilkingDTO>>`
  - [ ] Delegar a MilkingProcessor.getMilkingByLactation()
  - [ ] Test unitario

---

### Fase 6: Frontend

#### 📦 cattle-front (Frontend)

- [ ] **Crear lactationService.js** (AC: 1, 2, 3)
  - [ ] Archivo: `src/services/lactationService.js`
  - [ ] Función: `getCowsWithLactations()` → GET /milking/cows
  - [ ] Función: `getMilkingByLactation(bovineId, lactationNumber)` → GET /milking/{id}/lactation/{number}

- [ ] **Actualizar BovineSelect.jsx** (AC: 1)
  - [ ] Archivo: `src/components/Milking/BovineSelect.jsx`
  - [ ] Cambiar: Recibir `cows` y `loading` por props
  - [ ] Eliminar: useState y useEffect internos (ya no hace fetch)
  - [ ] Agregar prop: `onChange` que recibe el evento completo

- [ ] **Crear LactationSelect.jsx** (AC: 2)
  - [ ] Archivo: `src/components/Milking/LactationSelect.jsx`
  - [ ] Props: `lactations`, `value`, `onChange`, `disabled`
  - [ ] Renderizar: option "Todas las lactancias" + options de cada lactancia
  - [ ] Estilos: Reutilizar BovineSelect.css

- [ ] **Actualizar MilkingHooks.js** (AC: 1, 2, 3)
  - [ ] Archivo: `src/components/Milking/hooks/MilkingHooks.js`
  - [ ] Agregar: `useCowsWithLactations()` hook para cargar vacas una vez
  - [ ] Modificar: `useMilkingRecords()` para incluir `lactationNumber` y `setLactationNumber`
  - [ ] Modificar: `fetchData()` para llamar endpoint por lactancia si se selecciona

- [ ] **Actualizar MilkingTable.jsx** (AC: 1, 2, 3)
  - [ ] Archivo: `src/components/Milking/table/MilkingTable.jsx`
  - [ ] Agregar props: `cows`, `cowsLoading`, `lactationNumber`, `setLactationNumber`
  - [ ] Integrar: BovineSelect y LactationSelect en filtros
  - [ ] Lógica: Obtener lactaciones de la vaca seleccionada desde `cows`

- [ ] **Actualizar MilkingPage.jsx** (AC: 1, 2, 3)
  - [ ] Archivo: `src/components/Milking/page/MilkingPage.jsx`
  - [ ] Agregar: llamada a `useCowsWithLactations()`
  - [ ] Pasar: `cows` y `cowsLoading` a MilkingTable

---

### Fase N: QA y Deployment

#### 🔍 Code Quality

- [ ] **Ejecutar Agente Peer Review** (MANUAL)
  - [ ] Ejecutar `*peer-review` en el agente
  - [ ] Documentar hallazgos

- [ ] **Resolver incidentes del Peer Review** (MANUAL - Condicional)
  - [ ] Aplicar correcciones si hay hallazgos

#### 🚀 Deployment DEV

- [ ] **Crear Pull Request** (MANUAL)
  - [ ] Branch: feature/HU-001-consulta-lactancias
  - [ ] Descripción: Resumen de cambios (mínimo 100 palabras)
  - [ ] Linked: HU-001

- [ ] **Ejecutar pipeline deployment DEV** (MANUAL)
  - [ ] Verificar CI/CD green
  - [ ] Validar deploy exitoso

#### 🧪 Testing Manual

- [ ] **Diseñar set de pruebas manuales** (MANUAL)
  - [ ] Caso 1: Cargar módulo ordeño - verificar selector vacas con lactancias
  - [ ] Caso 2: Seleccionar vaca - verificar selector lactancias se actualiza
  - [ ] Caso 3: Seleccionar lactancia y buscar - verificar filtrado correcto
  - [ ] Caso 4: Crear nuevo registro de ordeño - verificar se asigna lactancia

- [ ] **Ejecutar pruebas manuales** (MANUAL)
  - [ ] Ejecutar casos diseñados
  - [ ] Documentar resultados

---

**Notas sobre vinculación con Criterios de Aceptación:**
- AC-01: Fases 0-5 Backend + Fase 6 Frontend (BovineSelect, hooks, page)
- AC-02: Fase 4 Processor + Fase 6 Frontend (LactationSelect, hooks, table)
- AC-03: Todas las fases (GSI2, repository, service, processor, controller, frontend)

---

### FASE 1: Infraestructura
- [ ] 1.1 Crear script actualización GSI2 en tabla milking-records (DynamoDB)
- [ ] 1.2 Crear script migración datos existentes (backfill GSI2PK, GSI2SK, lactationNumber)
- [ ] 1.3 Ejecutar scripts en ambiente de desarrollo
- [ ] 1.4 Validar GSI2 creado correctamente

### FASE 2: Entidades y DTOs
- [ ] 2.1 Actualizar MilkingRecord.java (lactationNumber, GSI2PK, GSI2SK con anotaciones)
- [ ] 2.2 Crear CowWithLactationsDTO.java
- [ ] 2.3 Crear LactationSummaryDTO.java
- [ ] 2.4 Actualizar MilkingDTO.java (agregar lactationNumber)

### FASE 3: Repository
- [ ] 3.1 Agregar findOpenLactations() en ProfileLactancyRepository.java (reutilizar existente)
- [ ] 3.2 Agregar getMilkingByGSI2() en MilkingRepository.java

### FASE 4: Services y Processor
- [ ] 4.1 Actualizar MilkingService.java con método getMilkingByBovineAndLactation()
- [ ] 4.2 Actualizar MilkingProcessor.java: inyectar ProfileLactancyRepository, agregar getCowsWithLactations(), getMilkingByLactation(), modificar createMilking()

### FASE 5: Controller (2 endpoints)
- [ ] 5.1 GET /milking/cows (vacas con lactancias activas + historial)
- [ ] 5.2 GET /milking/{id}/lactation/{number} (ordeños por lactancia)

### FASE 6: Frontend
- [ ] 6.1 Crear lactationService.js (2 funciones: getCowsWithLactations, getMilkingByLactation)
- [ ] 6.2 Actualizar BovineSelect.jsx (recibe cows por props)
- [ ] 6.3 Crear LactationSelect.jsx (recibe lactations por props)
- [ ] 6.4 Actualizar MilkingHooks.js (useCowsWithLactations + useMilkingRecords con lactancia)
- [ ] 6.5 Actualizar MilkingTable.jsx (integrar ambos selects)
- [ ] 6.6 Actualizar MilkingPage.jsx (orquestar hooks)

### Testing
- [ ] Tests unitarios ProfileLactancyRepository (findOpenLactations)
- [ ] Tests unitarios MilkingRepository (getMilkingByGSI2)
- [ ] Tests unitarios MilkingService
- [ ] Tests unitarios MilkingProcessor (getCowsWithLactations, getMilkingByLactation, createMilking)
- [ ] Tests unitarios MilkingController (2 nuevos endpoints)
- [ ] Tests integración E2E

---

## � Estimación (Developer)

### Parámetros de Estimación

| Parámetro | Valor |
|-----------|-------|
| **Complejidad** | MEDIA |
| **Descuento Método Ceiba** | 60% |
| **Multiplicador Junior** | ×2.5 |
| **Multiplicador Semi Senior** | ×1.6 |
| **Fecha Estimación** | 2026-02-04 |
| **Estimador** | jhon.fernandez |

### Estimación por Tareas (Desarrollo - Aumentado por IA)

| # | Tarea | Junior | Semi Sr | Senior | MC Jr | MC Semi Sr | MC Sr |
|---|-------|--------|---------|--------|-------|------------|-------|
| 1 | Crear script GSI2 para milking-records | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| 2 | Crear script migración de datos | 5.0h | 3.2h | 2.0h | 2.0h | 1.3h | 0.8h |
| 3 | Actualizar MilkingRecord.java | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| 4 | Crear CowWithLactationsDTO.java | 1.3h | 0.8h | 0.5h | 0.5h | 0.3h | 0.2h |
| 5 | Crear LactationSummaryDTO.java | 1.3h | 0.8h | 0.5h | 0.5h | 0.3h | 0.2h |
| 6 | Actualizar MilkingDTO.java | 0.6h | 0.4h | 0.3h | 0.3h | 0.2h | 0.1h |
| 7 | Actualizar MilkingMapper.java | 1.3h | 0.8h | 0.5h | 0.5h | 0.3h | 0.2h |
| 8 | Agregar findOpenLactations() + test | 3.8h | 2.4h | 1.5h | 1.5h | 1.0h | 0.6h |
| 9 | Agregar findAllLactationsByBovine() + test | 3.8h | 2.4h | 1.5h | 1.5h | 1.0h | 0.6h |
| 10 | Agregar getMilkingByGSI2() + test | 5.0h | 3.2h | 2.0h | 2.0h | 1.3h | 0.8h |
| 11 | Agregar métodos en MilkingService + test | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| 12 | Inyectar dependencias en MilkingProcessor | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| 13 | Implementar getCowsWithLactations() + test | 6.3h | 4.0h | 2.5h | 2.5h | 1.6h | 1.0h |
| 14 | Implementar getMilkingByLactation() + test | 5.0h | 3.2h | 2.0h | 2.0h | 1.3h | 0.8h |
| 15 | Modificar createMilking() + test | 5.0h | 3.2h | 2.0h | 2.0h | 1.3h | 0.8h |
| 16 | Endpoint GET /milking/cows + test | 3.8h | 2.4h | 1.5h | 1.5h | 1.0h | 0.6h |
| 17 | Endpoint GET /milking/{id}/lactation/{n} + test | 3.8h | 2.4h | 1.5h | 1.5h | 1.0h | 0.6h |
| 18 | Crear lactationService.js | 1.3h | 0.8h | 0.5h | 0.5h | 0.3h | 0.2h |
| 19 | Actualizar BovineSelect.jsx | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| 20 | Crear LactationSelect.jsx | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| 21 | Actualizar MilkingHooks.js | 5.0h | 3.2h | 2.0h | 2.0h | 1.3h | 0.8h |
| 22 | Actualizar MilkingTable.jsx | 5.0h | 3.2h | 2.0h | 2.0h | 1.3h | 0.8h |
| 23 | Actualizar MilkingPage.jsx | 2.5h | 1.6h | 1.0h | 1.0h | 0.6h | 0.4h |
| | **SUBTOTAL DESARROLLO** | **74.8h** | **47.6h** | **29.8h** | **29.8h** | **19.0h** | **11.9h** |

### Tareas Manuales (Sin descuento IA)

| # | Tarea | Tiempo |
|---|-------|--------|
| 24 | Ejecutar Agente Peer Review | 1.0h |
| 25 | Resolver incidentes del Peer Review | 1.0h |
| 26 | Crear Pull Request | 0.5h |
| 27 | Ejecutar pipeline deployment DEV | 0.5h |
| 28 | Diseñar set de pruebas manuales | 1.0h |
| 29 | Ejecutar pruebas manuales | 1.5h |
| | **SUBTOTAL MANUAL** | **5.5h** |

### Resumen de Totales

| Perfil | Tradicional | Método Ceiba (Dev + Manual) | Ahorro |
|--------|-------------|----------------------------|--------|
| **Senior** | 29.8h | 11.9h + 5.5h = **17.4h** | **42%** |
| **Semi Senior** | 47.6h | 19.0h + 5.5h = **24.5h** | **49%** |
| **Junior** | 74.8h | 29.8h + 5.5h = **35.3h** | **53%** |

### Supuestos y Riesgos

**Supuestos:**
- Ambiente de desarrollo disponible y funcional
- Acceso a DynamoDB para crear GSI
- No hay cambios en requisitos durante desarrollo
- Disponibilidad completa del desarrollador

**Riesgos no incluidos en estimación:**
- Tiempo de indexación GSI puede variar según volumen de datos
- Migración de datos puede requerir ajustes si hay datos inconsistentes
- Coordinación con equipo de infraestructura para ejecutar scripts

**Tareas con mayor incertidumbre:**
- Crear script migración de datos (riesgo: datos legacy inconsistentes)
- Implementar getCowsWithLactations() (riesgo: performance con muchos bovinos)

---

## �📝 Registro de Cambios

| Fecha | Versión | Descripción | Autor |
|-------|---------|-------------|-------|
| 2026-02-03 | 1.0 | Creación de historia de usuario | jhon.fernandez (PO) |
| 2026-02-04 | 1.1 | Análisis arquitectónico completado | jhon.fernandez (Arquitecto) |
| 2026-02-04 | 1.2 | Refinamiento técnico con tareas detalladas | jhon.fernandez (Developer) |
| 2026-02-04 | 1.3 | Estimación completada | jhon.fernandez (Developer) |
| 2026-02-04 | 1.4 | Desarrollo completado - Todas las fases implementadas | jhon.fernandez (Developer) |

---

## 🤖 Dev Agent Record

### Completion Notes

**Fecha de desarrollo:** 2026-02-04

**Fases implementadas:**
- ✅ Fase 1: Entidades y DTOs (MilkingRecord, MilkingDTO, CowWithLactationsDTO, LactationSummaryDTO, MilkingMapper)
- ✅ Fase 2: Repository (ProfileLactancyRepository, MilkingRepository)
- ✅ Fase 3: Services (MilkingService)
- ✅ Fase 4: Processor (MilkingProcessor - getCowsWithLactations, getMilkingByLactation, createMilking modificado)
- ✅ Fase 5: Controller (MilkingController - 2 nuevos endpoints)
- ✅ Fase 6: Frontend (lactationService, BovineSelect, LactationSelect, MilkingHooks, MilkingTable, MilkingPage)

**Decisiones técnicas:**
1. GSI2 usa formato `BOVINE#<id>#LACT#<nn>` para partition key y `<date>#<shift>` para sort key
2. Campo `lactationNumber` en MilkingRecord es Integer, pero en ProfileLactancy es String (parseado al usar)
3. `createMilking()` ahora valida y asigna lactancia OPEN automáticamente, rechazando si no existe
4. Frontend carga vacas con lactaciones en un solo fetch al montar MilkingPage

**Tests actualizados:**
- MilkingRecordProcessorTest: Agregado mock de ProfileLactancyRepository
- MilkingRecordDTOTest: Actualizado constructor con nuevo campo lactationNumber

### File List

**Backend (cattle-lambda-function):**
- `docs/scripts/update-milking-gsi.sh` - **Nuevo** - Script para crear GSI2 en DynamoDB
- `docs/scripts/migrate-milking-records.py` - **Nuevo** - Script Python para migrar datos existentes
- `src/main/java/com/cattle/entities/MilkingRecord.java` - Modificado (campos GSI2)
- `src/main/java/com/cattle/dtos/MilkingDTO.java` - Modificado (lactationNumber)
- `src/main/java/com/cattle/dtos/CowWithLactationsDTO.java` - **Nuevo**
- `src/main/java/com/cattle/dtos/LactationSummaryDTO.java` - **Nuevo**
- `src/main/java/com/cattle/mapper/MilkingMapper.java` - Modificado
- `src/main/java/com/cattle/repository/ProfileLactancyRepository.java` - Modificado (2 métodos nuevos)
- `src/main/java/com/cattle/repository/MilkingRepository.java` - Modificado (1 método nuevo)
- `src/main/java/com/cattle/services/MilkingService.java` - Modificado (1 método nuevo)
- `src/main/java/com/cattle/processor/MilkingProcessor.java` - Modificado (3 métodos nuevos + modificación createMilking)
- `src/main/java/com/cattle/controller/MilkingController.java` - Modificado (2 endpoints nuevos)
- `src/test/java/com/cattle/processor/MilkingRecordProcessorTest.java` - Modificado (mock actualizado)
- `src/test/java/com/cattle/dtos/MilkingRecordDTOTest.java` - Modificado (constructor actualizado)

**Frontend (cattle-front):**
- `src/services/lactationService.js` - **Nuevo**
- `src/components/Milking/BovineSelect.jsx` - Modificado
- `src/components/Milking/LactationSelect.jsx` - **Nuevo**
- `src/components/Milking/hooks/MilkingHooks.js` - Modificado
- `src/components/Milking/table/MilkingTable.jsx` - Modificado
- `src/components/Milking/page/MilkingPage.jsx` - Modificado

---

## 📚 Referencias

- [bovineIdentityItem-lact.json](../tables/bovineIdentityItem-lact.json) - Modelo de lactancias
- [milking-records.json](../tables/milking-records.json) - Modelo de registros de ordeño
- [MilkingController.java](../../src/main/java/com/cattle/controller/MilkingController.java)
- [MilkingRecord.java](../../src/main/java/com/cattle/entities/MilkingRecord.java)
