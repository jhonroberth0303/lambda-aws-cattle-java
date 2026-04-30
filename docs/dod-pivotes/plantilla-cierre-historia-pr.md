# Plantilla de Cierre de Historia o PR

## Objetivo

Ofrecer una plantilla corta y reutilizable para cerrar una historia, bug, refactor, tarea documental o PR usando los pivotes DoD definidos en esta carpeta.

La idea es que el equipo pueda copiar esta estructura y dejar evidencia mínima, riesgo residual y estado de pivotes sin depender de texto libre inconsistente.

## Cuándo usarla

Usar esta plantilla cuando se cierre cualquiera de estos casos:

- historia funcional
- corrección de bug
- refactor
- cambio de configuración
- cambio de integración o contrato
- PR técnico con impacto real en código o documentación

## Plantilla corta

```md
## Cierre de Cambio

### Resumen
- Tipo de cambio: <feature|bug|refactor|documentacion|configuracion|integracion>
- Alcance: <qué cambia y en qué módulo>
- Motivación: <problema o necesidad que resuelve>

### Pivotes DoD
- Alcance claro: <cumplido|cumplido con observación|pendiente|exceptuado>
- Patrón técnico: <cumplido|cumplido con observación|pendiente|exceptuado>
- Validación ejecutable: <cumplido|cumplido con observación|pendiente|exceptuado>
- Riesgo residual: <cumplido|cumplido con observación|pendiente|exceptuado>
- Impacto documental: <cumplido|cumplido con observación|pendiente|exceptuado>

### Evidencia
- Archivos principales: <lista corta>
- Validación ejecutada: <comando, prueba o check>
- Resultado: <pass/fail + nota breve>

### Riesgos abiertos
- <riesgo 1 o "sin riesgos abiertos relevantes">

### Documentación afectada
- <archivo actualizado o "no aplica">

### Decisión de cierre
- Estado final: <listo para merge|listo para revisión|cerrado documentalmente|requiere seguimiento>
- Siguiente paso: <si aplica>
```

## Plantilla ampliada

```md
## Cierre de Historia o PR

### 1. Contexto
- ID o referencia: <si existe>
- Tipo de cambio: <feature|bug|refactor|documentacion|configuracion|integracion>
- Repositorio: <cattle-front|lambda-aws-cattle-java|ambos>
- Slice afectado: <componente, flujo, endpoint, documento, servicio>

### 2. Qué se hizo
- <cambio principal 1>
- <cambio principal 2>
- <cambio principal 3>

### 3. Pivotes DoD

| Pivote | Estado | Evidencia | Observación |
|---|---|---|---|
| Alcance claro | <estado> | <fuente o archivo> | <opcional> |
| Patrón técnico | <estado> | <archivo o diff> | <opcional> |
| Validación ejecutable | <estado> | <comando o check> | <opcional> |
| Riesgo residual | <estado> | <nota breve> | <opcional> |
| Impacto documental | <estado> | <archivo doc o no aplica> | <opcional> |

### 4. Validación aplicada
- Comando o check principal: <ej. npm run lint / gradlew test / get_errors>
- Resultado: <pass/fail>
- Alcance de la validación: <qué cubre y qué no cubre>

### 5. Riesgos y deuda abierta
- <riesgo, gap o deuda>
- <mitigación o siguiente paso>

### 6. Documentación y trazabilidad
- Documentos actualizados: <lista>
- Documentos revisados y sin cambio: <lista o no aplica>

### 7. Cierre
- Estado final: <listo para merge|listo para aprobación|cerrado>
- Responsable siguiente paso: <si aplica>
```

## Ejemplo breve

```md
## Cierre de Cambio

### Resumen
- Tipo de cambio: bug
- Alcance: separación de endpoint summary y endpoint CRUD en bovinos frontend
- Motivación: evitar que el formulario use `/summary` como ruta por defecto de escritura

### Pivotes DoD
- Alcance claro: cumplido
- Patrón técnico: cumplido
- Validación ejecutable: cumplido
- Riesgo residual: cumplido con observación
- Impacto documental: cumplido

### Evidencia
- Archivos principales: `src/services/bovinesServices.ts`, `src/components/Bovines/list/BovineList.jsx`, `src/components/Bovines/hooks/useBovineForm.ts`
- Validación ejecutada: `get_errors` sobre archivos tocados
- Resultado: pass

### Riesgos abiertos
- siguen existiendo endpoints hardcodeados por ambiente

### Documentación afectada
- `docs/arquitectura/componente-summary-bovinos.md`

### Decisión de cierre
- Estado final: listo para revisión
- Siguiente paso: unificar configuración por ambiente
```

## Regla práctica

Si un pivote queda en `pendiente` o `exceptuado`, el cierre no debería verse como completo sin una justificación explícita y un siguiente paso claro.