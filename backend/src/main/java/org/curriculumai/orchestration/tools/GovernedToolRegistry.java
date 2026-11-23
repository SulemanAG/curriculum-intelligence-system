package org.curriculumai.orchestration.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.repository.*;
import org.curriculumai.evidence.dto.EvidenceChunkDto;
import org.curriculumai.evidence.dto.HybridSearchRequest;
import org.curriculumai.evidence.dto.HybridSearchResponse;
import org.curriculumai.evidence.dto.ProvenanceDto;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.EvidenceSearchService;
import org.curriculumai.evidence.service.embedding.EmbeddingProvider;
import org.curriculumai.evidence.service.retrieval.HybridRetrievalService;
import org.curriculumai.evidence.service.retrieval.SemanticSearchService;
import org.curriculumai.orchestration.agent.AgentDefinition;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.WorkflowEvent;
import org.curriculumai.orchestration.domain.WorkflowEventType;
import org.curriculumai.orchestration.repository.WorkflowEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GovernedToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(GovernedToolRegistry.class);

    private final Map<String, GovernedTool> tools = new ConcurrentHashMap<>();
    private final AgentRegistry agentRegistry;
    private final WorkflowEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    // Domain dependencies for tools
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgrammeRepository programmeRepository;
    private final CurriculumVersionRepository versionRepository;
    private final EvidenceSearchService searchService;
    private final HybridRetrievalService hybridRetrievalService;
    private final SemanticSearchService semanticSearchService;
    private final EvidenceIngestionService ingestionService;
    private final EmbeddingProvider embeddingProvider;

    public GovernedToolRegistry(AgentRegistry agentRegistry,
                                WorkflowEventRepository eventRepository,
                                ObjectMapper objectMapper,
                                CourseRepository courseRepository,
                                DepartmentRepository departmentRepository,
                                ProgrammeRepository programmeRepository,
                                CurriculumVersionRepository versionRepository,
                                EvidenceSearchService searchService,
                                HybridRetrievalService hybridRetrievalService,
                                SemanticSearchService semanticSearchService,
                                EvidenceIngestionService ingestionService,
                                EmbeddingProvider embeddingProvider) {
        this.agentRegistry = agentRegistry;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.programmeRepository = programmeRepository;
        this.versionRepository = versionRepository;
        this.searchService = searchService;
        this.hybridRetrievalService = hybridRetrievalService;
        this.semanticSearchService = semanticSearchService;
        this.ingestionService = ingestionService;
        this.embeddingProvider = embeddingProvider;

        registerBuiltinTools();
    }

    private void registerBuiltinTools() {
        // 1. Academic Catalog Retrieval
        registerTool(new GovernedTool() {
            @Override
            public String getToolId() {
                return "academic_catalog_retrieval";
            }

            @Override
            public ToolDefinition getDefinition() {
                return new ToolDefinition(
                        "academic_catalog_retrieval",
                        "Academic Catalog Retrieval",
                        "Retrieves accredited institutional courses, departments, and programmes",
                        "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"},\"departmentId\":{\"type\":\"string\"}}}",
                        "{\"type\":\"object\",\"properties\":{\"courses\":{\"type\":\"array\"}}}",
                        20
                );
            }

            @Override
            public ToolResult execute(ToolExecutionContext ctx) {
                String query = (String) ctx.parameters().get("query");
                var courses = (query != null && !query.isBlank())
                        ? courseRepository.findByTitleContainingIgnoreCase(query)
                        : courseRepository.findAll();
                return ToolResult.ok(courses, Collections.emptyList());
            }
        });

        // 2. Curriculum Version Retrieval
        registerTool(new GovernedTool() {
            @Override
            public String getToolId() {
                return "curriculum_version_retrieval";
            }

            @Override
            public ToolDefinition getDefinition() {
                return new ToolDefinition(
                        "curriculum_version_retrieval",
                        "Curriculum Version Retrieval",
                        "Retrieves curriculum version definitions, offerings, and outcomes",
                        "{\"type\":\"object\",\"properties\":{\"programmeId\":{\"type\":\"string\"}}}",
                        "{\"type\":\"object\",\"properties\":{\"versions\":{\"type\":\"array\"}}}",
                        10
                );
            }

            @Override
            public ToolResult execute(ToolExecutionContext ctx) {
                String programmeId = (String) ctx.parameters().get("programmeId");
                var versions = programmeId != null
                        ? versionRepository.findByProgrammeId(programmeId)
                        : versionRepository.findAll();
                return ToolResult.ok(versions, Collections.emptyList());
            }
        });

        // 3. Evidence Keyword Search
        registerTool(new GovernedTool() {
            @Override
            public String getToolId() {
                return "evidence_keyword_search";
            }

            @Override
            public ToolDefinition getDefinition() {
                return new ToolDefinition(
                        "evidence_keyword_search",
                        "Evidence Keyword Search",
                        "Searches evidence passages using lexical text search with citation provenance",
                        "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"},\"limit\":{\"type\":\"integer\"}}}",
                        "{\"type\":\"object\",\"properties\":{\"results\":{\"type\":\"array\"}}}",
                        20
                );
            }

            @Override
            public ToolResult execute(ToolExecutionContext ctx) {
                String query = (String) ctx.parameters().get("query");
                int limit = Optional.ofNullable((Integer) ctx.parameters().get("limit")).orElse(10);
                var page = searchService.searchEvidence(query, null, null, null, null, PageRequest.of(0, limit));

                List<ProvenanceDto> citations = page.getContent().stream()
                        .map(EvidenceChunkDto::provenance)
                        .filter(Objects::nonNull)
                        .toList();

                return ToolResult.ok(page.getContent(), citations);
            }
        });

        // 4. Evidence Semantic Search
        registerTool(new GovernedTool() {
            @Override
            public String getToolId() {
                return "evidence_semantic_search";
            }

            @Override
            public ToolDefinition getDefinition() {
                return new ToolDefinition(
                        "evidence_semantic_search",
                        "Evidence Semantic Vector Search",
                        "Performs dense vector similarity search across extracted evidence passages",
                        "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"},\"limit\":{\"type\":\"integer\"}}}",
                        "{\"type\":\"object\",\"properties\":{\"status\":{\"type\":\"string\"},\"modality\":{\"type\":\"string\"},\"results\":{\"type\":\"array\"}}}",
                        20
                );
            }

            @Override
            public ToolResult execute(ToolExecutionContext ctx) {
                String query = (String) ctx.parameters().get("query");
                int limit = Optional.ofNullable((Integer) ctx.parameters().get("limit")).orElse(10);

                if (embeddingProvider == null || !embeddingProvider.isAvailable()) {
                    Map<String, Object> unavailablePayload = new LinkedHashMap<>();
                    unavailablePayload.put("status", "UNAVAILABLE");
                    unavailablePayload.put("modality", "SEMANTIC_VECTOR");
                    unavailablePayload.put("message", "Dense vector embedding provider is not configured or offline. Semantic search is unavailable.");
                    unavailablePayload.put("totalResults", 0);
                    unavailablePayload.put("results", Collections.emptyList());
                    return ToolResult.ok(unavailablePayload, Collections.emptyList());
                }

                var rankedResults = semanticSearchService.searchSemantic(query, null, null, null, null, limit);
                List<EvidenceChunkDto> chunkDtos = rankedResults.stream()
                        .map(r -> ingestionService.mapChunkToDto(r.chunk()))
                        .toList();

                List<ProvenanceDto> citations = chunkDtos.stream()
                        .map(EvidenceChunkDto::provenance)
                        .filter(Objects::nonNull)
                        .toList();

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("status", "AVAILABLE");
                payload.put("modality", "SEMANTIC_VECTOR");
                payload.put("totalResults", chunkDtos.size());
                payload.put("results", chunkDtos);

                return ToolResult.ok(payload, citations);
            }
        });

        // 5. Evidence Hybrid Search
        registerTool(new GovernedTool() {
            @Override
            public String getToolId() {
                return "evidence_hybrid_search";
            }

            @Override
            public ToolDefinition getDefinition() {
                return new ToolDefinition(
                        "evidence_hybrid_search",
                        "Evidence Hybrid Search (RRF)",
                        "Fuses dense vector semantic similarity and lexical keyword scoring via Reciprocal Rank Fusion",
                        "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"},\"limit\":{\"type\":\"integer\"}}}",
                        "{\"type\":\"object\",\"properties\":{\"results\":{\"type\":\"array\"},\"contributingModalities\":{\"type\":\"array\"},\"rrfConstant\":{\"type\":\"integer\"}}}",
                        20
                );
            }

            @Override
            public ToolResult execute(ToolExecutionContext ctx) {
                String query = (String) ctx.parameters().get("query");
                int limit = Optional.ofNullable((Integer) ctx.parameters().get("limit")).orElse(10);
                HybridSearchRequest req = new HybridSearchRequest(query, null, null, null, null, limit);
                HybridSearchResponse response = hybridRetrievalService.searchHybrid(req);

                boolean semanticAvailable = embeddingProvider != null && embeddingProvider.isAvailable();
                List<String> contributingModalities = semanticAvailable
                        ? List.of("KEYWORD", "SEMANTIC_VECTOR")
                        : List.of("KEYWORD");

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("query", response.query());
                payload.put("totalResults", response.totalFound());
                payload.put("results", response.results());
                payload.put("rrfConstant", response.rrfConstant());
                payload.put("model", response.embeddingModel());
                payload.put("contributingModalities", contributingModalities);
                payload.put("semanticAvailable", semanticAvailable);

                List<ProvenanceDto> citations = response.results().stream()
                        .map(r -> r.chunk().provenance())
                        .filter(Objects::nonNull)
                        .toList();

                return ToolResult.ok(payload, citations);
            }
        });

        log.info("GovernedToolRegistry initialized with {} tools", tools.size());
    }

    public void registerTool(GovernedTool tool) {
        tools.put(tool.getToolId(), tool);
    }

    public Optional<GovernedTool> getTool(String toolId) {
        return Optional.ofNullable(tools.get(toolId));
    }

    public List<ToolDefinition> getAllToolDefinitions() {
        return tools.values().stream().map(GovernedTool::getDefinition).toList();
    }

    /**
     * Executes a tool with strict agent permission enforcement and audit event logging.
     */
    public ToolResult invokeTool(String toolId, ToolExecutionContext context) {
        GovernedTool tool = tools.get(toolId);
        if (tool == null) {
            recordAuditEvent(context, toolId, false, "Tool not found: " + toolId);
            return ToolResult.error("Governed tool not found: " + toolId);
        }

        // Verify calling agent permissions
        Optional<AgentDefinition> agentDef = agentRegistry.getAgentDefinition(context.agentId());
        if (agentDef.isEmpty() || !agentDef.get().permittedTools().contains(toolId)) {
            String errorMsg = "Agent '" + context.agentId() + "' is not authorized to invoke tool '" + toolId + "'";
            log.warn("Security violation: {}", errorMsg);
            recordAuditEvent(context, toolId, false, errorMsg);
            return ToolResult.error(errorMsg);
        }

        try {
            ToolResult result = tool.execute(context);
            recordAuditEvent(context, toolId, result.success(), result.success() ? "Execution successful" : result.errorMessage());
            return result;
        } catch (Exception e) {
            String errorMsg = "Tool execution failed: " + e.getMessage();
            log.error("Tool '{}' threw exception: {}", toolId, e.getMessage());
            recordAuditEvent(context, toolId, false, errorMsg);
            return ToolResult.error(errorMsg);
        }
    }

    private void recordAuditEvent(ToolExecutionContext ctx, String toolId, boolean success, String summary) {
        try {
            String eventId = "evt-" + UUID.randomUUID();
            Map<String, Object> meta = new HashMap<>();
            meta.put("toolId", toolId);
            meta.put("success", success);
            meta.put("parameters", ctx.parameters());

            WorkflowEvent event = new WorkflowEvent(
                    eventId,
                    ctx.runId(),
                    ctx.taskId(),
                    ctx.agentId(),
                    WorkflowEventType.TOOL_INVOKED,
                    "Tool '" + toolId + "' invoked by '" + ctx.agentId() + "': " + summary,
                    objectMapper.writeValueAsString(meta)
            );
            eventRepository.save(event);
        } catch (Exception e) {
            log.warn("Failed to record tool invocation audit event: {}", e.getMessage());
        }
    }
}

