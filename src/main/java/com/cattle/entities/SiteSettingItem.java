package com.cattle.entities;

import com.cattle.enums.SiteSettingValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class SiteSettingItem {

    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi1sk;

    private String siteId;
    private String settingKey;
    private String valueType;
    private Double valueNumber;
    private String valueString;
    private Boolean valueBoolean;
    private String valueJson;
    private Integer version;
    private Boolean active;
    private String effectiveFrom;
    private String effectiveTo;
    private String createdAt;
    private String updatedAt;
    private String updatedBy;
    private String changeReason;

    public static String buildPk(String siteId) {
        return "SITE#" + siteId;
    }

    public static String buildCurrentSk(String settingKey) {
        return "SETTING#" + settingKey + "#CURRENT";
    }

    public static String buildHistorySk(String settingKey, String effectiveFrom) {
        return "SETTING#" + settingKey + "#HISTORY#" + effectiveFrom;
    }

    public static String buildGsi1Pk(String settingKey) {
        return "SETTING#" + settingKey;
    }

    public static String buildCurrentGsi1Sk(String siteId) {
        return "SITE#" + siteId;
    }

    public static String buildHistoryGsi1Sk(String siteId, String effectiveFrom) {
        return "SITE#" + siteId + "#HISTORY#" + effectiveFrom;
    }

    public boolean isNumberType() {
        return SiteSettingValueType.NUMBER.name().equalsIgnoreCase(valueType);
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() {
        return sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1pk")
    public String getGsi1pk() {
        return gsi1pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1sk")
    public String getGsi1sk() {
        return gsi1sk;
    }

    @DynamoDbAttribute("siteId")
    public String getSiteId() {
        return siteId;
    }

    @DynamoDbAttribute("settingKey")
    public String getSettingKey() {
        return settingKey;
    }

    @DynamoDbAttribute("valueType")
    public String getValueType() {
        return valueType;
    }

    @DynamoDbAttribute("valueNumber")
    public Double getValueNumber() {
        return valueNumber;
    }

    @DynamoDbAttribute("valueString")
    public String getValueString() {
        return valueString;
    }

    @DynamoDbAttribute("valueBoolean")
    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    @DynamoDbAttribute("valueJson")
    public String getValueJson() {
        return valueJson;
    }

    @DynamoDbAttribute("version")
    public Integer getVersion() {
        return version;
    }

    @DynamoDbAttribute("active")
    public Boolean getActive() {
        return active;
    }

    @DynamoDbAttribute("effectiveFrom")
    public String getEffectiveFrom() {
        return effectiveFrom;
    }

    @DynamoDbAttribute("effectiveTo")
    public String getEffectiveTo() {
        return effectiveTo;
    }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDbAttribute("updatedBy")
    public String getUpdatedBy() {
        return updatedBy;
    }

    @DynamoDbAttribute("changeReason")
    public String getChangeReason() {
        return changeReason;
    }
}