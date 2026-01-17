-- Setup Auth DB for Postgres
-- Run this with: psql -U postgres -f setup_auth_db.sql

-- 1. Create Database (Manually run this first if needed, as you cannot create DB inside a transaction block easily)
-- CREATE DATABASE auth_db;
-- \c auth_db;

-- 2. Drop Tables if exist (Clean Slate)
DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS ROLES CASCADE;

-- 3. Create ROLES Table
CREATE TABLE ROLES (
    RoleID SERIAL PRIMARY KEY,
    RoleName VARCHAR(50) NOT NULL UNIQUE
);

-- 4. Create USERS Table
CREATE TABLE USERS (
    UserID SERIAL PRIMARY KEY,
    Email VARCHAR(100) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    FirstName VARCHAR(50),
    LastName VARCHAR(50),
    Phone VARCHAR(20),
    Address TEXT,
    RoleID INT NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role FOREIGN KEY (RoleID) REFERENCES ROLES(RoleID)
);

-- 5. Insert Default Data
INSERT INTO ROLES (RoleName) VALUES ('ADMIN'), ('DESIGNER'), ('CUSTOMER');

-- Default Admin (Password: admin123 -> Need hash, but for now plain or placeholder)
-- In a real scenario, use BCrypt. Here we assume simple hash or plain for compatibility with existing logic.
INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID)
VALUES ('admin@dott.com', 'admin123', 'Super', 'Admin', 1);

INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID)
VALUES ('user@dott.com', 'user123', 'John', 'Doe', 3);
