ALTER TABLE interns ADD COLUMN end_date DATE;

UPDATE interns SET end_date = make_date(EXTRACT(YEAR FROM COALESCE(created_at, now()))::int, 12, 31)
WHERE end_date IS NULL;

UPDATE interns SET status = 'Completed' WHERE status = 'Active' AND end_date < CURRENT_DATE;
