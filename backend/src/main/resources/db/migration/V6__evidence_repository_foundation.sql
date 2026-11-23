-- ========================================================
-- Curriculum Intelligence System - Flyway Migration V6
-- Phase 2.3.1: Evidence Repository & Research Source Ingestion Foundation
-- ========================================================

-- 1. Research Sources (Source Identity)
CREATE TABLE IF NOT EXISTS research_sources (
    id VARCHAR(64) PRIMARY KEY,
    canonical_url TEXT NOT NULL UNIQUE,
    source_type VARCHAR(50) NOT NULL,
    publisher VARCHAR(255),
    title VARCHAR(500),
    domain VARCHAR(255),
    source_classification VARCHAR(100),
    institution_id VARCHAR(64) REFERENCES institutions(id) ON DELETE SET NULL,
    department_id VARCHAR(64) REFERENCES departments(id) ON DELETE SET NULL,
    programme_id VARCHAR(64) REFERENCES programmes(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(64) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_research_sources_type ON research_sources(source_type);
CREATE INDEX IF NOT EXISTS idx_research_sources_publisher ON research_sources(publisher);
CREATE INDEX IF NOT EXISTS idx_research_sources_inst ON research_sources(institution_id);
CREATE INDEX IF NOT EXISTS idx_research_sources_dept ON research_sources(department_id);

-- 2. Source Retrievals (Retrieval Attempts)
CREATE TABLE IF NOT EXISTS source_retrievals (
    id VARCHAR(64) PRIMARY KEY,
    source_id VARCHAR(64) NOT NULL REFERENCES research_sources(id) ON DELETE CASCADE,
    retrieval_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    http_status INT,
    final_url TEXT,
    content_type VARCHAR(255),
    retrieval_outcome VARCHAR(50) NOT NULL,
    failure_reason TEXT,
    content_hash VARCHAR(128),
    etag VARCHAR(255),
    last_modified_header VARCHAR(255),
    elapsed_millis BIGINT NOT NULL DEFAULT 0,
    initiated_by VARCHAR(64) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_source_retrievals_source ON source_retrievals(source_id);
CREATE INDEX IF NOT EXISTS idx_source_retrievals_outcome ON source_retrievals(retrieval_outcome);

-- 3. Source Versions (Immutable Raw Content Versions)
CREATE TABLE IF NOT EXISTS source_versions (
    id VARCHAR(64) PRIMARY KEY,
    source_id VARCHAR(64) NOT NULL REFERENCES research_sources(id) ON DELETE CASCADE,
    retrieval_id VARCHAR(64) REFERENCES source_retrievals(id) ON DELETE SET NULL,
    content_hash VARCHAR(128) NOT NULL,
    version_identifier VARCHAR(64) NOT NULL,
    original_publication_date VARCHAR(50),
    retrieved_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    original_filename VARCHAR(255),
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_reference VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_source_content_hash UNIQUE (source_id, content_hash)
);

CREATE INDEX IF NOT EXISTS idx_source_versions_source ON source_versions(source_id);
CREATE INDEX IF NOT EXISTS idx_source_versions_hash ON source_versions(content_hash);

-- 4. Extracted Documents (Normalized Content Extractions)
CREATE TABLE IF NOT EXISTS extracted_documents (
    id VARCHAR(64) PRIMARY KEY,
    source_version_id VARCHAR(64) NOT NULL UNIQUE REFERENCES source_versions(id) ON DELETE CASCADE,
    extraction_status VARCHAR(50) NOT NULL,
    extractor_name VARCHAR(100) NOT NULL,
    extractor_version VARCHAR(50) NOT NULL,
    extraction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    document_title VARCHAR(500),
    detected_language VARCHAR(50),
    structured_metadata_json TEXT,
    total_pages INT,
    total_paragraphs INT,
    total_tables INT,
    total_characters INT,
    error_details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_extracted_docs_status ON extracted_documents(extraction_status);

-- 5. Evidence Chunks (Traceable Passages for Citations)
CREATE TABLE IF NOT EXISTS evidence_chunks (
    id VARCHAR(64) PRIMARY KEY,
    extracted_document_id VARCHAR(64) NOT NULL REFERENCES extracted_documents(id) ON DELETE CASCADE,
    sequence_number INT NOT NULL,
    stable_chunk_id VARCHAR(100) NOT NULL,
    text_content TEXT NOT NULL,
    page_number INT,
    section_heading VARCHAR(500),
    char_offset_start INT,
    char_offset_end INT,
    chunking_strategy VARCHAR(50) NOT NULL,
    strategy_version VARCHAR(20) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    citation_metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_doc_seq UNIQUE (extracted_document_id, sequence_number)
);

CREATE INDEX IF NOT EXISTS idx_evidence_chunks_doc ON evidence_chunks(extracted_document_id);
CREATE INDEX IF NOT EXISTS idx_evidence_chunks_hash ON evidence_chunks(content_hash);
CREATE INDEX IF NOT EXISTS idx_evidence_chunks_fts ON evidence_chunks USING gin(to_tsvector('english', text_content));

