package com.cattle.events.payloads.pastures;

import com.cattle.events.payloads.EventPayload;

public class FertilizedPayload implements EventPayload {

    private String product;
    private Double appliedKg;
    private String pastureType;
    private String weather;

    // getters / setters
}
