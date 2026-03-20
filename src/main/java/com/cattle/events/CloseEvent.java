package com.cattle.events;

import com.cattle.enums.EventType;

public record CloseEvent(String user, String lotId, Integer animals, Integer residualCm) implements PastureEvent{
    @Override
    public EventType type() {
        return EventType.CLOSE;
    }
}
