CREATE TABLE trainings (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  assigned_to_intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  start_date DATE,
  end_date DATE,
  status VARCHAR(20) NOT NULL CHECK (status IN ('Not_Started','In_Progress','Completed')),
  progress SMALLINT NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100)
);

CREATE INDEX idx_trainings_assigned_to_intern_id ON trainings(assigned_to_intern_id);

CREATE TABLE evaluations (
  id BIGSERIAL PRIMARY KEY,
  intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  technical_rating SMALLINT NOT NULL CHECK (technical_rating BETWEEN 1 AND 5),
  communication_rating SMALLINT NOT NULL CHECK (communication_rating BETWEEN 1 AND 5),
  problem_solving_rating SMALLINT NOT NULL CHECK (problem_solving_rating BETWEEN 1 AND 5),
  overall_rating SMALLINT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
  feedback TEXT
);

CREATE INDEX idx_evaluations_intern_id ON evaluations(intern_id);

CREATE TABLE documents (
  id BIGSERIAL PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  document_type VARCHAR(20) NOT NULL CHECK (document_type IN ('Resume','Certificate','Offer_Letter','Report','Other')),
  uploaded_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  upload_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_uploaded_by_user_id ON documents(uploaded_by_user_id);

CREATE TABLE certificates (
  id BIGSERIAL PRIMARY KEY,
  intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  certificate_name VARCHAR(255) NOT NULL,
  issued_by VARCHAR(255),
  issue_date DATE,
  expiry_date DATE,
  status VARCHAR(10) NOT NULL CHECK (status IN ('Active','Expired'))
);

CREATE INDEX idx_certificates_intern_id ON certificates(intern_id);

CREATE TABLE announcements (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  publish_date DATE,
  expiry_date DATE,
  priority VARCHAR(10) NOT NULL CHECK (priority IN ('High','Medium','Low')),
  target_audience VARCHAR(10) NOT NULL CHECK (target_audience IN ('All','HR','Mentor','Intern'))
);

CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  message TEXT,
  recipient VARCHAR(10) NOT NULL CHECK (recipient IN ('HR','Mentor','Intern','All')),
  created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  notification_date TIMESTAMP NOT NULL DEFAULT now(),
  status VARCHAR(10) NOT NULL CHECK (status IN ('Unread','Read'))
);

CREATE TABLE ppo (
  id BIGSERIAL PRIMARY KEY,
  intern_id BIGINT NOT NULL REFERENCES interns(id) ON DELETE CASCADE,
  mentor_id BIGINT REFERENCES mentors(id) ON DELETE SET NULL,
  attendance_pct DOUBLE PRECISION NOT NULL DEFAULT 0,
  training_completion_pct DOUBLE PRECISION NOT NULL DEFAULT 0,
  technical_score DOUBLE PRECISION NOT NULL DEFAULT 0,
  communication_score DOUBLE PRECISION NOT NULL DEFAULT 0,
  overall_score DOUBLE PRECISION NOT NULL DEFAULT 0,
  mentor_recommendation VARCHAR(5) CHECK (mentor_recommendation IN ('Yes','No')),
  hr_recommendation VARCHAR(5) CHECK (hr_recommendation IN ('Yes','No')),
  status VARCHAR(20) NOT NULL CHECK (status IN ('Eligible','Not_Eligible','Offered')),
  UNIQUE(intern_id)
);

CREATE TABLE feedback (
  id BIGSERIAL PRIMARY KEY,
  from_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  subject VARCHAR(255),
  message TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_settings (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
  push_notifications BOOLEAN NOT NULL DEFAULT TRUE,
  theme VARCHAR(20) DEFAULT 'light',
  phone VARCHAR(30),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
