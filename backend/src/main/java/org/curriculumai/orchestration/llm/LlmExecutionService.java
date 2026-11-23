package org.curriculumai.orchestration.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class LlmExecutionService {

    private static final Logger log = LoggerFactory.getLogger(LlmExecutionService.class);

    private final LlmProperties properties;
    private final ChatLanguageModel chatModel;

    public LlmExecutionService(LlmProperties properties) {
        this.properties = properties;
        if (properties.isEnabled() && properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            log.info("Initializing LangChain4j OpenAI Chat Model (model={}, baseUrl={})",
                    properties.getModel(), properties.getBaseUrl());
            this.chatModel = OpenAiChatModel.builder()
                    .apiKey(properties.getApiKey())
                    .modelName(properties.getModel())
                    .baseUrl(properties.getBaseUrl())
                    .temperature(properties.getTemperature())
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .logRequests(true)
                    .logResponses(true)
                    .build();
        } else {
            log.info("LangChain4j LLM execution is unconfigured or disabled (safe fallback active).");
            this.chatModel = null;
        }
    }

    public boolean isAvailable() {
        return chatModel != null;
    }

    public String getProviderName() {
        return properties.getProvider();
    }

    public String getModelName() {
        return properties.getModel();
    }

    public String executePrompt(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("LLM execution service is not available. Please configure app.llm with a valid API key.");
        }

        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        if (userPrompt != null && !userPrompt.isBlank()) {
            messages.add(new UserMessage(userPrompt));
        }

        Response<AiMessage> response = chatModel.generate(messages);
        return response.content().text();
    }
}

