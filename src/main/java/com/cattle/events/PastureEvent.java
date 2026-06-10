package com.cattle.events;

import com.cattle.enums.EventType;

public sealed interface PastureEvent permits OpenEvent, CloseEvent, MaintenanceSetEvent, MaintenanceClearEvent, PreEntryCheckEvent, LaborEvent {
    EventType type();
    String user();
}
