package com.cattle.events;

import com.cattle.enums.EventType;

public record MaintenanceClearEvent(String user) implements PastureEvent{

    @Override
    public EventType type() {
        return EventType.MAINTENANCE_CLEAR;
    }

}