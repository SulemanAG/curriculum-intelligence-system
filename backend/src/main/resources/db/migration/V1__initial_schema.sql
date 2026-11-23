-- ========================================================
-- Curriculum Intelligence System - Normalized PostgreSQL Schema
-- Migration V1__initial_schema.sql
-- ========================================================

-- 1. Users & Authorization
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'FACULTY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 2. Conversations
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    is_custom_title BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conversations_user_updated ON conversations(user_id, updated_at DESC);

-- 3. Messages
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(64) PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    payload_type VARCHAR(50) DEFAULT 'text',
    research_run_id VARCHAR(64),
    selected_alternative_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at ASC);

-- 4. Analysis Runs (Coordinator Orchestration)
CREATE TABLE IF NOT EXISTS analysis_runs (
    id VARCHAR(64) PRIMARY KEY,
    conversation_id VARCHAR(64) REFERENCES conversations(id) ON DELETE SET NULL,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id),
    raw_prompt TEXT NOT NULL,
    department VARCHAR(255) NOT NULL,
    degree_level VARCHAR(20) NOT NULL DEFAULT 'UG',
    status VARCHAR(50) NOT NULL DEFAULT 'initialized',
    current_stage_id VARCHAR(100) NOT NULL,
    elapsed_seconds INT NOT NULL DEFAULT 0,
    has_alternatives BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analysis_runs_status ON analysis_runs(status);

-- 5. Run Stages
CREATE TABLE IF NOT EXISTS run_stages (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    stage_id VARCHAR(100) NOT NULL,
    stage_order INT NOT NULL,
    label VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'pending', 'running', 'completed', 'failed'
    message TEXT,
    elapsed_seconds INT NOT NULL DEFAULT 0,
    has_parallel_agents BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_run_stage UNIQUE (run_id, stage_id)
);

CREATE INDEX IF NOT EXISTS idx_run_stages_run_order ON run_stages(run_id, stage_order ASC);

-- 6. Parallel Agent Execution Records
CREATE TABLE IF NOT EXISTS parallel_agent_executions (
    id VARCHAR(64) PRIMARY KEY,
    run_stage_id VARCHAR(64) NOT NULL REFERENCES run_stages(id) ON DELETE CASCADE,
    agent_name VARCHAR(100) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Locked Plans (Lab 1 / Lab 3)
CREATE TABLE IF NOT EXISTS locked_plans (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE REFERENCES analysis_runs(id) ON DELETE CASCADE,
    course_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    degree_level VARCHAR(20) NOT NULL,
    course_nature VARCHAR(50) NOT NULL,
    target_semester INT NOT NULL,
    contact_distribution VARCHAR(50) NOT NULL,
    source_scope TEXT,
    standards TEXT,
    validation_constraints TEXT,
    is_locked BOOLEAN NOT NULL DEFAULT TRUE,
    locked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Course Proposals (Recommendation Stage: 4 Alternatives)
CREATE TABLE IF NOT EXISTS course_proposals (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL, -- 'ALT-1', 'ALT-2', 'ALT-3', 'ALT-4'
    title VARCHAR(255) NOT NULL,
    strategy_name VARCHAR(255) NOT NULL,
    tagline VARCHAR(255) NOT NULL,
    curriculum_philosophy TEXT NOT NULL,
    credits INT NOT NULL,
    lecture_hours INT NOT NULL,
    tutorial_hours INT NOT NULL,
    practical_hours INT NOT NULL,
    total_hours INT NOT NULL,
    key_highlights_json TEXT,
    evidence_coverage_percent INT NOT NULL DEFAULT 0,
    is_recommended_strategy BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_proposals_run_code ON course_proposals(run_id, code);

-- 9. Evidence Records (Grounding - Lab 9)
CREATE TABLE IF NOT EXISTS evidence_records (
    id VARCHAR(64) PRIMARY KEY,
    proposal_id VARCHAR(64) REFERENCES course_proposals(id) ON DELETE CASCADE,
    run_id VARCHAR(64) NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    publisher VARCHAR(255) NOT NULL,
    publication_year INT,
    clause_or_section VARCHAR(255),
    url TEXT,
    verified_claim TEXT NOT NULL,
    retrieval_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evidence_proposal ON evidence_records(proposal_id);

-- 10. Feedback & Faculty Preference (Human Gate - Lab 12)
CREATE TABLE IF NOT EXISTS feedback_records (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    selected_alternative_id VARCHAR(64) NOT NULL,
    rejected_alternative_ids_json TEXT,
    faculty_name VARCHAR(255),
    faculty_id VARCHAR(100),
    rationale TEXT NOT NULL,
    suggested_modifications TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. Final Published Plans (Lab 14 Governed Output)
CREATE TABLE IF NOT EXISTS final_course_plans (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    course_code VARCHAR(50) NOT NULL,
    course_title VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    semester INT NOT NULL,
    credits INT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    audit_trail_id VARCHAR(64) NOT NULL,
    content_json TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'GOVERNED_DRAFT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. Audit Events (Governance & Traceability - Lab 6 / Lab 12)
CREATE TABLE IF NOT EXISTS audit_events (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64),
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    safe_summary TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_events_run_action ON audit_events(run_id, action);
CREATE INDEX IF NOT EXISTS idx_audit_events_created ON audit_events(created_at DESC);

