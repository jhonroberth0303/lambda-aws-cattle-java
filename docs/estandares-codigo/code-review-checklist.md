# Checklist de Code Review para PRs

## Objetivo

Tener una lista corta y operativa para revisar cambios en `cattle-front` y `lambda-aws-cattle-java` sin reabrir todos los estándares completos en cada PR.

Esta checklist resume los criterios mínimos derivados de:

- `frontend-standards.md`
- `backend-standards.md`

## Uso recomendado

Aplicarla como filtro rapido antes de aprobar un PR.

Si alguna respuesta es `no`, el review debe dejar hallazgo, pedir ajuste o justificar explicitamente la excepcion.

## Checklist general

1. El cambio respeta el patrón dominante del módulo en vez de introducir una arquitectura paralela.
2. Los nombres de archivos, componentes, clases y métodos son consistentes con el estilo existente.
3. No hay configuración, endpoint o contrato nuevo oculto sin documentación o explicación.
4. El cambio fue validado con el comando más cercano disponible para ese repositorio.
5. No se mezclan refactors amplios con cambios funcionales pequeños sin necesidad real.

## Checklist frontend

1. El componente o hook tiene una responsabilidad clara y reconocible desde su nombre.
2. La lógica de pantalla, formulario o carga repetida se extrajo a hook solo cuando realmente aporta claridad.
3. Los endpoints de lectura y escritura no quedaron mezclados por conveniencia.
4. Si backend ya calcula un estado, el frontend no duplicó esa lógica en varios lugares.
5. El cambio pasa `npm run lint` cuando toca archivos cubiertos por ESLint.
6. No se degradaron tipos existentes a `any` sin una justificación concreta.
7. Los estilos siguen el patrón local del módulo y no introducen una capa visual ajena al repo.

## Checklist backend

1. El `Controller` hace validación HTTP básica y delega; no concentra lógica de negocio pesada.
2. El `Service` contiene la regla principal y no depende de `ResponseEntity`.
3. El `Repository` encapsula acceso a DynamoDB o integración externa sin decidir reglas del dominio.
4. El manejo de errores respeta la capa donde ocurre y no colapsa todo en `Exception` sin contexto.
5. Los logs son útiles, no sensibles y contienen identificadores operativos cuando aplica.
6. El cambio mantiene consistencia con DTOs, mappers y sufijos de clases ya existentes.
7. Hay prueba o validación ejecutable razonable para el slice tocado: `gradlew test`, `gradlew build` o verificación enfocada equivalente.

## Señales de alerta

- frontend escribiendo sobre endpoints pensados para proyección o summary
- controllers backend recalculando reglas complejas que deberían vivir en services
- repositorios que empiezan a mezclar formato de respuesta para UI
- documentación que afirma herramientas o validaciones que el repo no ejecuta realmente
- PRs que no explican un acoplamiento nuevo de URL, seguridad o configuración

## Cierre de review

Antes de aprobar, la respuesta ideal a estas preguntas es `sí`:

1. ¿Entiendo qué cambia y por qué cambia?
2. ¿El cambio encaja con el patrón actual del repositorio?
3. ¿Hay una validación suficiente para reducir riesgo?
4. ¿Queda deuda técnica explícita cuando no se resolvió en este PR?