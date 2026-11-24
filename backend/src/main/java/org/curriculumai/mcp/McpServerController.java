package org.curriculumai.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.curriculumai.mcp.dto.McpRequest;
import org.curriculumai.mcp.dto.McpResponse;
import org.curriculumai.orchestration.tools.GovernedToolRegistry;
import org.curriculumai.orchestration.tools.ToolDefinition;
import org.curriculumai.orchestration.tools.ToolExecutionContext;
import org.curriculumai.orchestration.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Model Context Protocol (MCP) Server Endpoint
 * Implements the standard JSON-RPC 2.0 MCP protocol for tool discovery and execution.
 */
@RestController
@RequestMapping("/api/mcp")
@Tag(name = "Model Context Protocol (MCP)", description = "Standard MCP JSON-RPC 2.0 and REST Tool Endpoints")
public class McpServerController {

    private static final Logger log = LoggerFactory.getLogger(McpServerController.class);

    private final GovernedToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public McpServerController(GovernedToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @Operation(summary = "Standard MCP JSON-RPC 2.0 protocol endpoint")
    public ResponseEntity<McpResponse> handleMcpRequest(@RequestBody McpRequest request) {
        if (request == null || request.method() == null) {
            return ResponseEntity.badRequest().body(McpResponse.error(null, -32600, "Invalid JSON-RPC Request"));
        }

        Object id = request.id() != null ? request.id() : 1;
        String method = request.method().trim();
        log.info("MCP JSON-RPC request received: method='{}', id={}", method, id);

        return switch (method) {
            case "initialize" -> handleInitialize(id);
            case "tools/list" -> handleToolsList(id);
            case "tools/call" -> handleToolsCall(id, request.params());
            case "ping" -> ResponseEntity.ok(McpResponse.ok(id, Map.of()));
            default -> {
                log.warn("MCP method not found: '{}'", method);
                yield ResponseEntity.ok(McpResponse.error(id, -32601, "Method not found: " + method));
            }
        };
    }

    private ResponseEntity<McpResponse> handleInitialize(Object id) {
        Map<String, Object> serverInfo = Map.of(
                "name", "curriculum-intelligence-mcp-server",
                "version", "1.0.0"
        );
        Map<String, Object> capabilities = Map.of(
                "tools", Map.of("listChanged", false)
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        result.put("instructions", "Curriculum Intelligence System MCP Server providing accredited institutional catalog lookups and evidence search.");

        return ResponseEntity.ok(McpResponse.ok(id, result));
    }

    private ResponseEntity<McpResponse> handleToolsList(Object id) {
        List<ToolDefinition> definitions = toolRegistry.getAllToolDefinitions();
        List<Map<String, Object>> mcpTools = new ArrayList<>();

        for (ToolDefinition def : definitions) {
            Map<String, Object> toolMap = new LinkedHashMap<>();
            toolMap.put("name", def.id());
            toolMap.put("description", def.description());

            try {
                if (def.inputSchemaJson() != null && !def.inputSchemaJson().isBlank()) {
                    toolMap.put("inputSchema", objectMapper.readValue(def.inputSchemaJson(), Object.class));
                } else {
                    toolMap.put("inputSchema", Map.of("type", "object"));
                }
            } catch (Exception e) {
                toolMap.put("inputSchema", Map.of("type", "object"));
            }

            mcpTools.add(toolMap);
        }

        return ResponseEntity.ok(McpResponse.ok(id, Map.of("tools", mcpTools)));
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<McpResponse> handleToolsCall(Object id, Map<String, Object> params) {
        if (params == null || !params.containsKey("name")) {
            return ResponseEntity.ok(McpResponse.error(id, -32602, "Invalid params: 'name' is required for tools/call"));
        }

        String toolName = (String) params.get("name");
        Map<String, Object> arguments = params.containsKey("arguments")
                ? (Map<String, Object>) params.get("arguments")
                : Collections.emptyMap();

        ToolExecutionContext ctx = new ToolExecutionContext(
                "mcp-session-" + UUID.randomUUID().toString().substring(0, 8),
                "mcp-task-" + System.currentTimeMillis(),
                "mcp-gateway",
                arguments != null ? arguments : Collections.emptyMap()
        );

        ToolResult toolResult = toolRegistry.invokeTool(toolName, ctx);

        List<Map<String, Object>> contentList = new ArrayList<>();
        if (toolResult.success()) {
            try {
                String resultText = objectMapper.writeValueAsString(toolResult.data());
                contentList.add(Map.of("type", "text", "text", resultText));
            } catch (Exception e) {
                contentList.add(Map.of("type", "text", "text", String.valueOf(toolResult.data())));
            }
            Map<String, Object> resultPayload = new LinkedHashMap<>();
            resultPayload.put("content", contentList);
            resultPayload.put("isError", false);
            return ResponseEntity.ok(McpResponse.ok(id, resultPayload));
        } else {
            contentList.add(Map.of("type", "text", "text", "Error: " + toolResult.errorMessage()));
            Map<String, Object> errorPayload = new LinkedHashMap<>();
            errorPayload.put("content", contentList);
            errorPayload.put("isError", true);
            return ResponseEntity.ok(McpResponse.ok(id, errorPayload));
        }
    }

    @GetMapping("/tools")
    @Operation(summary = "Convenience REST endpoint to list all discoverable MCP tools")
    public ResponseEntity<Map<String, Object>> listToolsRest() {
        List<ToolDefinition> definitions = toolRegistry.getAllToolDefinitions();
        return ResponseEntity.ok(Map.of(
                "status", "CONNECTED",
                "protocol", "Model Context Protocol (MCP) 2024-11-05",
                "totalTools", definitions.size(),
                "tools", definitions
        ));
    }

    @PostMapping("/tools/{toolId}")
    @Operation(summary = "Convenience REST endpoint to invoke a specific MCP tool directly")
    public ResponseEntity<ToolResult> invokeToolRest(
            @PathVariable("toolId") String toolId,
            @RequestBody(required = false) Map<String, Object> parameters
    ) {
        ToolExecutionContext ctx = new ToolExecutionContext(
                "rest-mcp-session",
                "rest-task",
                "mcp-gateway",
                parameters != null ? parameters : Collections.emptyMap()
        );

        ToolResult result = toolRegistry.invokeTool(toolId, ctx);
        return ResponseEntity.ok(result);
    }
}
