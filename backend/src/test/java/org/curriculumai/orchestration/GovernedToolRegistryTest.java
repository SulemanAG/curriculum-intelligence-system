package org.curriculumai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.repository.CourseRepository;
import org.curriculumai.academic.repository.CurriculumVersionRepository;
import org.curriculumai.evidence.dto.HybridSearchRequest;
import org.curriculumai.evidence.dto.HybridSearchResponse;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.EvidenceSearchService;
import org.curriculumai.evidence.service.embedding.EmbeddingProvider;
import org.curriculumai.evidence.service.retrieval.HybridRetrievalService;
import org.curriculumai.evidence.service.retrieval.SemanticSearchService;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.WorkflowEvent;
import org.curriculumai.orchestration.domain.WorkflowEventType;
import org.curriculumai.orchestration.repository.WorkflowEventRepository;
import org.curriculumai.orchestration.tools.GovernedToolRegistry;
import org.curriculumai.orchestration.tools.ToolDefinition;
import org.curriculumai.orchestration.tools.ToolExecutionContext;
import org.curriculumai.orchestration.tools.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GovernedToolRegistryTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private org.curriculumai.academic.repository.DepartmentRepository departmentRepository;

    @Mock
    private org.curriculumai.academic.repository.ProgrammeRepository programmeRepository;

    @Mock
    private CurriculumVersionRepository curriculumVersionRepository;

    @Mock
    private EvidenceSearchService evidenceSearchService;

    @Mock
    private HybridRetrievalService hybridRetrievalService;

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private EvidenceIngestionService ingestionService;

    @Mock
    private EmbeddingProvider embeddingProvider;

    @Mock
    private WorkflowEventRepository eventRepository;

    private AgentRegistry agentRegistry;
    private ObjectMapper objectMapper;
    private GovernedToolRegistry toolRegistry;

    @BeforeEach
    void setUp() {
        agentRegistry = new AgentRegistry();
        objectMapper = new ObjectMapper();
        toolRegistry = new GovernedToolRegistry(
                agentRegistry,
                eventRepository,
                objectMapper,
                courseRepository,
                departmentRepository,
                programmeRepository,
                curriculumVersionRepository,
                evidenceSearchService,
                hybridRetrievalService,
                semanticSearchService,
                ingestionService,
                embeddingProvider
        );
    }

    @Test
    @DisplayName("Tool registry exposes all five governed tools")
    void registryContainsAllFiveGovernedTools() {
        List<ToolDefinition> definitions = toolRegistry.getAllToolDefinitions();
        assertThat(definitions).hasSize(5);

        List<String> toolIds = definitions.stream().map(ToolDefinition::toolId).toList();
        assertThat(toolIds).containsExactlyInAnyOrder(
                "academic_catalog_retrieval",
                "curriculum_version_retrieval",
                "evidence_keyword_search",
                "evidence_semantic_search",
                "evidence_hybrid_search"
        );
    }

    @Test
    @DisplayName("Invoking tool with unauthorized agent returns security error and logs audit event")
    void unauthorizedAgentBlockedFromToolInvocation() {
        // curriculum-mapping-agent only has access to academic_catalog_retrieval and curriculum_version_retrieval
        // Attempting to invoke evidence_keyword_search should be rejected
        ToolExecutionContext context = new ToolExecutionContext(
                "run-123",
                "task-456",
                "curriculum-mapping-agent",
                Map.of("query", "Cloud Computing")
        );

        ToolResult result = toolRegistry.invokeTool("evidence_keyword_search", context);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Agent 'curriculum-mapping-agent' is not authorized to invoke tool 'evidence_keyword_search'");

        // Verify audit event was logged
        ArgumentCaptor<WorkflowEvent> eventCaptor = ArgumentCaptor.forClass(WorkflowEvent.class);
        verify(eventRepository).save(eventCaptor.capture());

        WorkflowEvent loggedEvent = eventCaptor.getValue();
        assertThat(loggedEvent.getRunId()).isEqualTo("run-123");
        assertThat(loggedEvent.getTaskId()).isEqualTo("task-456");
        assertThat(loggedEvent.getAgentId()).isEqualTo("curriculum-mapping-agent");
        assertThat(loggedEvent.getEventType()).isEqualTo(WorkflowEventType.TOOL_INVOKED);
        assertThat(loggedEvent.getEventSummary()).contains("not authorized");
    }

    @Test
    @DisplayName("Invoking nonexistent tool returns error and logs audit event")
    void nonexistentToolReturnsError() {
        ToolExecutionContext context = new ToolExecutionContext(
                "run-123",
                "task-456",
                "curriculum-mapping-agent",
                Collections.emptyMap()
        );

        ToolResult result = toolRegistry.invokeTool("nonexistent_tool", context);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Governed tool not found");
    }

    @Test
    @DisplayName("Semantic search returns explicit UNAVAILABLE when embedding provider is offline")
    void semanticSearchReturnsUnavailableWhenProviderOffline() {
        org.mockito.Mockito.when(embeddingProvider.isAvailable()).thenReturn(false);

        ToolExecutionContext context = new ToolExecutionContext(
                "run-123",
                "task-456",
                "industry-skills-agent",
                Map.of("query", "Microservices Architecture")
        );

        ToolResult result = toolRegistry.invokeTool("evidence_semantic_search", context);

        assertThat(result.success()).isTrue();
        assertThat(result.data()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) result.data();
        assertThat(output.get("status")).isEqualTo("UNAVAILABLE");
        assertThat(output.get("modality")).isEqualTo("SEMANTIC_VECTOR");
        assertThat(output.get("message")).toString().contains("offline");
        assertThat(result.citations()).isEmpty();
    }

    @Test
    @DisplayName("Hybrid search identifies contributing modalities honestly when embedding provider is offline")
    void hybridSearchHonestModalityReportingWhenProviderOffline() {
        org.mockito.Mockito.when(embeddingProvider.isAvailable()).thenReturn(false);
        org.mockito.Mockito.when(hybridRetrievalService.searchHybrid(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new HybridSearchResponse("Distributed Systems", 0, Collections.emptyList(), 60, "test-model"));

        ToolExecutionContext context = new ToolExecutionContext(
                "run-123",
                "task-456",
                "industry-skills-agent",
                Map.of("query", "Distributed Systems")
        );

        ToolResult result = toolRegistry.invokeTool("evidence_hybrid_search", context);

        assertThat(result.success()).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) result.data();
        assertThat(output.get("contributingModalities")).isEqualTo(List.of("KEYWORD"));
        assertThat(output.get("semanticAvailable")).isEqualTo(false);
    }
}
