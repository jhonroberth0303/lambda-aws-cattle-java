# HU-20260909: Buscador global (bovinos y potreros)

**ID**: HU-20260909-buscador-global
**Tipo**: Nueva funcionalidad frontend + contrato backend diferido
**Prioridad**: Media
**Fecha**: 2026-09-09
**Estado actual**: Opción C implementada en frontend — búsqueda en cliente con contrato normalizado listo para migrar a backend

## Trazabilidad

- Origen: el input "Buscar bovino, potrero, insumo..." de la topbar (`cattle-front/src/components/Topbar/SearchBar.jsx`) estaba sin funcionamiento: `<input>` sin `value`/`onChange`, `onSearch` nunca conectado desde `DashboardLayout`, y el módulo "insumo" no existe.
- Decisión: se ejecutó la **Opción C (híbrida)** — resolver ya con búsqueda en el cliente, pero fijando el contrato de resultado y aislando el origen de datos para que la migración a un endpoint de backend sea transparente.

## Contexto

- No hay endpoint de búsqueda en `lambda-aws-cattle-java` (solo `ChatbotController` para lenguaje natural).
- Cada vista carga sus propios datos: bovinos por `GET /summary` (`BovinesSummaryController`), potreros por `GET /farms/{farmId}/pastures`.
- El volumen actual (decenas–cientos de registros por finca) hace viable indexar en memoria en el navegador.
- El chatbot cubre consultas conversacionales; el buscador debe ser determinista y directo (ir a la ficha), no redirigir al chat.

## Objetivo

Que desde cualquier pantalla del dashboard el ganadero pueda escribir nombre, ID/arete, raza o estado y saltar directo a la ficha del bovino o al potrero correspondiente.

## Alcance

Incluye:

- Buscador funcional en la topbar con resultados agrupados (Bovinos / Potreros).
- Coincidencia tolerante a acentos y mayúsculas, multi-término.
- Navegación por teclado (`/` enfoca, `↑`/`↓` recorren, `Enter` abre, `Esc` cierra) y por click.
- Carga perezosa del índice (al primer foco) y recarga al cambiar la finca activa.
- Al elegir: bovino → `/detail/:id`; potrero → `/potreros?focus=<pastureId>` (abre su drawer).
- Contrato de resultado normalizado (`SearchHit`) reutilizable por un futuro endpoint.

No incluye:

- Endpoint backend `GET /farms/{farmId}/search` (diferido — ver "Migración a backend").
- Módulo de insumos (no existe); el tipo `insumo` queda reservado en el contrato.
- Búsqueda difusa/typo-tolerante, historial de búsquedas, resultados paginados.
- Búsqueda dentro de eventos, lactancias u otras subentidades.

## Contrato de resultado (`SearchHit`)

Definido en `cattle-front/src/search/globalSearch.js`. Es el punto de integración estable: hoy lo produce el cliente, mañana debe producirlo el backend con la misma forma.

```jsonc
{
  "type": "bovine",              // "bovine" | "pasture" | (futuro) "insumo"
  "id": "167",
  "label": "Luna",               // título principal
  "sublabel": "ID 167 · Holstein · Vaca",
  "keywords": ["167", "Holstein", "FEMALE", "COW", "OPEN"],
  "route": "/detail/167",        // destino de navegación en el front
  "raw": { }                      // entidad original (opcional para el backend)
}
```

Reglas de construcción actuales:

| type    | id          | label                     | sublabel                              | route                          |
|---------|-------------|---------------------------|---------------------------------------|--------------------------------|
| bovine  | `bovineId`  | `name` o `Bovino <id>`    | `ID <id> · <raza> · <categoría> [· <estado≠activo>]` | `/detail/<id>`                 |
| pasture | `pastureId` | `name` o `<pastureId>`    | `<pastureId> · <especie> · <estado>`  | `/potreros?focus=<pastureId>`  |

Ranking (multi-token, todos los tokens deben aparecer): match exacto de `id` > `label` exacto > `label` empieza-por > haystack empieza-por > posición de la coincidencia. Límite 6 por grupo, 12 total. Mínimo 2 caracteres.

## Contrato API propuesto (diferido)

`GET /farms/{farmId}/search?q=<texto>&types=bovine,pasture&limit=12`

- `q`: obligatorio, mínimo 2 caracteres, normalizado sin acentos en el servidor.
- `types`: opcional, CSV; por defecto todos los soportados.
- Respuesta: `SearchHit[]` ya ordenado (el front respeta el orden recibido y solo agrupa por `type`).
- Estrategia de datos sugerida en DynamoDB:
  - A corto plazo: `scan` con `FilterExpression` sobre las tablas ya existentes (aceptable al volumen actual).
  - A medio plazo: GSI por `farmId` + prefijo normalizado de nombre, o índice de búsqueda dedicado (OpenSearch) si crece el catálogo o se suman insumos.
- `route` la puede seguir calculando el front a partir de `type` + `id` si se prefiere no acoplar el backend a rutas de UI.

## Migración a backend (cómo se hace la transición)

En `cattle-front/src/search/useGlobalSearch.js`, `runSearch`/`loadIndex` es el único punto que conoce el origen de datos:

1. Hoy: `Promise.all([fetchBovineHits(), fetchPastureHits(farmId)])` + índice en memoria + `searchIndex()`.
2. Mañana: reemplazar por `fetch(apiUrl('/farms/'+farmId+'/search?q='+q))` que ya devuelve `SearchHit[]`.
3. `GlobalSearch.jsx`, el contrato `SearchHit`, el agrupado y la navegación **no cambian**.

## Criterios de aceptación

1. Escribir ≥2 caracteres muestra un panel con resultados agrupados por Bovinos y Potreros.
2. La coincidencia ignora acentos y mayúsculas y admite varios términos (`"luna holstein"`).
3. `Enter` sobre el resultado resaltado (o click) navega: bovino → ficha `/detail/:id`; potrero → `/potreros` con su drawer abierto.
4. `/` enfoca el buscador desde cualquier parte; `Esc` cierra el panel.
5. Sin resultados muestra mensaje explícito; error de carga muestra "Reintentar".
6. El índice se carga al primer foco, no en cada render del layout, y se recarga al cambiar de finca.
7. El placeholder ya no menciona "insumo".

## Implementación realizada (2026-09-09)

### Frontend — `cattle-front`

Nuevos:

- `src/search/globalSearch.js` — contrato `SearchHit`, `normalizeText` (NFD + `\p{Diacritic}`), adaptadores `bovineToHit` / `pastureToHit`, `buildSearchIndex`, `searchIndex` (ranking + agrupado). Puro, sin React ni red.
- `src/search/useGlobalSearch.js` — hook: carga perezosa del índice (`/summary` + `/farms/{farmId}/pastures`), debounce 180 ms, estado de query/resultados/carga/error. Contiene el seam de migración a backend.
- `src/components/Topbar/GlobalSearch.jsx` + `GlobalSearch.css` — panel desplegable, navegación por teclado, click-fuera, atajo `/`, estados vacío/carga/error.

Modificados:

- `src/components/Topbar/SearchBar.jsx` — pasa a input presentacional controlado (`forwardRef`, `value`, `onChange`, `onKeyDown`, `onFocus`, `onSubmit`); placeholder → "Buscar bovino o potrero...".
- `src/layouts/DashboardLayout.jsx` — usa `<GlobalSearch />` en lugar del `<SearchBar>` inerte.
- `src/components/Paddock/page/PaddockPage.jsx` — lee `?focus=<pastureId>`, abre el drawer de ese potrero y limpia el parámetro.

### Backend — `lambda-aws-cattle-java`

Sin cambios. Este documento fija el contrato para el endpoint `GET /farms/{farmId}/search` cuando se priorice.

## Deuda técnica registrada

- Índice en memoria: no escala a miles de registros ni cubre multi-finca real; se resuelve con el endpoint backend.
- `/summary` no está scopeado por finca; el buscador hereda esa limitación hasta que el endpoint lo esté.
- Sin tests automatizados (el front no tiene runner configurado); `globalSearch.js` es puro y quedaría cubierto al añadir Vitest.
- Tipo `insumo` reservado en `SEARCH_GROUPS` pero inactivo hasta que exista el módulo.

## Validación

- `npm run build` (cattle-front): `built in 2.85s`, 0 errores.
- `npx eslint` sobre archivos nuevos y modificados: 0 hallazgos.
- Smoke test manual de `searchIndex` (nombre, ID, raza, estado, prefijo de potrero, sin resultados): OK.

## Definition of Done

- [x] Buscador funcional con resultados agrupados y navegación por teclado.
- [x] Contrato `SearchHit` documentado y aislado del origen de datos.
- [x] Bovino y potrero navegables desde el resultado.
- [x] Placeholder sin "insumo".
- [x] Build y lint del frontend en verde.
- [ ] Endpoint `GET /farms/{farmId}/search` (diferido a priorización de backend).
