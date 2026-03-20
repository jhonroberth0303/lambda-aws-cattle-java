package com.cattle.services.knowledge;

import com.cattle.config.LambdaContext;
import com.cattle.config.BedrockAgentConfig.KnowledgeBaseProperties;
import com.cattle.dtos.knowledge.CitationDTO;
import com.cattle.dtos.knowledge.KnowledgeResponseDTO;
import com.cattle.enums.LogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de integración con Amazon Bedrock Knowledge Base.
 * Usa la API RetrieveAndGenerate para consultas RAG (Retrieval-Augmented Generation).
 */
@Slf4j
@Service
public class KnowledgeBaseService {

    private final BedrockAgentRuntimeClient agentClient;
    private final KnowledgeBaseProperties kbProperties;
    private final LambdaContext lambdaContext;

    @Autowired
    public KnowledgeBaseService(BedrockAgentRuntimeClient agentClient,
                                KnowledgeBaseProperties kbProperties, LambdaContext lambdaContext) {
        this.agentClient = agentClient;
        this.kbProperties = kbProperties;
        this.lambdaContext = lambdaContext;
    }

    // Constructor para testing
    KnowledgeBaseService(BedrockAgentRuntimeClient agentClient, String knowledgeBaseId, String modelArn, LambdaContext lambdaContext) {
        this.agentClient = agentClient;
        this.lambdaContext = lambdaContext;
        this.kbProperties = new KnowledgeBaseProperties(knowledgeBaseId, modelArn);
    }

    /**
     * Consulta la Knowledge Base con una pregunta del usuario.
     * 
     * @param question Pregunta del usuario
     * @return Respuesta con answer, citations y sources
     */
    public KnowledgeResponseDTO query(String question) {
        lambdaContext.logInfo(LogType.SERVICE, "Querying Knowledge Base with question: " + question);
        long startTime = System.currentTimeMillis();
        
        // Validar que la Knowledge Base esté configurada
        if (!kbProperties.isConfigured()) {
            lambdaContext.logInfo(LogType.SERVICE,"Knowledge Base ID not configured. Set BEDROCK_KB_ID environment variable.");
            return KnowledgeResponseDTO.builder()
                    .answer("El servicio de Knowledge Base no está configurado. Contacta al administrador.")
                    .citations(java.util.List.of())
                    .sources(java.util.List.of())
                    .durationMs(System.currentTimeMillis() - startTime)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
        }
        
        try {
            // Construir la configuración de Knowledge Base
            KnowledgeBaseRetrieveAndGenerateConfiguration kbConfig = KnowledgeBaseRetrieveAndGenerateConfiguration.builder()
                    .knowledgeBaseId(kbProperties.getKnowledgeBaseId())
                    .modelArn(kbProperties.getModelArn())
                    .build();
            
            // Construir la configuración de RetrieveAndGenerate
            RetrieveAndGenerateConfiguration ragConfig = RetrieveAndGenerateConfiguration.builder()
                    .type(RetrieveAndGenerateType.KNOWLEDGE_BASE)
                    .knowledgeBaseConfiguration(kbConfig)
                    .build();
            
            // Construir el input
            RetrieveAndGenerateInput input = RetrieveAndGenerateInput.builder()
                    .text(question)
                    .build();
            
            // Construir el request
            RetrieveAndGenerateRequest request = RetrieveAndGenerateRequest.builder()
                    .input(input)
                    .retrieveAndGenerateConfiguration(ragConfig)
                    .build();
            
            // Invocar Knowledge Base
            RetrieveAndGenerateResponse response = agentClient.retrieveAndGenerate(request);
            
            // Extraer la respuesta
            String answer = response.output().text();
            
            // Parsear citaciones
            List<CitationDTO> citations = parseCitations(response);
            
            // Auditoría: loggear los chunks, rutas S3 y categoría usada
            if (!citations.isEmpty()) {
                for (CitationDTO citation : citations) {
                    String chunkText = citation.getText();
                    String s3Uri = citation.getDocumentUri();
                    String categoria = extraerCategoriaDesdeS3Uri(s3Uri);
                    lambdaContext.logInfo(LogType.SERVICE, "Chunk recuperado para respuesta: S3 URI: " + s3Uri + ", Categoría: " + categoria + ", Texto: " + (chunkText != null ? chunkText.substring(0, Math.min(100, chunkText.length())) + (chunkText.length() > 100 ? "..." : "") : "null"));
                }
            } else {
                lambdaContext.logInfo(LogType.SERVICE, "No se recuperaron chunks/citaciones para la respuesta.");
            }

            // Extraer fuentes únicas
            List<String> sources = extractSources(citations);
            
            long duration = System.currentTimeMillis() - startTime;
            lambdaContext.logInfo(LogType.SERVICE,"Knowledge Base query completed in " + duration +"ms");
            
            return KnowledgeResponseDTO.builder()
                    .answer(answer)
                    .citations(citations)
                    .sources(sources)
                    .durationMs(duration)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            lambdaContext.logException(LogType.SERVICE, "Error querying Knowledge Base", e);
            throw new RuntimeException("Failed to query Knowledge Base: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parsea las citaciones de la respuesta de Knowledge Base.
     */
    private List<CitationDTO> parseCitations(RetrieveAndGenerateResponse response) {
        List<CitationDTO> citations = new ArrayList<>();
        
        if (response.citations() == null || response.citations().isEmpty()) {
            return citations;
        }
        
        for (Citation citation : response.citations()) {
            if (citation.retrievedReferences() != null) {
                for (RetrievedReference ref : citation.retrievedReferences()) {
                    String text = null;
                    String uri = null;
                    String categoria = null;

                    if (ref.content() != null) {
                        text = ref.content().text();
                    }
                    
                    if (ref.location() != null && ref.location().s3Location() != null) {
                        uri = ref.location().s3Location().uri();
                        categoria = extraerCategoriaDesdeS3Uri(uri);
                    }
                    
                    CitationDTO citationDTO = CitationDTO.builder()
                            .text(text)
                            .documentUri(uri)
                            .categoria(categoria)
                            .build();
                    citations.add(citationDTO);
                }
            }
        }
        
        return citations;
    }
    
    /**
     * Extrae las URIs únicas de los documentos fuente.
     */
    private List<String> extractSources(List<CitationDTO> citations) {
        return citations.stream()
                .map(CitationDTO::getDocumentUri)
                .filter(uri -> uri != null && !uri.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Extrae la categoría del nombre de archivo en la URI S3.
     * Ejemplo: s3://bucket/ruta/02_kb_nutricion.csv → nutricion
     */
    private String extraerCategoriaDesdeS3Uri(String s3Uri) {
        if (s3Uri == null || s3Uri.isBlank()) return "desconocida";
        // Obtener el nombre de archivo
        int lastSlash = s3Uri.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? s3Uri.substring(lastSlash + 1) : s3Uri;
        // Buscar el primer guion bajo y el punto
        int underscore = fileName.indexOf('_');
        int dot = fileName.lastIndexOf('.');
        if (underscore >= 0 && dot > underscore) {
            String categoria = fileName.substring(underscore + 1, dot);
            // Si el nombre es tipo "kb_nutricion", quitar el prefijo "kb_"
            if (categoria.startsWith("kb_")) {
                categoria = categoria.substring(3);
            }
            return categoria;
        }
        return "desconocida";
    }
}
