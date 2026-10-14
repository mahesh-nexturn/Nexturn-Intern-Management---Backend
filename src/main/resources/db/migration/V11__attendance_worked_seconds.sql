ALTER TABLE attendance ADD COLUMN worked_seconds BIGINT NOT NULL DEFAULT 0;

UPDATE attendance
SET worked_seconds = GREATEST(0, EXTRACT(EPOCH FROM (check_out_time - check_in_time))::BIGINT)
WHERE check_in_time IS NOT NULL AND check_out_time IS NOT NULL;
