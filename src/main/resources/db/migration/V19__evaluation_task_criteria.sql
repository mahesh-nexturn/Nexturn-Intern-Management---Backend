-- Evaluations are now task-based, rated 1-5 on five criteria. Legacy columns are kept but optional.
ALTER TABLE evaluations
    ADD COLUMN requirement_understanding SMALLINT CHECK (requirement_understanding BETWEEN 1 AND 5),
    ADD COLUMN technical_knowledge SMALLINT CHECK (technical_knowledge BETWEEN 1 AND 5),
    ADD COLUMN work_quality SMALLINT CHECK (work_quality BETWEEN 1 AND 5),
    ADD COLUMN task_completion SMALLINT CHECK (task_completion BETWEEN 1 AND 5),
    ADD COLUMN task_demonstration SMALLINT CHECK (task_demonstration BETWEEN 1 AND 5),
    ADD COLUMN task_id BIGINT REFERENCES tasks(id) ON DELETE SET NULL;

ALTER TABLE evaluations
    ALTER COLUMN technical_rating DROP NOT NULL,
    ALTER COLUMN communication_rating DROP NOT NULL,
    ALTER COLUMN problem_solving_rating DROP NOT NULL;

CREATE INDEX idx_evaluations_task_id ON evaluations(task_id);
