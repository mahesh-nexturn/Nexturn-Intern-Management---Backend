-- HR role: HR users own a set of mentors and interns (hr_id references the HR's user account).
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN','HR','MENTOR','INTERN'));

ALTER TABLE mentors ADD COLUMN hr_id BIGINT REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE interns ADD COLUMN hr_id BIGINT REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_mentors_hr_id ON mentors(hr_id);
CREATE INDEX idx_interns_hr_id ON interns(hr_id);
