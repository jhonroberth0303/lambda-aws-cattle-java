package com.cattle.events;

import com.cattle.enums.EventType;

public record OpenEvent(String user, String lotId, Integer animals) implements PastureEvent {

    @Override
    public EventType type() {
        return EventType.OPEN;
    }

}
