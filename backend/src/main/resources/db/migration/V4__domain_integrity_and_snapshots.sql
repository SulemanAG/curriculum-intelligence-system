-- ========================================================
-- Curriculum Intelligence System - Academic Domain Integrity & Snapshots
-- Migration V4__domain_integrity_and_snapshots.sql
-- ========================================================

-- 1. Relax non-null & defaults on academic attributes so unknown values remain NULL
ALTER TABLE programmes ALTER COLUMN degree_level DROP DEFAULT;
ALTER TABLE programmes ALTER COLUMN degree_level DROP NOT NULL;
ALTER TABLE programmes ALTER COLUMN duration_years DROP DEFAULT;
ALTER TABLE programmes ALTER COLUMN duration_years DROP NOT NULL;

ALTER TABLE courses ALTER COLUMN course_type DROP DEFAULT;
ALTER TABLE courses ALTER COLUMN course_type DROP NOT NULL;
ALTER TABLE courses ALTER COLUMN lecture_hours DROP DEFAULT;
ALTER TABLE courses ALTER COLUMN lecture_hours DROP NOT NULL;
ALTER TABLE courses ALTER COLUMN tutorial_hours DROP DEFAULT;
ALTER TABLE courses ALTER COLUMN tutorial_hours DROP NOT NULL;
ALTER TABLE courses ALTER COLUMN practical_hours DROP DEFAULT;
ALTER TABLE courses ALTER COLUMN practical_hours DROP NOT NULL;

ALTER TABLE curriculum_courses ALTER COLUMN credits DROP NOT NULL;
ALTER TABLE curriculum_courses ALTER COLUMN course_type DROP NOT NULL;
ALTER TABLE curriculum_courses ALTER COLUMN lecture_hours DROP DEFAULT;
ALTER TABLE curriculum_courses ALTER COLUMN lecture_hours DROP NOT NULL;
ALTER TABLE curriculum_courses ALTER COLUMN tutorial_hours DROP DEFAULT;
ALTER TABLE curriculum_courses ALTER COLUMN tutorial_hours DROP NOT NULL;
ALTER TABLE curriculum_courses ALTER COLUMN practical_hours DROP DEFAULT;
ALTER TABLE curriculum_courses ALTER COLUMN practical_hours DROP NOT NULL;

-- 2. Curriculum-Semester Referential Integrity
-- Ensure a curriculum course cannot reference a semester belonging to another curriculum version.
ALTER TABLE semesters ADD CONSTRAINT uk_semester_id_curriculum UNIQUE (id, curriculum_version_id);

ALTER TABLE curriculum_courses ADD CONSTRAINT fk_curr_course_semester_curriculum 
    FOREIGN KEY (semester_id, curriculum_version_id) 
    REFERENCES semesters(id, curriculum_version_id) ON DELETE CASCADE;

-- 3. Historical Outcome Snapshots
-- Frozen, immutable snapshots of POs, COs, and CO-PO mappings created at curriculum approval time.
CREATE TABLE IF NOT EXISTS curriculum_outcome_snapshots (
    id VARCHAR(64) PRIMARY KEY,
    curriculum_version_id VARCHAR(64) NOT NULL REFERENCES curriculum_versions(id) ON DELETE CASCADE,
    snapshot_type VARCHAR(50) NOT NULL, -- 'COURSE_OUTCOME', 'PROGRAMME_OUTCOME', 'CO_PO_MAPPING'
    source_entity_id VARCHAR(64) NOT NULL,
    course_id VARCHAR(64) REFERENCES courses(id) ON DELETE SET NULL,
    course_code VARCHAR(50),
    outcome_code VARCHAR(20) NOT NULL,
    title VARCHAR(255),
    description TEXT NOT NULL,
    bloom_level VARCHAR(50),
    target_outcome_code VARCHAR(20),
    mapping_level INT,
    justification TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_curr_snapshots_version ON curriculum_outcome_snapshots(curriculum_version_id);
CREATE INDEX IF NOT EXISTS idx_curr_snapshots_course ON curriculum_outcome_snapshots(curriculum_version_id, course_id);

-- 4. Seed Academic Leadership Users for Approvals
INSERT INTO users (id, username, email, full_name, role, is_active, created_at, updated_at)
VALUES 
    ('usr-academic-admin', 'academic_admin', 'admin@curriculumai.internal', 'Academic System Administrator', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('usr-academic-dean', 'academic_dean', 'dean@curriculumai.internal', 'Dr. Academic Dean', 'DEAN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
