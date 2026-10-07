-- Seed a default ADMIN user for local development.
-- Login: admin@nexturn.com / Admin@123
INSERT INTO users (email, password_hash, role, name)
VALUES ('admin@nexturn.com', '$2b$10$A1V9pCW9A0DQ5468v7Cdm.hj2.EZTpJoJEHMJEbK13RW2s0u81z8y', 'ADMIN', 'System Admin');
