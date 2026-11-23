package org.curriculumai.orchestration.tools;

import org.curriculumai.evidence.dto.ProvenanceDto;

import java.util.Collections;
import java.util.List;

public record ToolResult(
        boolean success,
        Object data,
        List<ProvenanceDto> citations,
        String errorMessage
) {
    public ToolResult {
        if (citations == null) citations = Collections.emptyList();
    }

    public static ToolResult ok(Object data, List<ProvenanceDto> citations) {
        return new ToolResult(true, data, citations, null);
    }

    public static ToolResult error(String errorMessage) {
        return new ToolResult(false, null, Collections.emptyList(), errorMessage);
    }
}

