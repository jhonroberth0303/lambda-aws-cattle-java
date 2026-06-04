package com.cattle.events.entities;

import com.cattle.entities.BaseDdbItem;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * TABLE_TOOLS item: IDENTITY
 * PK = TOOL#<toolId>
 * SK = IDENTITY
 */
@DynamoDbBean
public class ToolIdentityItem extends BaseDdbItem {

    private String toolId;
    private String toolName;   // "Picapastos Penagos PP300"
    private String brand;
    private String model;
    private String serialNumber; // opcional

    @DynamoDbAttribute("toolId")
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }

    @DynamoDbAttribute("toolName")
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    @DynamoDbAttribute("brand")
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    @DynamoDbAttribute("model")
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    @DynamoDbAttribute("serialNumber")
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
}
