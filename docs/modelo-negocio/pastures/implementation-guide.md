# 🚀 Guía de Implementación: Tareas P0 del Módulo Pastures

**Fecha**: 2026-01-09

## 🎯 Objetivo

Guía paso a paso para implementar las tareas críticas (P0) que harán funcional el módulo Pastures.

---

## 📚 Tabla de Contenidos

1. [Tareas P0 (Críticas)](#tareas-p0-críticas)
2. [Tarea P0#1: Endpoint de Eventos](#tarea-p01-endpoint-de-eventos)
3. [Tarea P0#2: Conectar Botones Frontend](#tarea-p02-conectar-botones-frontend)
4. [Tarea P0#3: DetailPanel Funcional](#tarea-p03-detailpanel-funcional)
5. [Testing & Validación](#testing--validación)

---

## Tareas P0 (Críticas)

| # | Tarea | Prioridad | Est. | Estado |
|---|-------|-----------|------|--------|
| 1 | Endpoint POST eventos (OPEN/CLOSE/MAINTENANCE) | 🔴 P0 | 4-6h | ❌ Falta |
| 2 | Conectar botones Abrir/Cerrar (frontend) | 🔴 P0 | 2-3h | ❌ Falta |
| 3 | DetailPanel funcional (drawer lateral) | 🔴 P0 | 6-8h | ❌ Falta |

**Total P0**: 12-17 horas (2-3 semanas).

---

## Tarea P0#1: Endpoint de Eventos

### 📍 Descripción

Implementar **POST /farms/{farmId}/pastures/{pastureId}/events** que aplique eventos (OPEN, CLOSE, MAINTENANCE_SET) usando `PastureEvent` (sealed interface).

### 🎯 Deliverables

- ✅ Crear `PastureEventController.java`
- ✅ Crear DTO `PastureEventRequest.java`
- ✅ Método en `PastureService.applyEvent()`
- ✅ Convertir request → PastureEvent
- ✅ Usar `PastureStatusEngine.applyEvent()`
- ✅ Persistir cambios en DynamoDB

### 📋 Paso 1: Crear PastureEventRequest DTO

**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/dtos/PastureEventRequest.java`

```java
package com.cattle.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastureEventRequest {
    
    // Común a todos
    private String eventType;           // "OPEN", "CLOSE", "MAINTENANCE_SET", "MAINTENANCE_CLEAR"
    private String user;                // Usuario que genera el evento
    
    // Para OPEN y CLOSE
    private String lotId;               // ID del lote/grupo
    private Integer animals;            // Cantidad de animales
    
    // Para CLOSE
    private Integer residualCm;         // Altura residual después del pastoreo
    
    // Para MAINTENANCE_SET
    private String substatus;           // "FERTILIZANDO", "REPARANDO", "CUARENTENA"
    private String holdUntil;           // Fecha de liberación (YYYY-MM-DD)
    
    // Validaciones
    public void validate() {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType es requerido");
        }
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("user es requerido");
        }
        
        switch (eventType) {
            case "OPEN", "CLOSE" -> {
                if (lotId == null || lotId.isBlank()) {
                    throw new IllegalArgumentException("lotId requerido para " + eventType);
                }
                if (animals == null || animals <= 0) {
                    throw new IllegalArgumentException("animals debe ser > 0");
                }
            }
            case "MAINTENANCE_SET" -> {
                if (substatus == null || substatus.isBlank()) {
                    throw new IllegalArgumentException("substatus requerido");
                }
                if (holdUntil == null || holdUntil.isBlank()) {
                    throw new IllegalArgumentException("holdUntil requerido");
                }
            }
        }
    }
}
```

### 📋 Paso 2: Crear PastureEventController

**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/controller/PastureEventController.java`

```java
package com.cattle.controller;

import com.cattle.dtos.PastureEventRequest;
import com.cattle.dtos.PastureDTO;
import com.cattle.services.PastureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/farms/{farmId}/pastures/{pastureId}/events")
public class PastureEventController {
    
    @Autowired
    private PastureService pastureService;
    
    /**
     * Aplica un evento a un potrero
     * 
     * POST /farms/F001/pastures/P001/events
     * {
     *   "eventType": "OPEN",
     *   "user": "juan@farm.com",
     *   "lotId": "LOT001",
     *   "animals": 15
     * }
     */
    @PostMapping
    public ResponseEntity<?> applyEvent(
        @PathVariable String farmId,
        @PathVariable String pastureId,
        @RequestBody PastureEventRequest request) {
        
        try {
            // Validar request
            request.validate();
            
            // Aplicar evento
            PastureDTO result = pastureService.applyEvent(farmId, pastureId, request);
            
            // Retornar potrero actualizado
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }
}
```

### 📋 Paso 3: Implementar PastureService.applyEvent()

**Ubicación**: `cattle-lambda-function/src/main/java/com/cattle/services/PastureService.java`

```java
public PastureDTO applyEvent(String farmId, String pastureId, PastureEventRequest request) {
    // 1. Obtener potrero
    String pk = "PASTURE#" + pastureId;
    Pasture pasture = pastureRepository.findById(pk)
        .orElseThrow(() -> new NotFoundException("Pasture not found: " + pastureId));
    
    // Validar que pertenece a la finca
    if (!pasture.getFarmId().equals(farmId)) {
        throw new UnauthorizedException("Pasture does not belong to farm");
    }
    
    // 2. Obtener plan de rotación
    Plan plan = planRepository.findByFarmAndSpecies(farmId, pasture.getSpecies())
        .orElseThrow(() -> new NotFoundException("Plan not found for species: " + pasture.getSpecies()));
    
    // 3. Convertir request a PastureEvent (sealed interface)
    PastureEvent event = requestToPastureEvent(request);
    
    // 4. Aplicar evento (genera cambios)
    EntityPatch patch = pastureStatusEngine.applyEvent(pasture, plan, event);
    
    // 5. Aplicar patch en memoria
    PatchApplier.applyLocal(pasture, patch);
    
    // 6. Persistir cambios en BD
    pastureRepository.update(pk, patch);
    
    // 7. (Opcional) Registrar evento para auditoría
    recordEventForAudit(farmId, pastureId, request);
    
    // 8. Retornar DTO actualizado
    return PasturesMapper.toPastureDTO(pasture);
}

/**
 * Convierte PastureEventRequest a PastureEvent (sealed interface)
 */
private PastureEvent requestToPastureEvent(PastureEventRequest request) {
    return switch (request.getEventType()) {
        case "OPEN" -> new OpenEvent(
            request.getUser(),
            request.getLotId(),
            request.getAnimals()
        );
        case "CLOSE" -> new CloseEvent(
            request.getUser(),
            request.getLotId(),
            request.getAnimals(),
            request.getResidualCm()
        );
        case "MAINTENANCE_SET" -> new MaintenanceSetEvent(
            request.getUser(),
            PastureSubstatus.valueOf(request.getSubstatus()),
            request.getHoldUntil()
        );
        case "MAINTENANCE_CLEAR" -> new MaintenanceClearEvent(
            request.getUser()
        );
        default -> throw new IllegalArgumentException("Unknown event type: " + request.getEventType());
    };
}

/**
 * Registra evento para auditoría (opcional)
 */
private void recordEventForAudit(String farmId, String pastureId, PastureEventRequest request) {
    // Usar Event.java (builder pattern) para auditoría histórica
    Event auditEvent = new EventBuilder()
        .pk("farm#" + farmId + "#pasture#" + pastureId)
        .sk("eventAt#" + LocalDateTime.now().toString() + "#PASTURE_" + request.getEventType())
        .eventType("PASTURE_" + request.getEventType())
        .eventAt(LocalDateTime.now().toString())
        .user(request.getUser())
        .animals(request.getAnimals())
        .residualCm(request.getResidualCm())
        .build();
    
    eventRepository.save(auditEvent);
}
```

### 🧪 Paso 4: Testing

```java
@SpringBootTest
public class PastureEventControllerTest {
    
    @MockBean
    private PastureService pastureService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testOpenEvent() throws Exception {
        // Arrange
        String payload = """
            {
              "eventType": "OPEN",
              "user": "juan@farm.com",
              "lotId": "LOT001",
              "animals": 15
            }
            """;
        
        PastureDTO expected = new PastureDTO();
        expected.setStatus("EN_USO");
        when(pastureService.applyEvent("F001", "P001", any()))
            .thenReturn(expected);
        
        // Act & Assert
        mockMvc.perform(post("/farms/F001/pastures/P001/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EN_USO"));
    }
}
```

### ✅ Checklist

- [ ] Crear `PastureEventRequest.java` con validaciones
- [ ] Crear `PastureEventController.java` con mapeo POST
- [ ] Implementar `applyEvent()` en `PastureService`
- [ ] Crear método `requestToPastureEvent()` (converter)
- [ ] Tests unitarios de controller
- [ ] Tests de integración (end-to-end)
- [ ] Documentar endpoint en OpenAPI/Swagger (TO-DO)

---

## Tarea P0#2: Conectar Botones Frontend

### 📍 Descripción

Conectar botones "Abrir" y "Cerrar" en `PastureTable` para llamar al endpoint POST de eventos.

### 🎯 Deliverables

- ✅ Crear handlers `handleOpenPasture()` y `handleClosePasture()` en `PaddockPage`
- ✅ Pasar callbacks a `PastureTable`
- ✅ Mostrar loading/error mientras se procesa
- ✅ Refetch de datos tras éxito

### 📋 Paso 1: Actualizar PaddockPage.jsx

**Ubicación**: `cattle-front/src/components/Paddock/page/PaddockPage.jsx`

```jsx
// ... importes ...
import { useState, useEffect, useMemo } from "react";
import RotationSemaphore from "../rotationSemaphore/rotationSemaphore";
import PastureTable from "../pastureTable/pastureTable";
import { useFilteredPastures } from "../hooks/padockHooks";

export default function PaddockPage() {
  const [species, setSpecies] = useState("ALL");
  const [status, setStatus] = useState("ALL");
  const [query, setQuery] = useState("");
  const [pastures, setPastures] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch inicial
  useEffect(() => {
    fetchPastures();
  }, []);

  // Función para refetch
  const fetchPastures = async () => {
    try {
      setLoading(true);
      const response = await fetch(
        "https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/farms/F001/pastures"
      );
      const data = await response.json();
      setPastures(data);
      setError(null);
    } catch (err) {
      setError("Error cargando potreros: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // ✨ NUEVO: Handler para abrir potrero
  const handleOpenPasture = async (pasture) => {
    try {
      setLoading(true);
      const response = await fetch(
        `https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/farms/F001/pastures/${pasture.pastureId}/events`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            eventType: "OPEN",
            user: "admin@farm.com",  // TO-DO: Usar usuario real
            lotId: "LOT001",         // TO-DO: Selector de lote
            animals: 15              // TO-DO: Input de cantidad
          })
        }
      );

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      // Refetch para actualizar estado
      await fetchPastures();
      setError(null);
    } catch (err) {
      setError("Error abriendo potrero: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // ✨ NUEVO: Handler para cerrar potrero
  const handleClosePasture = async (pasture) => {
    // Pedir input de altura residual
    const residualCm = prompt("Altura residual en cm:", "8");
    if (residualCm === null) return; // Cancelado

    try {
      setLoading(true);
      const response = await fetch(
        `https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/farms/F001/pastures/${pasture.pastureId}/events`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            eventType: "CLOSE",
            user: "admin@farm.com",
            lotId: "LOT001",
            animals: 15,
            residualCm: parseInt(residualCm)
          })
        }
      );

      if (!response.ok) {
        throw new Error(`Error ${response.status}`);
      }

      await fetchPastures();
      setError(null);
    } catch (err) {
      setError("Error cerrando potrero: " + err.message);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar
  const filtered = useFilteredPastures(pastures, species, status, query);

  // KPIs
  const kpiList = useMemo(() => [
    {
      title: "Hectáreas totales",
      value: `${pastures.reduce((a, p) => a + p.areaHa, 0).toFixed(2)} ha`,
    },
    {
      title: "Potreros disponibles",
      value: pastures.filter(p => p.status === "DISPONIBLE").length,
    },
    {
      title: "En uso",
      value: pastures.filter(p => p.status === "EN_USO").length,
    },
  ], [pastures]);

  return (
    <div className="paddockpage-root">
      <header>Dashboard Potreros</header>

      {/* Mostrar error si existe */}
      {error && <div className="error-banner">{error}</div>}

      {/* Mostrar loading */}
      {loading && <div className="loading-spinner">Cargando...</div>}

      {/* KPIs */}
      <div className="paddockpage-kpi-grid">
        {kpiList.map(kpi => (
          <div key={kpi.title} className="kpicard">
            <div className="kpicard-title">{kpi.title}</div>
            <div className="kpicard-value">{kpi.value}</div>
          </div>
        ))}
      </div>

      {/* Filtros */}
      <section>
        <h2>Filtros</h2>
        <select value={species} onChange={e => setSpecies(e.target.value)}>
          <option value="ALL">Todas las especies</option>
          <option value="Kikuyo">Kikuyo</option>
          <option value="Pasto azul">Pasto azul</option>
        </select>
        {/* ... otros filtros ... */}
      </section>

      {/* Semáforo */}
      <RotationSemaphore
        pastures={pastures}
        onOpen={setSelected}
      />

      {/* Tabla (CON EVENTOS CONECTADOS) */}
      <section>
        <h2>Tabla de Potreros</h2>
        <PastureTable
          rows={filtered}
          onOpen={setSelected}
          onOpenPasture={handleOpenPasture}    {/* ✨ NUEVO */}
          onClosePasture={handleClosePasture}  {/* ✨ NUEVO */}
        />
      </section>

      {/* Calendar, Alerts, Detail Panel ... */}
    </div>
  );
}
```

### 📋 Paso 2: Actualizar PastureTable.jsx

**Ubicación**: `cattle-front/src/components/Paddock/pastureTable/pastureTable.jsx`

```jsx
export default function PastureTable({
  rows,
  onOpen,
  onOpenPasture,      // ✨ NUEVO
  onClosePasture      // ✨ NUEVO
}) {
  return (
    <div className="pasturetable-root">
      <table className="pasturetable-table">
        <thead>
          {/* ... encabezados ... */}
        </thead>
        <tbody>
          {rows.map(p => (
            <tr key={p.pastureId}>
              {/* ... celdas ... */}
              <td>
                <div className="pasturetable-actions">
                  <button onClick={() => onOpen(p)}>Detalle</button>
                  
                  {/* ✨ Conectar Abrir */}
                  <button
                    onClick={() => onOpenPasture(p)}
                    disabled={p.status !== "DISPONIBLE"}  // Solo si está disponible
                  >
                    Abrir
                  </button>

                  {/* ✨ Conectar Cerrar */}
                  <button
                    onClick={() => onClosePasture(p)}
                    disabled={p.status !== "EN_USO"}      // Solo si está en uso
                  >
                    Cerrar
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

### ✅ Checklist

- [ ] Crear `handleOpenPasture()` en `PaddockPage`
- [ ] Crear `handleClosePasture()` en `PaddockPage`
- [ ] Pasar callbacks a `PastureTable`
- [ ] Conectar click de botones a handlers
- [ ] Mostrar loading mientras se procesa
- [ ] Mostrar error si falla
- [ ] Refetch de datos tras éxito
- [ ] Deshabilitar botones según estado
- [ ] Tests manuales en UI

---

## Tarea P0#3: DetailPanel Funcional

### 📍 Descripción

Convertir `DetailPanel` en drawer lateral funcional con acciones (abrir, cerrar, bloquear, editar).

### 🎯 Deliverables

- ✅ Mostrar como drawer lateral (overlay)
- ✅ Botones de acción funcionales
- ✅ Formulario de bloqueo (MAINTENANCE_SET)
- ✅ Cerrar drawer con ESC o X
- ✅ Historial de eventos (opcional)

### 📋 Paso 1: Crear Drawer Component

**Ubicación**: `cattle-front/src/components/Paddock/drawer/drawer.jsx`

```jsx
import React from "react";
import "./drawer.css";

/**
 * Drawer lateral reutilizable
 * 
 * Props:
 *   - isOpen: boolean
 *   - onClose: () => void
 *   - title: string (opcional)
 *   - children: ReactNode
 */
export default function Drawer({ isOpen, onClose, title, children }) {
  if (!isOpen) return null;

  return (
    <>
      {/* Overlay (para cerrar al hacer click afuera) */}
      <div className="drawer-overlay" onClick={onClose} />

      {/* Drawer lateral */}
      <div className="drawer-root">
        <div className="drawer-header">
          {title && <h2 className="drawer-title">{title}</h2>}
          <button className="drawer-close" onClick={onClose}>✕</button>
        </div>

        <div className="drawer-content">
          {children}
        </div>
      </div>
    </>
  );
}
```

**CSS** (`drawer.css`):
```css
.drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.drawer-root {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 400px;
  background-color: white;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow-y: auto;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.drawer-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
}

.drawer-content {
  padding: 20px;
}
```

### 📋 Paso 2: Actualizar PaddockPage para usar Drawer

```jsx
export default function PaddockPage() {
  // ... estado existente ...
  const [selected, setSelected] = useState(null);

  return (
    <div className="paddockpage-root">
      {/* ... resto del dashboard ... */}

      {/* ✨ NUEVO: Drawer con DetailPanel */}
      <Drawer
        isOpen={!!selected}
        onClose={() => setSelected(null)}
        title={selected?.name}
      >
        {selected && (
          <DetailPanelEnhanced
            pasture={selected}
            onClose={() => setSelected(null)}
            onOpenPasture={handleOpenPasture}
            onClosePasture={handleClosePasture}
            onBlockPasture={handleBlockPasture}  // ✨ NUEVO
          />
        )}
      </Drawer>
    </div>
  );
}
```

### 📋 Paso 3: Crear DetailPanelEnhanced.jsx

**Ubicación**: `cattle-front/src/components/Paddock/detailPanel/DetailPanelEnhanced.jsx`

```jsx
import React, { useState } from "react";
import "./detailPanel.css";
import { RULES } from "../mocks/mockPastures";

/**
 * DetailPanel mejorado con acciones
 * 
 * Props:
 *   - pasture: Pasture
 *   - onClose: () => void
 *   - onOpenPasture: (pasture) => void
 *   - onClosePasture: (pasture) => void
 *   - onBlockPasture: (pasture, substatus, holdUntil) => void
 */
export default function DetailPanelEnhanced({
  pasture,
  onClose,
  onOpenPasture,
  onClosePasture,
  onBlockPasture
}) {
  const [showBlockForm, setShowBlockForm] = useState(false);
  const [blockForm, setBlockForm] = useState({ substatus: "FERTILIZANDO", holdUntil: "" });

  const rule = RULES[pasture.species];
  const entryTarget = pasture.species === "CUBA22"
    ? `${rule.entryHeightCm} cm`
    : `${rule.entryHeightCm} cm / ${rule.restDaysMin} d`;

  const handleBlockSubmit = () => {
    onBlockPasture(pasture, blockForm.substatus, blockForm.holdUntil);
    setShowBlockForm(false);
  };

  return (
    <div className="detailpanel-root">
      {/* SECCIÓN: General */}
      <section className="detailpanel-section">
        <h3>General</h3>
        <div className="detailpanel-grid">
          <div><span className="key">ID:</span> {pasture.pastureId}</div>
          <div><span className="key">Nombre:</span> {pasture.name}</div>
          <div><span className="key">Especie:</span> {pasture.species}</div>
          <div><span className="key">Área:</span> {pasture.areaHa} ha</div>
          <div><span className="key">Estado:</span> <strong>{pasture.status}</strong></div>
          <div><span className="key">Último uso:</span> {pasture.lastUseAt}</div>
        </div>
      </section>

      {/* SECCIÓN: Métricas */}
      <section className="detailpanel-section">
        <h3>Métricas</h3>
        <div className="detailpanel-grid">
          <div><span className="key">Descanso:</span> {pasture.daysRest} días</div>
          <div><span className="key">ETA apertura:</span> {pasture.etaOpenDays} días</div>
          <div><span className="key">Altura actual:</span> {pasture.currentHeightCm} cm</div>
          <div><span className="key">Residual previo:</span> {pasture.residualPrevCm} cm</div>
        </div>
      </section>

      {/* SECCIÓN: Reglas */}
      <section className="detailpanel-section">
        <h3>Objetivos (Plan)</h3>
        <div>Entrada: <strong>{entryTarget}</strong></div>
        <div>Residual: <strong>{rule.exitResidualCm} cm</strong></div>
      </section>

      {/* SECCIÓN: Acciones Rápidas */}
      <section className="detailpanel-section">
        <h3>Acciones</h3>
        <div className="detailpanel-actions">
          <button
            onClick={() => onOpenPasture(pasture)}
            disabled={pasture.status !== "DISPONIBLE"}
            className="btn btn-success"
          >
            🟢 Abrir
          </button>

          <button
            onClick={() => onClosePasture(pasture)}
            disabled={pasture.status !== "EN_USO"}
            className="btn btn-warning"
          >
            🔵 Cerrar
          </button>

          <button
            onClick={() => setShowBlockForm(!showBlockForm)}
            className="btn btn-danger"
          >
            🔴 Bloquear
          </button>
        </div>

        {/* ✨ Formulario de bloqueo */}
        {showBlockForm && (
          <div className="detailpanel-form">
            <div className="form-group">
              <label>Razón de bloqueo:</label>
              <select
                value={blockForm.substatus}
                onChange={e => setBlockForm({ ...blockForm, substatus: e.target.value })}
              >
                <option value="FERTILIZANDO">Fertilización</option>
                <option value="REPARANDO">Reparación</option>
                <option value="CUARENTENA">Cuarentena</option>
              </select>
            </div>

            <div className="form-group">
              <label>Liberar el (fecha):</label>
              <input
                type="date"
                value={blockForm.holdUntil}
                onChange={e => setBlockForm({ ...blockForm, holdUntil: e.target.value })}
              />
            </div>

            <div className="form-actions">
              <button onClick={handleBlockSubmit} className="btn btn-success">
                Bloquear
              </button>
              <button onClick={() => setShowBlockForm(false)} className="btn btn-gray">
                Cancelar
              </button>
            </div>
          </div>
        )}
      </section>

      {/* SECCIÓN: Notas */}
      {pasture.notes && (
        <section className="detailpanel-section">
          <h3>Notas</h3>
          <p>{pasture.notes}</p>
        </section>
      )}
    </div>
  );
}
```

### 📋 Paso 4: Agregar handler handleBlockPasture en PaddockPage

```javascript
const handleBlockPasture = async (pasture, substatus, holdUntil) => {
  try {
    setLoading(true);
    const response = await fetch(
      `https://xi0ygax4hg.execute-api.us-east-1.amazonaws.com/dev/farms/F001/pastures/${pasture.pastureId}/events`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          eventType: "MAINTENANCE_SET",
          user: "admin@farm.com",
          substatus,
          holdUntil
        })
      }
    );

    if (!response.ok) {
      throw new Error(`Error ${response.status}`);
    }

    await fetchPastures();
    setSelected(null);  // Cerrar drawer
  } catch (err) {
    setError("Error bloqueando potrero: " + err.message);
  } finally {
    setLoading(false);
  }
};
```

### ✅ Checklist

- [ ] Crear `Drawer.jsx` reutilizable
- [ ] Crear `DetailPanelEnhanced.jsx` con acciones
- [ ] Conectar botón Abrir (POST OPEN)
- [ ] Conectar botón Cerrar (POST CLOSE)
- [ ] Conectar botón Bloquear (POST MAINTENANCE_SET)
- [ ] Formulario de bloqueo con validaciones
- [ ] Cerrar drawer al presionar ESC
- [ ] Cerrar drawer al hacer click en overlay
- [ ] Tests manuales en UI
- [ ] Mejorar CSS (responsive, animaciones)

---

## Testing & Validación

### 🧪 Testing Manual

1. **Abrir Potrero**:
   - Ir a tabla
   - Seleccionar potrero con status "DISPONIBLE"
   - Click "Abrir"
   - Verificar: status cambia a "EN_USO", loading desaparece

2. **Cerrar Potrero**:
   - Seleccionar potrero con status "EN_USO"
   - Click "Cerrar"
   - Ingresaraltura residual (ej: 8 cm)
   - Verificar: status cambia a "EN_DESCANSO"

3. **Bloquear Potrero**:
   - Click "Bloquear"
   - Seleccionar razón (Fertilizando)
   - Elegir fecha liberación
   - Click Bloquear
   - Verificar: status = "MANTENIMIENTO", substatus visible

### 🧪 Testing Automatizado

```javascript
// PaddockPage.test.jsx
describe("PaddockPage - Event Handling", () => {
  test("handleOpenPasture calls POST endpoint", async () => {
    // Arrange
    const mockFetch = jest.fn();
    global.fetch = mockFetch;
    
    // Act
    await handleOpenPasture({ pastureId: "P001" });
    
    // Assert
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/events"),
      expect.objectContaining({ method: "POST" })
    );
  });

  test("handleClosePasture prompts for residual cm", () => {
    // Arrange
    const prompt = jest.fn(() => "8");
    window.prompt = prompt;
    
    // Act
    handleClosePasture({ pastureId: "P001" });
    
    // Assert
    expect(prompt).toHaveBeenCalled();
  });
});
```

---

## 📝 Resumen P0

| Tarea | Tiempo | Prioridad | Bloqueante |
|-------|--------|-----------|-----------|
| #1: Endpoint POST | 4-6h | 🔴 P0 | Sí (bloquea #2) |
| #2: Frontend buttons | 2-3h | 🔴 P0 | Sí (necesita #1) |
| #3: DetailPanel | 6-8h | 🔴 P0 | No (mejora UX) |
| **TOTAL** | **12-17h** | - | - |

**Timeline recomendado**:
- Semana 1: #1 + #2 (6-9 horas)
- Semana 2: #3 (6-8 horas)

---

## 🔗 Referencias

- [components-frontend.md](components-frontend.md): Detalles de componentes
- [events-architecture.md](events-architecture.md): PastureEvent sealed interface
- [pastures-overview.md](pastures-overview.md): Visión técnica completa
- [tasks-pending.md](tasks-pending.md): Todas las tareas (P0-P3)

---

**Generado**: 2026-01-09 | **Versión**: 1.0
