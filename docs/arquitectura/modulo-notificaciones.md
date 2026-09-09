# Módulo de Notificaciones

**Fecha**: 2026-09-09
**Estado**: Diseño — pendiente de refinamiento y desarrollo
**Alcance**: transversal (backend `lambda-aws-cattle-java` + frontend `cattle-front`)

## Registro de cambios

| Fecha | Versión | Descripción |
|------|---------|-------------|
| 2026-09-09 | 0.1 | Diseño inicial de la plataforma de notificaciones con el recordatorio de ordeño AM como primer productor |
| 2026-09-09 | 0.2 | Revisión de infraestructura y seguridad: SnapStart, un schedule por ventana de productor, validación SSRF del endpoint de suscripción, decisión de auth, aislamiento de fallos por productor, sección de alternativas consideradas |
| 2026-09-09 | 0.3 | **Fase 1 implementada** (backend + campana frontend). Ver sección "Estado de implementación". Auth resuelta como camino B. Dual config siteId/farmId para el productor de ordeño. |

## Contexto

Se necesita un canal de notificaciones que llegue al dispositivo del operario (Windows y Android; iOS fuera de alcance por ahora) **aunque la app esté cerrada**. El primer caso es el recordatorio del ordeño de la mañana, pero el módulo debe ser **base reutilizable** para otros dominios: sanidad de bovinos, rotación y descanso de potreros, ventas, etc.

Por eso el diseño separa **plataforma de notificaciones** (genérica) de **productores** (una regla de negocio por tipo).

## Principios

1. **El backend es la fuente de verdad.** Un `Notification` es una instancia persistida por finca. Los canales (in-app, web push) son solo entrega de esa misma instancia. La campana y el push muestran lo mismo.
2. **Un productor por tipo de notificación.** Cada productor declara su *ventana horaria* y su *dedupe key*. Añadir "sanidad" o "potreros" = añadir un productor + un schedule declarativo, sin tocar plataforma ni frontend salvo el mapa de presentación.
3. **Idempotencia por `dedupeKey`.** El scheduler puede correr varias veces; nunca se crea la misma notificación dos veces el mismo día.
4. **Identidad por `farmId` + dispositivo, sin usuario.** Hoy no hay sesión real (`recordedBy` hardcodeado). Las suscripciones y notificaciones se anclan a `farmId`; `userId` se añade como atributo opcional cuando exista auth, sin romper contrato.
5. **Aislamiento de fallos por productor.** Un productor que lanza excepción se registra y se salta; no tumba el run ni impide que los demás productores emitan. Mismo principio que la tolerancia a fallos parciales de `refreshAllSummaries()`.
6. **Sin confianza en datos del cliente.** El `endpoint` de suscripción es una URL controlada por el cliente que el backend va a invocar: se valida contra una allowlist de push services y se bloquean destinos internos (ver [Seguridad](#seguridad)).

## Modelo de dominio

### `NotificationType` (enum)

| Valor | Dominio | Productor | Ventana (schedule) |
|---|---|---|---|
| `MILKING_AM_REMINDER` | Ordeño | `MilkingAmReminderProducer` | `MORNING` — 09:00 local |
| `MILKING_PM_REMINDER` | Ordeño | `MilkingPmReminderProducer` (fase posterior) | `AFTERNOON` — ~15:00 |
| `BOVINE_HEALTH_DUE` | Sanidad | `BovineHealthDueProducer` (fase posterior) | `EARLY` — 06:00 |
| `PASTURE_REST_READY` | Potreros | `PastureRestReadyProducer` (fase posterior) | `MIDDAY` — 13:00 |

El enum vive en backend; el frontend tiene un mapa de presentación paralelo (`notificationTypes.js`) con icono, etiqueta y color por tipo.

### `Notification` (instancia persistida)

Tabla DynamoDB `Notifications` (parámetro `TableNotifications`):

| Atributo | Tipo | Notas |
|---|---|---|
| `PK` | S | `FARM#<farmId>` |
| `SK` | S | `NOTIF#<createdAt ISO>#<uuid>` — orden cronológico natural |
| `notificationId` | S | uuid |
| `type` | S | `NotificationType` |
| `title` | S | texto corto |
| `body` | S | texto |
| `deeplink` | S | ruta relativa del frontend, p. ej. `/lactancia?focus=registro` |
| `data` | M | payload libre por tipo (p. ej. `{ pendingCows: 4, date: "2026-09-09" }`) |
| `status` | S | `UNREAD` \| `READ` |
| `dedupeKey` | S | `<type>#<farmId>#<yyyy-mm-dd>` (o más fino) — evita duplicados |
| `createdAt` | S | ISO-8601 `America/Bogota` |
| `readAt` | S | ISO-8601 o vacío |
| `ttl` | N | epoch; retención **por tipo** (`NotificationType.retentionDays()`) — DynamoDB borra el item al vencer. `MILKING_AM_REMINDER` = 3 días (deja de aportar pasado ese plazo). |

Consulta de la campana: `Query PK = FARM#<farmId>`, `ScanIndexForward=false`, `Limit=30`. Conteo no leídas: filtra `status = UNREAD` (o un GSI si el volumen lo exige; al inicio no).

Deduplicación: el dispatcher escribe primero un ítem centinela `PK=FARM#<farmId>` `SK=DEDUPE#<dedupeKey>` con `ConditionExpression attribute_not_exists(SK)` y su propio `ttl`. Si la condición falla, la notificación ya existía hoy y se descarta sin insertar. Sin GSI.

### `PushSubscription` (dispositivo)

Tabla DynamoDB `PushSubscriptions` (parámetro `TablePushSubscriptions`):

| Atributo | Tipo | Notas |
|---|---|---|
| `PK` | S | `FARM#<farmId>` |
| `SK` | S | `SUB#<sha256(endpoint)>` |
| `endpoint` | S | URL del push service (FCM, Mozilla, WNS…) |
| `p256dh` | S | `subscription.keys.p256dh` |
| `auth` | S | `subscription.keys.auth` |
| `userAgent` | S | diagnóstico |
| `topics` | SS | tipos suscritos; `["*"]` = todos |
| `createdAt` / `lastSeenAt` | S | ISO-8601 |
| `failureCount` | N | se purga a partir de 3 o ante `404/410` |

## Arquitectura

```text
┌─ Frontend (PWA, Windows/Android) ───────────────────────────────┐
│ NotificationsProvider (context)                                  │
│   • GET /farms/{farmId}/notifications        (lista, al abrir     │
│   • GET /farms/{farmId}/notifications/unread-count   panel / foco)│
│   • POST .../{id}/read  ·  POST .../read-all                      │
│ NotificationBell + NotificationPanel   (reemplaza placeholder)   │
│ PushOptIn: Notification.requestPermission()                       │
│            → pushManager.subscribe(VAPID_PUBLIC)                  │
│            → POST /farms/{farmId}/push-subscriptions              │
│ src/sw.js (injectManifest):                                       │
│   'push'              → showNotification(title, {body,tag,data})  │
│   'notificationclick' → clients.openWindow(data.deeplink)         │
└─────────────────────────────────────────────────────────────────┘
                   │ HTTPS (API Gateway /{proxy+} → Spring)
┌─ Backend Spring ────────────────────────────────────────────────┐
│ NotificationController      (lista / read / unread-count)        │
│ PushSubscriptionController   (alta / baja)                       │
│ PushConfigController         (GET /config/push → vapidPublicKey) │
│ NotificationService          (persistir, listar, marcar)         │
│ NotificationDispatcher       (draft → persistir in-app + push)   │
│ PushSubscriptionService                                          │
│ WebPushClient  → nl.martijndwars:web-push (VAPID + aes128gcm)    │
└─────────────────────────────────────────────────────────────────┘
                   ▲
┌─ Scheduler ─────────────────────────────────────────────────────┐
│ EventBridge Scheduler — un schedule por ventana de productor,    │
│ todos con TZ America/Bogota y target la MISMA Lambda:            │
│   NotificationMorningSchedule  cron(0 9 * * ? *)   {"window":"MORNING"}   │
│   NotificationEarlySchedule    cron(0 6 * * ? *)   {"window":"EARLY"}     │
│   NotificationMiddaySchedule   cron(0 13 * * ? *)  {"window":"MIDDAY"}    │
│     → Lambda NotificationSchedulerFunction (SnapStart activo)    │
│        → NotificationSchedulerHandler (contexto Spring, calcado  │
│          de SummaryRefreshSchedulerHandler)                      │
│        → window = event.window                                   │
│        → for each NotificationProducer con producer.window()==window: │
│            try { drafts += producer.evaluate(nowBogota) }        │
│            catch (e) { log + continue }   // aislamiento de fallos│
│        → NotificationDispatcher.dispatch(drafts)                 │
│             • dedupe por dedupeKey (ítem DEDUPE# condicional)    │
│             • persiste Notification (canal IN_APP)               │
│             • si topic suscrito → WebPushClient.send(...)        │
│             • purga suscripciones 404/410                        │
│        → CloudWatch Logs / DLQ SQS                               │
└─────────────────────────────────────────────────────────────────┘
```

### Interfaz `NotificationProducer`

```java
public interface NotificationProducer {
    NotificationType type();
    ScheduleWindow window();                                   // EARLY | MORNING | MIDDAY | ...
    List<NotificationDraft> evaluate(ZonedDateTime nowBogota); // 0..N drafts, por finca
}
```

`NotificationDraft` = `{ farmId, type, title, body, deeplink, data, dedupeKey }`.

El scheduler descubre los productores por inyección de Spring (`List<NotificationProducer>`) y solo corre los de la ventana que dispara el schedule. Añadir un productor nuevo = una clase + (si estrena ventana) un `AWS::Scheduler::Schedule` declarativo. El handler no cambia.

Se elige **un schedule por ventana** en vez de un único cron frecuente con filtro en runtime porque: menos invocaciones ociosas, la ventana queda declarada en infraestructura (auditable), y cada dominio ajusta su hora sin tocar código.

### Productor 1 — `MilkingAmReminderProducer`

- `window()`: `MORNING` (schedule a las 09:00).
- `evaluate`: para cada finca operativa, cuenta vacas con lactancia `LACTATING` sin `MilkingRecord` (`date = hoy`, `shift = AM`). Si `> 0` → un draft:
  - `title`: "Ordeño de la mañana pendiente"
  - `body`: "Faltan N vacas por registrar el ordeño AM de hoy."
  - `deeplink`: `/lactancia?focus=registro`
  - `data`: `{ pendingCows: N, date }`
  - `dedupeKey`: `MILKING_AM_REMINDER#<farmId>#<yyyy-mm-dd>`
- Reusa `MilkingQueryService` / `MilkingRepository` y la consulta de lactancias activas. Mientras el módulo de ordeño use `SITE_ID = "001"`, opera sobre esa sola finca; el draft ya sale con `farmId` para no re-trabajar.

## Contrato API

```
GET    /farms/{farmId}/notifications?limit=30&status=all|unread
         → [{ notificationId, type, title, body, deeplink, data, status, createdAt, readAt }]
GET    /farms/{farmId}/notifications/unread-count      → { count }
POST   /farms/{farmId}/notifications/{notificationId}/read   → 204
POST   /farms/{farmId}/notifications/read-all               → { updated }

POST   /farms/{farmId}/push-subscriptions
         body { endpoint, keys:{p256dh,auth}, userAgent, topics }
         → { subscriptionId, farmId, topics }
DELETE /farms/{farmId}/push-subscriptions   body { endpoint }   → 204

GET    /config/push   → { vapidPublicKey }
```

Autenticación: ver [Seguridad](#seguridad). Se elige entre entrar con un Lambda Authorizer real o asumir el hueco de auth actual como deuda explícita; el contrato no cambia según la decisión.

## Frontend — estructura

```
src/notifications/
├── NotificationsProvider.jsx   context: { items, unreadCount, loading, refresh, markRead, markAllRead }
│                               fetch al montar + en window 'focus' / 'visibilitychange' (sin polling en intervalo:
│                               los recordatorios son diarios, no necesitan latencia sub-minuto)
├── useNotifications.js
├── NotificationBell.jsx        reemplaza <NotificationButton count={3}> hardcodeado en DashboardLayout
├── NotificationPanel.jsx       dropdown: lista, icono por tipo, tiempo relativo, no-leídas resaltadas,
│                               click → markRead + navigate(deeplink); botón "marcar todo leído"
├── PushOptIn.jsx               estados: unsupported | default | granted | denied ; activar/desactivar
├── pushClient.js               subscribe(farmId) / unsubscribe(farmId) / getState() ; urlBase64ToUint8Array
└── notificationTypes.js        { MILKING_AM_REMINDER: { icon:'🥛', label:'Ordeño', tone:'warn' }, ... }

src/sw.js                       injectManifest: precacheAndRoute(self.__WB_MANIFEST)
                                + rutas runtime migradas (bovines/pastures/events NetworkFirst)
                                + listeners 'push' y 'notificationclick'
```

`NotificationsProvider` se monta en `main.jsx` dentro de `ActiveFarmProvider`. La campana funciona **con o sin** permiso de push: sin push muestra igual las notificaciones generadas server-side (solo que el operario debe abrir la app para verlas); con push llegan al SO.

## Infraestructura — `template.yml`

- Parámetros nuevos: `TableNotifications`, `TablePushSubscriptions`, `PushEndpointAllowlist` (default con hosts de FCM/Mozilla/WNS), `VapidPublicKeyParam`, `VapidPrivateKeyParam`, `VapidSubject`.
- `TableNotifications`, `TablePushSubscriptions` (`AWS::DynamoDB::Table`, PK/SK String, PAY_PER_REQUEST, TTL en `ttl` para Notifications).
- Permisos de ambas tablas a la función HTTP principal y a `NotificationSchedulerFunction`.
- `NotificationSchedulerFunction` (Handler `com.cattle.NotificationSchedulerHandler::handleRequest`, timeout 300 s), **con `AutoPublishAlias` + `SnapStart: ApplyOn: PublishedVersions`**.
- `NotificationSchedulerDlq` (SQS, retención 14 días) + `NotificationSchedulerExecutionRole`.
- Un `AWS::Scheduler::Schedule` por ventana (`NotificationMorningSchedule` a las 09:00, más los que estrenen otros productores), todos con `ScheduleExpressionTimezone: !Ref AppTimezone`, `Input` con `{"window":"..."}`, retry 2, maxEventAge 3600, `DeadLetterConfig` a la DLQ.
- VAPID: `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`, `VAPID_SUBJECT` como env vars de las Lambdas, resueltas desde SSM Parameter Store (SecureString). **Nunca en el repo.**

### SnapStart (aplica más allá de este módulo)

El cold start de un handler que hace `new SpringApplicationBuilder(Application.class).run()` es de 10–20 s. Con **Lambda SnapStart para Java** (GA) el runtime toma un snapshot del JVM tras la init y lo restaura en ~1–2 s. Recomendado activarlo en:

- `NotificationSchedulerFunction` (este módulo)
- `SummaryRefreshSchedulerFunction` (ya existente)
- la función HTTP principal (mejora la latencia p99 de la API)

Requiere versiones publicadas (`AutoPublishAlias`) y que el target del scheduler/API apunte al alias, no a `$LATEST`. Ojo con estado que se cachea en la init (conexiones, aleatoriedad sembrada): revisar `ApplicationContextHolder` y los clientes de DynamoDB antes de activarlo en la función HTTP.

## Dependencias nuevas (backend)

- `nl.martijndwars:web-push` (+ `org.bouncycastle:bcprov-jdk18on`) para el envío VAPID. Vigilar tamaño del artefacto y compatibilidad con SnapStart (BouncyCastle registra un `Provider` en init — validar que el snapshot lo conserve).

## Seguridad

### S1 — SSRF en el `endpoint` de suscripción (obligatorio)

`POST /farms/{farmId}/push-subscriptions` recibe un `endpoint` que el backend (la Lambda del scheduler) va a invocar con un POST. Sin control, un atacante registra `endpoint: http://169.254.169.254/latest/meta-data/iam/security-credentials/...` (o cualquier IP interna) y el `WebPushClient` filtra credenciales o alcanza servicios internos.

Mitigación, al **registrar** y de nuevo antes de **enviar**:

- el `endpoint` debe ser `https://`
- el host debe coincidir con `PushEndpointAllowlist`: `fcm.googleapis.com`, `*.push.services.mozilla.com`, `*.notify.windows.com`, `updates.push.services.mozilla.com`, `*.push.apple.com` (aunque APNs quede fuera de alcance ahora)
- resolver el host y rechazar si apunta a rango privado/loopback/link-local (`10/8`, `172.16/12`, `192.168/16`, `127/8`, `169.254/16`, `::1`, `fc00::/7`)
- timeout corto y sin seguir redirecciones en el `WebPushClient`

### S2 — Autenticación e IDOR sobre `farmId`

Estado real (revisado en código): existe `SecurityConfig` + `JwtAuthenticationFilter` + `JwtTokenProvider` + `FarmUserPrincipal`. `/farms/**` está declarado `.authenticated()`. Pero: el filtro valida un JWT **HMAC emitido por el propio backend** (no el ID token RS256 de Google que guarda el frontend), `jwt.validate-issuer` es `false`, tokens sin expiración pasan, y `SECURITY_ENABLED` tiene default `'false'` en `template.yml` (dev = `permitAll`). El frontend tampoco adjunta `Authorization`.

**Decisión tomada para Fase 1 (camino B):** los endpoints de notificaciones entran bajo `/farms/{farmId}/notifications`, heredando **exactamente la misma postura de seguridad** que `BovineController` y `PastureController`. No se añade ni se empeora nada. El dato expuesto es de baja sensibilidad ("faltan N vacas").

**Sigue pendiente y es bloqueante para Fase 2:** un Lambda Authorizer real (valida el ID token de Google — firma RS256 con JWKS, `aud`, `exp` — y ata `token → farmId`, rechazando si el `farmId` del path no coincide). Es trabajo transversal que beneficia a toda la API; se agenda como historia aparte. Antes de que un canal saque la notificación fuera de la app (push), esto debe estar.

### S3 — Otros

- **VAPID private key**: SSM Parameter Store `SecureString`, leída en la init de la Lambda. No como texto plano en env var del template. Rotarla invalida todas las suscripciones (documentar).
- **Payload del push**: va cifrado extremo a extremo (aes128gcm con `p256dh`/`auth`); el push service no lo lee. Aun así, no incluir datos sensibles en `title`/`body`.
- **`notificationclick` → `openWindow`**: navegar solo a rutas relativas del propio origen; nunca abrir un `deeplink` absoluto que venga en `data`.
- **Rate limit** de `POST /push-subscriptions` por IP/finca para evitar inflar la tabla.

## Fases

| Fase | Entregable | Validable aquí | Requiere despliegue |
|---|---|---|---|
| **0 — Decisión de auth (S2)** | Elegir camino A (Lambda Authorizer) o B (deuda explícita). Bloquea la exposición de endpoints. | — | — |
| **1 — Plataforma + campana** | Tabla `Notifications` + centinela `DEDUPE#`, `NotificationService` + controller (list/unread-count/read/read-all), `NotificationSchedulerHandler` + `NotificationDispatcher` (solo canal IN_APP, aislamiento de fallos por productor), `MilkingAmReminderProducer`, `MORNING` schedule + SnapStart. Frontend: `NotificationsProvider` + `NotificationBell` + `NotificationPanel` contra endpoints reales. | Build front + gradle back | schedule + tabla |
| **2 — Canal Web Push** | `PushSubscriptions` + endpoints + `GET /config/push`, validación SSRF (S1), `WebPushClient`, canal `WEB_PUSH` en el dispatcher, purga de muertas. Frontend: `src/sw.js` injectManifest, `PushOptIn`, `pushClient`. | Build front + gradle back | VAPID keys en SSM, deploy |
| **3 — Más productores** | `BovineHealthDueProducer`, `PastureRestReadyProducer` (+ sus ventanas, deeplinks y presentación). | sí | deploy |
| **4 — Robustez** | métricas CloudWatch, segunda pasada condicional, rate limit de suscripción, preferencias por dispositivo, SnapStart en la función HTTP. | sí | deploy |

## Criterios de aceptación (Fase 1 + 2)

1. El scheduler crea a las 09:00 (una sola vez al día) una notificación `MILKING_AM_REMINDER` por finca con ordeño AM incompleto; no la crea si está completo.
2. La campana muestra el conteo real de no leídas y la lista, con o sin permiso de push.
3. Marcar leída (individual y "todo") persiste y actualiza el badge.
4. Un operario en Windows/Android puede activar el push; el navegador queda en `PushSubscriptions`.
5. Con push activo, la notificación del scheduler llega al SO aunque la app esté cerrada.
6. Tocar la notificación abre `/lactancia` en la sección de registro.
7. Desactivar el push borra la suscripción; deja de llegar.
8. Suscripciones con `404/410` se purgan solas.
9. Añadir un productor nuevo (sanidad) no requiere cambios en plataforma ni en la campana, solo el productor + (si estrena ventana) un schedule + una entrada en `notificationTypes.js`.
10. Ninguna llave VAPID ni secreto en el repositorio.
11. Un `endpoint` de suscripción fuera de la allowlist o que resuelve a IP privada se rechaza en el registro y no se invoca nunca (S1).
12. Un productor que lanza excepción queda registrado en CloudWatch y no impide que los demás emitan (principio 5).

## Riesgos / decisiones abiertas

- **Auth (S2)**: decidir Lambda Authorizer real vs deuda explícita **antes** de exponer los endpoints. Es la decisión de mayor peso del módulo.
- **Sin usuario**: la notificación la reciben todos los dispositivos suscritos de la finca, no "el encargado". Aceptable para fincas pequeñas; revisar con auth real.
- **SnapStart + BouncyCastle**: validar que el `Provider` de BC y los clientes de DynamoDB sobrevivan al snapshot; si no, `NotificationSchedulerFunction` va sin SnapStart y se acepta el cold start (job de fondo, no crítico en latencia).
- **Dedupe**: se elige el ítem centinela `PK=FARM#<farmId>` `SK=DEDUPE#<dedupeKey>` con `ConditionExpression attribute_not_exists` y su propio `ttl`; sin GSI. Revisar si aparece un patrón de consulta que lo exija.
- **`SITE_ID` fijo en ordeño**: la Fase 1 opera sobre una finca; el contrato ya es multi-finca.
- **Cadencia por ventana**: si crecen los productores con horas muy dispersas, se acumulan schedules. Aceptable hasta ~6-8; luego reconsiderar un único cron horario con filtro.

## Estado de implementación

### Fase 1 — hecha y validada (2026-09-09)

**Backend `lambda-aws-cattle-java`** — paquete `com.cattle.notifications`:

| Capa | Clases |
|---|---|
| Enums | `NotificationType`, `NotificationStatus`, `ScheduleWindow` |
| Modelo | `NotificationDraft` (record con validación), `entity/NotificationItem` |
| Persistencia | `repository/NotificationRepository` (save, `tryClaimDedupe` condicional, `findByFarm`, `findUnreadByFarm`, `findById`) |
| Servicios | `service/NotificationService` (list / unread-count / markRead / markAllRead), `service/NotificationDispatcher` (dedupe + persistir + aislamiento por draft) |
| Productores | `producer/NotificationProducer` (interfaz), `producer/MilkingAmReminderProducer`, `producer/milking/MorningMilkingStatusService` + `MorningMilkingStatus` |
| Web | `web/NotificationController` → `/farms/{farmId}/notifications` |
| Scheduler | `com.cattle.NotificationSchedulerHandler` (lazy Spring holder → **unit-testable**, a diferencia del de summary-refresh) |
| Infra | `template.yml`: `NotificationSchedulerFunction` + DLQ + `NotificationSchedulerExecutionRole` + `NotificationMorningSchedule` (`{"window":"MORNING"}`), params `TableNotifications` / `NotificationMorningScheduleExpression` / `NotificationMilkingSiteId` / `NotificationMilkingFarmId`. La tabla `notifications-prod` se crea **fuera del stack** (script en `create-scripts.txt`), igual que el resto de tablas de datos. |

- 40 tests unitarios nuevos (`./gradlew build` → `BUILD SUCCESSFUL`, 913 tests, 0 fallos). Cubren: validación del draft, parseo de ventana, cómputo de pendientes (con turnos/fechas/casos mixtos), emisión del productor, dedupe + aislamiento del dispatcher, mapeo y traducción de errores del servicio, clamping del controller, filtrado por ventana e aislamiento de fallos del handler.
- **Canal**: solo `IN_APP` (persistencia). Web push = Fase 2.
- **Dual config del productor de ordeño**: `NOTIFICATION_MILKING_SITE_ID` (default `001`, consulta los datos de ordeño) ≠ `NOTIFICATION_MILKING_FARM_ID` (default `F001`, finca destinataria = la que consulta la campana). Resuelve la impedancia `siteId` "001" del módulo de ordeño vs `farmId` "F001" de los endpoints `/farms/**`. Se unifica cuando el ordeño sea multi-finca real.

**Frontend `cattle-front`** — carpeta `src/notifications/`:

- `notificationsContext.js` (contexto + `useNotifications`), `NotificationsProvider.jsx` (estado, refresh al montar / `focus` / `visibilitychange`, `markRead`/`markAllRead` optimistas), `notificationService.js` (cliente HTTP), `notificationTypes.js` (presentación por tipo), `NotificationBell.jsx` (reemplaza el placeholder `count={3}`), `NotificationPanel.jsx` (toggle "Sin leer / Todas", por defecto "Sin leer"), `notifications.css`.
- Provider montado en `main.jsx` dentro de `ActiveFarmProvider`.
- `NotificationButton.jsx`/`.css` (placeholder muerto) eliminados; sus estilos migrados a `notifications.css`.
- `npm run build` OK, `eslint` limpio.

### Incidente en el primer deploy (2026-09-09)

Primera invocación del scheduler → `ResourceNotFoundException` en `ProfileLactancyRepository.findAllLactations`. **Causa raíz**: `samconfig.toml` tenía los nombres de tabla sin el sufijo `-prod` (`Bovines`, `MilkingRecords`, …) mientras las tablas reales son `bovines-prod`, `milking-records-prod`, etc. La Lambda HTTP funcionaba porque sus env vars se habían ajustado a mano en la consola; la Lambda nueva tomó los valores del IaC desactualizado.

El aislamiento de fallos por productor funcionó: el scheduler registró el error, saltó el productor y terminó limpio (sin DLQ).

**Corregido:**
- `samconfig.toml` (3 bloques) → nombres reales `-prod` + `TableNotifications="notifications-prod"` + `NotificationMilkingSiteId` / `NotificationMilkingFarmId`.
- `template.yml` → se quitaron los recursos `NotificationsTable` y `SiteSettingsTable` (`AWS::DynamoDB::Table`); todas las tablas de datos viven fuera del stack, el stack solo las referencia por nombre.
- Script de `notifications-prod` añadido a `create-scripts.txt`.

### Pendiente de Fase 1 para funcionar end-to-end

1. Crear la tabla `notifications-prod` (`create-scripts.txt`).
2. **Antes de redesplegar**: verificar que la tabla `SiteSettings` (creada por el stack, mayúsculas) esté vacía — `aws dynamodb scan --table-name SiteSettings --max-items 5`. Si tiene datos de precio de leche, no redesplegar hasta migrarlos a `site-settings-prod`.
3. `sam deploy` (ahora `samconfig.toml` refleja la realidad; alinea también las env vars de la Lambda HTTP con lo que ya tiene puesto a mano).
4. Invocar el scheduler manualmente y validar (`created` > 0 si hay ordeño AM pendiente).
5. Confirmar `NOTIFICATION_MILKING_FARM_ID` = el `farmId` real que usa la campana.
6. Opcional: `?focus=registro` en `/lactancia` aún no hace scroll al `DailyMilkRecordCard` (ya es la primera sección; impacto nulo).

## Alternativas consideradas

### Transporte del push

| Opción | Veredicto | Razón |
|---|---|---|
| **Web Push + VAPID (raw)** | ✅ elegida | Estándar, sin proveedor, sin costo, cubre Chrome/Edge/Firefox/Android. Todo queda en AWS. Cuesta implementar suscripciones, limpieza y cifrado (vía librería). |
| FCM (HTTP v1) directo | ❌ | Para web sigue usando VAPID por debajo; añade proyecto Google Cloud y service account sin ganancia real mientras sea PWA-only. Válido si algún día hay apps nativas. |
| Amazon SNS mobile push / Pinpoint (End User Messaging) | ❌ | No hacen web push nativo; necesitarían FCM en medio. Pinpoint en transición. |
| OneSignal | ⚠️ descartada por ahora | SDK web y dashboard listos, free tier amplio. No aporta bandeja in-app integrada al mismo modelo de datos; suma un proveedor. |

### Plataforma (bandeja, preferencias, digest, logs)

| Opción | Veredicto | Razón |
|---|---|---|
| **Construir sobre DynamoDB + Spring** | ✅ elegida | Un solo stack y una factura; coherente con `SummaryRefreshScheduler` y el resto del backend. El modelo "por finca sin usuario" encaja. Más código propio. |
| Novu (OSS, self-host o cloud) | ⚠️ reevaluar a futuro | Trae componente React de bandeja in-app, preferencias, digest/batching, logs de entrega — justo lo que se construye a mano. Contra: otro servicio que operar, datos fuera de DynamoDB, y su modelo gira en torno a `subscriberId` = usuario, que hoy no existe. Reconsiderar si el módulo crece a email/SMS/WhatsApp con plantillas y preferencias finas. |
| Knock / Courier | ❌ | Igual que Novu en beneficio; SaaS de pago, sin opción self-host relevante para este tamaño. |

### In-app: fuente de las notificaciones

| Opción | Veredicto | Razón |
|---|---|---|
| **Store en backend (elegida)** | ✅ | Una sola implementación de cada regla, historial y estado leído/no-leído, y el push necesita cómputo en servidor de todos modos. |
| Derivado en cliente (la campana calcula "AM pendiente" desde los datos de ordeño) | parcial | Se mantiene **solo** como banner en vivo dentro del dashboard de lechería (UX más inmediata en esa pantalla). La campana global usa el store. No se duplica la regla en JS para el resto de tipos. |
