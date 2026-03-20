# 🥛 Flujo Detallado: Producción Lechera (Milking)

**Fecha**: 2026-01-09 | **Parte**: Flujo de Negocio

## 🎯 Objetivo

Documentar el flujo completo de registros de lactancia, desde captura de datos hasta análisis de tendencias y toma de decisiones.

---

## Contexto Operativo

```
Sistema de Ordeno: 2 × diarios
├─ AM (mañana): 05:30 - 07:00 (ordeno temprano)
├─ PM (tarde): 17:00 - 18:30 (ordeno tarde)
└─ Intervalo: 12 horas (óptimo)

Bovinos en lactancia: 25
├─ Razas principales: Holstein (22), Jersey (3)
├─ Producción esperada: 18-22L/día/bovino
├─ Producción total finca: 450-550L/día
└─ Calidad leche: % grasa y proteína requeridos

Equipamiento:
├─ Sala de ordeno: 4 puestos (ordena 4 simultáneamente)
├─ Medidores: cada puesto tiene contador digital
├─ Registrador: persona que toma datos
└─ Sistema: captura manual + base datos
```

---

## Flujo AM: Ordeno Mañana (05:30-07:00)

### Pre-Ordeno

```
05:00 - PREPARACIÓN:
  │
  ├─ Personal llega a sala
  ├─ Verifica equipamiento:
  │   ├─ Medidores calibrados
  │   ├─ Tubería limpia
  │   ├─ Agua caliente disponible
  │   └─ Solución desinfectante OK
  │
  ├─ Preparar bovinos:
  │   ├─ Verificar en corral
  │   ├─ Revisar alertas previas de salud
  │   │   └─ BOVINES: si está "under treatment" → protocolo especial
  │   │
  │   ├─ Traer a sala de espera (10-15 bovinos primeros)
  │   └─ Calmar: música suave, ambiente tranquilo
  │
  └─ Registrador revisa:
      ├─ Sistema lista para capturas
      ├─ Formulario digital abierto
      ├─ Lista de bovinos ordenados ayer (para comparación)
      └─ Cualquier nota de incidencias previas
```

### Ordeno Propiamente Dicho

```
05:30 - INICIO ORDENO:
  │
  ├─ BOVINO #47 "ESTRELLA" (Puesto 1)
  │   │
  │   ├─ PRE-ORDENO INDIVIDUAL:
  │   │   ├─ Lavar ubres (agua caliente + soap)
  │   │   ├─ Secar con papel
  │   │   ├─ Desinfectar (solución: iodo o cloro)
  │   │   ├─ Estimulación manual: masaje de tetas
  │   │   └─ Objetivo: estimular reflejo eyección
  │   │
  │   ├─ COLOCAR EQUIPO:
  │   │   ├─ Conectar pezoneras
  │   │   ├─ Iniciar succión (máquina comienza)
  │   │   ├─ Monitoreo: 5-7 minutos típico
  │   │   └─ Alarma: si no sale leche en 2 min → revisar
  │   │
  │   ├─ DURANTE ORDENO:
  │   │   ├─ Observar flujo de leche
  │   │   ├─ Medidor (automático en puesto):
  │   │       ├─ Captura: litros en tiempo real
  │   │       ├─ Velocidad de flujo (L/min)
  │   │       └─ Tiempo de ordeno (minutos)
  │   │   │
  │   │   ├─ Visual inspection:
  │   │   │   ├─ Color de leche: blanco lechoso (normal)
  │   │   │   │   └─ Si amarillenta/sangre: ALERTA
  │   │   │   │
  │   │   │   ├─ Consistencia: uniforme
  │   │   │   │   └─ Si grumos: mastitis probable
  │   │   │   │
  │   │   │   └─ Comportamiento bovino:
  │   │   │       ├─ Tranquilo (normal)
  │   │   │       ├─ Nervioso (estrés)
  │   │   │       └─ Aggressive (mastitis/dolor)
  │   │   │
  │   │   └─ Tiempo: ~5 minutos
  │   │
  │   ├─ FIN DE ORDENO:
  │   │   ├─ Flujo cae (gota a gota)
  │   │   ├─ Detener máquina
  │   │   ├─ Remover pezoneras (cuidado)
  │   │   └─ DIP (baño post-ordeno):
  │   │       ├─ Sumergir tetas en solución
  │   │       └─ Objetivo: sello de teta post-ordeno
  │   │
  │   ├─ CAPTURA DE DATOS:
  │   │   ├─ Lectura manual de medidor: 9.8L
  │   │   ├─ Registrador escribe en tablet/papel:
  │   │   │   ├─ bovineId: 47
  │   │   │   ├─ name: "Estrella"
  │   │   │   ├─ date: 2025-12-20
  │   │   │   ├─ shift: AM
  │   │   │   ├─ liters: 9.8
  │   │   │   ├─ status: completo
  │   │   │   ├─ observations: "-"
  │   │   │   ├─ paddock_at_time: PASTURE_B
  │   │   │   └─ recordedBy: "jhonroberth"
  │   │   │
  │   │   └─ Tiempo para captura: 30 segundos
  │   │
  │   └─ POST-ORDENO:
  │       ├─ Enviar a potrero (si no hay más ordeno)
  │       ├─ Volver a PASTURE_B para pastar
  │       └─ Reposo: se acuesta después ordeno 2-3 horas
  │
  ├─ BOVINO #23 "MOLLY" (Puesto 2):
  │   │   [Mismo proceso que Estrella, pero...]
  │   │
  │   ├─ PROBLEMA DETECTADO:
  │   │   ├─ Pre-ordeno: ubre caliente en cuarto anterior-der
  │   │   ├─ Observación: inflamación visible
  │   │   └─ Sospecha: MASTITIS
  │   │
  │   ├─ ACCIÓN INMEDIATA:
  │   │   ├─ Esperar: no ordenar inmediatamente
  │   │   ├─ Examinar: tocar cada cuarto (duros, calientes)
  │   │   ├─ Test: California Mastitis Test (CMT) si disponible
  │   │   └─ Resultado: positivo → MASTITIS CLÍNICA
  │   │
  │   ├─ DECISIÓN:
  │   │   ├─ Ordenar manualmente (si posible) cuartos sanos
  │   │   ├─ Descartar leche del cuarto infectado
  │   │   ├─ Recolectar muestra para cultivo (veterinario)
  │   │   └─ Status ordeno: PARCIAL
  │   │
  │   ├─ CAPTURA MODIFICADA:
  │   │   ├─ liters: 7.2 (menos que ayer 9.0)
  │   │   ├─ status: parcial (bandera roja)
  │   │   ├─ observations: "Mastitis en cuarto anterior-der, descartar"
  │   │   ├─ health_alert: CRITICAL
  │   │   ├─ recordedBy: "jhonroberth"
  │   │   └─ veterinary_required: true
  │   │
  │   └─ ACCIONES POST-ORDENO:
  │       ├─ Llamar veterinario (de guardia)
  │       ├─ BOVINES: actualizar status
  │       │   └─ health_status: "UNDER_TREATMENT"
  │       │   └─ treatment_type: "Mastitis - antibiótico"
  │       │
  │       └─ PASTURES: reasignar
  │           └─ Mover a potrero enfermería (aislado)
  │               ├─ PASTURE_Q
  │               └─ Menor estrés, mejor control
  │
  ├─ BOVINO #31 "DAISY" (Puesto 3):
  │   ├─ Ordeno normal
  │   ├─ Liters: 8.0 (vs 9.0 ayer)
  │   ├─ Status: completo
  │   ├─ Observations: "Parece apagada, revisar mañana"
  │   │
  │   └─ Sistema NOTA:
  │       ├─ Caída -11% vs ayer
  │       ├─ Generará alerta de monitoreo
  │       └─ Incluir en análisis de diagnóstico
  │
  └─ BOVINO #44 "CLARA" (Puesto 4):
      ├─ Ordeno normal
      ├─ Liters: 8.9
      ├─ Status: completo
      ├─ Observations: "-"
      └─ Total: normal

06:30 - RONDA 2 (siguientes 4 bovinos):
  │
  ├─ Repetir proceso para #02, #15, #08, #12
  ├─ Capturas similar
  ├─ Duración: ~30 minutos más
  └─ Resultado ronda 2:
      ├─ Total litros: 450L (4 rondas × ~112L)
      └─ Tiempo total: 90 minutos

07:00 - FIN ORDENO AM:
  │
  ├─ RESUMEN AM:
  │   ├─ Bovinos ordenados: 24 (1 omitido: #23 enfermo)
  │   ├─ Litros totales AM: 450L
  │   ├─ Promedio: 18.75L por bovino
  │   ├─ Completitud: 95% (1 parcial)
  │   ├─ Calidad: 98% (1 con mastitis)
  │   ├─ Incidencias: 1 (mastitis #23)
  │   └─ Tiempo total: 90 minutos ✓
  │
  ├─ LIMPIEZA DE EQUIPAMIENTO:
  │   ├─ Tubería: enjuague agua caliente
  │   ├─ Pezoneras: desinfección
  │   ├─ Piso: lavado a presión
  │   └─ Tiempo: 20 minutos
  │
  └─ TRANSFERENCIA A REGISTRO:
      ├─ Datos en papel/tablet se sincronizan
      ├─ MILKING: POST /milkingRecord × 24 registros
      ├─ Para cada bovino:
      │   └─ Crear registro con datos capturados
      │
      └─ Sistema procesa:
          ├─ Validación de datos
          ├─ Cálculos automáticos
          ├─ Generación de alertas
          └─ Actualización de dashboards
```

---

## Análisis Automático: Correlaciones y Alertas

### Sistema de Inteligencia

```
BASE DE DATOS POST-ORDENO AM:

Registros capturados: 24
├─ #47 Estrella: 9.8L (AM), ayer 9.5L → +3.1% ✓
├─ #23 Molly: 7.2L (AM), ayer 9.0L → -20% ⚠️ MASTITIS
├─ #31 Daisy: 8.0L (AM), ayer 9.0L → -11% ⚠️
├─ #44 Clara: 8.9L (AM), ayer 8.7L → +2.3% ✓
├─ [+ 20 registros más]
│
└─ Total AM: 450L

ALERTAS GENERADAS:

1. NIVEL CRÍTICO:
   ├─ #23 Molly: Mastitis clínica
   │   ├─ Acción: Veterinario notificado
   │   ├─ Aislamiento: Reasignar potrero
   │   ├─ Tratamiento: Antibiótico iniciado
   │   ├─ Follow-up: Diarios × 7 días
   │   └─ Recuperación estimada: 5-7 días
   │
   └─ Impacto: -9L/día (producción pérdida)

2. NIVEL ADVERTENCIA:
   ├─ #31 Daisy: Caída producción -11%
   │   ├─ Causa probable: cambio de potrero (ayer)
   │   ├─ Acción: Monitorear próximos 2 días
   │   ├─ Si persiste: investigar más (salud, estrés)
   │   └─ Potrero actual: PASTURE_A (bajo forraje)
   │
   └─ Recomendación: Rotar a potrero mejor
```

---

## Flujo PM: Ordeno Tarde (17:00-18:30)

### Segundo Ordeno del Día

```
17:00 - PRE-ORDENO PM:
  │
  ├─ Bovinos regresan de pastura (tras ~10h pastaing)
  ├─ Traer a sala de espera
  ├─ Preparación similar a AM (lavar, desinfectar, estimular)
  └─ Nota: Algunos bovinos cansados después día de pastaing

17:30 - ORDENO PM (similar a AM):
  │
  ├─ BOVINO #47 "ESTRELLA":
  │   ├─ Pre-ordeno: bien
  │   ├─ Ordeno: 9.2L (vs 9.8L AM)
  │   ├─ Persistencia: 9.2/9.8 = 93.9% ✓ (excelente)
  │   │   └─ Normal: 80-90%, Estrella supera expectativa
  │   │
  │   ├─ Captura:
  │   │   ├─ date: 2025-12-20
  │   │   ├─ shift: PM
  │   │   ├─ liters: 9.2
  │   │   ├─ status: completo
  │   │   ├─ paddock_at_time: PASTURE_B
  │   │   └─ observations: "-"
  │   │
  │   └─ CORRELACIÓN SISTEMA:
  │       ├─ Potrero: PASTURE_B (optimal)
  │       ├─ Cambio ayer: de PASTURE_A a PASTURE_B
  │       ├─ Efecto: +2.2% vs ayer (registrado)
  │       ├─ ML update: "Estrella-B = response positiva confirmada"
  │       └─ Decisión futura: priorizar B para Estrella
  │
  ├─ BOVINO #23 "MOLLY" (BAJO TRATAMIENTO):
  │   ├─ Protocolo especial (mastitis):
  │   │   ├─ Ordeno selectivo (cuartos sanos)
  │   │   ├─ Cuarto enfermo: manejo especial
  │   │   │   ├─ Infusión de antibiótico intramamario
  │   │   │   ├─ Masaje suave (estimulación sangre)
  │   │   │   └─ Ordeno manual leve (no vacío completo)
  │   │   │
  │   │   └─ Leche descartada: no es vendible
  │   │
  │   ├─ Liters: 5.8L (solo cuartos sanos)
  │   ├─ Total día: 7.2 (AM) + 5.8 (PM) = 13.0L
  │   ├─ Caída vs ayer total: 22.1L → 13.0L = -41% CRÍTICO
  │   │
  │   ├─ Captura:
  │   │   ├─ liters: 5.8
  │   │   ├─ status: parcial
  │   │   ├─ observations: "Mastitis día 1 tratamiento, mejoría visible"
  │   │   ├─ treatment_ongoing: true
  │   │   └─ veterinary_notes: "Continuar antibiótico 3-5 días más"
  │   │
  │   └─ Predicción:
  │       ├─ Día 2: 15L estimado (+15%)
  │       ├─ Día 3: 17L estimado
  │       ├─ Día 4: 19L estimado
  │       └─ Día 5+: 20-21L (recuperación completa)
  │
  ├─ BOVINO #31 "DAISY":
  │   ├─ Ordeno PM: 9.0L (vs 8.0L AM)
  │   ├─ Total día: 8.0 (AM) + 9.0 (PM) = 17.0L
  │   ├─ Comparativa ayer: 18.5L → 17.0L = -8.1%
  │   │
  │   ├─ Observación:
  │   │   ├─ PM mejor que AM (normal: efecto acumulación)
  │   │   ├─ Pero total aún bajo (ayer 18.5L)
  │   │   └─ Potrero actual: PASTURE_A (bajo forraje)
  │   │
  │   ├─ Captura:
  │   │   ├─ liters: 9.0
  │   │   ├─ status: completo
  │   │   ├─ observations: "Mejora vs AM, monitor potrero"
  │   │   └─ recommendation: "Considerar cambio PASTURE_A"
  │   │
  │   └─ PLAN:
  │       └─ Rotación mañana: PASTURE_A → PASTURE_C (mejor)
  │
  └─ [+ 21 registros más bovinos]

18:30 - FIN ORDENO PM:
  │
  ├─ RESUMEN PM:
  │   ├─ Bovinos ordenados: 24 (mismos que AM)
  │   ├─ Litros totales PM: 475L
  │   ├─ Promedio: 19.79L por bovino
  │   ├─ Completitud: 96% (1 parcial: #23)
  │   ├─ Calidad: 98%
  │   └─ Tiempo: 90 minutos ✓
  │
  ├─ TOTAL DÍA:
  │   ├─ Litros: 450 + 475 = 925L
  │   ├─ Promedio/bovino: 37.0L
  │   ├─ vs ayer: 950L → 925L = -2.6%
  │   │   └─ Razón: Mastitis #23 (-9L)
  │   │
  │   └─ Estado: NORMAL (mastitis transitoria)
  │
  └─ LIMPIEZA Y CIERRE:
      ├─ Equipo: limpieza y desinfección
      ├─ Datos: sincronizar sistema
      └─ Reportes: generar para manager
```

---

## Análisis Diario y Toma de Decisiones

### Dashboard Post-Ordeno

```
20:00 - CIERRE DE DÍA:
  │
  ├─ PRODUCCIÓN:
  │   ├─ Total: 925L (vs 950L ayer)
  │   ├─ Promedio bovino: 37.0L (vs 38.0L ayer)
  │   ├─ Variabilidad: +/- 8L (Estrella 19L, Daisy 17L)
  │   └─ KPI: -2.6% vs ayer, pero dentro de variabilidad normal
  │
  ├─ SALUD:
  │   ├─ Bovinos enfermos: 1 (#23 mastitis)
  │   ├─ Bovinos en alerta: 1 (#31 producción baja)
  │   ├─ Incidencias nuevas: 1 (mastitis detectada AM)
  │   └─ Incidencias resueltas: 0
  │
  ├─ CORRELACIONES:
  │   ├─ Potrero PASTURE_B: +5% efecto (confirmado con Estrella)
  │   ├─ Potrero PASTURE_A: -1.5% efecto (Daisy)
  │   ├─ Mastitis: correlaciona con estrés potrero baja calidad
  │   └─ Conclusión integrada: mejora forraje = mejor salud + producción
  │
  ├─ PREDICCIÓN MAÑANA:
  │   ├─ Si #23 mejora con antibiótico: +10L
  │   ├─ Si Daisy rota a PASTURE_C: +1.5L
  │   ├─ Predicción: 925 + 10 + 1.5 = 936.5L
  │   └─ Umbral alerta: < 900L (accionar)
  │
  └─ ACCIONES PENDIENTES:
      ├─ [ ] Veterinario: Follow-up #23 mañana AM
      ├─ [ ] Rotar Daisy: PASTURE_A → PASTURE_C
      ├─ [ ] Investigar PASTURE_A: bajo forraje
      ├─ [ ] Solicitar análisis de suelo PASTURE_A
      └─ [ ] Presentar reporte a gerencia

REPORTE DIARIO A GERENCIA:
├─ Producción: 925L (-2.6% vs ayer)
├─ Incidentes: 1 mastitis (controlada)
├─ Acciones tomadas: aislamiento, tratamiento iniciado
├─ Pronóstico: recuperación esperada en 5-7 días
├─ Impacto acumulado mes: -9L × 7 días = -63L estimado
├─ Costo: 63L × $0.50 = $31.50 pérdida
└─ Mitigación: inversión drenaje PASTURE_A ($500) previene recurrencia
```

---

## Análisis Semanal: Tendencias

### Semana: 16-22 de Diciembre 2025

```
DÍA 1 (16/12): 940L → promedio/bovino: 37.6L
DÍA 2 (17/12): 950L → promedio/bovino: 38.0L ← PICO
DÍA 3 (18/12): 925L → promedio/bovino: 37.0L
DÍA 4 (19/12): 910L → promedio/bovino: 36.4L ← CAÍDA (estrés)
DÍA 5 (20/12): 925L → promedio/bovino: 37.0L ← Mastitis #23
DÍA 6 (21/12): 935L → promedio/bovino: 37.4L
DÍA 7 (22/12): 940L → promedio/bovino: 37.6L

ANÁLISIS:
├─ Promedio semanal: 932L/día
├─ Tendencia: ↑ levemente al final (recuperación)
├─ Volatilidad: -40L a +20L (rango 65L)
│
├─ Causas de variabilidad:
│   ├─ Día 4: cambio potrero (estrés temporal)
│   ├─ Día 5: detección mastitis #23
│   ├─ Día 6-7: recuperación y tratamiento
│   └─ Conclusión: variabilidad por manejo, no por genética
│
├─ Bovinos con cambios:
│   ├─ #31 Daisy: caída días 4-5, luego estable
│   ├─ #47 Estrella: consistente alto (19-20L/día)
│   └─ #23 Molly: caída abrupta día 5 (mastitis)
│
└─ Proyección Semana 2:
    ├─ Si #23 se recupera: +1,000L en semana 2
    ├─ Si Daisy se adapta potrero: +2.5L/día
    ├─ Proyección: 960L/día promedio
    └─ KPI semanal: +3,000L vs semana 1
```

---

## Análisis Mensual: Reportaje Ejecutivo

### Mes Completo: Diciembre 2025

```
PERÍODO: 01-31 de Diciembre 2025

PRODUCCIÓN:
├─ Total: 27,850L (vs 28,500L noviembre)
├─ Promedio diario: 898L
├─ Promedio por bovino: 35.9L/día
├─ Producción/bovino/mes: 1,078L
└─ Desempeño: -2.3% vs mes anterior

GRÁFICA MENSUAL:
│
│ 950L ├────────╱╲────╱─╲─────
│ 925L ├───╱────╱──╲──╱───╲────
│ 900L ├──╱────╱────╲╱─────╲───
│ 875L ├─╱────╱──────────────╲──
│  1  │5 10 15 20 25 30
│
├─ Pico: 950L (día 2)
├─ Valle: 870L (día 18)
├─ Tendencia: ↓ leve décadas 1-2, ↑ década 3

INCIDENTES SANITARIOS:
├─ Mastitis: 3 casos
│   ├─ #23 día 20: clínica (tratada, mejorando)
│   ├─ #01 día 5: subclinica (tratada, resuelta)
│   ├─ #44 día 27: clínica (tratada, en curso)
│   └─ Tasa: 12% bovinos (3/25 lactando)
│
├─ Otras incidencias: 0
└─ Tasa general: 12% (vs 5% industria)
    └─ Acción: Mejorar protocolo higiene ordeno

DESEMPEÑO INDIVIDUAL:
├─ Top 5 productoras:
│   ├─ #47 Estrella: 608L (19.6L/día)
│   ├─ #02 Bossy: 595L (19.2L/día)
│   ├─ #15 Molly: 580L (18.7L/día)
│   ├─ #08 Daisy: 575L (18.5L/día)
│   └─ #44 Clara: 570L (18.4L/día)
│
├─ Bottom 5 productoras:
│   ├─ #23 Molly: 425L (13.7L/día) ← mastitis
│   ├─ #31 Daisy: 550L (17.7L/día) ← potrero
│   ├─ #29: 555L (17.9L/día)
│   ├─ #35: 565L (18.2L/día)
│   └─ #38: 568L (18.3L/día)
│
└─ Variabilidad: 12L/día (608-425) = 183L rango
    └─ Causa: genes (40%), potrero (30%), salud (20%), manejo (10%)

CORRELACIONES INTEGRADAS:
├─ Salud + Potrero:
│   ├─ Mastitis (3 casos) correlaciona con PASTURE_A baja calidad
│   ├─ Estrés potrero A → inmunidad baja → susceptibilidad mastitis
│   └─ Acción: invertir en drenaje PASTURE_A
│
├─ Potrero + Producción:
│   ├─ PASTURE_B: +5% producción vs promedio
│   ├─ PASTURE_A: -1.5% vs promedio
│   ├─ Diferencia POTRERO: 6.5 puntos (crítico)
│   └─ Acción: expandir PASTURE_B, mejorar A
│
└─ Reproducción + Lactancia (integrado BOVINES):
    ├─ Bovinas preñadas: 3 (ciclo 2 meses parto)
    ├─ Proyección nuevos partos enero: 3 terneros
    ├─ Impacto producción enero: +2 vacas lactando
    └─ Producción estimada enero: 29,500L (+3.3%)

PROYECCIONES ENERO 2026:
├─ Nuevos partos: +2 vacas lactando
├─ Recuperación #23 mastitis: +9L/día
├─ Mejora potrero A (drenaje): +1L/día
├─ Total predicción: 898L + 12L = 910L/día
├─ Producción enero: 28,210L (+2.5% vs diciembre)
└─ Meta anual 2026: 340,000L (vs 330,000L 2025)
```

---

**Generado**: 2026-01-09 | **Versión**: 1.0
