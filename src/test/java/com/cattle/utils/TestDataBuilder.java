package com.cattle.utils;

import com.cattle.dtos.chatbot.ChatRequestDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.Pasture;
import com.cattle.enums.QueryIntent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern para crear datos de test reutilizables.
 * Simplifica la creación de entities con valores por defecto sensatos.
 */
public class TestDataBuilder {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Crea un bovino de test con valores por defecto
     */
    public static BovineIdentityItem createBovine(String farmId, String category) {
        BovineIdentityItem bovineIdentityItem = new BovineIdentityItem();
        bovineIdentityItem.setBovineId(1);
        bovineIdentityItem.setFarmId(farmId);
        bovineIdentityItem.setGender("female");
        bovineIdentityItem.setName("Test-Bovine-" + farmId);
        bovineIdentityItem.setBornDate(LocalDate.now().minusYears(2).format(DATE_FORMATTER));
        bovineIdentityItem.setBreed("Holstein");
        bovineIdentityItem.setGsi1pk("IDENTITY");
        bovineIdentityItem.setGsi1sk(String.valueOf(bovineIdentityItem.getBovineId()));
        bovineIdentityItem.setPk("BOVINE#1");
        bovineIdentityItem.setSk("IDENTITY");
        return bovineIdentityItem;
    }
    
    /**
     * Crea un bovino personalizado
     */
    public static BovineIdentityItem createBovine(Integer id, String farmId, String category, String gender) {
        BovineIdentityItem bovineIdentityItem = createBovine(farmId, category);
        bovineIdentityItem.setBovineId(id);
        bovineIdentityItem.setGender(gender);
        bovineIdentityItem.setName("Bovine-" + id);
        return bovineIdentityItem;
    }
    
    /**
     * Crea una lista de bovinos de test
     */
    public static List<BovineIdentityItem> createBovineList(String farmId, int count) {
        List<BovineIdentityItem> bovineIdentityItems = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String category = i % 2 == 0 ? "cow" : "bull";
            BovineIdentityItem bovineIdentityItem = createBovine(i, farmId, category, i % 2 == 0 ? "female" : "male");
            bovineIdentityItems.add(bovineIdentityItem);
        }
        return bovineIdentityItems;
    }
    
    /**
     * Crea un potrero de test
     */
    public static Pasture createPasture(String farmId, String status) {
        Pasture pasture = new Pasture();
        pasture.setId("pasture-1");
        pasture.setFarmId(farmId);
        pasture.setName("Test Pasture");
        pasture.setStatus(status != null ? status : "DISPONIBLE");
        pasture.setAreaHa(10.0);
        pasture.setSpecies("Kikuyu");
        pasture.setPk("FARM#" + farmId + "#PASTURE#pasture-1");
        return pasture;
    }
    
    /**
     * Crea un contexto de intención de test
     */
    public static IntentContext createIntent(QueryIntent intent) {
        return IntentContext.builder()
                .intent(intent)
                .category(null)
                .gender(null)
                .status(null)
                .filters(new HashMap<>())
                .confidenceScore(0.9)
                .build();
    }
    
    /**
     * Crea un contexto de intención con parámetros
     */
    public static IntentContext createIntent(QueryIntent intent, String category, String gender) {
        return IntentContext.builder()
                .intent(intent)
                .category(category)
                .gender(gender)
                .filters(new HashMap<>())
                .confidenceScore(0.9)
                .build();
    }
    
    /**
     * Crea un ChatRequestDTO de test
     */
    public static ChatRequestDTO createChatRequest(String message) {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setUserMessage(message);
        request.setConversationId("test-conversation-001");
        return request;
    }
    
    /**
     * Crea un mapa de contexto para Bedrock
     */
    public static Map<String, Object> createBedrockContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("totalBovines", 15);
        context.put("avgProduction", 25.5);
        context.put("availablePastures", 3);
        return context;
    }
}
