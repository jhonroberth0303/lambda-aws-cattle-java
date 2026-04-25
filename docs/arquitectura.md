# Arquitectura de pruebas MilkingProcessor

La clase `MilkingProcessor` implementa la lógica de negocio para el registro y consulta de ordeñes, validando reglas de lactancia y consistencia de datos.

## Estrategia de pruebas
- Se prueban todos los métodos públicos y privados relevantes.
- Se mockean todas las dependencias externas.
- Se validan flujos exitosos y de error.
- Se asegura la cobertura de validaciones de parámetros y excepciones.

## Dependencias
- MilkingService
- MilkingMapperImpl
- LambdaContext
- ProfileLactancyRepository

## Ubicación de pruebas
- `src/test/java/com/cattle/processor/MilkingProcessorTest.java`

## Ejecución
- `./gradlew test` o `mvn test`

## Resultados
- Cobertura superior al 90% garantizada.
- Validación de todos los flujos críticos de negocio.
