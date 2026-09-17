package com.cattle.services.chatbot;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.dtos.chatbot.PastureContextDTO;
import com.cattle.enums.QueryIntent;
import com.cattle.services.BovineQueryService;
import com.cattle.services.MilkingQueryService;
import com.cattle.services.PastureQueryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Resuelve QueryIntent.GENERAL_QUERY, la única intención que por naturaleza
 * combina datos de los 3 dominios (bovinos, ordeño, potreros).
 * <p>
 * Es intencionalmente la única clase del sistema que depende de los 3 QueryService
 * a la vez: esa dependencia cruzada es inherente a un resumen general, no un defecto
 * de diseño — aislarla aquí evita que se esconda dentro de ContextBuilderService.
 */
@Component
public class GeneralContextProvider implements ChatbotContextProvider {

    private static final Set<QueryIntent> SUPPORTED_INTENTS = Set.of(QueryIntent.GENERAL_QUERY);

    private final BovineQueryService bovineQueryService;
    private final MilkingQueryService milkingQueryService;
    private final PastureQueryService pastureQueryService;

    public GeneralContextProvider(BovineQueryService bovineQueryService,
                                   MilkingQueryService milkingQueryService,
                                   PastureQueryService pastureQueryService) {
        this.bovineQueryService = bovineQueryService;
        this.milkingQueryService = milkingQueryService;
        this.pastureQueryService = pastureQueryService;
    }

    @Override
    public Set<QueryIntent> supportedIntents() {
        return SUPPORTED_INTENTS;
    }

    /**
     * Construye contexto general con datos de todas las áreas.
     */
    @Override
    public String buildContext(IntentContext intent, String farmId) {
        StringBuilder context = new StringBuilder();

        Long totalBovines = bovineQueryService.countAllBovines(farmId);
        context.append("RESUMEN GENERAL:\n\n");
        context.append("Total de bovinos: ").append(totalBovines).append("\n");

        Double monthlyAvg = milkingQueryService.getMonthlyAverageProduction(farmId);
        context.append("Producción promedio mensual: ").append(String.format("%.2f", monthlyAvg)).append(" litros\n");

        List<PastureContextDTO> available = pastureQueryService.getAvailablePastures(farmId);
        context.append("Potreros disponibles: ").append(available.size()).append("\n");

        return context.toString();
    }
}
