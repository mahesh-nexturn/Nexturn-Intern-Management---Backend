-- Backfill login accounts for interns that were created before intern creation
-- auto-provisioned a user account. Default login password: Inexturn@123
-- (interns should change it after first login).

-- interns.user_id is UNIQUE, so when multiple interns share the same email only one of
-- them can be linked to a user account; DISTINCT ON picks a single deterministic match
-- per user, and the NOT EXISTS guard skips users already claimed by another intern,
-- leaving duplicate-email interns unlinked for manual resolution.

-- 1) Link interns whose email already matches an existing, unclaimed user account.
WITH matched AS (
    SELECT DISTINCT ON (u.id) i.id AS intern_id, u.id AS user_id
    FROM interns i
    JOIN users u ON u.email = i.email
    WHERE i.user_id IS NULL
      AND NOT EXISTS (SELECT 1 FROM interns i2 WHERE i2.user_id = u.id)
    ORDER BY u.id, i.id
)
UPDATE interns i
SET user_id = matched.user_id
FROM matched
WHERE i.id = matched.intern_id;

-- 2) Create new INTERN user accounts for remaining interns without a login.
INSERT INTO users (email, password_hash, role, name)
SELECT DISTINCT ON (i.email) i.email, '$2b$10$yL6wv5q3W7n96pVdpaMTVOwzhyIBFyLxliDYc0uW.vBQ6Wu2GKyKu', 'INTERN', i.name
FROM interns i
WHERE i.user_id IS NULL
ORDER BY i.email, i.id
ON CONFLICT (email) DO NOTHING;

-- 3) Link the newly created accounts back to their interns.
WITH matched AS (
    SELECT DISTINCT ON (u.id) i.id AS intern_id, u.id AS user_id
    FROM interns i
    JOIN users u ON u.email = i.email
    WHERE i.user_id IS NULL
      AND NOT EXISTS (SELECT 1 FROM interns i2 WHERE i2.user_id = u.id)
    ORDER BY u.id, i.id
)
UPDATE interns i
SET user_id = matched.user_id
FROM matched
WHERE i.id = matched.intern_id;
