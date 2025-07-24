-- Seed data for local development

-- Insert a test user for development
-- Password is '000000' hashed with bcrypt
INSERT INTO users (email, password_hash) 
VALUES ('test@example.com', 'bcrypt+sha512$88187dac32bcb6cd9b22bb05b73a40e0$10$8b4591516207fbbbc34a4643ea49a28766b304b902df1aa0')
ON CONFLICT (email) DO NOTHING;

-- Note: The above hash was generated with bcrypt for password '000000'
-- For production, always use proper password generation and never commit real passwords.
