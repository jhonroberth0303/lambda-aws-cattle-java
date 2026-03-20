package com.cattle.events.payloads.bovines;

import com.cattle.events.payloads.EventPayload;

public class PurchasedBovinePayload implements EventPayload {

    private Integer ageMonths;
    private String breed;
    private Double amountCop;
    private String sellerName;
    private String sellerPhone;

    // getters / setters
}