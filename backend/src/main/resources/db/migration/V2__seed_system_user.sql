-- ========================================================
-- Seed Initial Academic User
-- Migration V2__seed_system_user.sql
-- ========================================================

INSERT INTO users (id, username, email, full_name, role, is_active, created_at, updated_at)
VALUES (
    'usr-default-faculty',
    'faculty_member',
    'faculty@curriculumai.internal',
    'Dr. Faculty Member',
    'FACULTY',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

