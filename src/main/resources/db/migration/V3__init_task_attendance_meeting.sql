CREATE TABLE tasks (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  assigned_to_intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  priority VARCHAR(10) NOT NULL CHECK (priority IN ('High','Medium','Low')),
  due_date DATE,
  status VARCHAR(20) NOT NULL CHECK (status IN ('Pending','In_Progress','Completed')),
  progress SMALLINT NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_assigned_to_intern_id ON tasks(assigned_to_intern_id);

CREATE TABLE attendance (
  id BIGSERIAL PRIMARY KEY,
  intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  attendance_date DATE NOT NULL,
  status VARCHAR(10) NOT NULL CHECK (status IN ('Present','Absent','Leave','Holiday','WFH')),
  UNIQUE(intern_id, attendance_date)
);

CREATE INDEX idx_attendance_intern_id ON attendance(intern_id);

CREATE TABLE meetings (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  agenda TEXT,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  intern_id BIGINT REFERENCES interns(id) ON DELETE CASCADE,
  meeting_date DATE NOT NULL,
  meeting_time TIME NOT NULL,
  status VARCHAR(20) NOT NULL CHECK (status IN ('Scheduled','Completed','Cancelled'))
);

CREATE INDEX idx_meetings_intern_id ON meetings(intern_id);
