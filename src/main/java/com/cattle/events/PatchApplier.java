package com.cattle.events;

import com.cattle.entities.Pasture;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;

import java.util.Map;

public final class PatchApplier {

    /** Aplica en memoria el Patch devuelto por el engine a la entidad Pasture. */
    public static void applyLocal(Pasture pasture, EntityPatch patch) {
        if (patch == null || patch.isEmpty() || pasture == null) return;

        // 1) SETS
        for (Map.Entry<String, Object> e : patch.set().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            switch (key) {
                case "status" -> pasture.setStatus(parseStatus(value).name());
                case "substatus" -> pasture.setSubstatus(parseSubstatus(value).name());
                case "holdUntilIso" -> pasture.setHoldUntil(asString(value));
                case "lastUseAtIso" -> pasture.setLastUseAt(asString(value));

                case "gsi2pk" -> pasture.setGsi2pk(asString(value));
                case "gsi2sk" -> pasture.setGsi2sk(asInteger(value));

                default -> {

                }
            }
        }

        // 2) REMOVES
        for (String k : patch.remove()) {
            switch (k) {
                case "holdUntilIso" -> pasture.setHoldUntil(null);
                case "gsi2pk" -> pasture.setGsi2pk(null);
                case "gsi2sk" -> pasture.setGsi2sk(null);

                default -> {

                }
            }
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static Integer asInteger(Object object) {
        if (object == null) return null;
        if (object instanceof Integer i) return i;
        if (object instanceof Number n) return n.intValue();
        try { return Integer.parseInt(object.toString()); } catch (Exception e) { return null; }
    }

    private static PastureStatus parseStatus(Object object) {
        if (object == null) return null;
        if (object instanceof PastureStatus s) return s;
        return PastureStatus.valueOf(object.toString());
    }

    private static PastureSubstatus parseSubstatus(Object object) {
        if (object == null) return null;
        if (object instanceof PastureSubstatus s) return s;
        return PastureSubstatus.valueOf(object.toString());
    }

}
