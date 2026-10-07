-- V4's original bcrypt hash did not actually match "Admin@123" (verified failing
-- bcrypt comparison), which caused every login attempt to return 401 Unauthorized.
-- This migration repairs the hash on databases where V4 already ran.
-- Login: admin@nexturn.com / Admin@123
UPDATE users
SET password_hash = '$2b$10$CPMH4bBWTe2Gca3ZuaWclenEZUcVAPNSoGKtut5iPdMl5hCICnmu.'
WHERE email = 'admin@nexturn.com';
