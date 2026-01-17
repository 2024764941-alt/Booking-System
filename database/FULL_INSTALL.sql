-- ========================================================
-- DOTSSTUDIO FULL INSTALLATION SCRIPT (PostgreSQL)
-- ========================================================
-- Run this with: psql -U postgres -d booking_db -f FULL_INSTALL.sql
-- (Ensure 'booking_db' is created first: CREATE DATABASE booking_db;)
-- ========================================================

-- 1. CLEANUP (Drop tables in correct order)
DROP TABLE IF EXISTS BOOKING_CATEGORIES CASCADE;
DROP TABLE IF EXISTS BOOKINGS CASCADE;
DROP TABLE IF EXISTS DESIGNER_SCHEDULE CASCADE;
DROP TABLE IF EXISTS DESIGNER_SPECIALTIES CASCADE;
DROP TABLE IF EXISTS DESIGNERS CASCADE;
DROP TABLE IF EXISTS STAFF CASCADE;
DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS ROLES CASCADE;
DROP TABLE IF EXISTS CATEGORIES CASCADE;
DROP TABLE IF EXISTS SPECIALTIES CASCADE;
DROP TABLE IF EXISTS REFERRALS CASCADE;

-- 2. AUTH & USER TABLES
CREATE TABLE ROLES (
    RoleID SERIAL PRIMARY KEY,
    RoleName VARCHAR(50) NOT NULL UNIQUE
);

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

-- 3. BOOKING SYSTEM TABLES
CREATE TABLE REFERRALS (
    ReferralID SERIAL PRIMARY KEY,
    ReferralSource VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);

CREATE TABLE SPECIALTIES (
    SpecialtyID SERIAL PRIMARY KEY,
    SpecialtyName VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);

CREATE TABLE CATEGORIES (
    CategoryID SERIAL PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);

CREATE TABLE STAFF (
    StaffID SERIAL PRIMARY KEY,
    UserID INTEGER UNIQUE REFERENCES USERS(UserID) ON DELETE CASCADE,
    StaffType VARCHAR(20) CHECK (StaffType IN ('ADMIN', 'DESIGNER', 'MANAGER')),
    Department VARCHAR(100),
    ManagerID INTEGER REFERENCES STAFF(StaffID),
    JoinDate DATE DEFAULT CURRENT_DATE,
    EndDate DATE,
    Salary DECIMAL(10,2)
);

CREATE TABLE DESIGNERS (
    DesignerID SERIAL PRIMARY KEY,
    StaffID INTEGER UNIQUE REFERENCES STAFF(StaffID) ON DELETE CASCADE,
    Bio TEXT,
    PrimarySpecialty VARCHAR(100),
    PortfolioURL VARCHAR(255),
    Status VARCHAR(20) DEFAULT 'Active' CHECK (Status IN ('Active', 'On Leave', 'Inactive')),
    EmploymentType VARCHAR(20) DEFAULT 'FULL_TIME' CHECK (EmploymentType IN ('FULL_TIME', 'PART_TIME', 'CONTRACT')),
    MaxHoursPerWeek INTEGER DEFAULT 40,
    MinHoursGuaranteed INTEGER DEFAULT 0,
    MaxSimultaneousProjects INTEGER DEFAULT 5,
    MaxBookingsPerWeek INTEGER DEFAULT 20,
    FlexibleSchedule CHAR(1) DEFAULT '0' CHECK (FlexibleSchedule IN ('0', '1')),
    AvailableHours VARCHAR(500),
    TotalProjectsCompleted INTEGER DEFAULT 0,
    AverageRating DECIMAL(3,2),
    YearsExperience INTEGER
);

CREATE TABLE DESIGNER_SPECIALTIES (
    DesignerID INTEGER REFERENCES DESIGNERS(DesignerID) ON DELETE CASCADE,
    SpecialtyID INTEGER REFERENCES SPECIALTIES(SpecialtyID) ON DELETE CASCADE,
    ProficiencyLevel VARCHAR(20) DEFAULT 'INTERMEDIATE' CHECK (ProficiencyLevel IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')),
    YearsExperience INTEGER DEFAULT 0,
    PRIMARY KEY (DesignerID, SpecialtyID)
);

CREATE TABLE DESIGNER_SCHEDULE (
    ScheduleID SERIAL PRIMARY KEY,
    DesignerID INTEGER REFERENCES DESIGNERS(DesignerID) ON DELETE CASCADE,
    AvailableDate DATE NOT NULL,
    StartTime VARCHAR(5) DEFAULT '09:00',
    EndTime VARCHAR(5) DEFAULT '17:00',
    IsAvailable CHAR(1) DEFAULT '1' CHECK (IsAvailable IN ('0', '1')),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (DesignerID, AvailableDate)
);

CREATE TABLE BOOKINGS (
    BookingID SERIAL PRIMARY KEY,
    CustomerID INTEGER REFERENCES USERS(UserID) ON DELETE CASCADE,
    DesignerID INTEGER REFERENCES DESIGNERS(DesignerID) ON DELETE CASCADE,
    BookingDate DATE NOT NULL,
    BookingTime VARCHAR(10) NOT NULL,
    Duration INTEGER DEFAULT 60,
    Status VARCHAR(20) DEFAULT 'Pending' CHECK (Status IN ('Pending', 'Confirmed', 'Completed', 'Cancelled', 'No-Show')),
    ServiceType VARCHAR(100),
    Notes TEXT,
    EstimatedCost DECIMAL(10,2),
    ActualCost DECIMAL(10,2),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CancelledAt TIMESTAMP,
    CancellationReason VARCHAR(500),
    UNIQUE (DesignerID, BookingDate, BookingTime)
);

CREATE TABLE BOOKING_CATEGORIES (
    BookingID INTEGER REFERENCES BOOKINGS(BookingID) ON DELETE CASCADE,
    CategoryID INTEGER REFERENCES CATEGORIES(CategoryID) ON DELETE CASCADE,
    PRIMARY KEY (BookingID, CategoryID)
);

-- 4. INSERT DEFAULT DATA
INSERT INTO ROLES (RoleName) VALUES ('ADMIN'), ('DESIGNER'), ('CUSTOMER');

INSERT INTO REFERRALS (ReferralSource) VALUES 
('Website'), ('Social Media'), ('Friend Referral'), ('Advertisement'), ('Other');

INSERT INTO SPECIALTIES (SpecialtyName, Description) VALUES
('Modern', 'Contemporary and sleek design'),
('Minimalist', 'Clean lines, simple, functional'),
('Industrial', 'Exposed brick, metal, urban aesthetic'),
('Bohemian', 'Eclectic, colorful, artistic'),
('Traditional', 'Classic, elegant, timeless'),
('Scandinavian', 'Light, airy, functional, cozy'),
('Eclectic', 'Mix of visual elements and various styles');

INSERT INTO CATEGORIES (CategoryName, Description) VALUES
('Modern', 'Contemporary and sleek design'),
('Minimalist', 'Clean lines, simple, functional'),
('Industrial', 'Exposed brick, metal, urban aesthetic'),
('Bohemian', 'Eclectic, colorful, artistic'),
('Traditional', 'Classic, elegant, timeless'),
('Eclectic', 'Mix of visual elements and various styles');

-- Default Users
INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID) VALUES 
('admin@dott.com', 'admin123', 'Super', 'Admin', 1);

INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID) VALUES 
('user@dott.com', 'user123', 'Job', 'Doe', 3);

-- Default Designer (John Design)
INSERT INTO USERS (Email, PasswordHash, FirstName, LastName, RoleID) VALUES 
('designer@dott.com', 'designer123', 'John', 'Design', 2);

-- Link Designer to Staff
INSERT INTO STAFF (UserID, StaffType, Department, JoinDate, Salary) 
SELECT UserID, 'DESIGNER', 'Interiors', CURRENT_DATE, 60000.00 FROM USERS WHERE Email = 'designer@dott.com';

-- Link Staff to Designers
INSERT INTO DESIGNERS (StaffID, Bio, PrimarySpecialty, Status)
SELECT StaffID, 'Expert in Modern and Industrial', 'Modern', 'Active' 
FROM STAFF WHERE UserID = (SELECT UserID FROM USERS WHERE Email = 'designer@dott.com');

-- Add Specialties to Designer
INSERT INTO DESIGNER_SPECIALTIES (DesignerID, SpecialtyID)
SELECT d.DesignerID, s.SpecialtyID 
FROM DESIGNERS d, SPECIALTIES s 
WHERE d.StaffID IN (SELECT StaffID FROM STAFF WHERE UserID = (SELECT UserID FROM USERS WHERE Email = 'designer@dott.com'))
AND s.SpecialtyName IN ('Modern', 'Industrial');
