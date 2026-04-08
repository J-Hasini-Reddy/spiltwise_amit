DROP TABLE IF EXISTS users;
DROP TYPE IF EXISTS user_status;

CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(254) UNIQUE,
    phone VARCHAR(32) UNIQUE,
    global_credit_limit DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (global_credit_limit >= 0),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    status user_status
);
