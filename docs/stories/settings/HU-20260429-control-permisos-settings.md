# HU-20260429: Control de Permisos para Settings Operativos

**ID**: HU-20260429-control-permisos-settings  
**Tipo**: Seguridad y control de acceso backend  
**Prioridad**: Media  
**Fecha**: 2026-04-29  
**Estado actual**: Pendiente de análisis y desarrollo

## Trazabilidad

- HU relacionada: `lambda-aws-cattle-java/docs/stories/milking/HU-20260429-configuracion-precio-leche-por-sitio.md`
- Consumidor impactado inicialmente: `cattle-front/docs/stories/HU-20260429-panel-ventas-leche.md`

## Contexto de negocio

La incorporación de configuraciones operativas persistidas por sitio, comenzando por `milk-price`, resuelve la consistencia funcional entre usuarios. Sin embargo, también abre una necesidad inmediata de seguridad: no todo usuario del sitio debería poder modificar configuraciones con impacto operativo o económico.

El sistema ya expone settings mediante endpoints específicos de negocio, pero todavía no existe una política explícita y verificable para autorizar lectura y escritura según rol, perfil o capacidad.

## Problema observado

Actualmente un endpoint de settings puede quedar protegido solo al mismo nivel general de la API, sin distinción fina entre:

- consulta de configuración
- actualización de configuración
- tipos de settings con mayor sensibilidad operativa

Esto deja al sistema expuesto a cambios no autorizados, errores operativos y falta de trazabilidad de quién puede alterar parámetros críticos por sitio.

## Objetivo

Definir e implementar un control de permisos para settings operativos por sitio que permita distinguir al menos entre lectura y actualización, con especial énfasis en settings sensibles como el precio de leche por litro.

## Alcance funcional

Incluye:

- definición de permisos mínimos para consultar y editar settings por sitio
- aplicación del control sobre endpoints específicos como `GET/PUT /site/{siteId}/settings/milk-price`
- validación de permisos por usuario autenticado y sitio
- respuesta explícita cuando el usuario no tenga autorización
- trazabilidad básica del usuario que modifica la configuración

No incluye:

- motor completo de RBAC corporativo multiaplicación
- administración visual de roles y permisos
- auditoría avanzada con consola de revisión histórica

## Criterios de aceptación

1. Los endpoints de settings distinguen entre permiso de lectura y permiso de escritura.
2. Un usuario sin permiso de escritura no puede actualizar `milk-price`.
3. Un usuario sin permiso de lectura no puede consultar el setting.
4. El rechazo por autorización responde con código HTTP consistente con la estrategia de seguridad del backend.
5. La validación se realiza considerando el `siteId` del recurso solicitado.
6. El cambio no rompe el contrato funcional ya implementado para usuarios autorizados.

## Análisis inicial

La historia nace como continuación natural del slice reusable `SiteSetting`. Primero se resolvió la persistencia y el contrato operativo. El siguiente riesgo real no es de almacenamiento sino de gobernanza: quién puede cambiar un setting compartido.

La solución debe integrarse con el esquema de seguridad existente del backend y no acoplar la autorización a un solo setting. El control debe diseñarse para reuso futuro sobre otros settings operativos.

## Refinamiento técnico inicial

### Línea base sugerida

1. Identificar cómo llega hoy la identidad/autenticación al backend cuando `SecurityEnabled=true`.
2. Definir una capacidad reusable, por ejemplo `SettingsPermissionService`, para evaluar permisos por acción (`READ`, `WRITE`) y `siteId`.
3. Aplicar esa validación en la fachada de settings antes de delegar al processor.
4. Mantener el contrato actual de `milk-price` para usuarios autorizados.
5. Agregar pruebas de autorización para casos permitidos y denegados.

### Subtareas sugeridas

- [ ] Levantar el modelo actual de autenticación/autorización en la API.
- [ ] Definir la matriz mínima de permisos para settings operativos.
- [ ] Diseñar el servicio reusable de autorización por acción y sitio.
- [ ] Aplicar el control en `MilkPriceSettingsController` o capa equivalente.
- [ ] Incorporar pruebas de autorización positiva y negativa.
- [ ] Documentar comportamiento HTTP para `401/403` según corresponda.

## Estimación inicial

Historia de complejidad media. La dificultad principal no está en `milk-price` sino en acoplar la autorización al modelo de seguridad real ya existente sin introducir reglas duplicadas.

Estimación preliminar: 5 puntos.

## Dependencias

- La persistencia reusable de settings debe estar implementada.
- Debe existir claridad sobre el modelo actual de autenticación del backend en ambientes donde `SecurityEnabled=true`.

## Resultado esperado

Los settings operativos quedan persistidos, consumibles por frontend y además protegidos por permisos explícitos y reutilizables para futuras configuraciones del sitio.