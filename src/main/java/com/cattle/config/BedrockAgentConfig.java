package com.cattle.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;

@Slf4j
@Configuration
public class BedrockAgentConfig {

    private static final String DEFAULT_MODEL_ARN = "arn:aws:bedrock:us-east-1::foundation-model/anthropic.claude-3-haiku-20240307-v1:0";

    @Bean
    public BedrockAgentRuntimeClient bedrockAgentRuntimeClient() {
        return BedrockAgentRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    /**
     * Bean con la configuración de Knowledge Base.
     * Lee las variables de entorno BEDROCK_KB_ID y BEDROCK_KB_MODEL_ARN.   
     */
    @Bean
    public KnowledgeBaseProperties knowledgeBaseProperties() {
        String kbId = System.getenv("BEDROCK_KB_ID");
        String modelArn = System.getenv("BEDROCK_KB_MODEL_ARN");
        
        log.info("Loading Knowledge Base config - KB_ID: {}, MODEL_ARN: {}", 
                kbId != null ? kbId : "not-configured",
                modelArn != null ? modelArn : DEFAULT_MODEL_ARN);
        
        return new KnowledgeBaseProperties(
                kbId,
                modelArn != null ? modelArn : DEFAULT_MODEL_ARN
        );
    }

    /**
     * Record inmutable con las propiedades de Knowledge Base.
     */
    @Getter
    public static class KnowledgeBaseProperties {
        private final String knowledgeBaseId;
        private final String modelArn;

        public KnowledgeBaseProperties(String knowledgeBaseId, String modelArn) {
            this.knowledgeBaseId = knowledgeBaseId;
            this.modelArn = modelArn;
        }

        public boolean isConfigured() {
            return knowledgeBaseId != null && !knowledgeBaseId.isBlank() 
                    && !"not-configured".equals(knowledgeBaseId);
        }
    }
}
