package com.cattle.rules;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for bovine lifecycle rules per farm.
 * Supports both LifeStage (100% derivable from age) and BovineCategory (age + sex + events).
 * Loads rules from bovine-category-rules.yaml file.
 */
@Component
@ConfigurationProperties
public class BovineCategoryRulesConfig {
    private Map<String, FarmRules> farms;

    public Map<String, FarmRules> getFarms() {
        return farms;
    }
    public void setFarms(Map<String, FarmRules> farms) {
        this.farms = farms;
    }

    public static class FarmRules {
        private List<LifeStageRule> lifeStage;
        private List<CategoryRule> female;
        private List<CategoryRule> male;
        private OxRule ox;

        public List<LifeStageRule> getLifeStage() {
            return lifeStage;
        }
        public void setLifeStage(List<LifeStageRule> lifeStage) {
            this.lifeStage = lifeStage;
        }
        public List<CategoryRule> getFemale() {
            return female;
        }
        public void setFemale(List<CategoryRule> female) {
            this.female = female;
        }
        public List<CategoryRule> getMale() {
            return male;
        }
        public void setMale(List<CategoryRule> male) {
            this.male = male;
        }
        public OxRule getOx() {
            return ox;
        }
        public void setOx(OxRule ox) {
            this.ox = ox;
        }
    }

    /**
     * Rule for LifeStage derivation based on age only.
     */
    public static class LifeStageRule {
        private int minAge;
        private int maxAge;
        private String stage;

        public int getMinAge() {
            return minAge;
        }
        public void setMinAge(int minAge) {
            this.minAge = minAge;
        }
        public int getMaxAge() {
            return maxAge;
        }
        public void setMaxAge(int maxAge) {
            this.maxAge = maxAge;
        }
        public String getStage() {
            return stage;
        }
        public void setStage(String stage) {
            this.stage = stage;
        }
    }

    /**
     * Rule for BovineCategory derivation based on age and sex.
     */
    public static class CategoryRule {
        private int minAge;
        private int maxAge;
        private String category;

        public int getMinAge() {
            return minAge;
        }
        public void setMinAge(int minAge) {
            this.minAge = minAge;
        }
        public int getMaxAge() {
            return maxAge;
        }
        public void setMaxAge(int maxAge) {
            this.maxAge = maxAge;
        }
        public String getCategory() {
            return category;
        }
        public void setCategory(String category) {
            this.category = category;
        }
    }

    /**
     * Special rule for OX: derived from castration event, not age.
     */
    public static class OxRule {
        private boolean castrated;
        private String category;

        public boolean isCastrated() {
            return castrated;
        }
        public void setCastrated(boolean castrated) {
            this.castrated = castrated;
        }
        public String getCategory() {
            return category;
        }
        public void setCategory(String category) {
            this.category = category;
        }
    }
}
