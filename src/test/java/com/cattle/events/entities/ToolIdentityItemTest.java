package com.cattle.events.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
class ToolIdentityItemTest {

    @Test
    void gettersAndSetters_persistValues() {
        ToolIdentityItem item = new ToolIdentityItem();

        item.setPk("TOOL#T1");
        item.setSk("IDENTITY");
        item.setToolId("T1");
        item.setToolName("Picapastos Penagos PP300");
        item.setBrand("Penagos");
        item.setModel("PP300");
        item.setSerialNumber("SN-12345");

        assertEquals("TOOL#T1", item.getPk());
        assertEquals("IDENTITY", item.getSk());
        assertEquals("T1", item.getToolId());
        assertEquals("Picapastos Penagos PP300", item.getToolName());
        assertEquals("Penagos", item.getBrand());
        assertEquals("PP300", item.getModel());
        assertEquals("SN-12345", item.getSerialNumber());
    }

    @Test
    void dynamoDbBeanSchema_isResolvable() {
        assertNotNull(TableSchema.fromBean(ToolIdentityItem.class));
    }
}
