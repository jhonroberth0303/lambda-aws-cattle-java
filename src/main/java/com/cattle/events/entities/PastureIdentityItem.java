package com.cattle.events.entities;

import com.cattle.entities.BaseDdbItem;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * TABLE_PASTURES item: IDENTITY (o PROFILE)
 * PK = PASTURE#<pastureId>  (o PADDOCK#<id>)
 * SK = IDENTITY
 */
@DynamoDbBean
public class PastureIdentityItem extends BaseDdbItem {

    private String pastureId;
    private String name;        // "Potrero 04"
    private Double areaM2;
    private String grassType;   // Kikuyo, Ryegrass, Cuba22...
    private String location;    // opcional

    @DynamoDbAttribute("pastureId")
    public String getPastureId() { return pastureId; }
    public void setPastureId(String pastureId) { this.pastureId = pastureId; }

    @DynamoDbAttribute("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDbAttribute("areaM2")
    public Double getAreaM2() { return areaM2; }
    public void setAreaM2(Double areaM2) { this.areaM2 = areaM2; }

    @DynamoDbAttribute("grassType")
    public String getGrassType() { return grassType; }
    public void setGrassType(String grassType) { this.grassType = grassType; }

    @DynamoDbAttribute("location")
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
