-- =====================================================
-- Local PostgreSQL Database Setup Script
-- User Service - Splitwise on CreditLine
-- =====================================================
-- This script sets up the local development database
-- Run this manually in your local PostgreSQL instance
-- =====================================================

-- Step 1: Create Database (run this as postgres superuser)
-- =====================================================
-- Connect to postgres database first: psql -U postgres
-- Then run the following commands:

-- Create the database if it doesn't exist
DROP DATABASE IF EXISTS users;
CREATE DATABASE users;

-- Create dedicated user for the service
DROP USER IF EXISTS userservice_admin;
CREATE USER userservice_admin WITH PASSWORD 'password';

-- Grant all privileges on the database to the user
GRANT ALL PRIVILEGES ON DATABASE users TO userservice_admin;

-- Connect to the users database
\c users

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO userservice_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO userservice_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO userservice_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO userservice_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO userservice_admin;

-- =====================================================
-- Step 2: Create Tables and Types
-- =====================================================

-- Drop existing tables and types if they exist
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS user_status;

-- Create ENUM type for user status
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');

-- Create users table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(254) UNIQUE,
    phone VARCHAR(32) UNIQUE,
    global_credit_limit DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (global_credit_limit >= 0),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    status user_status DEFAULT 'ACTIVE'
);

-- =====================================================
-- Step 3: Create Indexes for Performance
-- =====================================================

-- Index on email for faster lookups
CREATE INDEX idx_users_email ON users(email);

-- Index on phone for faster lookups
CREATE INDEX idx_users_phone ON users(phone);

-- Index on status for filtering active users
CREATE INDEX idx_users_status ON users(status);

-- Index on created_at for time-based queries
CREATE INDEX idx_users_created_at ON users(created_at);

-- =====================================================
-- Step 4: Insert Sample Data (Optional)
-- =====================================================


-- =====================================================
-- Step 5: Verify Setup
-- =====================================================

-- Check if tables are created
\dt

-- Check table structure
\d users

-- Check sample data
SELECT * FROM users;

-- =====================================================
-- USAGE INSTRUCTIONS:
-- =====================================================
-- 
-- Option 1: Run entire script (as postgres superuser)
-- $ psql -U postgres -f setup_local_db.sql
--
-- Option 2: Run step by step
-- $ psql -U postgres
-- postgres=# \i setup_local_db.sql
--
-- Option 3: After database is created, connect as service user
-- $ psql -U userservice_admin -d users
--
-- Option 4: To drop and recreate everything
-- $ psql -U postgres -c "DROP DATABASE IF EXISTS users;"
-- $ psql -U postgres -f setup_local_db.sql
--
-- =====================================================
-- Update application.properties with these credentials:
-- =====================================================
-- spring.datasource.url=jdbc:postgresql://localhost:5432/users
-- spring.datasource.username=userservice_admin
-- spring.datasource.password=password
-- spring.datasource.driver-class-name=org.postgresql.Driver
-- =====================================================
