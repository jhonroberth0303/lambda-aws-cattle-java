# 📋 Tareas Faltantes: Módulo Potreros (Pastures)

**Fecha de Generación**: 2026-01-08

**Estado General**: 🟡 **Parcialmente implementado**

Este documento lista tareas identificadas durante el análisis del módulo de Potreros. Se clasifican por prioridad, complejidad y área (Frontend, Backend, Testing, Documentación).

---

## 🎯 Resumen Ejecutivo

**Completado**:
- ✅ Modelo de datos (TABLE_PASTURE, TABLE_PLAN)
- ✅ Motor de rotación (PastureStatusEngine, EtaCalculator)
- ✅ Dashboard básico (UI con filtros, KPIs, tabla, semáforo)
- ✅ Servicio de lectura (GET /farms/{farmId}/pastures)
- ✅ Mapeo Entity → DTO

**Pendiente** (~25 tareas):
- ❌ Eventos OPEN/CLOSE (backend endpoint)
- ❌ Edición de potreros (PUT)
- ❌ Creación de potreros (POST)
- ❌ Bloqueo/Mantenimiento de potreros
- ❌ Tests de integración y unitarios
- ❌ Componente DetailPanel funcional
- ❌ Panel de edición de potrero
- ❌ Calendario funcional
- ❌ Centro de alertas con datos reales
- ❌ Validaciones en frontend
- ❌ Manejo de errores mejorado
- ❌ Documentación de API REST

---

## 📊 Tareas por Prioridad

### 🔴 CRÍTICO (P0)

#### 1. **Backend: Endpoint POST para Eventos (OPEN/CLOSE/MAINTENANCE)**

**Descripción**: Implementar endpoint que aplique eventos a potreros usando `PastureEvent` (sealed interface).

**Endpoint**: `POST /farms/{farmId}/pastures/{pastureId}/events`

**Payload Esperado** (ejemplos por tipo):

```json
// OPEN Event
{
  "eventType": "OPEN",
  "user": "juan.perez@farm.com",
  "lotId": "LOT001",
  "animals": 15
}

// CLOSE Event
{
  "eventType": "CLOSE",
  "user": "juan.perez@farm.com",
  "lotId": "LOT001",
  "animals": 15,
  "residualCm": 8
}

// MAINTENANCE_SET Event
{
  "eventType": "MAINTENANCE_SET",
  "user": "juan.perez@farm.com",
  "substatus": "FERTILIZANDO",
  "holdUntil": "2025-12-15"
}

// MAINTENANCE_CLEAR Event
{
  "eventType": "MAINTENANCE_CLEAR",
  "user": "juan.perez@farm.com"
}
```

**Componentes a Crear/Modificar**:
- Crear `PastureEventController.java` (exponer endpoint).
- Crear DTO `PastureEventRequest.java` (deserializar payload).
- Crear método `applyEvent(String pastureId, PastureEventRequest request)` en `PastureService`.
- Convertir payload a instancia de `PastureEvent` (OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent).
- Usar `PastureStatusEngine.applyEvent(pasture, plan, event)` para obtener cambios.
- Guardar con `PastureRepository.update(pk, entityPatch)`.

**Lógica Interna**:
```java
@PostMapping("/{pastureId}/events")
public ResponseEntity<?> applyEvent(
    @PathVariable String farmId,
    @PathVariable String pastureId,
    @RequestBody PastureEventRequest request) {
    
    // 1. Buscar potrero
    Pasture pasture = pastureRepository.findById("PASTURE#" + pastureId).orElseThrow();
    
    // 2. Buscar plan
    Plan plan = planRepository.findByFarmAndSpecies(farmId, pasture.getSpecies()).orElseThrow();
    
    // 3. Convertir request a PastureEvent (switch o mapper)
    PastureEvent event = requestToEvent(request);
    
    // 4. Aplicar evento
    EntityPatch patch = pastureStatusEngine.applyEvent(pasture, plan, event);
    
    // 5. Guardar
    pastureRepository.update(pasture.getPk(), patch);
    
    // 6. Retornar potrero actualizado
    return ResponseEntity.ok(pasture);
}
```

**Complejidad**: Media
**Tiempo Estimado**: 4-6 horas

---

#### 2. **Frontend: Conectar Botones Abrir/Cerrar**

**Descripción**: Implementar lógica en botones "Abrir" y "Cerrar" de PastureTable.

**Ubicación**: `cattle-front/src/components/Paddock/pastureTable/pastureTable.jsx`

**Cambios**:
- Pasar callback `onOpen`, `onClose` a PastureTable.
- En PaddockPage, definir handlers que llamar endpoint POST.
- Mostrar loading/error mientras se procesa.
- Actualizar estado local tras éxito.

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

#### 3. **Frontend: DetailPanel Funcional**

**Descripción**: Convertir DetailPanel en drawer/modal que muestre detalles completos y permita acciones.

**Ubicación**: `cattle-front/src/components/Paddock/detailPanel/detailPanel.jsx`

**Funcionalidades**:
- Mostrar todos los atributos del potrero (name, species, area, status, ETA, etc.).
- Botones de acción: Abrir, Cerrar, Bloquear (con fecha), Editar.
- Mostrar plan de rotación (restDays, minHeightCm, growthRate).
- Historial de eventos (últimas 10 transiciones).

**Complejidad**: Media
**Tiempo Estimado**: 6-8 horas

---

### 🟠 ALTO (P1)

#### 4. **Backend: Endpoint PUT para Editar Potrero**

**Descripción**: Permitir editar atributos de un potrero (nombre, notas, área, especie, etc.).

**Endpoint**: `PUT /farms/{farmId}/pastures/{pastureId}`

**Payload**:
```json
{
  "name": "Nuevo nombre",
  "species": "Pasto azul",
  "areaHa": 3.0,
  "notes": "Observaciones actualizadas"
}
```

**Complejidad**: Baja
**Tiempo Estimado**: 3-4 horas

---

#### 5. **Backend: Endpoint POST para Crear Potrero**

**Descripción**: Crear nuevo potrero en finca.

**Endpoint**: `POST /farms/{farmId}/pastures`

**Payload**:
```json
{
  "name": "Potrero Nuevo",
  "species": "Kikuyo",
  "areaHa": 2.5,
  "establishmentDate": "2025-12-08",
  "initialHeightCm": 15
}
```

**Complejidad**: Media
**Tiempo Estimado**: 4-5 horas

---

#### 6. **Frontend: Componente Modal de Bloqueo/Mantenimiento**

**Descripción**: Modal para registrar bloqueo de potrero con fecha y razón.

**Ubicación**: Nuevo componente `cattle-front/src/components/Paddock/maintenanceModal/MaintenanceModal.jsx`

**Funcionalidades**:
- Input de fecha (holdUntil).
- Select de razón (Fertilización, Reparación, Cuarentena, Otro).
- Textbox de observaciones.
- Botón Guardar y Cancelar.

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

#### 7. **Frontend: Validaciones en Formularios**

**Descripción**: Añadir validaciones en todos los formularios (crear, editar, bloquear).

**Validaciones**:
- Nombre no vacío.
- Área > 0.
- Especie seleccionada.
- Fecha de establecimiento válida.
- holdUntil > fecha actual (para bloqueo).

**Ubicación**: Crear utilidad `cattle-front/src/components/Paddock/validators/pastureValidators.js`

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

#### 8. **Backend: Tests Unitarios para PastureStatusEngine**

**Descripción**: Completa suite de tests para motor de estados.

**Ubicación**: `cattle-lambda-function/src/test/java/com/cattle/utils/PastureStatusEngineTest.java`

**Casos**:
- [x] Transición OPEN
- [x] Transición CLOSE
- [ ] Evento MAINTENANCE_SET
- [ ] Evento MAINTENANCE_CLEAR
- [ ] Expiración automática de holdUntil
- [ ] Bloqueo efectivo
- [ ] Derivación de estado efectivo

**Complejidad**: Media
**Tiempo Estimado**: 4-5 horas

---

#### 9. **Backend: Tests para EtaCalculator**

**Ubicación**: Crear `cattle-lambda-function/src/test/java/com/cattle/utils/EtaCalculatorTest.java`

**Casos**:
- [ ] ETA con altura deficit > 0
- [ ] ETA con días desde uso = 0
- [ ] ETA negativo (potrero listo)
- [ ] ETA con growthRate fraccional

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

### 🟡 MEDIO (P2)

#### 10. **Frontend: Calendario Funcional**

**Descripción**: Conectar CalendarMini para mostrar rotaciones proyectadas.

**Ubicación**: `cattle-front/src/components/Paddock/calendarMini/calendarMini.jsx`

**Funcionalidades**:
- Mostrar mes actual/seleccionado.
- Indicar potreros disponibles por día (proyección).
- Seleccionar mes para ver diferentes períodos.
- Click en día → filtrar potreros disponibles ese día.

**Complejidad**: Media
**Tiempo Estimado**: 5-6 horas

---

#### 11. **Frontend: AlertCenter con Datos Reales**

**Descripción**: Conectar centro de alertas para mostrar eventos reales de rotación.

**Ubicación**: `cattle-front/src/components/Paddock/alertCenter/alertCenter.jsx`

**Tipos de Alertas**:
- Potrero completó descanso (ETA <= 0).
- Potrero en mantenimiento próximo a finalizar.
- Exceso de potreros bloqueados (< 20% disponible).
- Potrero usado > días óptimos.

**Complejidad**: Media
**Tiempo Estimado**: 4-5 horas

---

#### 12. **Backend: Endpoint GET para Historial de Eventos**

**Descripción**: Obtener historial de transiciones de estado de un potrero.

**Endpoint**: `GET /farms/{farmId}/pastures/{pastureId}/events?limit=20`

**Retorno**: Lista de `{ eventType, timestamp, reason, fromStatus, toStatus }`

**Complejidad**: Media
**Tiempo Estimado**: 3-4 horas

---

#### 13. **Backend: Auditoria de Cambios (EntityPatch → Log)**

**Descripción**: Registrar quién y cuándo cambió cada potrero.

**Tabla Nueva**: `TABLE_AUDIT_LOGS` o atributo `auditLog` en `TABLE_PASTURE`

**Campos**:
- `timestamp`
- `userId` (quién hizo el cambio)
- `operation` (PUT, PATCH, DELETE, EVENT)
- `changes` (diff)

**Complejidad**: Media
**Tiempo Estimado**: 4-5 horas

---

#### 14. **Frontend: Componente EditorPanel**

**Descripción**: Panel para editar atributos de potrero.

**Ubicación**: Nuevo componente `cattle-front/src/components/Paddock/editorPanel/EditorPanel.jsx`

**Campos Editables**:
- Nombre
- Especie
- Área (ha)
- Notas

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

#### 15. **Frontend: Integración de react-calendar o librería similar**

**Descripción**: Reemplazar CalendarMini con componente robusto.

**Opciones**:
- `react-calendar`
- `react-big-calendar`
- Componente custom mejorado

**Complejidad**: Media
**Tiempo Estimado**: 3-4 horas

---

### 🟢 BAJO (P3)

#### 16. **Documentación: OpenAPI/Swagger para Endpoints Pastures**

**Descripción**: Especificar todos los endpoints con OpenAPI 3.0.

**Ubicación**: `docs/pastures/api-spec.yaml`

**Endpoints a Documentar**:
- GET /farms/{farmId}/pastures
- POST /farms/{farmId}/pastures
- PUT /farms/{farmId}/pastures/{pastureId}
- DELETE /farms/{farmId}/pastures/{pastureId}
- POST /farms/{farmId}/pastures/{pastureId}/events
- GET /farms/{farmId}/pastures/{pastureId}/events

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

#### 17. **Backend: Integración con Sistema de Notificaciones (SNS/SQS)**

**Descripción**: Publicar eventos de rotación para consumo de otros servicios.

**Eventos a Publicar**:
- PastureOpened
- PastureClosed
- PastureMaintenanceSet
- PastureAvailable (ETA <= 0)

**Complejidad**: Media
**Tiempo Estimado**: 4-5 horas

---

#### 18. **Frontend: Estadísticas y Reportes**

**Descripción**: Añadir sección de reportes/analytics para potreros.

**Métricas**:
- % disponibilidad por semana/mes
- Altura promedio
- Uso promedio por especie
- Tiempo promedio de descanso

**Complejidad**: Media
**Tiempo Estimado**: 5-6 horas

---

#### 19. **Backend: Endpoint DELETE para Eliminar Potrero**

**Descripción**: Eliminar un potrero (soft delete con flag).

**Endpoint**: `DELETE /farms/{farmId}/pastures/{pastureId}`

**Lógica**:
- Marcar como `enabled: false` en lugar de borrar físicamente.

**Complejidad**: Baja
**Tiempo Estimado**: 1-2 horas

---

#### 20. **Frontend: Exportar Datos a CSV/Excel**

**Descripción**: Botón para descargar tabla de potreros en CSV.

**Ubicación**: Botón en PaddockPage header.

**Complejidad**: Baja
**Tiempo Estimado**: 1-2 horas

---

#### 21. **Backend: Soporte Multi-tenant**

**Descripción**: Validar que farmId del usuario coincida con farmId del potrero.

**Ubicación**: Interceptor o AOP en controller.

**Complejidad**: Media
**Tiempo Estimado**: 2-3 horas

---

#### 22. **Frontend: Modo Oscuro para Dashboard**

**Descripción**: Añadir tema oscuro a PaddockPage.

**Ubicación**: Archivo `PaddockPage-dark.css` o usar CSS variables.

**Complejidad**: Baja
**Tiempo Estimado**: 1-2 horas

---

#### 23. **Tests de Integración E2E**

**Descripción**: Tests con Cypress/Playwright para flujos críticos.

**Flujos**:
- Listar potreros.
- Abrir/Cerrar potrero.
- Bloquear con mantenimiento.
- Editar potrero.

**Complejidad**: Media
**Tiempo Estimado**: 5-6 horas

---

#### 24. **Backend: Performance y Caching**

**Descripción**: Implementar caché para planes de rotación (no cambian frecuentemente).

**Estrategia**: Redis o caché local con TTL.

**Complejidad**: Media
**Tiempo Estimado**: 3-4 horas

---

#### 25. **Frontend: Responsive Design Mejorado**

**Descripción**: Optimizar UI para móvil y tablets.

**Cambios**:
- Ocultar columnas en vista mobile (mostrar solo ID, nombre, estado).
- Drawer responsivo.
- Tabla con horizontal scroll en móvil.

**Complejidad**: Baja
**Tiempo Estimado**: 2-3 horas

---

## 📈 Roadmap Recomendado

### Fase 1: Funcionalidad Básica (Semanas 1-2)
**Objetivo**: Completar flujos CRUD y eventos.

1. ✅ Endpoint POST para Eventos (OPEN/CLOSE/MAINTENANCE)
2. ✅ Conectar botones en frontend (Abrir/Cerrar)
3. ✅ Endpoint PUT para Editar potrero
4. ✅ DetailPanel funcional
5. ✅ Validaciones en formularios

**Hito**: Dashboard completamente funcional para consulta y acciones básicas.

---

### Fase 2: Robustez y Testing (Semanas 3-4)
**Objetivo**: Tests y manejo de errores.

6. ✅ Tests unitarios para PastureStatusEngine
7. ✅ Tests para EtaCalculator
8. ✅ Endpoint GET para Historial
9. ✅ Auditoria de cambios
10. ✅ Manejo mejorado de errores (backend + frontend)

**Hito**: Código robusto, testeado, con trazabilidad de cambios.

---

### Fase 3: UX y Features Avanzadas (Semanas 5-6)
**Objetivo**: Mejora de experiencia y features nice-to-have.

11. ✅ Calendario funcional
12. ✅ AlertCenter con datos reales
13. ✅ Endpoint POST para Crear potrero
14. ✅ Componente EditorPanel
15. ✅ Documentación OpenAPI

**Hito**: Dashboard con UX mejorada y capacidad de crear nuevos potreros.

---

### Fase 4: Optimización y Escala (Semanas 7+)
**Objetivo**: Performance, notificaciones, multi-tenant.

16. ✅ Integración SNS/SQS
17. ✅ Caching y performance
18. ✅ Soporte multi-tenant
19. ✅ Reportes y estadísticas
20. ✅ Tests E2E

**Hito**: Sistema listo para producción con escalabilidad.

---

## 🔗 Dependencias Entre Tareas

```
                       ┌─────────────────────────────┐
                       │ Modelo Datos (completado)   │
                       └──────────────┬──────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
         ┌──────────▼────────┐  ┌─────▼──────────┐  ┌──▼──────────────┐
         │ EndPoint Events   │  │ EndPoint PUT   │  │ EndPoint POST   │
         │ (CRITICAL)        │  │ (ALTO)         │  │ (ALTO)          │
         └──────────┬────────┘  └────────────────┘  └─────────────────┘
                    │
         ┌──────────▼────────────────────────┐
         │ Frontend: Conectar botones (CRIT) │
         └──────────┬───────────────────────┘
                    │
         ┌──────────▼────────────────────────┐
         │ DetailPanel Funcional (CRIT)      │
         └──────────┬───────────────────────┘
                    │
         ┌──────────▼────────────────────────┐
         │ Tests: StatusEngine, EtaCalc      │
         │ (ALTO)                            │
         └──────────┬───────────────────────┘
                    │
         ┌──────────▼────────────────────────┐
         │ Calendario, AlertCenter (MEDIO)   │
         └───────────────────────────────────┘
```

---

## 📊 Estimación Global

| Categoría | Horas | Días (@ 8h/día) |
|-----------|-------|-----------------|
| Crítico (P0) | 12-17 | 2-3 |
| Alto (P1) | 24-35 | 3-5 |
| Medio (P2) | 25-35 | 3-5 |
| Bajo (P3) | 15-25 | 2-3 |
| **TOTAL** | **76-112** | **10-16** |

**Recomendación**: Distribuir en 3-4 sprints de 2 semanas cada uno, priorizando P0 y P1 para MVP.

---

## 🎯 Criterios de Aceptación (Definition of Done)

Para marcar una tarea como **completada**:

1. ✅ Código escrito y funcional.
2. ✅ Tests unitarios (cobertura > 80%).
3. ✅ Tests de integración (si aplica).
4. ✅ Code review aprobado.
5. ✅ Documentación actualizada (docstrings, README).
6. ✅ Sin advertencias de linter/compiler.
7. ✅ Testeado en dev, staging y producción (si P0/P1).

---

## 📞 Contacto y Escalación

Para preguntas o bloqueos, contactar a:
- **Arquitectura**: revisar `docs/pastures/pastures-overview.md`
- **Backend**: revisar `cattle-lambda-function/src/main/java/com/cattle/`
- **Frontend**: revisar `cattle-front/src/components/Paddock/`
