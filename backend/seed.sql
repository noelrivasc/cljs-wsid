-- Seed data for local development

-- Insert a test user for development
-- Password is '000000' hashed with bcrypt
INSERT INTO users (email, password_hash) 
VALUES ('test@example.com', 'bcrypt+sha512$8d5f1258f55e1a33acffd4404af754c7$12$b3f9439e5f0af84b9b0e35dc87f388f8ea54a1ee22bf574d')
ON CONFLICT (email) DO NOTHING;

-- Note: The above hash was generated with bcrypt for password '000000'
-- For production, always use proper password generation and never commit real passwords.
