package org.curriculumai.orchestration.tools;

public interface GovernedTool {

    String getToolId();

    ToolDefinition getDefinition();

    ToolResult execute(ToolExecutionContext context);
}

