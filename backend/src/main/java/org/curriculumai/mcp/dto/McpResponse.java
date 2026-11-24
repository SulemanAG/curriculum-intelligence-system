package org.curriculumai.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard Model Context Protocol (MCP) JSON-RPC 2.0 Response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record McpResponse(
        String jsonrpc,
        Object id,
        Object result,
        McpError error
) {
    public record McpError(int code, String message, Object data) {}

    public static McpResponse ok(Object id, Object result) {
        return new McpResponse("2.0", id, result, null);
    }

    public static McpResponse error(Object id, int code, String message) {
        return new McpResponse("2.0", id, null, new McpError(code, message, null));
    }
}
