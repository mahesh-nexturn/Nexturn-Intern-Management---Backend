-- Backfill login accounts for mentors that were created before mentor creation
-- auto-provisioned a user account. Default login password: nexturn@123
-- (mentors should change it after first login).

-- 1) Link mentors whose email already matches an existing user account.
UPDATE mentors m
SET user_id = u.id
FROM users u
WHERE m.user_id IS NULL AND u.email = m.email;

-- 2) Create new MENTOR user accounts for remaining mentors without a login.
INSERT INTO users (email, password_hash, role, name)
SELECT m.email, '$2b$10$ZPItrYag7c/xmjH9Wd9ci.eAWKPhJpevazFV2j53uJtQx9WN8XrRy', 'MENTOR', m.name
FROM mentors m
WHERE m.user_id IS NULL
ON CONFLICT (email) DO NOTHING;

-- 3) Link the newly created accounts back to their mentors.
UPDATE mentors m
SET user_id = u.id
FROM users u
WHERE m.user_id IS NULL AND u.email = m.email;
