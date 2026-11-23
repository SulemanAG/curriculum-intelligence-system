package org.curriculumai.orchestration.tools;

public record ToolDefinition(
        String id,
        String name,
        String description,
        String inputSchemaJson,
        String outputSchemaJson,
        int maxExecutionsPerTask
) {
    public String toolId() {
        return id;
    }
}
