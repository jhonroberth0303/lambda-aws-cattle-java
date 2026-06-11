package com.cattle.builders;

import com.cattle.entities.Plan;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

public class PlanBuilder {
    private static Plan fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Plan.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al construir Plan desde JSON", e);
        }
    }

    public static Plan buildPlanRyegrass() {
        return Plan.builder()
                .pk("farm#F001#species#RYEGRASS")
                .farmId("F001")
                .fertWindows(Arrays.asList(
                        "N post-pastoreo 50–90 kg/ha por rotación",
                        "P/K según análisis 1–2 veces/año"
                ))
                .growthRateCmPerDay(0.5)
                .notes("Evitar ingreso con suelo húmedo.")
                .planType("GRAZING")
                .rules(Plan.Rules.builder()
                        .entryHeightCm(20)
                        .exitResidualCm(7)
                        .restDaysMin(35)
                        .build())
                .species("RYEGRASS")
                .updatedAt("2025-10-06T00:00:00Z")
                .version(1)
                .build();
    }

    public static Plan buildPlanRyegrassFromJson() {
        String json = "{\"pk\":\"farm#F001#species#RYEGRASS\",\"farmId\":\"F001\",\"fertWindows\":[\"N post-pastoreo 50–90 kg/ha por rotación\",\"P/K según análisis 1–2 veces/año\"],\"growthRateCmPerDay\":\"0.5\",\"notes\":\"Evitar ingreso con suelo húmedo.\",\"planType\":\"GRAZING\",\"rules\":{\"entryHeightCm\":20,\"exitResidualCm\":7,\"restDaysMin\":35},\"species\":\"RYEGRASS\",\"updatedAt\":\"2025-10-06T00:00:00Z\",\"version\":1}";
        return fromJson(json);
    }

    public static Plan buildPlanCuba22FromJson() {
        String json = "{\"pk\":\"farm#F001#species#CUBA22\",\"farmId\":\"F001\",\"fertWindows\":[\"NK post-corte\",\"P y cal según análisis anual\"],\"growthRateCmPerDay\":1.7,\"notes\":\"Cortar a 1.2–1.5 m; dejar tocón 20–30 cm.\",\"planType\":\"CUT\",\"rules\":{\"cutIntervalDays\":60,\"entryHeightCm\":130,\"exitResidualCm\":25},\"species\":\"CUBA22\",\"updatedAt\":\"2025-10-06T00:00:00Z\",\"version\":1}";
        return fromJson(json);
    }

    public static Plan buildPlanKikuyoFromJson() {
        String json = "{\"pk\":\"farm#F001#species#KIKUYO\",\"farmId\":\"F001\",\"fertWindows\":[\"N post-pastoreo 60–100 kg/ha por rotación\",\"P/K según análisis 1–2 veces/año\",\"Encalado si pH < 5.5 cada 1–2 años\"],\"growthRateCmPerDay\":0.5,\"notes\":\"Mantener residual ≥6 cm para rebrote.\",\"planType\":\"GRAZING\",\"rules\":{\"entryHeightCm\":20,\"exitResidualCm\":6,\"restDaysMin\":28},\"species\":\"KIKUYO\",\"updatedAt\":\"2025-10-06T00:00:00Z\",\"version\":1}";
        return fromJson(json);
    }

    public static Plan buildPlanAvenaForrajeraFromJson() {
        String json = "{\"pk\":\"farm#F001#species#AVENA_FORRAJERA\",\"farmId\":\"F001\",\"fertWindows\":[\"N fraccionado: macollaje y/o post-corte\",\"P/K según análisis\"],\"growthRateCmPerDay\":0.4,\"notes\":\"Si se maneja por corte, usar intervalos 40–50 días.\",\"planType\":\"GRAZING\",\"rules\":{\"entryHeightCm\":22,\"exitResidualCm\":8,\"restDaysMin\":30},\"species\":\"AVENA_FORRAJERA\",\"updatedAt\":\"2025-10-06T00:00:00Z\",\"version\":1}";
        return fromJson(json);
    }

    public static Plan buildPlanMaizForrajeroFromJson() {
        String json = "{\"pk\":\"farm#F001#species#MAIZ_FORRAJERO\",\"farmId\":\"F001\",\"fertWindows\":[\"N a la siembra + cobertera al V6–V8\",\"P/K según análisis a la siembra\"],\"growthRateCmPerDay\":1.5,\"notes\":\"No es para pastoreo; manejar por fenología y %MS para ensilaje.\",\"planType\":\"ANNUAL_SILAGE\",\"rules\":{\"harvestCue\":\"Línea de leche 1/2 a 3/4\",\"harvestDaysAfterSowing\":95,\"rowSpacingCm\":70,\"targetDryMatterPercent\":32},\"species\":\"MAIZ_FORRAJERO\",\"updatedAt\":\"2025-10-06T00:00:00Z\",\"version\":1}";
        return fromJson(json);
    }


}

