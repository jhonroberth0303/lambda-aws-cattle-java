package com.cattle.events.payloads.bovines;

import com.cattle.events.payloads.EventPayload;

public class DewormedPayload implements EventPayload {

    private String product;
    private String composition;
    private Double doseMl;
    private String appliedBy;
    private String withdrawalUntil; // fecha o comentario

    // getters / setters
}
