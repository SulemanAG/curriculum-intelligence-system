package org.curriculumai.mcp.dto;

import java.util.Map;

/**
 * Standard Model Context Protocol (MCP) JSON-RPC 2.0 Request
 */
public record McpRequest(
        String jsonrpc,
        Object id,
        String method,
        Map<String, Object> params
) {}
