-- ========================================================
-- Curriculum Intelligence System - Flyway Migration V9
-- Phase 3.1: AURA Coordinator & Multi-Agent Orchestration Foundation
-- Tables: orchestration_runs, workflow_tasks, task_dependencies,
--         agent_execution_records, workflow_event_history
-- ========================================================

-- 1. Orchestration Runs
CREATE TABLE IF NOT EXISTS orchestration_runs (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id),
    conversation_id VARCHAR(64) REFERENCES conversations(id) ON DELETE SET NULL,
    message_id VARCHAR(64) REFERENCES messages(id) ON DELETE SET NULL,
    raw_prompt TEXT NOT NULL,
    subject VARCHAR(500) NOT NULL,
    workflow_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    plan_definition_json TEXT,
    shared_context_json TEXT,
    error_message TEXT,
    current_checkpoint VARCHAR(100),
    total_tasks INT NOT NULL DEFAULT 0,
    completed_tasks INT NOT NULL DEFAULT 0,
    failed_tasks INT NOT NULL DEFAULT 0,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Workflow Tasks
CREATE TABLE IF NOT EXISTS workflow_tasks (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES orchestration_runs(id) ON DELETE CASCADE,
    task_key VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    assigned_agent_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    sequence_order INT NOT NULL DEFAULT 0,
    is_parallel_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    attempt_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 2,
    timeout_seconds INT NOT NULL DEFAULT 120,
    input_payload_json TEXT,
    output_payload_json TEXT,
    error_message TEXT,
    error_type VARCHAR(100),
    retryable BOOLEAN NOT NULL DEFAULT FALSE,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_run_task UNIQUE (run_id, task_key)
);

-- 3. Task Dependencies (DAG edges)
CREATE TABLE IF NOT EXISTS task_dependencies (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES orchestration_runs(id) ON DELETE CASCADE,
    task_id VARCHAR(64) NOT NULL REFERENCES workflow_tasks(id) ON DELETE CASCADE,
    depends_on_task_id VARCHAR(64) NOT NULL REFERENCES workflow_tasks(id) ON DELETE CASCADE,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_task_dependency UNIQUE (task_id, depends_on_task_id)
);

-- 4. Agent Execution Records (Structured execution attempts and findings)
CREATE TABLE IF NOT EXISTS agent_execution_records (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES orchestration_runs(id) ON DELETE CASCADE,
    task_id VARCHAR(64) NOT NULL REFERENCES workflow_tasks(id) ON DELETE CASCADE,
    agent_id VARCHAR(100) NOT NULL,
    attempt_number INT NOT NULL,
    execution_status VARCHAR(50) NOT NULL,
    findings_json TEXT,
    evidence_references_json TEXT,
    limitations_json TEXT,
    error_details TEXT,
    execution_metadata_json TEXT,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Workflow Event History (Audit trail of state transitions and tool invocations)
CREATE TABLE IF NOT EXISTS workflow_event_history (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES orchestration_runs(id) ON DELETE CASCADE,
    task_id VARCHAR(64) REFERENCES workflow_tasks(id) ON DELETE SET NULL,
    agent_id VARCHAR(100),
    event_type VARCHAR(100) NOT NULL,
    event_summary TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Indexes for queries, joins, and state checks
CREATE INDEX IF NOT EXISTS idx_orch_run_user ON orchestration_runs(user_id);
CREATE INDEX IF NOT EXISTS idx_orch_run_status ON orchestration_runs(status);
CREATE INDEX IF NOT EXISTS idx_orch_run_conv ON orchestration_runs(conversation_id);
CREATE INDEX IF NOT EXISTS idx_orch_task_run ON workflow_tasks(run_id);
CREATE INDEX IF NOT EXISTS idx_orch_task_status ON workflow_tasks(status);
CREATE INDEX IF NOT EXISTS idx_task_dep_task ON task_dependencies(task_id);
CREATE INDEX IF NOT EXISTS idx_task_dep_depends ON task_dependencies(depends_on_task_id);
CREATE INDEX IF NOT EXISTS idx_agent_exec_task ON agent_execution_records(task_id);
CREATE INDEX IF NOT EXISTS idx_workflow_event_run ON workflow_event_history(run_id);
CREATE INDEX IF NOT EXISTS idx_workflow_event_task ON workflow_event_history(task_id);

