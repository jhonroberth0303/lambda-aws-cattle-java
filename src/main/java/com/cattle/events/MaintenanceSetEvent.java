package com.cattle.events;

import com.cattle.enums.EventType;
import com.cattle.enums.PastureSubstatus;

public record MaintenanceSetEvent(String user, PastureSubstatus substatus, String holdUntil) implements PastureEvent{

    @Override
    public EventType type() {
        return EventType.MAINTENANCE_SET;
    }

}
