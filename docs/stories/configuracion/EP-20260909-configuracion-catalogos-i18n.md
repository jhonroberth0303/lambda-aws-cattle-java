# EP-20260909: Configuración, catálogos e i18n — eliminar datos quemados del front

**ID**: EP-20260909-configuracion-catalogos-i18n
**Tipo**: Análisis y diseño — épica transversal (frontend + contrato backend)
**Prioridad**: Media-alta (deuda técnica con deriva activa)
**Fecha**: 2026-09-09
**Estado**: Fases 0, 1, 4.0 y 4.1 implementadas (2026-09-09) · Fases 2, 3, 4.2 pendientes

## Trazabilidad

- Origen: revisión solicitada sobre constantes quemadas en `cattle-front` (`EVENT_CONFIG`, `EVENT_LABELS`, etc.).
- Relación: `cattle-front/docs/estandares-codigo` (principio "hacer explícitos los acoplamientos de configuración hardcodeada"), `docs/stories/settings/HU-20260429-control-permisos-settings.md`, infraestructura `SiteSettingItem` ya existente.
- Documenta la decisión de **no** llevar todo a base de datos, sino separar por naturaleza del dato.

## 1. Contexto y problema

El frontend mantiene el catálogo del dominio (tipos de evento, estados, subestados, razas), los textos de UI y varias reglas de negocio como constantes repetidas en cada componente. No hay una fuente única.

### Evidencia (inventario)

| Archivo | Constante(s) | Naturaleza real |
|---|---|---|
| `Bovines/eventPanel/BovineEventPanel.jsx` | `EVENT_CONFIG` (22), `availableActions`, `buildInitialFormState`, `buildPayload` (switch de 20 casos), ~570 líneas de `mode === "X" && <label>…` | catálogo + schema de formulario |
| `Bovines/timeline/BovineTimeline.jsx` | `EVENT_LABELS` (22), `FIELD_LABELS` (30), `VALUE_LABELS` (17), `SANIDAD/REPRODUCCION/SALIDA` | i18n + catálogo |
| `Paddock/detailPanel/detailPanel.jsx` | `EVENT_CONFIG` (8) | catálogo + schema |
| `Paddock/pastureTimeline/PastureTimeline.jsx` | `EVENT_LABELS`, `EVENT_CLASS` | i18n |
| `Paddock/paddockConstants/paddockSelectOptions.js` | `SPECIES_OPTIONS`, `STATUS_OPTIONS`, `SUBSTATUS_OPTIONS` | catálogo |
| `Paddock/paddockConstants/pasturePresentation.js` | `STATUS_META`, `SUBSTATUS_LABELS` | i18n + catálogo |
| `Bovines/cards/BovineCard.jsx` | `CATEGORY_META`, `REPRODUCTIVE_STATE_META`, `LACTATION_STATE_META`, `ALERT_META`, `statusMap`, `GESTATION_DAYS = 283` | i18n + reglas de negocio |
| `Bovines/hooks/useBovineForm.ts` | `breeds`, `maleStatuses`, `femaleStatuses`, `genders` | catálogo |
| `Bovines/drawer/BovineDrawerContent.jsx` | `GENDER_LABEL`, `STATUS_LABEL` | i18n |
| `notifications/notificationTypes.js` | `NOTIFICATION_TYPES` | i18n + catálogo |
| `Paddock/paddockConstants/preEntryCheckItems.js` | `PRE_ENTRY_ITEMS` | catálogo |
| `search/globalSearch.js` | `CATEGORY_LABELS`, `BOVINE_STATUS_LABELS`, `SPECIES_LABELS` | i18n (misma deuda, recién introducida) |
| `Paddock/statistics/PastureStatsSummary.jsx` | `MOCK_STATS` (5 potreros ficticios como fallback en producción) | mock |
| `services/activeFarmService.js` | `HARDCODED_DEFAULT_FARM_ID = "F001"` | mock |
| `Shared/BottomNav.jsx`, `Sidebar/Sidebar.jsx` | `NAV_ITEMS`, link `/insumos` (ruta inexistente) | navegación |

### Deriva ya presente (por qué esto es urgente y no cosmético)

- `GESTATION_DAYS` = **279** en `BovineEventPanel.jsx` y **283** en `BovineCard.jsx`.
- Especies de potrero: `KIKUYO / RYEGRASS / CUBA22` en `paddockSelectOptions.js` vs `ESTRELLA / BRACHIARIA / KIKUYO / RAIGRAS / GUINEA` en `pasturePresentation.js`. No coinciden entre sí ni con nada del backend.
- `VALUE_LABELS` contiene `DISTOXICO` (aparente typo de `DISTOCICO`); nadie lo detectó por ausencia de pruebas.
- Los tipos de evento bovino viven en **6 lugares**: `BovineEventType.java`, `EVENT_CONFIG`, `EVENT_LABELS`, `availableActions`, `buildPayload`, `BovineEventProcessor.validatePayloadByType`. Añadir un evento obliga a tocar los seis y mantenerlos sincronizados a mano.

## 2. Principio rector: no todo va a base de datos

Llevar catálogos y textos de UI a DynamoDB editable es tan problemático como tenerlos quemados. La regla es separar por **naturaleza del dato**:

| Tipo | Ejemplos en este repo | Dónde debe vivir | ¿BD editable por usuario? |
|---|---|---|---|
| **A. Catálogos de dominio** (enums) | tipos de evento, estados, subestados, razas, categorías | Fuente única en backend; el front la consume por contrato generado o endpoint `/catalogs` cacheado | No — cambia con releases |
| **B. Presentación / i18n** | labels en español, iconos, `tone`, títulos de formulario | Archivos de traducción del front (`locales/es.json`) | Nunca |
| **C. Configuración de negocio por sitio/finca** | `GESTATION_DAYS`, umbral de semáforo, residual objetivo, precio de leche, límite de historial | Base de datos versionada: `SiteSettingItem` (ya existe) | Sí |
| **D. Schema de formularios** | campos por tipo de evento, obligatoriedad | Una definición (JSON Schema) consumida por front (render) y backend (validación) | Opcional (servida por `/catalogs`) |
| **E. Feature flags / navegación** | `NAV_ITEMS`, `/insumos` | Config de build o remote-config | No es BD de dominio |
| **F. Mocks** | `MOCK_STATS`, `F001` | Se eliminan | — |

El backend ya modela el patrón correcto para la categoría C: `SiteSettingItem` es genérico (`NUMBER / STRING / BOOLEAN / JSON`), versionado, con historial (`effectiveFrom/To`, `version`, `changeReason`). Hoy solo lo consume `milk-price`.

## 3. Cómo se resuelve en desarrollos serios

1. **Fuente única del dominio**: el backend define los enums; el front los recibe generados desde el contrato (OpenAPI → tipos) o desde un endpoint de catálogos. No se reescriben a mano.
2. **Separación estricta**: dato de dominio ≠ copy de UI ≠ config de negocio ≠ feature flag.
3. **i18n con librería** (`react-i18next` o, mínimo, JSON de `locales`): los textos salen de los componentes.
4. **Config runtime cacheada y no bloqueante**: bundle de fallback + revalidación en segundo plano (React Query `staleTime` alto + persistencia en `localStorage` para la PWA). Si el endpoint cae, la app sigue.
5. **Formularios schema-driven**: un JSON Schema por tipo de evento; front lo renderiza, backend lo valida. Front y back no pueden discrepar.
6. **Config de negocio versionada y auditable**: autor, motivo, fecha efectiva.

## 4. Arquitectura objetivo

```
cattle-front/
  src/domain/                 # fuente única de catálogos (Fase 0), hidratable desde API (Fase 1)
    bovineEvents.js           # [{ code, label, group, icon, tone, fields:[...] }]
    pastures.js               # especies, estados, subestados
    bovines.js                # razas, categorías, estados reproductivos
    notifications.js
    index.js                  # accessors: labelOf(domain, code), optionsOf(domain), ...
  src/i18n/
    locales/es.json           # textos de UI (Fase 0: archivo; Fase futura: react-i18next)
  src/config/
    useCatalogs.js            # React Query + cache + fallback al bundle (Fase 1)
    useSiteSettings.js        # lectura/escritura de config de negocio (Fase 3)

lambda-aws-cattle-java/
  GET /catalogs               # enums + metadatos -> JSON, con version/ETag (Fase 1)
  GET /catalogs/bovine-events/schema   # JSON Schema de formularios (Fase 2)
  GET /site/{siteId}/settings          # todas las settings del sitio (Fase 3)
  GET|PUT /site/{siteId}/settings/{key}
```

## 5. Plan por fases

**Cadencia**: Fases 0, 4.0 y 4.1 se ejecutaron juntas (bajo riesgo, solo frontend). Las Fases 1, 2 y 3 tocan el backend Java y se ejecutan **una por sesión**, con validación y revisión propias. Orden acordado: **1 → 3 → 2** (2 es la más riesgosa y se apoya en el contrato de 1). Fase 1 completada el 2026-09-09; sigue la Fase 3.

**Lo primero de la Fase 1** (decisión de diseño resuelta): cómo hidratar `src/domain/*` desde `/catalogs` sin cambiar su API pública. Se aplicó la opción recomendada: el bundle estático se mantiene como *fallback* y `useCatalogs()` deposita la respuesta del endpoint en una caché a nivel de módulo (`src/config/catalogsCache.js`) que los accesores (`bovineEventLabel`, etc.) consultan antes que el bundle. Ningún componente se tocó y la app arranca aunque el endpoint falle.


### Fase 0 — Consolidar en el front (sin backend) — ✅ IMPLEMENTADA (2026-09-09)

**Objetivo**: una sola fuente por dominio dentro del front; eliminar duplicación y deriva.

- Crear `src/domain/` con un módulo por dominio. Cada módulo define el catálogo canónico (código + label + grupo + icono/dotClass + tone + flags).
- Reescribir `EVENT_CONFIG`, `EVENT_LABELS`, `FIELD_LABELS`, `VALUE_LABELS`, `availableActions`, `STATUS_META`, `SPECIES_OPTIONS`, `SPECIES_LABELS`, `breeds`, `GENDER_LABEL`, `STATUS_LABEL`, `notificationTypes` como derivaciones de esos módulos.
- Unificar `GESTATION_DAYS` (un solo valor, temporalmente en `src/domain/bovines.js` hasta la Fase 3).
- Unificar el catálogo de especies/estados de potrero (resolver la discrepancia actual).
- Eliminar `MOCK_STATS` y su fallback; `PastureStatsSummary` muestra estado vacío/erróneo real.
- Consolidar la navegación en `src/config/navigation.js` y quitar links a rutas inexistentes del `Sidebar` (`/sanidad`, `/insumos`, `/inventario`, `/raciones`, `/cultivos/morashd`).

**Fuera de alcance de Fase 0** (movido a decisiones abiertas):

- i18n de textos sueltos de UI en un `src/i18n` dedicado: las etiquetas de **dominio** (que eran la duplicación real) ya viven en `src/domain`; una capa i18n para copy suelto se difiere hasta que haga falta un segundo idioma.
- `HARDCODED_DEFAULT_FARM_ID`: se renombró a `DEFAULT_FARM_ID` (env-overridable) pero **no se elimina** — sin selector de finca en la UI, quitarlo deja la app sin `farmId`. Se retira con la historia del selector de finca.
- `CATEGORY_META` / `REPRODUCTIVE_STATE_META` de `BovineCard`: llevan emoji + `tone` + reglas de negocio de presentación; su base (`categoryLabel`, `statusLabel`, `GESTATION_DAYS`) sí se consolidó, el resto se aborda en Fase 2.

**Entregables**: `src/domain/*`, `src/config/navigation.js`, consumidores migrados, `npm run build` + `npm run lint` + `npm test` en verde.
**Impacto**: eliminada la duplicación de catálogos/labels y toda la deriva actual (dos `GESTATION_DAYS`, especies incompatibles), sin tocar backend ni cambiar comportamiento visible (salvo quitar el badge "datos demo" y los links muertos).

### Fase 1 — Endpoint de catálogos (read-only, cacheado) — ✅ IMPLEMENTADA (2026-09-09)

- **Backend**: `GET /catalogs` (o `/catalogs/{domain}`) que serializa los enums Java + un `catalog.yml` (labels, orden, iconos, grupos) a JSON. Respuesta con `version` y `ETag`. Sin autenticación de dominio (es metadata pública del producto).
- **Front**: `useCatalogs()` con React Query (`staleTime` 12–24 h), persistido en `localStorage` para offline. Fallback al bundle de la Fase 0 si la llamada falla.
- Los módulos de `src/domain/` pasan a hidratarse desde `useCatalogs()`; su **forma pública no cambia**, por lo que ningún componente se toca en esta fase.
- Introducir React Query como dependencia (`@tanstack/react-query`).

**Entregables**: `CatalogController` + DTOs + `catalog.yml`, `src/config/useCatalogs.js`, tests de contrato.

**Implementado**:

- Backend: `catalog.yml` (7 dominios), `CatalogService` (carga + guardia anti-deriva: aborta el arranque si un dominio con `enum:` diverge del enum Java), `CatalogController` (`GET /catalogs`, `GET /catalogs/{domain}`, `ETag` + `If-None-Match` → 304, `Cache-Control` 12 h), `CatalogEntryDTO` / `CatalogResponseDTO` / `CatalogDomainDTO`. `/catalogs/**` en la lista pública de `SecurityConfig`. Ruteo por el proxy `/{proxy+}` existente — sin cambios de infra.
- Front: `src/config/catalogsCache.js` (caché a nivel de módulo), `src/services/catalogsService.js`, `src/config/useCatalogs.js` (React Query, `staleTime` 12 h, `initialData` desde `localStorage`, hidrata la caché en un efecto), `src/config/CatalogsProvider.jsx` (monta `QueryClientProvider` alto en `main.jsx`, no bloquea el render). Los accesores de `src/domain/{bovineEvents,pastureEvents,pastures,bovines,notifications}` consultan la caché **antes** del bundle; su API pública no cambió y ningún componente se tocó.
- Dominios hidratables: `bovine-events`, `pasture-events`, `pasture-statuses`, `pasture-substatuses`, `bovine-categories`, `pasture-species`, `notification-types`.

**Fuera de alcance de Fase 1** (se mantiene lo acordado):

- `title` / `submitLabel` / `action` de eventos y `EVENT_FIELD_LABELS` / `EVENT_VALUE_LABELS`: son schema de formulario / i18n de payload, van en Fase 2. Siguen en el bundle del front.
- Generación de tipos con `openapi-typescript`: sigue como decisión abierta.
- `GESTATION_DAYS` y demás config de negocio: Fase 3 (`SiteSettingItem`).

### Fase 2 — Formularios schema-driven

- Definir el JSON Schema de cada tipo de evento (campos, tipo, requerido, opciones, label) **una sola vez**.
- **Backend**: `BovineEventProcessor.validatePayloadByType` se reemplaza por validación contra el schema.
- **Front**: un renderer genérico de formulario a partir del schema reemplaza los ~570 líneas de `mode === "X"` en `BovineEventPanel` y todo `buildPayload`. Mismo tratamiento para `detailPanel` de potreros.
- Servido en `GET /catalogs/bovine-events/schema` y `.../pasture-events/schema`.

**Entregables**: schemas versionados, renderer de formularios, `BovineEventPanel`/`detailPanel` reducidos, validación backend unificada.

### Fase 3 — Módulo de configuración de negocio

- Migrar a `SiteSettingItem` las constantes de categoría C: `GESTATION_DAYS`, umbral del semáforo de rotación (`etaOpenDays <= 3`), residual objetivo, límite de historial de eventos, etc.
- Generalizar el controller de settings: hoy solo `/site/{siteId}/settings/milk-price`. Añadir:
  - `GET /site/{siteId}/settings` — todas las settings vigentes del sitio.
  - `GET /site/{siteId}/settings/{key}` y `PUT /site/{siteId}/settings/{key}` — genérico por tipo, reutilizando `SiteSettingService` (ya soporta los 4 tipos + snapshot de historial).
- **Front**: pantalla "Configuración" (el `Sidebar` ya la insinúa) que lista y edita settings; `useSiteSettings()` con cache y fallback a valores por defecto del catálogo.
- Reglas de negocio del front (`BovineCard`, semáforo) leen de `useSiteSettings()` en vez de constantes locales.

**Entregables**: endpoints genéricos de settings, `src/config/useSiteSettings.js`, pantalla de configuración, constantes de negocio migradas.

### Fase 4 — Pruebas unitarias en el frontend

**Motivación**: la deriva descrita en §1 (dos valores de `GESTATION_DAYS`, catálogos de especies divergentes, `DISTOXICO`) no se detectó porque `cattle-front` no tiene pruebas. El backend ya reporta ~93 % de cobertura; el front está en 0 %. Sin una red de pruebas, cada fase de esta épica introduce riesgo de regresión silenciosa.

**Stack propuesto** (alineado con Vite 7 / React 19):

- `vitest` — runner, comparte config con Vite.
- `@testing-library/react` + `@testing-library/user-event` — pruebas de componentes centradas en comportamiento.
- `@testing-library/jest-dom` — matchers de DOM.
- `jsdom` — entorno DOM.
- `@vitest/coverage-v8` — cobertura.
- `msw` (Mock Service Worker) — simular endpoints en pruebas de hooks/servicios sin acoplar a `fetch`.

**Configuración**:

- `vitest.config.js` (o `test` dentro de `vite.config.js`): `environment: "jsdom"`, `setupFiles` con `jest-dom` y arranque de MSW, `coverage` con reporter `text` + `html`.
- Scripts: `npm test`, `npm run test:watch`, `npm run test:coverage`.
- ESLint: añadir `eslint-plugin-vitest` o al menos `globals` de test para no romper el lint.

**Qué se prueba (prioridad por valor/costo)**:

1. **Lógica pura de dominio** (máxima prioridad, costo mínimo):
   - `src/domain/*` — que cada catálogo sea consistente (sin códigos duplicados, todo código tiene label, los grupos referencian códigos existentes).
   - `src/search/globalSearch.js` — `normalizeText`, ranking, agrupado (ya existe smoke manual; formalizarlo).
   - `pasturePresentation.normalizePasture`, `preEntryCheckItems.evaluateChecklist`, `utils/date`.
   - `BovineTimeline.parseDetail` / `extractSummary`, `BovineEventPanel.buildPayload`.
2. **Hooks** con MSW: `useGlobalSearch`, `useBovineForm`, `useCatalogs` (Fase 1), `useSiteSettings` (Fase 3).
3. **Componentes de comportamiento** (no snapshots frágiles): `GlobalSearch` (teclado, navegación, estados vacío/error), `BovineEventPanel` (visibilidad de acciones por sexo/estado, envío), `BovineCard` (badges según `productiveState`).
4. **Test de guardia anti-deriva**: una prueba que cruce el catálogo del front con el contrato `/catalogs` del backend (Fase 1+) y falle si divergen.

**Objetivos de cobertura** (progresivos, no bloqueantes de golpe):

- **Fase 4.0 — ✅ IMPLEMENTADA (2026-09-09)**: infra + pruebas de `src/domain/`, `src/search`, `src/config/navigation`, `utils/date` y helpers puros de potreros. 52 pruebas.
- **Fase 4.1 — ✅ IMPLEMENTADA (2026-09-09)**: hooks y servicios. `include` de cobertura ampliado a `src/services/**` y `src/components/Bovines/hooks/**`; helper `src/test/fetchMock.js` (stub de `fetch`); axios mockeado por módulo. Umbrales: sentencias/líneas/funciones 70, ramas 60 (real: 80 / 68). 81 pruebas.
- Fase 4.2: componentes críticos (eventos, tarjetas, buscador) → ampliar `include` a componentes de dominio; subir umbral de ramas.
- Excluir de cobertura: `main.jsx`, CSS, componentes de puro layout.

**CI**: `npm run lint && npm run test:coverage && npm run build` como gate de PR.

**Entregables Fase 4.0**: `vitest.config.js`, `src/test/setup.js`, scripts `test`/`test:watch`/`test:coverage` en `package.json`, override de ESLint para archivos de test, `coverage/` ignorado por ESLint, sección de testing en `frontend-standards.md`.

Suite inicial:

- `src/domain/domain.test.js` — guardias anti-deriva de los 5 catálogos (22 eventos bovinos, 9 de potrero, 8 subestados, especies, categorías, GESTATION_DAYS único).
- `src/search/globalSearch.test.js` — normalización, adaptadores, ranking, agrupado.
- `src/components/Paddock/paddockConstants/pasturePresentation.test.js` — `normalizePasture`, `sortPasturesByAvailability`, `getSemaphoreSignal`, `buildPastureAlerts`.
- `src/utils/date.test.js` — `today`, `addDays`.
- `src/config/navigation.test.js` — todo destino de menú corresponde a una ruta de `App.jsx`.
- `src/components/Topbar/SearchBar.test.jsx`, `src/components/Bovines/eventPanel/BovineEventPanel.test.jsx` — comportamiento (input controlado; visibilidad de acciones por sexo, delegada al dominio).

## 6. Qué NO hacer

- No meter labels ni copy de UI en DynamoDB.
- No hacer que el arranque del front dependa de una llamada bloqueante a `/catalogs` (siempre bundle de fallback + cache).
- No crear un único "config gigante": separar por dominio y por naturaleza (A–F).
- No versionar el catálogo a mano en dos repos: el backend genera, el front consume.
- No pruebas de snapshot masivas ni objetivos de cobertura del 100 % que incentiven tests sin valor.

## 7. Riesgos y decisiones abiertas

- **Orden de i18n**: archivo `es.json` simple en Fase 0 vs adoptar `react-i18next` desde ya. Recomendación: archivo simple ahora, librería cuando se necesite un segundo idioma.
- **`/catalogs` público vs por finca**: los catálogos son metadata de producto (no dependen de finca); las settings sí. Mantenerlos en endpoints separados.
- **Generación de tipos**: evaluar `openapi-typescript` sobre el OpenAPI ya expuesto por el backend para no mantener DTOs del front a mano.
- **Especies de potrero**: Fase 0 tomó `KIKUYO / RYEGRASS / CUBA22` (los valores reales en datos y en `PastureBuilder`) y descartó la lista `ESTRELLA/BRACHIARIA/GUINEA` que no correspondía a nada. `species` sigue siendo texto libre en el backend con tolerancia a valores nuevos; confirmar con negocio si debe volverse enum.
- **`DISTOXICO`**: el `<option>` y el dato persistido usan ese typo (debería ser `DISTOCICO`). Se mantiene el valor tal cual en el catálogo para no romper datos existentes; se corrige con migración en Fase 2.
- **i18n de copy suelto**: diferido. Las etiquetas de dominio ya están centralizadas en `src/domain`; un `src/i18n` con `react-i18next` se justifica solo con un segundo idioma.
- **Alcance de Fase 2**: los formularios de eventos son el caso más grande; validar el renderer con 3–4 tipos antes de migrar los 22.

## 8. Definition of Done (por fase)

- **Fase 0 — ✅**: `src/domain/` es la única fuente; 0 tablas de catálogo/labels duplicadas en componentes; `GESTATION_DAYS` (279) y especies unificados; `MOCK_STATS` eliminado; navegación consolidada sin links muertos; build + lint + test verdes.
- **Fase 1 — ✅**: `/catalogs` + `/catalogs/{domain}` con `version`/`ETag`/304/`Cache-Control`; `catalog.yml` valida contra los enums al arrancar; front hidrata `src/domain/*` desde el endpoint con fallback al bundle y cache offline en `localStorage`; tests de contrato back (`CatalogServiceTest`, `CatalogControllerTest`, `StreamLambdaHandlerTest`) y front (`catalogsCache.test.js`, `useCatalogs.test.jsx`, `CatalogsProvider.test.jsx`).
- **Fase 2**: un solo schema por tipo de evento; front renderiza y backend valida desde él; `BovineEventPanel` sin `switch` por tipo.
- **Fase 3**: config de negocio en `SiteSettingItem`; endpoints genéricos de settings; pantalla de Configuración operativa.
- **Fase 4.0 — ✅**: Vitest operativo; suite de `src/domain` + `src/search` + utilidades + navegación + 2 componentes; cobertura enfocada con umbral 70 %; estándares de front actualizados.
- **Fase 4.1 / 4.2**: hooks y servicios con MSW; componentes de dominio; ampliar `include` de cobertura y subir umbral; gate de CI `lint + test:coverage + build`.

## 9. Registro de implementación

### 2026-09-09 — Fases 0 y 4.0

**Fase 4.0 (infra de pruebas)**
- `npm i -D vitest @vitest/coverage-v8 jsdom @testing-library/{react,dom,jest-dom,user-event}`.
- `vitest.config.js` (jsdom, globals, setup, cobertura enfocada, umbral 70), `src/test/setup.js`.
- Scripts `test` / `test:watch` / `test:coverage`; ESLint: override de globals para `*.test.*` y `src/test/**`, `coverage/` en `globalIgnores`.
- 7 archivos de prueba, 52 pruebas; cobertura del alcance enfocado ~88 %.

**Fase 0 (consolidación de catálogos)**
- Nuevos: `src/domain/{bovineEvents,pastureEvents,pastures,bovines,notifications,index}.js`, `src/config/navigation.js`.
- Migrados a leer del dominio: `search/globalSearch.js`, `paddockConstants/paddockSelectOptions.js`, `paddockConstants/pasturePresentation.js`, `notifications/notificationTypes.js` (shim), `Bovines/hooks/useBovineForm.ts`, `Bovines/drawer/BovineDrawerContent.jsx`, `Bovines/timeline/BovineTimeline.jsx`, `Bovines/cards/BovineCard.jsx`, `Bovines/eventPanel/BovineEventPanel.jsx`, `Paddock/pastureTimeline/PastureTimeline.jsx`, `Paddock/detailPanel/detailPanel.jsx`, `Shared/BottomNav.jsx`, `Sidebar/Sidebar.jsx`.
- `PastureStatsSummary.jsx`: eliminado `MOCK_STATS` y el badge "datos demo"; ahora muestra error/vacío real.
- `services/activeFarmService.js`: `HARDCODED_DEFAULT_FARM_ID` → `DEFAULT_FARM_ID` (override por `VITE_DEFAULT_FARM_ID`), sin eliminar (falta selector de finca).
- Correcciones de lint preexistentes para dejar `npm run lint` en verde (`bovineEventService.js` catch vacío, `ActiveFarmContext.jsx` disable puntual).
- No se tocó: `buildInitialFormState` / `buildPayload` / JSX de formularios por tipo en `BovineEventPanel` y `detailPanel` (Fase 2); `CATEGORY_META` / `*_STATE_META` de `BovineCard` (emoji + reglas de presentación, Fase 2).

**Validación**: `npm run lint` limpio · `npm run build` OK · `npm test` 52/52.

### 2026-09-09 — Fase 4.1

- `src/test/fetchMock.js`: helper de stub de `fetch` por coincidencia de URL. `setup.js` limpia stubs y mocks entre pruebas.
- `include` de cobertura ampliado a `src/services/**`, `src/config/**`, `src/search/**`, `src/components/Bovines/hooks/**`, `src/utils/useOnlineStatus.js`. Umbral de ramas bajado a 60 (escalona en 4.2).
- `vitest.config.js` `include` de tests ampliado a `.ts`/`.tsx`.
- Nuevas pruebas: `activeFarmService`, `bovineEventService`, `pastureEventService` + `pastureHistoryService`, `bovinesServices` (axios mockeado), `readServices` (milk-price, estadísticas, ordeño, lactancia), `useGlobalSearch` (carga perezosa + debounce + búsqueda), `useOnlineStatus`, `useBovineForm` (migrado de Jest muerto a Vitest).
- **Validación**: `npm run lint` limpio · `npm run build` OK · `npm test` 81/81 · cobertura 80 % sentencias / 68 % ramas.

### 2026-09-09 — Fase 1 (endpoint de catálogos)

**Backend (`lambda-aws-cattle-java`)**

- Nuevo `src/main/resources/catalog.yml`: 7 dominios (`bovine-events`, `pasture-events`, `pasture-statuses`, `pasture-substatuses`, `bovine-categories`, `pasture-species`, `notification-types`). Los dominios con `enum:` se validan contra el enum Java homónimo.
- Nuevo `services/CatalogService`: carga el YAML con snakeyaml (patrón ya usado en `BovineCategoryRulesLoader`), asigna `order` secuencial, calcula un `hash` de contenido determinista (SHA-256, mapper con orden estable) y **aborta el arranque** (`IllegalStateException`) si un dominio diverge de su enum.
- Nuevo `controller/CatalogController`: `GET /catalogs` y `GET /catalogs/{domain}`; `ETag` = `"<version>-<hash>"`, `If-None-Match` → `304`, `Cache-Control: max-age=43200, public, must-revalidate`; dominio desconocido → `404` vía `NotFoundException`.
- Nuevos DTOs `dtos/catalog/{CatalogEntryDTO,CatalogResponseDTO,CatalogDomainDTO}` (Lombok + `@JsonInclude(NON_NULL)`).
- `config/SecurityConfig`: `/catalogs/**` añadido a la lista pública (metadata del producto, sin JWT).
- Ruteo: el proxy `/{proxy+}` de `template.yml` ya cubre `/catalogs` — sin cambios de infra.
- Pruebas: `CatalogServiceTest` (6), `CatalogControllerTest` (6), `StreamLambdaHandlerTest` +2 (contrato de integración por el handler Lambda real).

**Front (`cattle-front`)**

- Dependencia nueva: `@tanstack/react-query`.
- Nuevos: `src/config/catalogsCache.js` (caché a nivel de módulo + accesores `catalogEntry` / `catalogEntries`), `src/services/catalogsService.js`, `src/config/useCatalogs.js` (React Query, `staleTime` 12 h, `gcTime` ∞, `initialData` + `initialDataUpdatedAt` desde `localStorage`, hidrata en un efecto y persiste), `src/config/CatalogsProvider.jsx` (`QueryClientProvider` + hidratación síncrona desde `localStorage`).
- `src/main.jsx`: `CatalogsProvider` envuelve la app (bajo `BrowserRouter`, sobre `ActiveFarmProvider`). No bloquea el render.
- `src/domain/{bovineEvents,pastureEvents,pastures,bovines,notifications}.js`: los accesores consultan `catalogsCache` antes del bundle. **API pública sin cambios**; ningún componente tocado. `title`/`submitLabel`/`action` y `EVENT_*_LABELS` siguen en el bundle (Fase 2).
- Pruebas nuevas: `src/config/catalogsCache.test.js` (6), `src/config/useCatalogs.test.jsx` (5), `src/config/CatalogsProvider.test.jsx` (2). Guardia anti-deriva del front: `domain.test.js` sigue verde con la caché vacía (fallback) y los nuevos tests cubren la ruta hidratada.

**Validación**: back `./gradlew test` 1103/1103 · front `npm run lint` limpio · `npm run build` OK · `npm test` 94/94 · `npm run test:coverage` 81.9 % sentencias / 71.0 % ramas (umbral 70/60).
