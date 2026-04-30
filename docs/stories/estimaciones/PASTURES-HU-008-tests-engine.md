# PASTURES-HU-008: Tests para PastureStatusEngine

**ID**: PASTURES-HU-008  
**Tipo**: Estimación / testing  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Definir la necesidad de cobertura automatizada sobre `PastureStatusEngine`, que concentra reglas relevantes de transición de estado en el dominio de potreros.

## Evidencia vigente

- existe `src/main/java/com/cattle/utils/PastureStatusEngine.java`
- existe `src/test/java/com/cattle/utils/PastureStatusEngineTest.java`
- el flujo soportado del proyecto para ejecutar pruebas usa Gradle Wrapper y JaCoCo

## Corrección documental importante

La versión anterior de esta HU proponía comandos y checklist basados en Maven. Para el estado actual del proyecto, la referencia vigente debe ser:

```bash
./gradlew test --tests "*PastureStatusEngineTest"
./gradlew test jacocoTestReport
```

## Qué sigue siendo válido de la intención original

- cubrir transiciones OPEN, CLOSE y mantenimiento
- cubrir transiciones inválidas
- cubrir casos límite del motor
- mantener pruebas determinísticas y rápidas

## Qué no debe asumirse automáticamente

- un porcentaje exacto de cobertura ya logrado si no se revalida con reporte actual
- que el checklist histórico refleje exactamente los nombres o fixtures actuales del proyecto

## Uso recomendado

Esta HU sirve como recordatorio de prioridad de testing. Si se reabre trabajo sobre el motor de estados, conviene actualizarla a partir del reporte JaCoCo real y de los tests actualmente presentes en `src/test/java`.