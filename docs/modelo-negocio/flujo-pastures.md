# 🌱 Flujo Detallado: Gestión de Pastures (Potreros)

**Fecha**: 2026-01-09 | **Parte**: Flujo de Negocio

## 🎯 Objetivo

Documentar el flujo completo de rotación de potreros, asignación de bovinos y decisiones de manejo de forraje.

---

## Contexto: Finca Modelo

```
Finca: 100 hectáreas
├─ 10 potreros de 10 hectáreas c/u
├─ Tipo forraje: Pasto ray-grass + trébol
├─ Producción: 3.5 kg/ha en época buena
├─ 50 bovinos totales
│   ├─ 25 en lactancia (máxima demanda)
│   ├─ 20 en cría/desarrollo
│   └─ 5 en secado/preñez
│
└─ Sistema: Rotacional
    ├─ Cambios: cada 1-2 días
    ├─ Objetivo: máximo aprovechamiento
    └─ Tecnología: Dashboard para asistencia
```

---

## Flujo Diario: Mañana (05:00-07:00)

### Inspección Inicial

```
USUARIO accede a PASTURES dashboard

06:00 - VISUALIZACIÓN DE ESTADO:
  │
  ├─ Panel: Estado actual de 10 potreros
  │   └─ Para cada potrero:
  │       ├─ ID: PASTURE_A, PASTURE_B, ...
  │       ├─ Área: 10 ha
  │       ├─ Estado visual: [fotografía]
  │       ├─ Bovinos actualmente: 5 (nombres)
  │       ├─ Carga: 0.5 vacas/ha
  │       ├─ Estimación forraje: 3.2 kg/ha (↑ verde, ✓ bueno)
  │       ├─ Última entrada: 1 día atrás
  │       ├─ Últimas salidas: 3 movimientos
  │       └─ Status: READY_FOR_ENTRY
  │
  ├─ Indicadores visuales:
  │   ├─ VERDE: potrero disponible, buena calidad (>3.0 kg/ha)
  │   ├─ AMARILLO: potrero aceptable (2.0-3.0 kg/ha)
  │   ├─ ROJO: potrero crítico (<2.0 kg/ha)
  │   ├─ GRIS: potrero cerrado (mantenimiento, recuperación)
  │   └─ AZUL: potrero en transición (preparándose)
  │
  └─ Resumen:
      ├─ Potreros disponibles: 8/10
      ├─ Potreros en alerta: 1 (PASTURE_A - amarillo)
      ├─ Potreros cerrados: 1 (PASTURE_D - en recuperación)
      ├─ Total carga actual: 25 bovinos / 80 ha = 0.3 vacas/ha
      └─ Capacidad utilización: 60% (hay espacio para crecer)

06:30 - DATOS HISTÓRICOS Y PREDICCIÓN:
  │
  ├─ PASTURE_A (ALERTA):
  │   ├─ Forraje actual: 2.2 kg/ha (amarillo)
  │   ├─ Tasa consumo: 0.3 kg/ha/día
  │   ├─ Tasa crecimiento: 0.2 kg/ha/día (invierno lento)
  │   ├─ Proyección mañana: 2.2 - 0.3 + 0.2 = 2.1 kg/ha
  │   ├─ Proyección 2 días: 2.0 kg/ha (CRÍTICO)
  │   │
  │   └─ RECOMENDACIÓN SISTEMA:
  │       ├─ Estado: "NEEDS_ATTENTION"
  │       ├─ Acción sugerida: "Rotar hoy si es posible"
  │       ├─ Potrero destino sugerido: PASTURE_B (verde, máxima)
  │       ├─ Bovinos a rotar: [#47 "Estrella", #02 "Bossy"]
  │       └─ Prioridad: MEDIA (hacer hoy, no emergencia)
  │
  └─ PASTURE_B (ÓPTIMO):
      ├─ Forraje: 3.5 kg/ha (verde, máximo)
      ├─ Capacidad: puede absorber 5-7 bovinos más
      ├─ Recomendación: "Potrero receptivo"
      └─ Candidato para relocaciones

07:00 - PRESENTAR PROPUESTA AL USUARIO:
  │
  ├─ INTERFAZ:
  │   └─ Mostrar 3-5 movimientos sugeridos
  │       ├─ [✓] Rotar PASTURE_A → PASTURE_B
  │       │   ├─ Bovinos: #47, #02
  │       │   ├─ Razón: PASTURE_A con forraje bajo
  │       │   ├─ Beneficio estimado: +2% producción
  │       │   └─ Urgencia: HOY
  │       │
  │       ├─ [ ] Mantener PASTURE_C (estable)
  │       │   ├─ Estado: bueno (2.8 kg/ha)
  │       │   └─ Sin cambios recomendados
  │       │
  │       └─ [ ] Preparar PASTURE_D para entrada
  │           ├─ Cerrado hace 3 días (recuperación)
  │           ├─ Forraje recuperado: 2.7 kg/ha
  │           ├─ Listo para: bovinos en secado (menos demanda)
  │           └─ Urgencia: mañana o pasado
  │
  └─ USUARIO DECIDE:
      ├─ Revisa cada movimiento
      ├─ Aprueba: [ ] sí
      ├─ Cancela: [ ] sí
      ├─ Modifica: [ ] sí (diferente bovino, otro destino)
      │
      └─ Click "Ejecutar rotación"
```

---

## Flujo Medio Día: Ejecución Física

### Movimiento Operativo

```
07:30 - PREPARACIÓN:
  │
  ├─ Ordeno finalizó (bovinos en corral después AM)
  ├─ Sistema registra: "bovinos listos para rotar"
  ├─ Abre compuertas y perchas necesarias
  └─ Personal en potrero prepara entradas

08:00 - MOVIMIENTO FÍSICO:
  │
  ├─ BOVINO #47 "Estrella"
  │   ├─ Sale de PASTURE_A (potrero viejo)
  │   │   └─ PASTURES evento: SALIDA
  │   │       ├─ type: "EXIT"
  │   │       ├─ bovineId: 47
  │   │       ├─ from_paddock: PASTURE_A
  │   │       ├─ timestamp: 2025-12-20 08:00:00
  │   │       ├─ reason: "Rotación programada - bajo forraje"
  │   │       └─ observer: "jhonroberth"
  │   │
  │   └─ Entra a PASTURE_B (potrero nuevo)
  │       └─ PASTURES evento: ENTRADA
  │           ├─ type: "ENTRY"
  │           ├─ bovineId: 47
  │           ├─ to_paddock: PASTURE_B
  │           ├─ timestamp: 2025-12-20 08:05:00
  │           ├─ capacity_utilization: (n+1)/ha
  │           ├─ expected_benefit: "+2%"
  │           └─ observer: "jhonroberth"
  │
  ├─ BOVINO #02 "Bossy"
  │   └─ Mismo proceso (few minutos después)
  │
  └─ SISTEMA ACTUALIZA:
      ├─ PASTURE_A:
      │   ├─ Bovinos: 5 → 3 (salieron Estrella, Bossy)
      │   ├─ Carga: 0.5 → 0.3 vacas/ha
      │   ├─ Proyección forr: 2.1 kg/ha → 2.5 kg/ha (menos consumo)
      │   └─ Status: ↓ AMARILLO pero mejorando
      │
      └─ PASTURE_B:
          ├─ Bovinos: 4 → 6 (entraron Estrella, Bossy)
          ├─ Carga: 0.4 → 0.6 vacas/ha
          ├─ Forraje: 3.5 kg/ha
          ├─ Proyección: 3.5 - 0.4 (consumo mayor) = 3.1 kg/ha
          └─ Status: ↓ VERDE (sigue bien)

08:30 - VALIDACIÓN:
  │
  ├─ Usuario verifica movimientos ejecutados
  ├─ Dashboard muestra actualizado
  ├─ Eventos registrados en DB
  │
  └─ Siguiente análisis: después de 1-2 días
      └─ Ver si Estrella produce más en PASTURE_B
```

---

## Flujo Tarde: Monitoreo de Respuesta

### Seguimiento de Desempeño

```
17:00 - ORDENO TARDE (PM):
  │
  ├─ BOVINO #47 "Estrella"
  │   ├─ Lleva ~9 horas en PASTURE_B
  │   ├─ Ordeno PM: 9.2L (ayer fue 9.0L)
  │   ├─ Mejora: +0.2L (+2.2%) vs ayer
  │   │
  │   └─ MILKING registra:
  │       ├─ date: 2025-12-20
  │       ├─ shift: PM
  │       ├─ liters: 9.2
  │       ├─ status: completo
  │       ├─ paddock_at_time: PASTURE_B (nuevo)
  │       └─ trend: ↑ mejora vs ayer
  │
  ├─ SISTEMA CORRELACIONA:
  │   ├─ Cambio de potrero: sí (PASTURE_A → PASTURE_B)
  │   ├─ Cambio de producción: sí (+2.2%)
  │   └─ Conclusión: "Potrero B más efectivo para Estrella"
  │
  └─ MACHINE LEARNING (Backend):
      ├─ Almacenar evento:
      │   ├─ bovineId: 47
      │   ├─ from_paddock: PASTURE_A
      │   ├─ to_paddock: PASTURE_B
      │   ├─ effect_delta: +2.2%
      │   ├─ duration_days: 1
      │   └─ status: "SUCCESS"
      │
      ├─ Actualizar modelo: "Estrella + PASTURE_B = alta respuesta"
      └─ Usar en futuras decisiones: priorizar PASTURE_B para Estrella

20:00 - CIERRE DIARIO:
  │
  ├─ DASHBOARD ACTUALIZADO:
  │   ├─ Rotaciones ejecutadas: 2 movimientos ✓
  │   ├─ Eficiencia: 100%
  │   ├─ Bovinos movidos: 2
  │   ├─ Producción PM post-rotación: +2.2% ✓
  │   ├─ Predicción para mañana: estable (sin nuevas rotaciones)
  │   └─ Alertas: ninguna nueva
  │
  └─ PREPARACIÓN PARA MAÑANA:
      ├─ PASTURE_A: seguimiento (si mejora, o si empeora)
      ├─ PASTURE_D: evaluar si listo para entrada mañana
      └─ Generar nuevas propuestas de rotación
```

---

## Escenario Crisis: Potrero Dañado

### Evento Inesperado

```
DÍA 2: 09:00 - ALERTA DE DAÑO:
  │
  ├─ Inspector reporta:
  │   ├─ "PASTURE_A inundado por lluvia intensa overnight"
  │   ├─ Encharcamiento: 40% del potrero
  │   ├─ Forraje degradado: marcado como NO COMESTIBLE
  │   └─ Riesgo: heces diluidas, parásitos, colapso
  │
  ├─ USUARIO ENTRA A SISTEMA:
  │   │
  │   └─ Click "Reporte de problema"
  │       ├─ Potrero: PASTURE_A
  │       ├─ Tipo: "Waterlogged - lluvia"
  │       ├─ Severidad: "CRITICAL"
  │       ├─ Bovinos actuales: 3 (quedaron después rotación ayer)
  │       └─ Submit
  │
  ├─ SISTEMA RESPONDE (AUTOMÁTICO):
  │   │
  │   ├─ PASO 1: Bloquear potrero
  │   │   └─ PASTURES: status = "UNAVAILABLE"
  │   │       ├─ reason: "Waterlogged"
  │   │       ├─ closed_until: 2025-12-25
  │   │       └─ no_entry: true
  │   │
  │   ├─ PASO 2: Identificar bovinos en riesgo
  │   │   ├─ Query: bovinos con location = PASTURE_A
  │   │   ├─ Resultado: 3 bovinos (#15, #23, #29)
  │   │   └─ Alert: CRITICAL - "3 bovinos necesitan rescate inmediato"
  │   │
  │   ├─ PASO 3: Generar plan de evacuación
  │   │   ├─ Opciones para cada bovino:
  │   │   │   ├─ #15 (lactancia): PASTURE_B (óptimo)
  │   │   │   ├─ #23 (mastitis): PASTURE_Q (enfermería)
  │   │   │   └─ #29 (cría): PASTURE_D (media calidad)
  │   │   │
  │   │   └─ Mostrar plan al usuario
  │   │       └─ "¿Ejecutar evacuación propuesta?"
  │   │
  │   ├─ PASO 4: Notificar stakeholders
  │   │   ├─ Email a gerente: "ALERTA: PASTURE_A fuera de servicio"
  │   │   ├─ Email a veterinario: "3 bovinos posible estrés hídrico"
  │   │   └─ SMS a operario: "Emergencia potrero A - ven ya"
  │   │
  │   └─ PASO 5: Proyectar impacto
  │       ├─ Producción esperada hoy: -45L (3 vacas menos ordenadas)
  │       ├─ Producción finca: 880L (vs 925L normal)
  │       ├─ Pérdida económica: $22 (45L × $0.50)
  │       └─ Duración impacto: 5 días (hasta recuperación)
  │
  └─ USUARIO EJECUTA EVACUACIÓN:
      │
      └─ 09:30: Movimientos manuales ejecutados
          ├─ #15 → PASTURE_B ✓
          ├─ #23 → PASTURE_Q ✓
          ├─ #29 → PASTURE_D ✓
          │
          └─ Sistema actualiza:
              ├─ PASTURE_A: status = UNAVAILABLE
              ├─ PASTURE_A: bovinos = 0
              ├─ PASTURE_B: bovinos = 6 → 7
              └─ Total: 3 bovinos reasignados exitosamente
```

### Evaluación y Recuperación

```
DÍAS 3-5 - MONITOREO DE RECUPERACIÓN:
  │
  ├─ PASTURE_A: encharcamiento disminuye
  │   ├─ Día 3: 30% encharcado (mejora)
  │   ├─ Día 4: 15% encharcado
  │   ├─ Día 5: 5% encharcado (apto)
  │   ├─ Forraje: regenerándose lentamente (1kg/ha/día)
  │   └─ Dia 5 proyección: 2.2 kg/ha (borde aceptable)
  │
  ├─ DECISIÓN:
  │   ├─ Abrir PASTURE_A: el día 6
  │   ├─ Pero: solo para bovinos en secado/baja demanda
  │   ├─ NO para lactancia: esperar a día 8 (3+ kg/ha)
  │   │
  │   └─ Sistema genera plan:
  │       └─ Día 6: introducir 2 bovinos en cría (baja demanda)
  │
  ├─ BOVINOS REASIGNADOS:
  │   ├─ #15 (lactancia): sigue en PASTURE_B (excelente)
  │   │   └─ Producción: +1% manteniéndose en B
  │   │
  │   ├─ #23 (mastitis): mejoró en PASTURE_Q (enfermería)
  │   │   └─ Producción: estable (recuperándose)
  │   │
  │   └─ #29 (cría): bien en PASTURE_D
  │       └─ Comportamiento: normal
  │
  └─ APRENDIZAJE DEL SISTEMA:
      ├─ Evento registrado en histórico
      ├─ Predicción de drenaje: mejorada
      ├─ Alertas tempranas: ajustadas para futuro
      └─ Plan de respuesta: documentado y reutilizable
```

---

## Análisis de Desempeño por Potrero

### Comparativa Mensual

```
MES: DICIEMBRE 2025

POTRERO A (PASTURE_A):
├─ Uso: 28 días
├─ Bovinos promedio: 4.2
├─ Carga: 0.42 vacas/ha
├─ Forraje promedio: 2.8 kg/ha
├─ Entrada de ganado: 12 eventos
├─ Salida de ganado: 12 eventos
├─ Rotaciones: 6 (cada 5 días promedio)
├─ Incidentes: 1 (encharcamiento)
├─ Producción de bovinos en A: 18.5L/día promedio
├─ Impacto (vs potrero neutro): -1.5% (ligero efecto negativo)
└─ Conclusión: POTRERO SUBÓPTIMO (mejorar drenaje)

POTRERO B (PASTURE_B):
├─ Uso: 28 días
├─ Bovinos promedio: 5.8
├─ Carga: 0.58 vacas/ha
├─ Forraje promedio: 3.3 kg/ha
├─ Entrada de ganado: 14 eventos (máxima usar)
├─ Salida de ganado: 14 eventos
├─ Rotaciones: 7 (cada 4 días promedio)
├─ Incidentes: 0
├─ Producción de bovinos en B: 20.2L/día promedio
├─ Impacto (vs potrero neutro): +5.0% (excelente efecto)
└─ Conclusión: POTRERO ESTRELLA (máximo potencial)

POTRERO C (PASTURE_C):
├─ Uso: 20 días (8 días cerrado para recuperación)
├─ Bovinos promedio: 4.0
├─ Carga: 0.40 vacas/ha
├─ Forraje promedio: 2.9 kg/ha
├─ Entrada de ganado: 10 eventos
├─ Salida de ganado: 10 eventos
├─ Rotaciones: 5 (cada 4 días promedio)
├─ Incidentes: 0
├─ Producción de bovinos en C: 19.2L/día promedio
├─ Impacto (vs potrero neutro): -1.0% (ligero efecto negativo)
└─ Conclusión: POTRERO NEUTRAL (necesita mejora menor)

RESUMEN:
├─ Mejor potrero: B (+5.0%)
├─ Peor potrero: A (-1.5%)
├─ Diferencia: 6.5 puntos
├─ Impacto en producción finca: 3% variabilidad gestión potrero
└─ Recomendación: Invertir en drenaje A, expandir B
```

---

## Decisiones Estratégicas de Potrero

### Asignación Óptima por Tipo de Bovino

```
MATRIZ DE ASIGNACIÓN:

LACTANCIA PLENA (máxima demanda - 20-22L/día):
├─ Asignación: PASTURE_B (máximo)
├─ Carga: 0.6 vacas/ha (máxima tolerancia)
├─ Rotación: cada 2 días
├─ Nivel forraje mínimo: 3.0 kg/ha
├─ Bovinos prioritarios: Top 5 productoras
└─ Resultado esperado: +5% producción

LACTANCIA MEDIA (reducción temprana - 15-18L/día):
├─ Asignación: PASTURE_C
├─ Carga: 0.5 vacas/ha
├─ Rotación: cada 3 días
├─ Nivel forraje mínimo: 2.5 kg/ha
├─ Bovinos: medio del rebaño
└─ Resultado esperado: neutral

PREÑEZ (nutrición moderada - lactancia previa):
├─ Asignación: PASTURE_D (media)
├─ Carga: 0.45 vacas/ha
├─ Rotación: cada 3-4 días
├─ Nivel forraje mínimo: 2.2 kg/ha
├─ Objetivo: ganancia peso lenta
└─ Resultado esperado: estable peso

SECADO (baja demanda - pre-parto):
├─ Asignación: PASTURE_E, F, G
├─ Carga: 0.4 vacas/ha
├─ Rotación: cada 4-5 días
├─ Nivel forraje mínimo: 1.8 kg/ha
├─ Objetivo: recuperación condición corporal
└─ Resultado esperado: BCS 3.0 al parto

CRÍA/DESARROLLO (crecimiento - terneros):
├─ Asignación: PASTURE_H (potrero especial)
├─ Carga: 0.3 vacas/ha (muy bajo - mejor nutrición)
├─ Rotación: cada 2 días
├─ Nivel forraje mínimo: 2.8 kg/ha (máxima calidad)
├─ Objetivo: ganancia 0.7 kg/día
└─ Resultado esperado: Crecimiento óptimo
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
