package com.cattle.config;

import com.cattle.mocks.MockBedrockClient;
import com.cattle.utils.TestDataBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuración base para todos los tests.
 * Proporciona beans compartidos: MockBedrockClient, TestDataBuilder, etc.
 */
@TestConfiguration
public class TestCattle {
    
    /**
     * Bean de cliente Bedrock mockeado para tests unitarios
     */
    @Bean
    public MockBedrockClient mockBedrockClient() {
        return new MockBedrockClient();
    }
    
    /**
     * Bean builder de datos de test reutilizables
     */
    @Bean
    public TestDataBuilder testDataBuilder() {
        return new TestDataBuilder();
    }
}
