package com.cattle.events;

import com.cattle.enums.EventType;

public sealed interface PastureEvent permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent, PreEntryCheckEvent {
    EventType type();
    String user();
}
