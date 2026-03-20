package com.cattle.services.knowledge;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.knowledge.CitationDTO;
import com.cattle.dtos.knowledge.KnowledgeResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para KnowledgeBaseService.
 * HU-BEDROCK-AGENT-001 - Knowledge Base Integration
 */
@Tag("unit")
@Tag("chatbot")
class KnowledgeBaseServiceTest {

    private static final String TEST_KB_ID = "test-kb-id";
    private static final String TEST_MODEL_ARN = "arn:aws:bedrock:us-east-1::foundation-model/test-model";

    @Mock
    private BedrockAgentRuntimeClient agentClient;

    @Mock
    private LambdaContext lambdaContext;

    private KnowledgeBaseService knowledgeBaseService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        // Mock logInfo para evitar NullPointerException
        doNothing().when(lambdaContext).logInfo(any(), any());
        // Usar constructor de testing con valores de prueba
        knowledgeBaseService = new KnowledgeBaseService(agentClient, TEST_KB_ID, TEST_MODEL_ARN, lambdaContext);
    }

    // ==================== query Tests ====================

    @Test
    void query_validQuestion_returnsResponse() {
        // Arrange
        String question = "¿Cuál es el protocolo de vacunación para terneros?";
        String expectedAnswer = "El protocolo de vacunación para terneros incluye...";
        
        RetrieveAndGenerateOutput output = RetrieveAndGenerateOutput.builder()
                .text(expectedAnswer)
                .build();
        
        RetrieveAndGenerateResponse response = RetrieveAndGenerateResponse.builder()
                .output(output)
                .citations(List.of())
                .build();

        when(agentClient.retrieveAndGenerate(any(RetrieveAndGenerateRequest.class)))
                .thenReturn(response);

        // Act
        KnowledgeResponseDTO result = knowledgeBaseService.query(question);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAnswer, result.getAnswer());
        assertNotNull(result.getDurationMs());
        assertNotNull(result.getTimestamp());
        verify(agentClient, times(1)).retrieveAndGenerate(any(RetrieveAndGenerateRequest.class));
    }

    @Test
    void query_validQuestion_includesCitations() {
        // Arrange
        String question = "¿Cómo detecto mastitis?";
        
        TextResponsePart textPart = TextResponsePart.builder()
                .text("La mastitis se detecta mediante...")
                .build();
        
        RetrievalResultS3Location s3Location = RetrievalResultS3Location.builder()
                .uri("s3://cattle-docs/mastitis-guide.pdf")
                .build();
        
        RetrievalResultLocation location = RetrievalResultLocation.builder()
                .s3Location(s3Location)
                .build();
        
        RetrievedReference reference = RetrievedReference.builder()
                .content(RetrievalResultContent.builder().text("La mastitis se detecta mediante...").build())
                .location(location)
                .build();
        
        Citation citation = Citation.builder()
                .generatedResponsePart(GeneratedResponsePart.builder().textResponsePart(textPart).build())
                .retrievedReferences(List.of(reference))
                .build();
        
        RetrieveAndGenerateOutput output = RetrieveAndGenerateOutput.builder()
                .text("La mastitis se detecta observando...")
                .build();
        
        RetrieveAndGenerateResponse response = RetrieveAndGenerateResponse.builder()
                .output(output)
                .citations(List.of(citation))
                .build();

        when(agentClient.retrieveAndGenerate(any(RetrieveAndGenerateRequest.class)))
                .thenReturn(response);

        // Act
        KnowledgeResponseDTO result = knowledgeBaseService.query(question);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCitations());
        assertFalse(result.getCitations().isEmpty());
        assertEquals("La mastitis se detecta mediante...", result.getCitations().get(0).getText());
        assertEquals("s3://cattle-docs/mastitis-guide.pdf", result.getCitations().get(0).getDocumentUri());
        
        assertNotNull(result.getSources());
        assertTrue(result.getSources().contains("s3://cattle-docs/mastitis-guide.pdf"));
    }

    @Test
    void query_bedrockException_throwsRuntimeException() {
        // Arrange
        String question = "Test question";
        
        when(agentClient.retrieveAndGenerate(any(RetrieveAndGenerateRequest.class)))
                .thenThrow(new RuntimeException("Knowledge Base error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> knowledgeBaseService.query(question));
        
        assertTrue(exception.getMessage().contains("Failed to query Knowledge Base"));
    }

    @Test
    void query_emptyResponse_handlesGracefully() {
        // Arrange
        String question = "Unknown topic";
        
        RetrieveAndGenerateOutput output = RetrieveAndGenerateOutput.builder()
                .text("")
                .build();
        
        RetrieveAndGenerateResponse response = RetrieveAndGenerateResponse.builder()
                .output(output)
                .citations(List.of())
                .build();

        when(agentClient.retrieveAndGenerate(any(RetrieveAndGenerateRequest.class)))
                .thenReturn(response);

        // Act
        KnowledgeResponseDTO result = knowledgeBaseService.query(question);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getAnswer());
        assertTrue(result.getCitations().isEmpty());
        assertTrue(result.getSources().isEmpty());
    }

    @Test
    void query_timeoutException_handlesGracefully() {
        // Arrange
        String question = "Test question";
        
        when(agentClient.retrieveAndGenerate(any(RetrieveAndGenerateRequest.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> knowledgeBaseService.query(question));
        
        assertTrue(exception.getMessage().contains("Failed to query Knowledge Base"));
        assertTrue(exception.getCause().getMessage().contains("timeout"));
    }
}
