INSERT INTO users (
    id,
    name,
    firstname,
    email,
    password
)
VALUES
(
    'c3aa95af-71d6-4735-b5f0-c020f17549e1',
    'Admin',
    'Test',
    'test@test.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO5C8K0WqJ8J5L7YxP8uNQ2V4h2jS9V8e'
);

INSERT INTO users (
    id,
    name,
    firstname,
    email,
    password
)
VALUES
(
    'c3aa95af-71d6-4735-b5f0-c020f17549e2',
    'Viewer',
    'Test',
    'viewer@test.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO5C8K0WqJ8J5L7YxP8uNQ2V4h2jS9V8e'
);

INSERT INTO events (
    id,
    name,
    location,
    start_date,
    end_date,
    description,
    public_event
)
VALUES
(
    'c3aa95af-71d6-4735-b5f0-c020f17549e3',
    'Event Test',
    'Paris',
    '2026-01-01',
    '2026-01-01',
    'Description',
    true
);

INSERT INTO user_event_roles (
    id,
    user_id,
    event_id,
    role
)
VALUES
(
    '0d9d0f8f-3c3a-43db-ae54-94b57d5b5d42',
    'c3aa95af-71d6-4735-b5f0-c020f17549e1',
    'c3aa95af-71d6-4735-b5f0-c020f17549e3',
    'CREATOR'
);