#!/usr/bin/env python3
"""
Curriculum Intelligence System — Model Context Protocol (MCP) Stdio Bridge
Standard MCP JSON-RPC 2.0 stdio server connecting external MCP clients (Claude Desktop, Cursor, Antigravity)
to the Curriculum Intelligence Governed Tool Registry.
"""

import sys
import json
import urllib.request
import urllib.error

API_URL = "http://localhost:8081/api/mcp"

def log_debug(msg):
    sys.stderr.write(f"[MCP-BRIDGE] {msg}\n")
    sys.stderr.flush()

def forward_request(request_data):
    try:
        req = urllib.request.Request(
            API_URL,
            data=json.dumps(request_data).encode("utf-8"),
            headers={"Content-Type": "application/json"}
        )
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.URLError as e:
        log_debug(f"HTTP request error: {e}")
        req_id = request_data.get("id")
        return {
            "jsonrpc": "2.0",
            "id": req_id,
            "error": {
                "code": -32603,
                "message": f"Curriculum backend not reachable at {API_URL}. Ensure Spring Boot backend is running on port 8081."
            }
        }
    except Exception as e:
        log_debug(f"Unexpected bridge error: {e}")
        req_id = request_data.get("id")
        return {
            "jsonrpc": "2.0",
            "id": req_id,
            "error": {
                "code": -32603,
                "message": f"Bridge internal error: {str(e)}"
            }
        }

def main():
    log_debug("Curriculum Intelligence MCP Stdio Bridge started.")
    while True:
        try:
            line = sys.stdin.readline()
            if not line:
                break
            line = line.strip()
            if not line:
                continue

            request_data = json.loads(line)
            response_data = forward_request(request_data)

            response_line = json.dumps(response_data)
            sys.stdout.write(response_line + "\n")
            sys.stdout.flush()
        except KeyboardInterrupt:
            break
        except Exception as e:
            log_debug(f"Loop error: {e}")

if __name__ == "__main__":
    main()
