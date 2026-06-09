package com.cattle.events;

import com.cattle.enums.EventType;

public record PreEntryCheckEvent(String user, boolean allCriticalOk, String completedAt) implements PastureEvent {

    @Override
    public EventType type() {
        return EventType.PRE_ENTRY_CHECK;
    }

}
