package com.cattle.builders;

import com.cattle.entities.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventBuilder {
    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi1sk;
    private String eventType;
    private String eventAt;
    private Integer animals;
    private Integer residualCm;
    private String user;
    private String lotId;

    public EventBuilder defaultsForGrazingEnd() {
        this.eventType = this.eventType != null ? this.eventType : "GRAZING_END";
        return this;
    }

    public Event build() {
        requireNonBlank(pk, "pk");
        requireNonBlank(sk, "sk");
        requireNonBlank(eventType, "eventType");
        requireNonBlank(eventAt, "eventAt");
        if (gsi1pk == null && eventType != null) {
            this.gsi1pk = "farm#UNKNOWN#type#" + eventType;
        }
        if (gsi1sk == null && eventAt != null) {
            this.gsi1sk = eventAt;
        }
        return Event.builder()
                .pk(pk).sk(sk)
                .gsi1pk(gsi1pk).gsi1sk(gsi1sk)
                .eventType(eventType)
                .eventAt(eventAt)
                .animals(animals)
                .residualCm(residualCm)
                .user(user)
                .build();
    }

    private static void requireNonBlank(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo requerido vacío: " + field);
        }
    }
}
