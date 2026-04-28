# HU-001: Consulta de Registros de Ordeño por Lactancia

**Fecha**: 2026-02-03 | **Versión**: 2.3 | **Prioridad**: Alta | **Estado**: Cerrada - implementada, validada y saneada documentalmente en su alcance

---

## 📋 Registro de Cambios

| Fecha | Versión | Descripción | Autor | Rol |
|-------|---------|-------------|-------|-----|
| 2026-04-27 | 2.3 | Barrido documental del alcance de la HU completado; sin referencias activas al contrato obsoleto y cierre del artefacto | GitHub Copilot | Validación y cierre documental |
| 2026-04-27 | 2.2 | Cobertura del flujo de lactancia completada en controller, service, processor y repository; estado actualizado a validación backend completa | GitHub Copilot | Desarrollo, validación y actualización documental |
| 2026-04-27 | 2.1 | Cobertura unitaria de controller completada para listado de vacas con lactancias y consulta por lactancia | GitHub Copilot | Desarrollo y validación |
| 2026-04-27 | 2.0 | Historia saneada y alineada con el código implementado, el contrato vigente y las brechas reales de pruebas | GitHub Copilot | Revisión y actualización documental |
| 2026-02-04 | 1.x | Implementación funcional registrada en backend y frontend | Equipo técnico | Desarrollo |
| 2026-02-03 | 1.0 | Historia creada | Equipo funcional | Product Owner |

---

## 📝 Descripción de la Historia

Como **productor ganadero**, quiero consultar los registros de ordeño filtrados por número de lactancia, para analizar la producción de leche de cada lactancia de forma independiente y comparar el rendimiento entre lactancias.

Adicionalmente, cuando se registra un nuevo ordeño, el sistema debe asociarlo a la lactancia activa del bovino y rechazar la operación si no existe una lactancia válida.

---

## 🎯 Objetivo

Consolidar la trazabilidad funcional y técnica del flujo de ordeño por lactancia, dejando explícito:

1. El comportamiento ya implementado en backend y frontend.
2. El contrato real actualmente expuesto por la API.
3. El estado de cierre documental dentro del alcance de la historia.

---

## ✅ Criterios de Aceptación

### AC-01: Consulta de vacas con lactancias
- Dado que existen vacas con al menos una lactancia registrada
- Cuando el usuario accede al módulo de ordeño
- Entonces el sistema muestra un selector con las vacas que tienen lactancias registradas

### AC-02: Consulta de lactancias por vaca
- Dado que el usuario selecciona una vaca
- Cuando se carga la información asociada
- Entonces el sistema muestra las lactancias disponibles para esa vaca

### AC-03: Consulta de registros de ordeño por lactancia
- Dado que el usuario selecciona una vaca y una lactancia
- Cuando ejecuta la consulta
- Entonces el sistema retorna únicamente los registros de ordeño correspondientes a esa lactancia
- Y el filtro por turno permanece opcional

### AC-04: Registro de ordeño asociado a lactancia activa
- Dado que se registra un nuevo ordeño para un bovino
- Cuando el sistema procesa la operación
- Entonces asigna automáticamente la lactancia activa del bovino
- Y si el bovino no tiene lactancias registradas o no tiene una lactancia activa válida, rechaza la operación con un error claro

---

## 📌 Estado Actual

La funcionalidad principal de esta historia ya se encuentra implementada.

### Backend
- El modelo de ordeño ya incluye `lactationNumber`, `gsi2pk` y `gsi2sk`.
- Existe consulta de vacas con lactancias registradas.
- Existe consulta de registros de ordeño por lactancia.
- El flujo de registro de ordeño asigna la lactancia activa del bovino al momento de persistir.
- La consulta por lactancia utiliza GSI2 para evitar recuperar todos los registros del bovino.
- La cobertura unitaria del flujo backend ya incluye controller, service, processor y repository para el slice principal de lactancia.

### Frontend
- El frontend ya consume el listado de vacas con lactancias.
- El frontend ya consume la consulta de registros de ordeño por lactancia.
- La selección de vaca y lactancia participa en la carga de información mostrada al usuario.

---

## 📐 Contrato Implementado

### Endpoints vigentes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/site/{siteId}/milking` | Lista vacas con lactancias registradas |
| GET | `/site/{siteId}/milking/{idBovine}/lactation/{lactationNumber}` | Obtiene registros de ordeño por lactancia |
| POST | `/site/{siteId}/milking` | Registra un ordeño y asigna la lactancia activa |

### Reglas de comportamiento relevantes
- El listado de vacas con lactancias se expone en la raíz del recurso `milking`, no en `/milking/cows`.
- El número de lactancia de entrada se normaliza antes de ejecutar la consulta por GSI2.
- El parámetro `shift` sigue siendo opcional en la consulta por lactancia.
- Los errores de validación del flujo de registro deben devolverse como respuesta de cliente inválido.

---

## 🧱 Diseño Consolidado

### Modelo de datos vigente

Ejemplo conceptual de un registro de ordeño con soporte de lactancia:

```json
{
  "PK": "BOVINE#172",
  "SK": "MILKING#2025-11-27#AM",
  "bovineId": 172,
  "date": "2025-11-27",
  "shift": "AM",
  "liters": 4.3,
  "lactationNumber": 2,
  "GSI2PK": "BOVINE#172#LACT#002",
  "GSI2SK": "2025-11-27#AM"
}
```

### Decisiones técnicas vigentes
- Se usa GSI2 para consultar ordeños por combinación bovino + lactancia.
- `lactationNumber` se conserva en el registro de ordeño para trazabilidad y respuesta al cliente.
- La asociación entre ordeño y lactancia se resuelve en el backend al momento de registrar el ordeño.
- El backend devuelve vacas con sus lactancias como una respuesta agregada.

---

## 🔎 Evidencia de Implementación

### Backend implementado
- Entidad de ordeño actualizada con soporte de lactancia.
- Servicio y repositorio con consulta por bovino y lactancia.
- Processor con lógica de:
  - obtención de vacas con lactancias
  - consulta de ordeños por lactancia
  - asignación de lactancia al registrar ordeño
- Controller con endpoint de listado y endpoint de consulta por lactancia.
- Suite backend alineada con cobertura directa del flujo en controller, service, processor y repository.

### Frontend implementado
- Servicio de consumo para listado de vacas con lactancias.
- Servicio de consumo para consulta de ordeños por lactancia.
- Integración del flujo de selección y consulta en la vista correspondiente.

---

## ⚠️ Pendientes Reales

No se identifican pendientes bloqueantes dentro del alcance funcional y documental inmediato de esta historia.

### Observaciones de mantenimiento
- Mantener vigilancia sobre divergencias futuras entre documentación satélite y contrato backend.
- Tratar la unificación amplia de terminología `OPEN` versus `LACTATING` como iniciativa transversal del repositorio, no como pendiente de cierre de esta HU.

---

## 🧪 Validación Funcional Esperada

### Escenarios cubiertos por la implementación
- Listar vacas con lactancias registradas.
- Obtener las lactancias disponibles de una vaca a partir de la respuesta agregada.
- Consultar registros de ordeño por lactancia.
- Registrar ordeños asociados a la lactancia activa del bovino.

### Escenarios que deben permanecer explícitos en QA
- Consulta sin resultados para una lactancia inexistente.
- Registro de ordeño para bovino sin lactancia activa.
- Normalización correcta del número de lactancia al consultar.
- Compatibilidad del frontend con el contrato actual del backend.

---

## ⚡ Riesgos y Observaciones

- Existe drift documental histórico entre el contrato originalmente propuesto y el contrato realmente expuesto por la API.
- Existe drift entre checklists anteriores de implementación y el estado efectivo del código actual.
- Existe drift entre algunos nombres de tests documentados y los archivos reales del proyecto.
- Si no se mantiene alineación documental, esta historia puede inducir cambios incompatibles con backend y frontend ya implementados.

---

## 📚 Referencias Técnicas

### Backend
- `MilkingController.java`
- `MilkingProcessor.java`
- `MilkingService.java`
- `MilkingRepository.java`
- `MilkingRecord.java`
- `CowWithLactationsDTO.java`
- `LactationSummaryDTO.java`

### Frontend
- `lactationService.js`

---

## 🧾 Decisión de Cierre

La historia se conserva como artefacto vigente y queda en estado de **cerrada**. El flujo principal cuenta con cobertura en controller, service, processor y repository, y el saneamiento documental inmediato del alcance de la HU quedó completado. Cualquier armonización documental adicional se considera trabajo transversal del repositorio y no bloquea el cierre de esta historia.