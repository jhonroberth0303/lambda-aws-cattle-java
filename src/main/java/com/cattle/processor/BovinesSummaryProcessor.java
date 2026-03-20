package com.cattle.processor;

import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.services.BovineSummaryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BovinesSummaryProcessor {
    private final BovineSummaryService bovineSummaryService;

    public BovinesSummaryProcessor(BovineSummaryService bovineSummaryService) {
        this.bovineSummaryService = bovineSummaryService;
    }

    public int refreshAllCategoriesSummary() {
        return bovineSummaryService.refreshAllSummaries();
    }

    public int refreshAllSummaries() {
        return bovineSummaryService.refreshAllSummaries();
    }

    public List<BovineSummaryDTO> findAll() {
        return bovineSummaryService.findAll();
    }

    public Optional<BovineSummaryDTO> findById(Integer id) {
        return bovineSummaryService.findById(id);
    }

    public BovineSummaryDTO refreshSummary(Integer id) {
        return bovineSummaryService.refreshSummary(id);
    }
}
