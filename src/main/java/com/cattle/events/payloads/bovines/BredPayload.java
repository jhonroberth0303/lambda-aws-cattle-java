package com.cattle.events.payloads.bovines;

import com.cattle.events.payloads.EventPayload;

public class BredPayload implements EventPayload {

    private String bullBreed;
    private String method; // NATURAL | IA
    private String estimatedCalvingDate;

    // getters / setters
}

