package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.enums.LogType;
import com.cattle.services.BovineSummaryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BovinesSummaryProcessor {

    private final BovineSummaryService bovineSummaryService;
    private final LambdaContext lambdaContext;

    public BovinesSummaryProcessor(BovineSummaryService bovineSummaryService, LambdaContext lambdaContext) {
        this.bovineSummaryService = bovineSummaryService;
        this.lambdaContext = lambdaContext;
    }

    public int refreshAllCategoriesSummary() {
        lambdaContext.logInfo(LogType.PROCESSOR, "Refreshing all bovine summaries for all categories");
        return bovineSummaryService.refreshAllSummaries();
    }

    public int refreshAllSummaries() {
        lambdaContext.logInfo(LogType.PROCESSOR, "Refreshing all bovine summaries");
        return bovineSummaryService.refreshAllSummaries();
    }

    public List<BovineSummaryDTO> findAll() {
        lambdaContext.logInfo(LogType.PROCESSOR, "Finding all bovine summaries");
        return bovineSummaryService.findAll();
    }

    public Optional<BovineSummaryDTO> findById(Integer id) {
        lambdaContext.logInfo(LogType.PROCESSOR, "Finding bovine summary by ID: " + id);
        return bovineSummaryService.findById(id);
    }

    public BovineSummaryDTO refreshSummary(Integer id) {
        lambdaContext.logInfo(LogType.PROCESSOR, "Refreshing bovine summary for ID: " + id);
        return bovineSummaryService.refreshSummary(id);
    }
}
