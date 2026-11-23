-- ========================================================
-- Curriculum Intelligence System - Outcome Snapshot Immutability Trigger
-- Migration V5__snapshot_immutability_trigger.sql
-- ========================================================

-- Function to reject any UPDATE or DELETE operations on approved outcome snapshots
CREATE OR REPLACE FUNCTION prevent_outcome_snapshot_mutation()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        RAISE EXCEPTION 'curriculum_outcome_snapshots is an immutable audit table: UPDATE operations are strictly prohibited.'
            USING ERRCODE = 'integrity_constraint_violation';
    ELSIF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'curriculum_outcome_snapshots is an immutable audit table: DELETE operations are strictly prohibited.'
            USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger enforcing append-only immutability at the PostgreSQL database engine level
DROP TRIGGER IF EXISTS trg_snapshot_immutable_mutation ON curriculum_outcome_snapshots;
CREATE TRIGGER trg_snapshot_immutable_mutation
BEFORE UPDATE OR DELETE ON curriculum_outcome_snapshots
FOR EACH ROW
EXECUTE FUNCTION prevent_outcome_snapshot_mutation();

