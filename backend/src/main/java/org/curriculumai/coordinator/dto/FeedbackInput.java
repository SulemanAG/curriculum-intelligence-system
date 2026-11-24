package org.curriculumai.coordinator.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record FeedbackInput(
        String requestId,
        String runId,
        @NotBlank(message = "selectedAlternativeId must not be blank")
        String selectedAlternativeId,
        List<String> rejectedAlternativeIds,
        String facultyName,
        String facultyId,
        String facultyRole,
        String rationale,
        String facultyRemarks,
        String suggestedModifications,
        String submittedAt,
        Integer rating
) {
    public FeedbackInput(
            String requestId,
            String selectedAlternativeId,
            List<String> rejectedAlternativeIds,
            String facultyName,
            String facultyRole,
            String rationale,
            String facultyRemarks,
            Integer rating
    ) {
        this(requestId, requestId, selectedAlternativeId, rejectedAlternativeIds, facultyName, "FAC-001", facultyRole, rationale, facultyRemarks, null, null, rating);
    }
    public String getEffectiveRequestId() {
        return (requestId != null && !requestId.isBlank()) ? requestId : runId;
    }

    public String getEffectiveRationale() {
        if (rationale != null && !rationale.isBlank()) return rationale;
        if (facultyRemarks != null && !facultyRemarks.isBlank()) return facultyRemarks;
        return "Proposal selected and approved for institutional curriculum integration.";
    }
}

