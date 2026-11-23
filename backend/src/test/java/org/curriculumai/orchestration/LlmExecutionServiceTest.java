package org.curriculumai.orchestration;

import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.curriculumai.orchestration.llm.LlmProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmExecutionServiceTest {

    @Test
    @DisplayName("Safe fallback when unconfigured: isAvailable is false and prompt execution throws honest exception")
    void safeFallbackWhenUnconfigured() {
        LlmProperties props = new LlmProperties();
        props.setEnabled(false);
        props.setApiKey("");

        LlmExecutionService service = new LlmExecutionService(props);

        assertThat(service.isAvailable()).isFalse();

        assertThatThrownBy(() -> service.executePrompt("System prompt", "User prompt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LLM execution service is not available");
    }

    @Test
    @DisplayName("Service exposes provider name and model name from configuration")
    void exposesProviderAndModel() {
        LlmProperties props = new LlmProperties();
        props.setProvider("openai");
        props.setModel("gpt-4o-mini");

        LlmExecutionService service = new LlmExecutionService(props);

        assertThat(service.getProviderName()).isEqualTo("openai");
        assertThat(service.getModelName()).isEqualTo("gpt-4o-mini");
    }
}

