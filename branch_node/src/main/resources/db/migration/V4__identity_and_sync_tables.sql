CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS common;
CREATE SCHEMA IF NOT EXISTS sync;

CREATE TABLE identity.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE common.audit_log (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES identity.users(id),
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sync.sync_queue (
    id SERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    action VARCHAR(50) NOT NULL,
    payload JSONB,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed defaults: Password is "password". Storing using Spring Security's {noop} delegating approach initially or bcrypt prefix format
INSERT INTO identity.users (username, password_hash, role) VALUES ('manager', '{noop}password', 'MANAGER');
INSERT INTO identity.users (username, password_hash, role) VALUES ('cashier', '{noop}password', 'CASHIER');
INSERT INTO identity.users (username, password_hash, role) VALUES ('waiter', '{noop}password', 'WAITER');
