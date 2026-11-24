package org.curriculumai.orchestration.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {

    private boolean enabled = true;
    private String primaryProvider = "groq";
    private boolean failoverEnabled = true;
    private int timeoutSeconds = 60;
    private double temperature = 0.2;

    // Legacy fallback fields
    private String provider = "groq";
    private String model = "openai/gpt-oss-120b";
    private String apiKey = "";
    private String baseUrl = "https://api.groq.com/openai/v1";

    // Dedicated provider configs
    private ProviderConfig groq = new ProviderConfig();
    private ProviderConfig gemini = new ProviderConfig();

    public static class ProviderConfig {
        private String apiKey = "";
        private String model = "";
        private String baseUrl = "";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey != null ? apiKey.trim() : "";
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model != null ? model.trim() : "";
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl != null ? baseUrl.trim() : "";
        }

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrimaryProvider() {
        if (primaryProvider != null && !primaryProvider.isBlank()) {
            return primaryProvider.trim().toLowerCase();
        }
        return provider != null ? provider.trim().toLowerCase() : "groq";
    }

    public void setPrimaryProvider(String primaryProvider) {
        this.primaryProvider = primaryProvider;
    }

    public boolean isFailoverEnabled() {
        return failoverEnabled;
    }

    public void setFailoverEnabled(boolean failoverEnabled) {
        this.failoverEnabled = failoverEnabled;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds > 0 ? timeoutSeconds : 60;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getProvider() {
        return getPrimaryProvider();
    }

    public void setProvider(String provider) {
        this.provider = provider;
        if (this.primaryProvider == null || this.primaryProvider.isBlank()) {
            this.primaryProvider = provider;
        }
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ProviderConfig getGroq() {
        return groq;
    }

    public void setGroq(ProviderConfig groq) {
        this.groq = groq;
    }

    public ProviderConfig getGemini() {
        return gemini;
    }

    public void setGemini(ProviderConfig gemini) {
        this.gemini = gemini;
    }

    /**
     * Resolves effective Groq API key (from groq.apiKey, or root apiKey if provider=groq).
     */
    public String getEffectiveGroqApiKey() {
        if (groq.isConfigured()) return groq.getApiKey();
        if ("groq".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return "";
    }

    public String getEffectiveGroqModel() {
        if (groq.getModel() != null && !groq.getModel().isBlank()) return groq.getModel();
        if ("groq".equalsIgnoreCase(provider) && model != null && !model.isBlank()) {
            return model;
        }
        return "openai/gpt-oss-120b";
    }

    public String getEffectiveGroqBaseUrl() {
        if (groq.getBaseUrl() != null && !groq.getBaseUrl().isBlank()) return groq.getBaseUrl();
        if ("groq".equalsIgnoreCase(provider) && baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }
        return "https://api.groq.com/openai/v1";
    }

    /**
     * Resolves effective Gemini API key (from gemini.apiKey, or root apiKey if provider=gemini).
     */
    public String getEffectiveGeminiApiKey() {
        if (gemini.isConfigured()) return gemini.getApiKey();
        if ("gemini".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return "";
    }

    public String getEffectiveGeminiModel() {
        if (gemini.getModel() != null && !gemini.getModel().isBlank()) return gemini.getModel();
        if ("gemini".equalsIgnoreCase(provider) && model != null && !model.isBlank()) {
            return model;
        }
        return "gemini-2.5-flash";
    }
}
