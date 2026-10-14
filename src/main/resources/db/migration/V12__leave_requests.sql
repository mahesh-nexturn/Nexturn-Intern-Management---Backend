CREATE TABLE leave_requests (
  id BIGSERIAL PRIMARY KEY,
  intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  hr_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  leave_type VARCHAR(40) NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  team_email VARCHAR(255),
  reason TEXT,
  status VARCHAR(10) NOT NULL CHECK (status IN ('Pending','Approved','Rejected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  decided_at TIMESTAMPTZ
);

CREATE INDEX idx_leave_requests_intern ON leave_requests(intern_id);
CREATE INDEX idx_leave_requests_hr ON leave_requests(hr_id);

-- A notification with a target user is only visible to that user.
ALTER TABLE notifications ADD COLUMN target_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;
