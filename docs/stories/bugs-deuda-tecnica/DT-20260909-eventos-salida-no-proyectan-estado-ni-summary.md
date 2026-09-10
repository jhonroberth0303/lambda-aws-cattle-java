# DT-20260909: Los eventos de salida (MUERTE/VENTA) no proyectan estado ni se reflejan en el summary

**ID**: DT-20260909-eventos-salida-no-proyectan-estado-ni-summary
**Tipo**: Bug funcional + deuda técnica estructural
**Prioridad**: Alta
**Fecha**: 2026-09-09
**Estado**: Hallazgo D ✅ resuelto (2026-09-10) · A parcialmente mitigado · B y C abiertos
**Componentes**: `BovineEventProcessor`, `BovineSummaryService`, `LifecycleRecalculationService`, `ProductiveStateCalculator`, `cattle-front/BovineEventPanel`

## Actualización 2026-09-10 (desde EP-20260909 Fase 2)

- **D — ✅ resuelto**: `BovineEventProcessor.enforceBovineIsRegistrable` rechaza eventos sobre bovinos inexistentes (id numérico sin identidad → `NotFoundException`) o dados de baja (`enabled == false` / `LifecycleStatus` ∈ {SOLD, DEAD, CULLED, TRANSFERRED, INACTIVE}), salvo MUERTE/VENTA. Front: `isBovineInactive()` en `domain/bovines.js` reemplaza el guard roto `status === "VENDIDO"/"MUERTO"` del panel.
- **A — parcialmente mitigado**: ya no se pueden *añadir* eventos a un animal de baja, pero **sigue sin existir la proyección** MUERTE/VENTA → `LifecycleStatus`. Registrar una muerte no cambia el estado del bovino ni el summary. Pendiente.
- **B y C** (estado productivo/alertas de animales inactivos, `farmId: null`): sin cambios.

## Trazabilidad

- Origen: prueba manual de la Fase 2 de `EP-20260909-configuracion-catalogos-i18n` (formularios schema-driven). Se registró un evento **MUERTE** sobre el bovino **172 (Mia)** — una vaca **inactiva** (`enabled: false`) y con estado **`SOLD`** — y luego se ejecutó `summary-refresh`.
- Evidencia: `docs/stories/configuracion/summaryResponse.json` (respuesta de `GET /summary`, `updatedAt` ~`2026-09-10T04:26Z`).
- **No es una regresión de la Fase 2.** La Fase 2 solo cambió la validación del payload (contra `event-forms.yml`); el evento se persiste igual que antes. El evento MUERTE se creó correctamente en el timeline. Lo que falla es la ausencia de proyección evento → perfil/estado, que ya existía antes.

## 1. Contexto y comportamiento observado

Tras `MUERTE` sobre el bovino 172 + `summary-refresh`, en `GET /summary` el bovino 172 sigue mostrando:

```json
{
  "bovineId": 172, "name": "Mia",
  "farmId": null,
  "status": "SOLD",          // NO cambió a DEAD
  "enabled": false,
  "isLactating": true,
  "lactationStatus": "LACTATING",
  "productiveState": "OPEN_LACTATING",
  "alerts": ["HEAT_WATCH"],  // alerta de celo sobre una vaca fuera de inventario
  "daysInLactation": 286
}
```

Comparación: los bovinos 168–171 (también `SOLD`, `enabled: false`) salen limpios (`isLactating: false`, `alerts: []`, `productiveState: OPEN`), pero solo porque no arrastran una lactancia activa colgada — no porque el cálculo los proteja.

## 2. Hallazgos

### 🔴 A. No existe capa de proyección de eventos a perfiles/estado (causa raíz)

- [`BovineEventProcessor.applyEvent`](../../../src/main/java/com/cattle/processor/BovineEventProcessor.java) hace: `validar → construir BovineEventItem → bovineEventService.save(item) → return`. **No carga el bovino, no toca `ProfileLifecycle`, `ProfileReproductive` ni `ProfileLactancy`.** MUERTE, VENTA, PARTO, SECADO, DESTETE, CASTRACION… quedan solo como registro en el timeline.
- [`BovineSummaryService.buildSummary`](../../../src/main/java/com/cattle/services/BovineSummaryService.java) (líneas ~170-291) se construye **exclusivamente** desde los perfiles (`lifecycle`, `reproductive`, `lactancy`, `pregnancy`). Nunca lee items `EVENT#`.
- [`LifecycleRecalculationService.recalculate`](../../../src/main/java/com/cattle/services/LifecycleRecalculationService.java) (líneas 72-104) solo deriva `lifeStage`/`category` por edad. TODO explícito en la línea 79: *"isCastrated should come from EVENT#CASTRATION when events are implemented"*.

**Impacto**: registrar una muerte o una venta **no cambia nada** que el summary muestre. `LifecycleStatus.DEAD` solo se vería editando el perfil a mano. El estado `SOLD` de la 172 ya venía de dato semilla o de una venta previa que tampoco proyectó.

**Recomendación**: crear una historia para la capa de proyección de eventos de salida:
- MUERTE → `LifecycleStatus.DEAD` + `enabled = false` + cerrar `ProfileLactancy`/`ProfilePregnancy` activas.
- VENTA → `LifecycleStatus.SOLD` + `enabled = false` + cerrar lactancia/preñez.
- Decidir dónde vive la proyección: síncrona en `BovineEventProcessor` tras `save`, o un proyector aparte disparado por el stream de DynamoDB / el `summary-refresh`.

### 🟠 B. `ProductiveStateCalculator` ignora `status`/`enabled` — alertas falsas en animales fuera de inventario

- [`BovineSummaryService.buildSummary`](../../../src/main/java/com/cattle/services/BovineSummaryService.java) líneas ~244-252: `productiveStateCalculator.calculate(...)` recibe preñez + lactancia, **no** recibe `status` ni `enabled`.
- Bovino 172: `status: SOLD`, `enabled: false`, pero `productiveState: OPEN_LACTATING` y `alerts: ["HEAT_WATCH"]` por una `ProfileLactancy` en `LACTATING` que nunca se cerró.

**Impacto**: KPIs productivos y alertas de celo falsas sobre vacas vendidas/muertas; ruido en tableros y notificaciones.

**Recomendación**: en `buildSummary`, si `enabled == false` o `status ∈ {SOLD, DEAD, CULLED, TRANSFERRED}` → no invocar al calculator (o forzar `productiveState` neutro, `alerts = []`, `daysInLactation = null`). Opcional: pasar el estado del ciclo de vida como entrada del calculator.

### 🟠 C. `farmId: null` en bovinos 172 y 174

- Ambos "Native"; el resto del hato tiene `FARM#001`. [`BovineSummaryService.buildSummary`](../../../src/main/java/com/cattle/services/BovineSummaryService.java) línea ~267 propaga el nulo tal cual.
- Peor: [`LifecycleRecalculationService`](../../../src/main/java/com/cattle/services/LifecycleRecalculationService.java) línea 82 — con `farmId` nulo usa `"default"` para las reglas de categoría, así que esos animales se categorizan con un ruleset que no es el de su finca.

**Impacto**: datos incompletos + categorización potencialmente incorrecta.

**Recomendación**: validar `farmId` no nulo al crear/importar bovino; decidir si el summary debe excluir o marcar bovinos sin finca. Revisar el dato semilla de la 172/174.

### 🟡 D. El guard `isBaja` del front nunca se activa con estados reales del backend

- [`cattle-front/src/components/Bovines/eventPanel/BovineEventPanel.jsx`](../../../../cattle-front/src/components/Bovines/eventPanel/BovineEventPanel.jsx) línea ~85: `isBaja = bovine?.status === "VENDIDO" || bovine?.status === "MUERTO"`.
- El backend (`LifecycleStatus`) emite `OPEN` / `SOLD` / `DEAD` / `TRANSFERRED` — nunca `"VENDIDO"` / `"MUERTO"` (esas cadenas solo viven en un mapa legacy de `cattle-front/src/domain/bovines.js` líneas ~124-125).
- Por eso se pudo abrir el formulario de MUERTE sobre una vaca vendida: el panel vio `status: "SOLD"`, `isBaja` fue `false` y mostró todos los botones, incluido "Registrar muerte".
- Es pre-existente, pero la Fase 2 hace que ese botón renderice ahora `SchemaEventForm`, así que el flujo roto pasa por el código nuevo.

**Impacto**: se registran eventos (muerte, venta, monta…) sobre animales ya dados de baja, sin barrera en el front (D) ni en el backend (A — `BovineEventProcessor` ni carga el bovino).

**Recomendación**:
- Front: alinear el guard con `LifecycleStatus` (`SOLD`/`DEAD`/`CULLED`/`TRANSFERRED`/`INACTIVE`) o usar directamente `enabled === false`.
- Backend: que `BovineEventProcessor` valide que el bovino existe y está `enabled` antes de aceptar el evento (con excepción explícita para el propio evento que causa la baja).

## 3. Qué NO es

- No es un fallo del formulario schema-driven de la Fase 2: MUERTE valida (`cause` requerido) y persiste correctamente.
- No es un fallo de `summary-refresh` en sí: hace lo que le toca (reconstruir desde perfiles). El problema es que los perfiles nunca se actualizan por eventos.

## 4. Alcance sugerido (separado en 2+ historias)

| Historia | Contenido | Prioridad |
|---|---|---|
| **Proyección de eventos de salida** | Hallazgo A: MUERTE/VENTA → `LifecycleStatus` + `enabled` + cierre de lactancia/preñez. Barrera de `BovineEventProcessor` (hallazgo D backend). | Alta |
| **Consistencia del summary para animales inactivos** | Hallazgos B y C: suprimir estado productivo/alertas y normalizar `farmId` para `enabled == false`. | Alta |
| **Guard de baja en el front** | Hallazgo D front: alinear `isBaja` con `LifecycleStatus` / `enabled`. | Media |

## 5. Definition of Done (orientativo)

- Registrar MUERTE sobre un bovino `OPEN` + `summary-refresh` → `status: DEAD`, `enabled: false`, `isLactating: false`, `alerts: []`, `productiveState` neutro.
- Registrar VENTA → `status: SOLD` con el mismo saneamiento.
- `GET /summary` no devuelve `productiveState` productivo ni alertas para ningún bovino con `enabled == false`.
- Ningún bovino en `GET /summary` con `farmId: null` (o política explícita documentada).
- El panel de eventos del front oculta las acciones para bovinos `SOLD`/`DEAD`/`CULLED`/`TRANSFERRED`/`INACTIVE`.
- Tests: proyección en `BovineEventProcessorTest` / test de integración; `BovineSummaryServiceTest` cubre el saneamiento por `enabled`.
