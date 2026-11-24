package org.curriculumai.orchestration.research;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.research")
public class ResearchProperties {

    private String geminiApiKey = "";
    private String geminiModel = "gemini-2.5-flash";
    private String openrouterApiKey = "";
    private int timeoutSeconds = 75;

    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public void setGeminiApiKey(String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    public void setGeminiModel(String geminiModel) {
        this.geminiModel = geminiModel;
    }

    public String getOpenrouterApiKey() {
        return openrouterApiKey;
    }

    public void setOpenrouterApiKey(String openrouterApiKey) {
        this.openrouterApiKey = openrouterApiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}

