package org.curriculumai.orchestration;

import org.curriculumai.orchestration.agent.AgentDefinition;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.AgentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRegistryTest {

    private AgentRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AgentRegistry();
    }

    @Test
    @DisplayName("Registry initializes all five specialist agents in REGISTERED_NOT_IMPLEMENTED status")
    void registryContainsAllFiveSpecialistAgentsWithHonestStatus() {
        List<AgentDefinition> agents = registry.getAllAgentDefinitions();

        assertThat(agents).hasSize(5);

        List<String> agentIds = agents.stream().map(AgentDefinition::id).toList();
        assertThat(agentIds).containsExactlyInAnyOrder(
                "curriculum-mapping-agent",
                "industry-skills-agent",
                "academic-benchmarking-agent",
                "gap-analysis-agent",
                "recommendation-agent"
        );

        for (AgentDefinition def : agents) {
            assertThat(def.status())
                    .as("Agent %s must honestly report REGISTERED_NOT_IMPLEMENTED in Phase 3.1", def.id())
                    .isEqualTo(AgentStatus.REGISTERED_NOT_IMPLEMENTED);
            assertThat(def.enabled()).isTrue();
            assertThat(def.permittedTools()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Lookup agent by ID returns expected metadata")
    void lookupAgentById() {
        Optional<AgentDefinition> mappingAgent = registry.getAgentDefinition("curriculum-mapping-agent");

        assertThat(mappingAgent).isPresent();
        assertThat(mappingAgent.get().displayName()).isEqualTo("Curriculum Mapping Agent");
        assertThat(mappingAgent.get().permittedTools())
                .contains("academic_catalog_retrieval", "curriculum_version_retrieval");
    }

    @Test
    @DisplayName("Disable and enable agent transitions status appropriately")
    void enableAndDisableAgent() {
        boolean disabled = registry.disableAgent("curriculum-mapping-agent");
        assertThat(disabled).isTrue();

        AgentDefinition def = registry.getAgentDefinition("curriculum-mapping-agent").orElseThrow();
        assertThat(def.status()).isEqualTo(AgentStatus.DISABLED);
        assertThat(def.enabled()).isFalse();

        boolean enabled = registry.enableAgent("curriculum-mapping-agent");
        assertThat(enabled).isTrue();

        AgentDefinition reEnabled = registry.getAgentDefinition("curriculum-mapping-agent").orElseThrow();
        assertThat(reEnabled.enabled()).isTrue();
    }

    @Test
    @DisplayName("Agent implementation status check reports false for unimplemented specialist agents")
    void isAgentImplementedReportsFalse() {
        assertThat(registry.isAgentImplemented("curriculum-mapping-agent")).isFalse();
        assertThat(registry.isAgentImplemented("industry-skills-agent")).isFalse();
        assertThat(registry.isAgentImplemented("academic-benchmarking-agent")).isFalse();
        assertThat(registry.isAgentImplemented("gap-analysis-agent")).isFalse();
        assertThat(registry.isAgentImplemented("recommendation-agent")).isFalse();
    }

    @Test
    @DisplayName("Registering specialist agent implementation updates its status to IMPLEMENTED")
    void registeringSpecialistAgentUpdatesStatusToImplemented() {
        org.curriculumai.orchestration.agent.SpecialistAgent mockAgent = org.mockito.Mockito.mock(org.curriculumai.orchestration.agent.SpecialistAgent.class);
        org.mockito.Mockito.when(mockAgent.getAgentId()).thenReturn("curriculum-mapping-agent");
        org.mockito.Mockito.when(mockAgent.getStatus()).thenReturn(AgentStatus.IMPLEMENTED);

        registry.registerImplementation(mockAgent);

        assertThat(registry.isAgentImplemented("curriculum-mapping-agent")).isTrue();
        AgentDefinition def = registry.getAgentDefinition("curriculum-mapping-agent").orElseThrow();
        assertThat(def.status()).isEqualTo(AgentStatus.IMPLEMENTED);

        // Other four agents must remain REGISTERED_NOT_IMPLEMENTED
        assertThat(registry.getAgentDefinition("industry-skills-agent").orElseThrow().status())
                .isEqualTo(AgentStatus.REGISTERED_NOT_IMPLEMENTED);
        assertThat(registry.getAgentDefinition("academic-benchmarking-agent").orElseThrow().status())
                .isEqualTo(AgentStatus.REGISTERED_NOT_IMPLEMENTED);
        assertThat(registry.getAgentDefinition("gap-analysis-agent").orElseThrow().status())
                .isEqualTo(AgentStatus.REGISTERED_NOT_IMPLEMENTED);
        assertThat(registry.getAgentDefinition("recommendation-agent").orElseThrow().status())
                .isEqualTo(AgentStatus.REGISTERED_NOT_IMPLEMENTED);
    }
}

