package org.curriculumai.orchestration.agent;

import org.curriculumai.orchestration.domain.AgentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentRegistry {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistry.class);

    private final Map<String, AgentDefinition> definitions = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, SpecialistAgent> implementations = Collections.synchronizedMap(new LinkedHashMap<>());

    public AgentRegistry() {
        this(Collections.emptyList());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AgentRegistry(List<SpecialistAgent> specialistAgents) {
        registerDefaultSpecialistAgents();
        if (specialistAgents != null) {
            specialistAgents.forEach(this::registerImplementation);
        }
    }

    private void registerDefaultSpecialistAgents() {
        // 1. Curriculum Mapping Agent
        registerDefinition(new AgentDefinition(
                "curriculum-mapping-agent",
                "Curriculum Mapping Agent",
                "Analyzes institutional course structures, outcomes (CO-PO), Bloom taxonomy levels, and prerequisites",
                "Curriculum outcome mapping, syllabus breakdown, prerequisite DAG validation",
                "{\"type\":\"object\",\"properties\":{\"subject\":{\"type\":\"string\"},\"curriculumVersionId\":{\"type\":\"string\"}}}",
                "{\"type\":\"object\",\"properties\":{\"mappedOutcomes\":{\"type\":\"array\"},\"prerequisites\":{\"type\":\"array\"}}}",
                List.of("academic_catalog_retrieval", "curriculum_version_retrieval"),
                AgentStatus.REGISTERED_NOT_IMPLEMENTED,
                true,
                Map.of("category", "INSTITUTIONAL_CURRICULUM")
        ));

        // 2. Industry Skills Agent
        registerDefinition(new AgentDefinition(
                "industry-skills-agent",
                "Industry Skills Agent",
                "Extracts real-world tech stack requirements, competency frameworks, and industry trends",
                "Labor market skill extraction, role competency clustering, industry framework mapping",
                "{\"type\":\"object\",\"properties\":{\"subject\":{\"type\":\"string\"},\"targetDomains\":{\"type\":\"array\"}}}",
                "{\"type\":\"object\",\"properties\":{\"requiredSkills\":{\"type\":\"array\"},\"emergingTrends\":{\"type\":\"array\"}}}",
                List.of("evidence_keyword_search", "evidence_semantic_search", "evidence_hybrid_search"),
                AgentStatus.REGISTERED_NOT_IMPLEMENTED,
                true,
                Map.of("category", "INDUSTRY_COMPETENCIES")
        ));

        // 3. Academic Benchmarking Agent
        registerDefinition(new AgentDefinition(
                "academic-benchmarking-agent",
                "Academic Benchmarking Agent",
                "Surveys peer institutional syllabi and professional standards (ACM/IEEE CS Curricula)",
                "Peer syllabi retrieval, academic curriculum comparison, professional guideline compliance",
                "{\"type\":\"object\",\"properties\":{\"subject\":{\"type\":\"string\"},\"benchmarkInstitutions\":{\"type\":\"array\"}}}",
                "{\"type\":\"object\",\"properties\":{\"benchmarkedCourses\":{\"type\":\"array\"},\"standardAlignments\":{\"type\":\"array\"}}}",
                List.of("evidence_keyword_search", "evidence_semantic_search", "evidence_hybrid_search", "academic_catalog_retrieval"),
                AgentStatus.REGISTERED_NOT_IMPLEMENTED,
                true,
                Map.of("category", "ACADEMIC_STANDARDS")
        ));

        // 4. Gap Analysis Agent
        registerDefinition(new AgentDefinition(
                "gap-analysis-agent",
                "Gap Analysis Agent",
                "Synthesizes institutional mappings, industry skills, and academic benchmarks to identify curricular deficits",
                "Curricular gap scoring, obsolete topic identification, missing prerequisite detection",
                "{\"type\":\"object\",\"properties\":{\"mappingFindings\":{\"type\":\"object\"},\"industryFindings\":{\"type\":\"object\"},\"benchmarkingFindings\":{\"type\":\"object\"}}}",
                "{\"type\":\"object\",\"properties\":{\"identifiedGaps\":{\"type\":\"array\"},\"severityScore\":{\"type\":\"number\"}}}",
                List.of("academic_catalog_retrieval", "curriculum_version_retrieval", "evidence_keyword_search"),
                AgentStatus.REGISTERED_NOT_IMPLEMENTED,
                true,
                Map.of("category", "SYNTHESIS")
        ));

        // 5. Recommendation Agent
        registerDefinition(new AgentDefinition(
                "recommendation-agent",
                "Recommendation Agent",
                "Formulates evidence-backed curriculum revisions, course outlines, and pedagogical recommendations",
                "Actionable course proposals, modern outcome formulations, grounded citation attribution",
                "{\"type\":\"object\",\"properties\":{\"gapAnalysisFindings\":{\"type\":\"object\"}}}",
                "{\"type\":\"object\",\"properties\":{\"recommendations\":{\"type\":\"array\"},\"suggestedCourseStructure\":{\"type\":\"object\"}}}",
                List.of("academic_catalog_retrieval", "curriculum_version_retrieval", "evidence_hybrid_search"),
                AgentStatus.REGISTERED_NOT_IMPLEMENTED,
                true,
                Map.of("category", "RECOMMENDATIONS")
        ));

        log.info("AgentRegistry initialized with 5 planned specialist agent definitions");
    }

    public void registerDefinition(AgentDefinition definition) {
        definitions.put(definition.id(), definition);
    }

    public void registerImplementation(SpecialistAgent agent) {
        implementations.put(agent.getAgentId(), agent);
        AgentDefinition existing = definitions.get(agent.getAgentId());
        if (existing != null) {
            definitions.put(agent.getAgentId(), new AgentDefinition(
                    existing.id(),
                    existing.displayName(),
                    existing.description(),
                    existing.responsibility(),
                    existing.inputSchemaJson(),
                    existing.outputSchemaJson(),
                    existing.permittedTools(),
                    agent.getStatus(),
                    existing.enabled(),
                    existing.executionConfig()
            ));
        }
    }

    public Optional<AgentDefinition> getAgentDefinition(String agentId) {
        return Optional.ofNullable(definitions.get(agentId));
    }

    public Optional<AgentDefinition> getAgent(String agentId) {
        return getAgentDefinition(agentId);
    }

    public Optional<SpecialistAgent> getAgentImplementation(String agentId) {
        return Optional.ofNullable(implementations.get(agentId));
    }

    public SpecialistAgent getAgentInstance(String agentId) {
        return implementations.get(agentId);
    }

    public List<AgentDefinition> getAllAgentDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    public List<AgentDefinition> getAllAgents() {
        return getAllAgentDefinitions();
    }

    public boolean isAgentImplemented(String agentId) {
        SpecialistAgent impl = implementations.get(agentId);
        return impl != null && (impl.getStatus() == AgentStatus.IMPLEMENTED || impl.getStatus() == AgentStatus.ACTIVE);
    }

    public boolean enableAgent(String agentId) {
        AgentDefinition existing = definitions.get(agentId);
        if (existing == null) return false;
        definitions.put(agentId, new AgentDefinition(
                existing.id(), existing.displayName(), existing.description(),
                existing.responsibility(), existing.inputSchemaJson(), existing.outputSchemaJson(),
                existing.permittedTools(), existing.status(), true, existing.executionConfig()
        ));
        return true;
    }

    public boolean disableAgent(String agentId) {
        AgentDefinition existing = definitions.get(agentId);
        if (existing == null) return false;
        definitions.put(agentId, new AgentDefinition(
                existing.id(), existing.displayName(), existing.description(),
                existing.responsibility(), existing.inputSchemaJson(), existing.outputSchemaJson(),
                existing.permittedTools(), AgentStatus.DISABLED, false, existing.executionConfig()
        ));
        return true;
    }
}
