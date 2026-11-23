-- ========================================================
-- Curriculum Intelligence System - Flyway Migration V8
-- Phase 2.3.3 Corrective: Vector Extension Forward Compatibility & Model-Versioned Schema
-- Target Model: BAAI/bge-small-en-v1.5 (384 dimensions)
-- ========================================================

-- 1. Conditionally enable pgvector extension if prebuilt extension package is available
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'vector') THEN
        CREATE EXTENSION IF NOT EXISTS vector;
    END IF;
END $$;

-- 2. Add vector(384) column and HNSW index if vector data type exists in PostgreSQL catalog
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vector') THEN
        -- Add vector(384) column if absent
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'evidence_chunk_embeddings' AND column_name = 'embedding_vec'
        ) THEN
            EXECUTE 'ALTER TABLE evidence_chunk_embeddings ADD COLUMN embedding_vec vector(384)';
        END IF;

        -- Create HNSW cosine index for the native vector column
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_chunk_embeddings_hnsw_vec 
                 ON evidence_chunk_embeddings USING hnsw (embedding_vec vector_cosine_ops)';
    END IF;
END $$;

-- 3. Composite performance index for model-versioned retrieval
CREATE INDEX IF NOT EXISTS idx_chunk_emb_model_status 
ON evidence_chunk_embeddings (model_name, status);

