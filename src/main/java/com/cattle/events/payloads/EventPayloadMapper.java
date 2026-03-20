package com.cattle.events.payloads;

import com.cattle.events.payloads.bovines.BredPayload;
import com.cattle.events.payloads.bovines.DewormedPayload;
import com.cattle.events.payloads.bovines.PurchasedBovinePayload;
import com.cattle.events.payloads.pastures.FertilizedPayload;
import com.cattle.events.payloads.pastures.LimedPayload;
import com.cattle.events.payloads.tools.MaintenancePayload;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class EventPayloadMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(EventPayload payload) {
        try { return MAPPER.writeValueAsString(payload); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static EventPayload fromJson(String eventType, String json) {
        try {
            return switch (eventType) {
                case "DEWORMED" -> MAPPER.readValue(json, DewormedPayload.class);
                case "LIMED" -> MAPPER.readValue(json, LimedPayload.class);
                case "FERTILIZED" -> MAPPER.readValue(json, FertilizedPayload.class);
                case "MAINTENANCE" -> MAPPER.readValue(json, MaintenancePayload.class);
                case "BRED" -> MAPPER.readValue(json, BredPayload.class);
                case "PURCHASED" -> MAPPER.readValue(json, PurchasedBovinePayload.class);
                default -> null; // o UnknownPayload
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
