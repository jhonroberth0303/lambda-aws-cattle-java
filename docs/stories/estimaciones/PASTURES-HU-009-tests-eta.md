# PASTURES-HU-009: Tests para EtaCalculator

**ID**: PASTURES-HU-009  
**Tipo**: Estimación / testing  
**Estado actual**: Revisión documental parcial - Abril 2026

## Objetivo

Mantener trazabilidad sobre la necesidad de pruebas del cálculo ETA, pieza importante para disponibilidad y lectura operativa de potreros.

## Evidencia vigente

- existe `src/main/java/com/cattle/utils/EtaCalculator.java`
- existe `src/test/java/com/cattle/utils/EtaCalculatorTest.java`
- el proyecto ejecuta pruebas y cobertura con Gradle Wrapper

## Corrección documental importante

La versión anterior usaba comandos Maven. La referencia vigente para este repositorio es:

```bash
./gradlew test --tests "*EtaCalculatorTest"
./gradlew test jacocoTestReport
```

## Intención técnica que sigue vigente

- cubrir valores límite
- cubrir escenarios de crecimiento y recuperación
- validar ETA positivo, cero y escenarios vencidos según el comportamiento actual del código

## Qué requiere validación adicional antes de reusar esta HU

- el detalle exacto de fixtures o builders de prueba
- el porcentaje de cobertura actualizado
- cualquier afirmación sobre fórmula definitiva si el cálculo cambió desde la estimación original

## Uso recomendado

Tomar esta HU como backlog de testing y contrastarla siempre con:

- `EtaCalculator.java`
- `EtaCalculatorTest.java`
- el reporte JaCoCo vigente