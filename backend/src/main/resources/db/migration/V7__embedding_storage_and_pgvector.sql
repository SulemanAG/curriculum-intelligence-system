-- ========================================================
-- Curriculum Intelligence System - Flyway Migration V7
-- Phase 2.3.3: Embedding Generation & Hybrid Retrieval Foundation
-- Model: BAAI/bge-small-en-v1.5 (384 dimensions)
-- ========================================================

-- 1. Conditionally enable pgvector extension if available in the PostgreSQL installation
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'vector') THEN
        CREATE EXTENSION IF NOT EXISTS vector;
    END IF;
END $$;

-- 2. Pure PostgreSQL array cosine distance function (used natively when pgvector extension binary is omitted)
CREATE OR REPLACE FUNCTION cosine_distance_array(a real[], b real[])
RETURNS double precision AS $$
DECLARE
    dot_product double precision := 0.0;
    norm_a double precision := 0.0;
    norm_b double precision := 0.0;
    len integer;
    i integer;
BEGIN
    IF a IS NULL OR b IS NULL THEN
        RETURN 1.0;
    END IF;
    len := array_length(a, 1);
    IF len IS NULL OR len != array_length(b, 1) THEN
        RETURN 1.0;
    END IF;
    FOR i IN 1..len LOOP
        dot_product := dot_product + (a[i] * b[i]);
        norm_a := norm_a + (a[i] * a[i]);
        norm_b := norm_b + (b[i] * b[i]);
    END LOOP;
    IF norm_a = 0.0 OR norm_b = 0.0 THEN
        RETURN 1.0;
    END IF;
    RETURN 1.0 - (dot_product / (sqrt(norm_a) * sqrt(norm_b)));
END;
$$ LANGUAGE plpgsql IMMUTABLE PARALLEL SAFE;

-- 3. Create evidence_chunk_embeddings table supporting pgvector or native real[] array storage
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vector') THEN
        EXECUTE 'CREATE TABLE IF NOT EXISTS evidence_chunk_embeddings (
            id VARCHAR(64) PRIMARY KEY,
            chunk_id VARCHAR(64) NOT NULL REFERENCES evidence_chunks(id) ON DELETE CASCADE,
            model_name VARCHAR(100) NOT NULL,
            dimension INT NOT NULL DEFAULT 384,
            embedding vector(384),
            embedding_json TEXT,
            status VARCHAR(50) NOT NULL DEFAULT ''PENDING'',
            error_message TEXT,
            retry_count INT NOT NULL DEFAULT 0,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT uk_chunk_model UNIQUE (chunk_id, model_name)
        )';
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_chunk_embeddings_hnsw ON evidence_chunk_embeddings USING hnsw (embedding vector_cosine_ops)';
    ELSE
        EXECUTE 'CREATE TABLE IF NOT EXISTS evidence_chunk_embeddings (
            id VARCHAR(64) PRIMARY KEY,
            chunk_id VARCHAR(64) NOT NULL REFERENCES evidence_chunks(id) ON DELETE CASCADE,
            model_name VARCHAR(100) NOT NULL,
            dimension INT NOT NULL DEFAULT 384,
            embedding real[],
            embedding_json TEXT,
            status VARCHAR(50) NOT NULL DEFAULT ''PENDING'',
            error_message TEXT,
            retry_count INT NOT NULL DEFAULT 0,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT uk_chunk_model UNIQUE (chunk_id, model_name)
        )';
    END IF;
END $$;

-- 4. Auxiliary indexes on embedding status, foreign key, and model name
CREATE INDEX IF NOT EXISTS idx_chunk_embeddings_chunk ON evidence_chunk_embeddings(chunk_id);
CREATE INDEX IF NOT EXISTS idx_chunk_embeddings_status ON evidence_chunk_embeddings(status);
CREATE INDEX IF NOT EXISTS idx_chunk_embeddings_model ON evidence_chunk_embeddings(model_name);

-- 5. Add document-level embedding status column to extracted_documents if absent
ALTER TABLE extracted_documents ADD COLUMN IF NOT EXISTS embedding_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
CREATE INDEX IF NOT EXISTS idx_extracted_docs_emb_status ON extracted_documents(embedding_status);

