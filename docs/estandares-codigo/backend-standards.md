# Estandares Backend - lambda-aws-cattle-java

## Objetivo

Definir convenciones practicas para escribir y revisar codigo backend en `lambda-aws-cattle-java` usando el stack y los patrones realmente presentes en el repositorio.

Este documento no describe un ideal generico. Resume como mantener consistencia con la Lambda Java actual, su arquitectura por capas y sus mecanismos de validacion observables.

## Evidencia revisada

- `build.gradle`
- `src/main/java/com/cattle/controller/BovinesSummaryController.java`
- `src/main/java/com/cattle/controller/BovineController.java`
- `src/main/java/com/cattle/services/BovineSummaryService.java`
- `src/test/java/com/cattle/controller/BovinesSummaryControllerTest.java`

## Stack vigente

- Java 21
- Gradle Wrapper
- Spring Boot 3.4.5
- AWS Lambda con `aws-serverless-java-container-springboot3`
- DynamoDB Enhanced Client
- Lombok y MapStruct
- JUnit 5, Mockito, AssertJ y Testcontainers
- JaCoCo para cobertura

## Principios de codigo

1. Mantener separacion clara entre HTTP, orquestacion, negocio y persistencia.
2. Favorecer nombres explicitos sobre abreviaturas o capas ambiguas.
3. Encapsular reglas de negocio en `Service`, no en `Controller` ni en `Repository`.
4. Tratar DynamoDB y Bedrock como integraciones externas con manejo defensivo de errores.
5. Propagar errores de infraestructura como excepciones del dominio de la capa correspondiente.

## Estructura recomendada por capas

El patron dominante confirmado en codigo es:

`Controller -> Processor -> Service -> Repository`

### Controller

Responsabilidades permitidas:

- mapear rutas HTTP
- validar parametros basicos
- delegar al processor o servicio adecuado
- devolver `ResponseEntity` con codigos HTTP consistentes
- registrar eventos operativos con `LambdaContext` cuando el flujo ya lo use

No debe:

- contener logica de negocio extensa
- construir queries DynamoDB
- recalcular estados del dominio

### Processor

Responsabilidades permitidas:

- mantener la orquestacion fina del caso de uso
- adaptar el patron usado por el proyecto entre controller y service
- centralizar delegacion cuando varias operaciones del controlador comparten flujo

No debe:

- duplicar logica pesada del service
- mezclar validacion HTTP con persistencia

### Service

Responsabilidades permitidas:

- implementar reglas de negocio
- coordinar varios repositorios
- construir proyecciones derivadas
- transformar errores de repositorio en `ServiceException` u otra excepcion propia

No debe:

- exponer detalles HTTP
- depender de `ResponseEntity`

### Repository

Responsabilidades permitidas:

- encapsular acceso a DynamoDB
- mapear claves, GSIs y operaciones sobre la tabla
- traducir errores SDK a `RepositoryException`

No debe:

- tomar decisiones de negocio
- componer respuestas para UI

## Convenciones de nombres

### Clases

- `PascalCase` para clases y records
- sufijos alineados al rol real: `Controller`, `Processor`, `Service`, `Repository`, `Mapper`, `DTO`
- evitar nombres que no coincidan con la responsabilidad real

Ejemplos confirmados:

- `BovinesSummaryController`
- `BovinesSummaryProcessor`
- `BovineSummaryService`
- `BovineSummaryRepository`

### Metodos

- `camelCase`
- verbo + objeto o intencion concreta
- preferir nombres alineados al contrato del dominio

Ejemplos consistentes:

- `getAllSummaries`
- `refreshSummaryById`
- `findById`
- `refreshAllSummaries`

### Constantes

- `UPPER_SNAKE_CASE`
- usar `private static final` salvo necesidad publica real

Ejemplos del repo:

- `ZONE_ID`
- `ISO_FORMATTER`
- `OPEN`

## Convenciones de implementacion

### Constructor injection

Usar inyeccion por constructor como patron principal en clases Spring.

Preferido:

```java
public BovineSummaryService(
        BovineSummaryRepository summaryRepository,
        BovineRepository bovineRepository,
        ProfileLifecycleRepository lifecycleRepository,
        LambdaContext lambdaContext) {
    this.summaryRepository = summaryRepository;
    this.bovineRepository = bovineRepository;
    this.lifecycleRepository = lifecycleRepository;
    this.lambdaContext = lambdaContext;
}
```

Aceptar `@Autowired` solo cuando el archivo ya siga ese estilo o el cambio local sea minimo.

### Logging

- usar `LambdaContext` cuando el modulo ya lo utilice como estandar operativo
- loggear eventos de entrada, errores relevantes y resultados batch
- no loggear secretos, JWTs ni payloads sensibles completos
- preferir mensajes con identificadores concretos: `farmId`, `bovineId`, conteos o duraciones

### Manejo de errores

- capturar errores de infraestructura en `Repository`
- transformar a `ServiceException` o excepcion de dominio en `Service`
- traducir a `ResponseEntity` en `Controller`
- evitar `catch (Exception)` salvo para cerramiento de borde del endpoint o batch tolerante

### DTOs y mapeo

- usar DTOs para contratos HTTP
- usar MapStruct cuando el mapper ya exista para ese agregado
- no exponer directamente entidades de persistencia en controladores

### Comentarios

- usar JavaDoc breve solo cuando el metodo encapsule una regla o contrato util de mantener
- evitar comentarios que repitan literalmente lo obvio del codigo

## DynamoDB y persistencia

- encapsular nombres de tablas e indices en el repositorio
- preferir un metodo por intencion de consulta o persistencia, no repositorios multiproposito difusos
- mantener el conocimiento de claves `PK`, `SK` y GSIs dentro de la capa de persistencia
- si una proyeccion derivada comparte tabla con otra entidad, documentarlo claramente en el repositorio o en docs de arquitectura

## Testing backend

### Tipos de test observados

- unit tests con JUnit 5 y Mockito
- tags como `@Tag("unit")` y `@Tag("controller")`
- soporte para Testcontainers y LocalStack en el build

### Convenciones recomendadas

- un archivo de test por clase o slice relevante
- nombres de test con patron `accion_condicion_resultado`
- verificar tanto casos felices como entradas invalidas y fallos internos
- testear controllers por contrato HTTP y services por reglas del dominio

Ejemplo consistente del repo:

- `getSummaryById_returnsBadRequestWhenInvalid`
- `refreshAllSummaries_returnsInternalServerErrorOnException`

## Comandos vigentes

- `gradlew test`
- `gradlew build`
- `gradlew jacocoTestReport`

## Gaps de enforcement actuales

No hay evidencia en `build.gradle` de:

- Checkstyle activo
- SpotBugs activo
- PMD activo

Conclusión: hoy el enforcement real del backend depende mas de tests, JaCoCo, revisiones y disciplina de capas que de validadores estaticos de estilo.

## Checklist de revision

1. El controller hace solo validacion basica y traduccion HTTP.
2. La regla de negocio principal vive en `Service`.
3. El repository no mezcla decisiones del dominio.
4. El manejo de errores respeta la capa donde ocurre.
5. Hay logs utiles y no sensibles.
6. Los tests cubren camino feliz y fallos basicos.
7. El cambio mantiene consistencia con nombres, DTOs y mappers existentes.