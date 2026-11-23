-- ========================================================
-- Curriculum Intelligence System - Academic Domain Model
-- Migration V3__academic_domain_model.sql
-- ========================================================

-- 1. Institutions
CREATE TABLE IF NOT EXISTS institutions (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    country VARCHAR(100),
    website VARCHAR(255),
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_institutions_code ON institutions(code);

-- 2. Departments
CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(64) PRIMARY KEY,
    institution_id VARCHAR(64) NOT NULL REFERENCES institutions(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_department_inst_code UNIQUE (institution_id, code)
);

CREATE INDEX IF NOT EXISTS idx_departments_inst ON departments(institution_id);

-- 3. Academic Programmes
CREATE TABLE IF NOT EXISTS programmes (
    id VARCHAR(64) PRIMARY KEY,
    department_id VARCHAR(64) NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    degree_level VARCHAR(50) NOT NULL DEFAULT 'UG', -- 'UG', 'PG', 'PHD', 'DIPLOMA'
    duration_years INT NOT NULL DEFAULT 4,
    total_credits_required INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_programme_dept_code UNIQUE (department_id, code)
);

CREATE INDEX IF NOT EXISTS idx_programmes_dept ON programmes(department_id);

-- 4. Curriculum Versions (Lifecycle Governed & Versioned)
CREATE TABLE IF NOT EXISTS curriculum_versions (
    id VARCHAR(64) PRIMARY KEY,
    programme_id VARCHAR(64) NOT NULL REFERENCES programmes(id) ON DELETE RESTRICT,
    version_code VARCHAR(50) NOT NULL,
    effective_year INT NOT NULL,
    total_credits INT,
    lifecycle_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- 'DRAFT', 'UNDER_REVIEW', 'APPROVED', 'ARCHIVED'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    approval_metadata_json TEXT,
    approved_by VARCHAR(64) REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(64) REFERENCES users(id),
    updated_by VARCHAR(64) REFERENCES users(id),
    source_provenance VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_curriculum_version UNIQUE (programme_id, version_code)
);

CREATE INDEX IF NOT EXISTS idx_curriculum_versions_prog ON curriculum_versions(programme_id);
CREATE INDEX IF NOT EXISTS idx_curriculum_versions_status ON curriculum_versions(lifecycle_status);

-- 5. Semesters
CREATE TABLE IF NOT EXISTS semesters (
    id VARCHAR(64) PRIMARY KEY,
    curriculum_version_id VARCHAR(64) NOT NULL REFERENCES curriculum_versions(id) ON DELETE CASCADE,
    semester_number INT NOT NULL,
    label VARCHAR(50) NOT NULL,
    credits_target INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_curriculum_semester UNIQUE (curriculum_version_id, semester_number)
);

CREATE INDEX IF NOT EXISTS idx_semesters_curriculum ON semesters(curriculum_version_id);

-- 6. Courses (Canonical Reusable Course Catalog)
CREATE TABLE IF NOT EXISTS courses (
    id VARCHAR(64) PRIMARY KEY,
    department_id VARCHAR(64) NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_type VARCHAR(50) NOT NULL DEFAULT 'CORE', -- 'CORE', 'PROFESSIONAL_ELECTIVE', 'OPEN_ELECTIVE', 'LAB', 'PROJECT'
    default_credits INT,
    lecture_hours INT NOT NULL DEFAULT 0,
    tutorial_hours INT NOT NULL DEFAULT 0,
    practical_hours INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_dept_code UNIQUE (department_id, code)
);

CREATE INDEX IF NOT EXISTS idx_courses_dept ON courses(department_id);
CREATE INDEX IF NOT EXISTS idx_courses_code ON courses(code);

-- 7. Curriculum Courses (Offering within a specific curriculum version & semester)
CREATE TABLE IF NOT EXISTS curriculum_courses (
    id VARCHAR(64) PRIMARY KEY,
    curriculum_version_id VARCHAR(64) NOT NULL REFERENCES curriculum_versions(id) ON DELETE CASCADE,
    semester_id VARCHAR(64) NOT NULL REFERENCES semesters(id) ON DELETE CASCADE,
    course_id VARCHAR(64) NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    course_code VARCHAR(50) NOT NULL,
    course_title VARCHAR(255) NOT NULL,
    course_type VARCHAR(50) NOT NULL,
    credits INT NOT NULL,
    lecture_hours INT NOT NULL DEFAULT 0,
    tutorial_hours INT NOT NULL DEFAULT 0,
    practical_hours INT NOT NULL DEFAULT 0,
    is_elective BOOLEAN NOT NULL DEFAULT FALSE,
    elective_group VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_curr_sem_course UNIQUE (curriculum_version_id, semester_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_curr_courses_version ON curriculum_courses(curriculum_version_id);
CREATE INDEX IF NOT EXISTS idx_curr_courses_semester ON curriculum_courses(semester_id);
CREATE INDEX IF NOT EXISTS idx_curr_courses_course ON curriculum_courses(course_id);

-- 8. Programme Outcomes (PO / PSO)
CREATE TABLE IF NOT EXISTS programme_outcomes (
    id VARCHAR(64) PRIMARY KEY,
    programme_id VARCHAR(64) NOT NULL REFERENCES programmes(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL, -- 'PO1', 'PO2', ..., 'PSO1'
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    outcome_type VARCHAR(50) NOT NULL DEFAULT 'PO', -- 'PO', 'PSO'
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_programme_po_code UNIQUE (programme_id, code)
);

CREATE INDEX IF NOT EXISTS idx_programme_outcomes_prog ON programme_outcomes(programme_id);

-- 9. Course Outcomes (CO)
CREATE TABLE IF NOT EXISTS course_outcomes (
    id VARCHAR(64) PRIMARY KEY,
    course_id VARCHAR(64) NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL, -- 'CO1', 'CO2', 'CO3'
    description TEXT NOT NULL,
    bloom_level VARCHAR(50), -- 'L1_REMEMBER', 'L2_UNDERSTAND', 'L3_APPLY', 'L4_ANALYZE', 'L5_EVALUATE', 'L6_CREATE'
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_co_code UNIQUE (course_id, code)
);

CREATE INDEX IF NOT EXISTS idx_course_outcomes_course ON course_outcomes(course_id);

-- 10. CO-PO Mappings
CREATE TABLE IF NOT EXISTS co_po_mappings (
    id VARCHAR(64) PRIMARY KEY,
    course_outcome_id VARCHAR(64) NOT NULL REFERENCES course_outcomes(id) ON DELETE CASCADE,
    programme_outcome_id VARCHAR(64) NOT NULL REFERENCES programme_outcomes(id) ON DELETE CASCADE,
    mapping_level INT NOT NULL, -- 1: Low, 2: Medium, 3: High
    justification TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_co_po_mapping UNIQUE (course_outcome_id, programme_outcome_id),
    CONSTRAINT ck_mapping_level CHECK (mapping_level IN (1, 2, 3))
);

CREATE INDEX IF NOT EXISTS idx_co_po_mapping_co ON co_po_mappings(course_outcome_id);
CREATE INDEX IF NOT EXISTS idx_co_po_mapping_po ON co_po_mappings(programme_outcome_id);

-- 11. Course Prerequisites
CREATE TABLE IF NOT EXISTS course_prerequisites (
    id VARCHAR(64) PRIMARY KEY,
    course_id VARCHAR(64) NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id VARCHAR(64) NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    requirement_type VARCHAR(50) NOT NULL DEFAULT 'HARD_PREREQUISITE', -- 'HARD_PREREQUISITE', 'RECOMMENDED', 'COREQUISITE'
    minimum_grade VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_prereq UNIQUE (course_id, prerequisite_course_id),
    CONSTRAINT ck_prereq_no_self CHECK (course_id <> prerequisite_course_id)
);

CREATE INDEX IF NOT EXISTS idx_prereqs_course ON course_prerequisites(course_id);
CREATE INDEX IF NOT EXISTS idx_prereqs_target ON course_prerequisites(prerequisite_course_id);

-- 12. Academic Import Batches
CREATE TABLE IF NOT EXISTS academic_import_batches (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id),
    institution_id VARCHAR(64) REFERENCES institutions(id),
    format VARCHAR(20) NOT NULL, -- 'JSON', 'CSV'
    status VARCHAR(50) NOT NULL DEFAULT 'PREVIEW', -- 'PREVIEW', 'COMMITTED', 'REJECTED', 'FAILED'
    total_records INT NOT NULL DEFAULT 0,
    valid_records INT NOT NULL DEFAULT 0,
    error_records INT NOT NULL DEFAULT 0,
    preview_data_json TEXT,
    validation_report_json TEXT,
    committed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_import_batches_user ON academic_import_batches(user_id);
CREATE INDEX IF NOT EXISTS idx_import_batches_status ON academic_import_batches(status);

