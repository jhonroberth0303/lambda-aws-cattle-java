# INC-001 — Post-mortem: CLOSE retorna estado DISPONIBLE

**Fecha del incidente:** 2026-06-10  
**Fecha de resolución:** 2026-06-11  
**Severidad:** Alta  
**Estado:** Resuelto

---

## Timeline

| Hora (UTC) | Evento |
|---|---|
| 2026-06-10 | Operario ejecuta CLOSE en P-04 con `residualCm: 8` |
| 2026-06-10 | Respuesta retorna `status: DISPONIBLE`, `etaOpenDays: 0` |
| 2026-06-10 | Incidente reportado y clasificado (INC-001) |
| 2026-06-11 | Diagnóstico confirmado — causa raíz en `enrichPatchForOperationalFields` |
| 2026-06-11 | Fix implementado, revisado y validado con 8 tests |

---

## Qué salió mal

El evento CLOSE no actualizaba `currentHeightCm` en DynamoDB al valor `residualCm` enviado en el payload. Esto provocó que el campo permaneciera en 20 cm (altura de entrada del plan), haciendo que `etaGrazing` calculara un gap de 0 cm y por tanto un `etaPorAltura = 0`. La función `deriveEffectiveStatus` recibe `etaOpenDays = min(28, 0) = 0` y deriva correctamente `DISPONIBLE`, pero con datos incorrectos como entrada.

El potrero quedó disponible para un nuevo ingreso de ganado sin haber iniciado su periodo de descanso real.

---

## Qué salió bien

- El campo `status = EN_DESCANSO` **sí se persistió correctamente** en DynamoDB. La falla era solo en la derivación del estado efectivo para la respuesta, no en el modelo de datos.
- `requirePositive(residualCm)` ya protegía el dominio contra valores nulos — la validación existía, solo faltaba propagar el valor.
- El diagnóstico fue completamente trazable desde la respuesta JSON hasta el código fuente sin necesidad de logs adicionales.

---

## Causa raíz

`enrichPatchForOperationalFields` en [PastureEventProcessor.java](../../src/main/java/com/cattle/processor/PastureEventProcessor.java) no incluía `patch.set("currentHeightCm", residualCm)` en el bloque CLOSE. El campo `residualCm` viajaba en el evento de dominio `CloseEvent` y se serializaba en el historial, pero no se propagaba al estado persistido de la entidad `Pasture`.

---

## Fix aplicado

**Archivo:** `src/main/java/com/cattle/processor/PastureEventProcessor.java`

```java
// Antes
if (event.type() == EventType.CLOSE) {
    patch.remove("blockReason");
    patch.set("lastUseAt", LocalDate.now(ZoneOffset.UTC).toString());
}

// Después — bloques CLOSE consolidados
if (event.type() == EventType.CLOSE) {
    patch.remove("blockReason");
    patch.set("lastUseAt", LocalDate.now(ZoneOffset.UTC).toString());
    patch.set("currentHeightCm", ((CloseEvent) event).residualCm());
    if (payload.getNotes() != null && !payload.getNotes().isBlank()) {
        patch.set("notes", payload.getNotes().trim());
    }
}
```

Tests añadidos: `applyEvent_closeEvent_updatesCurrentHeightCmToResidualCm` y `applyEvent_closeEvent_rejectsNullResidualCm`.

---

## Lecciones aprendidas

### 1. Todo campo que alimenta el ETA debe actualizarse en el evento que lo origina

`EtaCalculator.etaGrazing` depende de `currentHeightCm` y `lastUseAt`. Cada evento que modifica el estado físico del potrero (CLOSE = fin de pastoreo) debe actualizar los campos de los que depende el ETA. Esta responsabilidad debe revisarse explícitamente en cada nuevo tipo de evento.

**Acción:** Agregar en el checklist de implementación de nuevos eventos: *"¿Qué campos del ETA son afectados por este evento? ¿Están siendo actualizados en el patch?"*

### 2. `deriveEffectiveStatus` aplica min() — un solo ETA en cero lo tumba todo

La función `etaGrazing` usa `min(etaPorDias, etaPorAltura)`. Si cualquiera de los dos componentes es 0, el resultado es 0 y el potrero aparece DISPONIBLE. El comportamiento es correcto como función de evaluación de disponibilidad, pero requiere que todos los campos de entrada sean precisos post-evento.

**Acción:** Documentar en `EtaCalculator` que la semántica de `min` implica que ambas condiciones deben satisfacerse para que el potrero entre en descanso efectivo.

### 3. Los null checks silenciosos en invariantes ya garantizadas crean trampas

El fix inicial incluía `if (event instanceof CloseEvent ce && ce.residualCm() != null)`. La revisión detectó que ese null check silencioso creaba una ruta de falla invisible: si la invariante de `requirePositive` se relaja en el futuro, el campo no se actualiza y el bug reaparece sin señal. Se eliminó el null check en favor del cast directo.

**Regla:** Si una invariante ya está garantizada por el dominio, no protegerla con un null check silencioso — usar el tipo directamente o lanzar excepción explícita.

### 4. Los bloques de enriquecimiento para el mismo evento deben estar consolidados

El método tenía dos bloques `if (event.type() == EventType.CLOSE)` separados por otros eventos. Esto incrementa el riesgo de inserciones inconsistentes futuras y dificulta la lectura de qué mutaciones aplica un CLOSE. Se consolidaron en uno.

---

## Riesgos residuales

| Riesgo | Severidad | Acción |
|---|---|---|
| `residualCm == entryHeightCm` produce ETA altura = 0 → DISPONIBLE inmediato | Baja | Evaluar si el negocio requiere que CLOSE siempre imponga al menos `restDaysMin` independiente de la altura |
| Consistencia eventual en DynamoDB: `getRotationSemaphoreItems` puede leer réplica desactualizada | Baja (preexistente) | Evaluar uso de consistent read en la lectura post-patch |
| `substatus` en payload de CLOSE es ignorado silenciosamente | Media (gap funcional) | Definir con negocio si FERTILIZACION u otros subtipos de descanso deben modelarse como MAINTENANCE_SET separado o como campo adicional en CLOSE |

---

## Pendientes

- [ ] Test de integración end-to-end que verifique la cadena completa: `residualCm → currentHeightCm → etaGrazing → deriveEffectiveStatus → EN_DESCANSO`
- [ ] Definición de negocio sobre el `substatus` en CLOSE (FERTILIZACION, DESCANSO_SIMPLE, etc.)
- [ ] Revisar si otros eventos (HEIGHT_MEASURED con `heightCm`) actualizan `currentHeightCm` consistentemente
