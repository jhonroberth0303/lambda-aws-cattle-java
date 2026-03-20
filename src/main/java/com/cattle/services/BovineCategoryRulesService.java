package com.cattle.services;

import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.Sex;
import com.cattle.enums.profiles.Source;
import com.cattle.rules.BovineCategoryRulesConfig;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

/**
 * Service for inferring LifeStage and BovineCategory based on configurable rules.
 * 
 * Key principles:
 * - LifeStage: 100% derivable from age (always AUTO)
 * - BovineCategory: derivable from age + sex + events (can be AUTO or MANUAL)
 * - OX: derived from castration event, not age
 */
@Service
public class BovineCategoryRulesService {
    
    private static final String DEFAULT_FARM = "default";
    private final BovineCategoryRulesConfig rulesConfig;

    public BovineCategoryRulesService(BovineCategoryRulesConfig rulesConfig) {
        this.rulesConfig = rulesConfig;
    }

    /**
     * Result object containing both inferred values and next recalculation date.
     */
    public static class InferenceResult {
        private final LifeStage lifeStage;
        private final BovineCategory category;
        private final LocalDate nextRecalcDate;
        // No se usa status aquí, no se agrega ni se deja código innecesario

        public InferenceResult(LifeStage lifeStage, BovineCategory category, LocalDate nextRecalcDate) {
            this.lifeStage = lifeStage;
            this.category = category;
            this.nextRecalcDate = nextRecalcDate;
        }

        public LifeStage getLifeStage() { return lifeStage; }
        public BovineCategory getCategory() { return category; }
        public LocalDate getNextRecalcDate() { return nextRecalcDate; }
    }

    /**
     * Infers LifeStage based solely on age.
     * Always returns AUTO source since it's 100% derivable.
     */
    public LifeStage inferLifeStage(String farm, int ageMonths) {
        BovineCategoryRulesConfig.FarmRules farmRules = getFarmRules(farm);
        if (farmRules == null || farmRules.getLifeStage() == null) {
            return null;
        }
        
        for (BovineCategoryRulesConfig.LifeStageRule rule : farmRules.getLifeStage()) {
            if (ageMonths >= rule.getMinAge() && ageMonths < rule.getMaxAge()) {
                return LifeStage.valueOf(rule.getStage());
            }
        }
        return null;
    }

    /**
     * Infers BovineCategory based on age, sex, and castration status.
     */
    public BovineCategory inferCategory(String farm, Sex sex, int ageMonths, boolean isCastrated) {
        BovineCategoryRulesConfig.FarmRules farmRules = getFarmRules(farm);
        if (farmRules == null) {
            return null;
        }
        
        // OX takes precedence if castrated
        if (isCastrated && farmRules.getOx() != null && farmRules.getOx().isCastrated()) {
            return BovineCategory.valueOf(farmRules.getOx().getCategory());
        }
        
        if (sex == Sex.FEMALE && farmRules.getFemale() != null) {
            for (BovineCategoryRulesConfig.CategoryRule rule : farmRules.getFemale()) {
                if (ageMonths >= rule.getMinAge() && ageMonths < rule.getMaxAge()) {
                    return BovineCategory.valueOf(rule.getCategory());
                }
            }
        } else if (sex == Sex.MALE && farmRules.getMale() != null) {
            for (BovineCategoryRulesConfig.CategoryRule rule : farmRules.getMale()) {
                if (ageMonths >= rule.getMinAge() && ageMonths < rule.getMaxAge()) {
                    return BovineCategory.valueOf(rule.getCategory());
                }
            }
        }
        return null;
    }

    public String inferCategory(String farm, String gender, int ageMonths, boolean isCastrated) {
        Sex sex = "female".equalsIgnoreCase(gender) ? Sex.FEMALE : Sex.MALE;
        BovineCategory category = inferCategory(farm, sex, ageMonths, isCastrated);
        return category != null ? category.name() : null;
    }

    /**
     * Full inference: calculates lifeStage, category, and next recalculation date.
     * 
     * @param farm Farm ID for rules lookup
     * @param bornDate Birth date of the bovine
     * @param sex Biological sex
     * @param isCastrated Whether the bovine is castrated
     * @param categorySource Current category source (AUTO/MANUAL)
     * @return InferenceResult with all calculated values
     */
    public InferenceResult inferAll(String farm, LocalDate bornDate, Sex sex, 
                                     boolean isCastrated, Source categorySource) {
        int ageMonths = calculateAgeInMonths(bornDate);
        
        LifeStage lifeStage = inferLifeStage(farm, ageMonths);
        
        // Only infer category if source is AUTO (respect manual decisions)
        BovineCategory category = null;
        if (categorySource == null || categorySource == Source.AUTO) {
            category = inferCategory(farm, sex, ageMonths, isCastrated);
        }
        
        LocalDate nextRecalcDate = calculateNextRecalcDate(farm, bornDate, ageMonths);
        
        return new InferenceResult(lifeStage, category, nextRecalcDate);
    }

    /**
     * Calculates age in months from birth date.
     */
    public int calculateAgeInMonths(LocalDate bornDate) {
        if (bornDate == null) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(bornDate, LocalDate.now());
    }

    /**
     * Calculates the next date when the bovine will cross a threshold.
     * This optimizes batch processing by only recalculating when needed.
     */
    public LocalDate calculateNextRecalcDate(String farm, LocalDate bornDate, int currentAgeMonths) {
        BovineCategoryRulesConfig.FarmRules farmRules = getFarmRules(farm);
        if (farmRules == null || bornDate == null) {
            return LocalDate.now().plusMonths(1); // Default: check monthly
        }
        
        // Find the next threshold from lifeStage rules
        int nextThreshold = Integer.MAX_VALUE;
        
        if (farmRules.getLifeStage() != null) {
            for (BovineCategoryRulesConfig.LifeStageRule rule : farmRules.getLifeStage()) {
                if (rule.getMaxAge() > currentAgeMonths && rule.getMaxAge() < nextThreshold) {
                    nextThreshold = rule.getMaxAge();
                }
            }
        }
        
        if (nextThreshold == Integer.MAX_VALUE || nextThreshold >= 999) {
            // Already adult, check annually
            return LocalDate.now().plusYears(1);
        }
        
        // Calculate date when bovine reaches next threshold
        return bornDate.plusMonths(nextThreshold);
    }

    private BovineCategoryRulesConfig.FarmRules getFarmRules(String farm) {
        if (rulesConfig.getFarms() == null) {
            return null;
        }
        BovineCategoryRulesConfig.FarmRules farmRules = rulesConfig.getFarms().get(farm);
        if (farmRules == null) {
            farmRules = rulesConfig.getFarms().get(DEFAULT_FARM);
        }
        return farmRules;
    }
}
