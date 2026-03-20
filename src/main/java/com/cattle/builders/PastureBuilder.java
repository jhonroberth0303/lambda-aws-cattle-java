package com.cattle.builders;

import com.cattle.entities.Pasture;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PastureBuilder {
    private static Pasture fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Pasture.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al construir Pasture desde JSON", e);
        }
    }

    public static Pasture buildPastureRyegrass() {
        return Pasture.builder()
                .pk("farm#F001#pasture#P-01")
                .areaHa(0.4)
                .blockReason("NA")
                .currentHeightCm(5)
                .establishmentDate("2025-10-06")
                .farmId("F001")
                .gsi1pk("farm#F001#species#RYEGRASS")
                .gsi1sk(0)
                .gsi2pk("farm#F001#blocked#false")
                .gsi2sk(0)
                .id("P-01")
                .lastUseAt("2025-10-01")
                .name("Potrero 1")
                .species("RYEGRASS")
                .status("DISPONIBLE")
                .substatus("NINGUNO")
                .build();
    }

    public static Pasture buildPastureRyegrassFromJson() {
        String json = "{\"pk\":\"farm#F001#pasture#P-01\",\"areaHa\":0.4,\"blockReason\":\"NA\",\"currentHeightCm\":21,\"establishmentDate\":\"2025-10-06\",\"farmId\":\"F001\",\"gsi1pk\":\"farm#F001#species#RYEGRASS\",\"gsi1sk\":0,\"gsi2pk\":\"farm#F001#blocked#false\",\"gsi2sk\":0,\"id\":\"P-01\",\"lastUseAt\":\"2025-10-01\",\"name\":\"Potrero 1\",\"species\":\"RYEGRASS\",\"status\":\"DISPONIBLE\",\"substatus\":\"NINGUNO\"}";
        return fromJson(json);
    }

    public static Pasture buildPastureCuba22FromJson() {
        String json = "{\"pk\":\"farm#F001#pasture#P-05\",\"areaHa\":0.1,\"blockReason\":\"Establecimiento/arraigo\",\"currentHeightCm\":0,\"establishmentDate\":\"2025-09-20\",\"farmId\":\"F001\",\"gsi1pk\":\"farm#F001#species#CUBA22\",\"gsi1sk\":0,\"gsi2pk\":\"farm#F001#blocked#false\",\"gsi2sk\":0,\"id\":\"P-05\",\"lastUseAt\":\"2025-09-20\",\"name\":\"Cuba 22 - Colinda con el Camino\",\"species\":\"CUBA22\",\"status\":\"MANTENIMIENTO\",\"substatus\":\"ESTABLECIMIENTO\",\"notes\":\"NA\"}";
        return fromJson(json);
    }

    public static Pasture buildPastureKikuyoFromJson() {
        String json = "{\"pk\":\"farm#F001#pasture#P-08\",\"areaHa\":0.4,\"blockReason\":\"NA\",\"currentHeightCm\":5,\"establishmentDate\":\"2025-10-01\",\"farmId\":\"F001\",\"gsi1pk\":\"farm#F001#species#KIKUYO\",\"gsi1sk\":0,\"gsi2pk\":\"farm#F001#blocked#false\",\"gsi2sk\":0,\"id\":\"P-08\",\"lastUseAt\":\"2025-10-01\",\"name\":\"Potrero 8 - Con agua en el lindero\",\"species\":\"KIKUYO\",\"status\":\"EN_DESCANSO\",\"substatus\":\"NINGUNO\"}";
        return fromJson(json);
    }

}
