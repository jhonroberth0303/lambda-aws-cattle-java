# DT-20260910: Deuda diferida al cierre de la épica de configuración/catálogos/i18n

**ID**: DT-20260910-epica-configuracion-cierre-diferidos
**Tipo**: Deuda técnica — backlog de seguimiento
**Prioridad**: Baja-media (nada bloquea; deriva activa ya eliminada)
**Fecha**: 2026-09-10
**Estado**: Registrado — pendiente de refinar / priorizar por ítem

## Trazabilidad

- Origen: cierre de `docs/stories/configuracion/EP-20260909-configuracion-catalogos-i18n.md` (§7bis). La épica cumple su objetivo — cero deriva activa (`GESTATION_DAYS` 279/283, especies incompatibles, `DISTOXICO`), catálogos con fuente única backend + fallback, formularios schema-driven (front y back no pueden discrepar), config de negocio versionada (`SiteSettingItem`), red de pruebas front (184 tests, cobertura 89 %/75 %, gate de CI `ci.yml`).
- Este documento recoge lo que **se decidió no incluir**, con contexto suficiente para levantarlo como historia propia cuando toque.
- Relación: `DT-20260909-eventos-salida-no-proyectan-estado-ni-summary.md` (ítem 5), `frontend-standards.md`.

## Ítems

### 1. i18n con librería (`react-i18next` + `locales/es.json`) — copy suelto y `BovineCard`

- **Qué**: los textos de UI que no son catálogo de dominio siguen embebidos en componentes. En concreto `CATEGORY_META` / `REPRODUCTIVE_STATE_META` / `LACTATION_STATE_META` / `ALERT_META` de `cattle-front/src/components/Bovines/cards/BovineCard.jsx` (emoji + `tone` + label de presentación, categoría B).
- **Por qué se difiere**: `EP-20260909` §7 — un `src/i18n` con `react-i18next` solo se justifica con un segundo idioma. Estas tablas **no tienen deriva** (viven en un solo archivo) ni duplicación cross-componente; la base con deriva real (`GESTATION_DAYS`, `categoryLabel`, `statusLabel`) ya se consolidó en `src/domain`.
- **Impacto**: bajo. Cambiar un emoji/label obliga a tocar el `.jsx`.
- **Recomendación**: al adoptar un segundo idioma, mover copy suelto a `locales/es.json` (+ `en.json`) y `BovineCard` a claves de traducción. Antes de eso, no vale la pena.

### 2. Generación de tipos del front con `openapi-typescript`

- **Qué**: el front mantiene a mano los shapes de las respuestas del backend (DTOs de summary, eventos, settings, catálogos).
- **Por qué se difiere**: decisión abierta desde el análisis inicial (§7). El backend ya expone OpenAPI vía springdoc.
- **Impacto**: medio a largo plazo — riesgo de que un cambio de DTO en el backend rompa el front en runtime sin que ningún test lo detecte.
- **Recomendación**: PoC de `openapi-typescript` contra el OpenAPI del backend; generar `src/api/types.gen.ts` en build y usarlo en servicios/hooks. Evaluar el coste de mantener el pipeline vs. el beneficio.

### 3. `species` de potrero como enum backend

- **Qué**: `Pasture.species` es `String` libre. La Fase 0 fijó `KIKUYO / RYEGRASS / CUBA22` (valores reales en datos y `PastureBuilder`) y `catalog.yml` los sirve como `pasture-species` **sin `enum:`**.
- **Por qué se difiere**: falta confirmación de negocio de si el conjunto es cerrado.
- **Impacto**: bajo. Hoy el sistema tolera especies nuevas; el riesgo es datos con typos.
- **Recomendación**: si negocio confirma conjunto cerrado → crear `PastureSpecies` enum Java + `enum:` en `catalog.yml` (activa la guardia anti-deriva) + `PastureBuilder`/validación. Si no → dejar como está y documentar la política de "texto libre tolerante".

### 4. Selector de finca / sitio en la UI

- **Qué**: no hay selector de finca. `cattle-front/src/services/activeFarmService.js` expone `DEFAULT_FARM_ID` (env-overridable, `F001`) y `src/config/site.js` `resolveSiteId()` devuelve `001` por env.
- **Por qué se difiere**: quitar los defaults sin selector deja la app sin `farmId`/`siteId`. La Fase 0 y la Fase 3 lo dejaron explícitamente para "la historia del selector de finca".
- **Impacto**: bloquea multi-finca real; hoy la app es monofinca de facto.
- **Recomendación**: historia propia — endpoint de fincas del usuario, selector en el topbar, persistencia (contexto + `localStorage`), y **entonces** eliminar `DEFAULT_FARM_ID` / el fallback de `resolveSiteId()`.

### 5. Proyección de eventos de salida y saneamiento del summary

- **Qué**: registrar MUERTE/VENTA no cambia el `LifecycleStatus` del bovino ni se refleja en `GET /summary`; el estado productivo/alertas y `farmId` quedan inconsistentes para bovinos inactivos.
- **Estado**: ver `DT-20260909-eventos-salida-no-proyectan-estado-ni-summary.md`. Hallazgo **D resuelto** (guard en `BovineEventProcessor` + `isBovineInactive` en el front). **A / B / C abiertos** (no hay capa de proyección evento→perfil; `ProductiveStateCalculator` ignora `status`/`enabled`; `farmId: null` en 172/174).
- **Recomendación**: la que ya trae `DT-20260909` §4 (proyección de eventos de salida + consistencia del summary para animales inactivos).

### 6. Test de anti-deriva entre el bundle del front y `event-forms.yml`

- **Qué**: `BUNDLED_EVENT_FORMS` (`bovineEvents.js`, 22) y `BUNDLED_PASTURE_EVENT_FORMS` (`pastureEvents.js`, 8) son un **espejo a mano** de `lambda-aws-cattle-java/src/main/resources/event-forms.yml`. `domain.test.js` valida la consistencia interna del bundle, pero nada compara `required` / `options` / `min` / `appendNotes` con el YAML ni con `GET /catalogs/{domain}/schema`.
- **Por qué se difiere**: en runtime el esquema hidratado desde el endpoint **gana** sobre el bundle; la divergencia solo afecta cold-start y modo offline (el usuario vería una validación de cliente distinta a la del servidor hasta que hidrate). Impacto acotado.
- **Recomendación** (§4 punto 4 de la épica): test de contrato — en un test backend serializar `EventFormCatalog.getDomainSchemas(...)` a un JSON commiteado que el front importe y compare campo a campo con los bundles; o generar los bundles desde el YAML en build. Cualquiera de las dos cierra el hueco.

### 7. `PRE_ENTRY_ITEMS` como catálogo backend

- **Qué**: `cattle-front/src/components/Paddock/paddockConstants/preEntryCheckItems.js` mantiene la lista de ítems del checklist pre-entrada como constante del front.
- **Por qué se difiere**: es un catálogo front-only (lo consume `PreEntryChecklist` y `detailPanel` vía `getLabelById`), sin backend equivalente y sin deriva histórica.
- **Impacto**: bajo. Si el negocio quiere editar el checklist sin release, habría que llevarlo a `catalog.yml` o a `SiteSettingItem` (JSON).
- **Recomendación**: migrar solo si se toca el flujo de inspección o si negocio pide editarlo.

### 8. `SearchBar` derrama props ARIA sobre el contenedor equivocado

- **Qué**: `cattle-front/src/components/Topbar/SearchBar.jsx` hace `<div className="dashboard-search-group" {...props}>` — `GlobalSearch` le pasa `role="combobox"`, `aria-expanded`, `aria-owns`, que acaban en el `<div>`; el `<input>` queda con `role="searchbox"` fijo y sin el `aria-expanded`.
- **Por qué se difiere**: bug de accesibilidad **pre-existente** (no lo introdujo la épica), fuera del alcance (config/i18n/schema).
- **Impacto**: lectores de pantalla no anuncian el combobox correctamente; navegación por teclado funciona igual.
- **Recomendación**: fix aparte — reenviar `{...props}` al `<input>` y aplicar el patrón ARIA de combobox completo (`aria-autocomplete="list"`, `aria-activedescendant`, `aria-controls`). Ajustar `SearchBar.test.jsx` / `GlobalSearch.test.jsx` (rol del input pasa a `combobox`).

## Nota operativa (no es deuda, es un paso de deploy)

- `docs/scripts/migrate-birthtype-distocico.py` debe ejecutarse en la ventana del deploy que suba el rename `DISTOXICO → DISTOCICO`: escrituras nuevas con `DISTOXICO` ya se rechazan (no está en `options`); la lectura tolera ambos hasta que la migración termine.

## Prioridad sugerida

| # | Ítem | Prioridad | Tamaño |
|---|---|---|---|
| 5 | Proyección eventos de salida + summary inactivos | Media-alta | M-L (ver DT-20260909) |
| 6 | Test anti-deriva bundle ↔ YAML | Media | S |
| 4 | Selector de finca/sitio | Media | M |
| 8 | `SearchBar` ARIA | Baja | S |
| 2 | `openapi-typescript` | Baja (evaluación) | S (PoC) / M |
| 3 | `species` enum | Baja (bloqueado por negocio) | S |
| 1 | i18n librería | Baja (bloqueado por 2º idioma) | M |
| 7 | `PRE_ENTRY_ITEMS` catálogo | Baja | S |
