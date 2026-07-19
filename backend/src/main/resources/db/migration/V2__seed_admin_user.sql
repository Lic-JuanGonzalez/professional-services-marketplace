-- Seeds one ADMIN account so /test (admin-gated tooling) has a way in on a fresh deploy.
-- The API refuses self-registration as ADMIN by design, so this is the only route to one.
-- Password hash below is a real bcrypt(12) hash of "Admin123!" produced via the app's own
-- register endpoint, not hand-rolled — change the password after first login in real deployments.
INSERT INTO users (full_name, email, password, role, active)
VALUES (
    'Admin',
    'admin@marketplace.local',
    '$2a$12$Q64HC3jSXaUoC9LisFfwvebNxFJNbktCDfeRrNjr1b1b6/4iOeFLG',
    'ADMIN',
    TRUE
);
