package com.cattle.services;

import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.Sex;
import com.cattle.enums.profiles.Source;
import com.cattle.services.BovineCategoryRulesService.InferenceResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Batch service for recalculating bovine lifecycle attributes.
 * 
 * Key principles:
 * - lifeStage: ALWAYS updated (100% derivable from age)
 * - category: Only updated if categorySource == AUTO (respects manual decisions)
 * - nextRecalcDate: Optimizes batch by scheduling next check at threshold crossing
 * 
 * NOTE: sex and ageInMonths are obtained from Bovine (Identity), not stored in ProfileLifecycle.
 */
@Service
public class LifecycleRecalculationService {

    private final BovineCategoryRulesService rulesService;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public LifecycleRecalculationService(BovineCategoryRulesService rulesService) {
        this.rulesService = rulesService;
    }

    /**
     * Result of recalculation indicating what changed.
     */
    public static class RecalculationResult {
        private final boolean lifeStageChanged;
        private final boolean categoryChanged;
        private final LifeStage newLifeStage;
        private final BovineCategory newCategory;
        private final LocalDate nextRecalcDate;

        public RecalculationResult(boolean lifeStageChanged, boolean categoryChanged,
                                   LifeStage newLifeStage, BovineCategory newCategory,
                                   LocalDate nextRecalcDate) {
            this.lifeStageChanged = lifeStageChanged;
            this.categoryChanged = categoryChanged;
            this.newLifeStage = newLifeStage;
            this.newCategory = newCategory;
            this.nextRecalcDate = nextRecalcDate;
        }

        public boolean hasChanges() { return lifeStageChanged || categoryChanged; }
        public boolean isLifeStageChanged() { return lifeStageChanged; }
        public boolean isCategoryChanged() { return categoryChanged; }
        public LifeStage getNewLifeStage() { return newLifeStage; }
        public BovineCategory getNewCategory() { return newCategory; }
        public LocalDate getNextRecalcDate() { return nextRecalcDate; }
    }

    /**
     * Evaluates and recalculates lifecycle attributes for a bovine.
     * Sex is resolved from Bovine.gender (Identity is the source of truth).
     * 
     * @param bovineIdentityItem The bovine identity data (contains bornDate, gender, farmId)
     * @param lifecycle Current lifecycle profile
     * @return RecalculationResult with changes and new values
     */
    public RecalculationResult recalculate(BovineIdentityItem bovineIdentityItem, ProfileLifecycle lifecycle) {
        if (bovineIdentityItem == null || bovineIdentityItem.getBornDate() == null) {
            return new RecalculationResult(false, false, null, null, null);
        }

        LocalDate bornDate = LocalDate.parse(bovineIdentityItem.getBornDate(), ISO_DATE);
        Sex sex = resolveSexFromIdentity(bovineIdentityItem);
        // TODO: isCastrated should come from EVENT#CASTRATION when events are implemented
        boolean isCastrated = false;
        Source categorySource = lifecycle.getCategorySource();
        String farmId = bovineIdentityItem.getFarmId() != null ? bovineIdentityItem.getFarmId() : "default";

        // Perform inference (age calculation is done internally by rulesService)
        InferenceResult inference = rulesService.inferAll(farmId, bornDate, sex, isCastrated, categorySource);
        
        // Determine what changed
        boolean lifeStageChanged = inference.getLifeStage() != null && 
                                   inference.getLifeStage() != lifecycle.getLifeStage();
        
        boolean categoryChanged = false;
        if (categorySource == null || categorySource == Source.AUTO) {
            categoryChanged = inference.getCategory() != null && 
                             inference.getCategory() != lifecycle.getCategory();
        }

        return new RecalculationResult(
            lifeStageChanged,
            categoryChanged,
            inference.getLifeStage(),
            inference.getCategory(),
            inference.getNextRecalcDate()
        );
    }

    /**
     * Applies recalculation result to the lifecycle entity.
     * Updates only fields that should change based on rules.
     * 
     * @param lifecycle The lifecycle to update
     * @param result The recalculation result
     * @return Updated lifecycle (same instance, mutated)
     */
    public ProfileLifecycle applyRecalculation(ProfileLifecycle lifecycle, RecalculationResult result) {
        if (result == null) {
            return lifecycle;
        }

        // Always update lifeStage (100% derivable)
        if (result.getNewLifeStage() != null) {
            lifecycle.setLifeStage(result.getNewLifeStage());
            lifecycle.setLifeStageSource(Source.AUTO);
        }

        // Only update category if AUTO (or if source is not set yet)
        if (lifecycle.getCategorySource() == null || lifecycle.getCategorySource() == Source.AUTO) {
            if (result.getNewCategory() != null) {
                lifecycle.setCategory(result.getNewCategory());
                lifecycle.setCategorySource(Source.AUTO);
            }
        }

        // Update scheduling
        lifecycle.setLastEvaluatedAt(Instant.now().toString());
        
        if (result.getNextRecalcDate() != null) {
            lifecycle.setNextRecalcDate(result.getNextRecalcDate().format(ISO_DATE));
        }

        lifecycle.setUpdatedAt(Instant.now().toString());
        
        return lifecycle;
    }

    /**
     * Resolves sex from Bovine identity (source of truth for sex/gender).
     */
    private Sex resolveSexFromIdentity(BovineIdentityItem bovineIdentityItem) {
        if (bovineIdentityItem.getGender() != null) {
            return "female".equalsIgnoreCase(bovineIdentityItem.getGender()) ? Sex.FEMALE : Sex.MALE;
        }
        return null;
    }
}
