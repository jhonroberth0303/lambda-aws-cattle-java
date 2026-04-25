# Pruebas unitarias MilkingProcessor

Este documento describe la cobertura de pruebas unitarias para la clase `MilkingProcessor`.

## Cobertura

- Se cubren casos exitosos y de error para todos los métodos públicos y privados relevantes.
- Se validan flujos principales, validaciones de parámetros y excepciones.
- Se utilizan mocks para dependencias externas.
- Cobertura estimada: >90% líneas y ramas.

## Ubicación de las pruebas

Las pruebas se encuentran en:
- `src/test/java/com/cattle/processor/MilkingProcessorTest.java`

## Ejecución

Ejecutar con Maven o Gradle:

- `./gradlew test` o `mvn test`

## Métodos cubiertos
- getMilkingData
- createMilking (incluye validaciones y errores)
- getCowsWithLactations
- getMilkingByLactation
- extractBovineIdFromPk
- toLactationSummary
- parseLactationNumber

## Consideraciones
- Se mockean servicios, repositorios y mappers.
- Se prueban casos de datos válidos, nulos, vacíos y formatos incorrectos.
- Se validan mensajes de error lanzados.
