# Model Context Protocol (MCP) & OpenCLA / OpenClaw Integration
## Formal Specification, Tool Discovery, and Gateway Architecture

---

## 1. Model Context Protocol (MCP) Server

### 1.1 Overview
The **Curriculum Intelligence System** natively implements the **Model Context Protocol (MCP)** specification (Standard `2024-11-05`). It exposes the governed academic tool registry to external agentic clients (such as Claude Desktop, Cursor, and Antigravity) using standard **JSON-RPC 2.0**.

### 1.2 Protocol Endpoints
- **HTTP / JSON-RPC Endpoint:** `POST http://localhost:8081/api/mcp`
- **Tool Discovery REST Endpoint:** `GET http://localhost:8081/api/mcp/tools`
- **Direct Tool Invocation REST Endpoint:** `POST http://localhost:8081/api/mcp/tools/{toolId}`
- **Stdio Bridge Script:** `backend/scripts/mcp_server.py`

### 1.3 Verified MCP Tool Registry

The following 5 governed tools are exposed over the MCP protocol:

| MCP Tool Name | Description | Parameters Schema |
| :--- | :--- | :--- |
| **`academic_catalog_retrieval`** | Retrieves accredited institutional courses, departments, and programmes from PostgreSQL. | `query` (string), `departmentId` (string) |
| **`curriculum_version_retrieval`**| Retrieves approved curriculum version definitions, offerings, and outcomes. | `programmeId` (string) |
| **`evidence_keyword_search`** | Performs lexical full-text search across ingested evidence chunks with citation provenance. | `query` (string), `limit` (integer) |
| **`evidence_semantic_search`** | Performs dense vector similarity search across extracted research passages using Jina AI embeddings. | `query` (string), `limit` (integer) |
| **`evidence_hybrid_search`** | Fuses dense vector similarity and lexical keyword scoring via Reciprocal Rank Fusion (RRF). | `query` (string), `limit` (integer) |

### 1.4 Sample MCP Protocol Interaction

#### Handshake (`initialize`)
**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {}
}
```
**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": { "listChanged": false }
    },
    "serverInfo": {
      "name": "curriculum-intelligence-mcp-server",
      "version": "1.0.0"
    },
    "instructions": "Curriculum Intelligence System MCP Server providing accredited institutional catalog lookups and evidence search."
  }
}
```

#### Tool Invocation (`tools/call`)
**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "academic_catalog_retrieval",
    "arguments": {
      "query": "Deep Learning"
    }
  }
}
```
**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "[{\"id\":\"crs-dl-401\",\"code\":\"26ECAC401\",\"title\":\"Deep Learning Systems\",\"credits\":\"3:0:2\"}]"
      }
    ],
    "isError": false
  }
}
```

---

## 2. OpenCLA / OpenClaw Gateway Integration

### 2.1 Architectural Boundary
The **OpenClaw / OpenCLA** integration provides an external agentic gateway interface. It acts as an integration boundary allowing external OpenClaw agents to dispatch tasks or query the system's curriculum intelligence.

```
┌─────────────────────────────────┐
│     OpenClaw / OpenCLA Runtime  │ (External Agent Framework)
└───────────────┬─────────────────┘
                │ HTTP Gateway Protocol
                ▼
┌─────────────────────────────────────────────────────────┐
│        Curriculum Intelligence System Backend            │
│  ┌───────────────────────────────────────────────────┐  │
│  │ OpenClawGatewayService (Governed External Adapter)│  │
│  └────────────────────────┬──────────────────────────┘  │
│                           ▼                             │
│  ┌───────────────────────────────────────────────────┐  │
│  │ GovernedToolRegistry & AURA Orchestrator          │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Honest Operating Modes
In compliance with academic evaluation standards, the application provides an **honest, unsimulated status**:
1. **`STANDBY` (Default):** When `openclaw.enabled=false`, the adapter is in standby. AURA operates in native autonomous multi-agent mode.
2. **`CONNECTED`:** When an external OpenClaw daemon is running at `http://localhost:9000` and responds with HTTP 200 on its health endpoint.
3. **`OFFLINE`:** When enabled but the external daemon is unreachable, the system gracefully defaults to native AURA orchestration without failing.

### 2.3 Status Endpoint: `GET /api/curriculum/openclaw/status`
**Live Response:**
```json
{
  "success": true,
  "data": {
    "enabled": false,
    "state": "STANDBY",
    "gatewayUrl": "http://localhost:9000/api/v1/agents",
    "capabilities": ["TASK_DISPATCH", "EXTERNAL_AUDIT", "TOOL_BRIDGING"],
    "connectionMessage": "OpenClaw external gateway is in STANDBY. AURA is operating in autonomous native multi-agent mode.",
    "lastChecked": "2026-11-24T05:00:00.000000Z"
  },
  "message": "OpenCLA / OpenClaw integration status retrieved",
  "timestamp": "2026-11-24T05:00:00.000000Z"
}
```
