package com.cattle.events;

import com.cattle.enums.EventType;

public record LaborEvent(EventType type, String user) implements PastureEvent {}
