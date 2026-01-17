-- PostgreSQL DDL for Booking System

-- 1. Referrals
CREATE TABLE IF NOT EXISTS REFERRALS (
    ReferralID SERIAL PRIMARY KEY,
    ReferralSource VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);

-- 2. Specialties
CREATE TABLE IF NOT EXISTS SPECIALTIES (
    SpecialtyID SERIAL PRIMARY KEY,
    SpecialtyName VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);

-- 3. Categories
CREATE TABLE IF NOT EXISTS CATEGORIES (
    CategoryID SERIAL PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);

-- 4. Staff (Linked to Users)
CREATE TABLE IF NOT EXISTS STAFF (
    StaffID SERIAL PRIMARY KEY,
    UserID INTEGER UNIQUE REFERENCES USERS(UserID) ON DELETE CASCADE,
    StaffType VARCHAR(20) CHECK (StaffType IN ('ADMIN', 'DESIGNER', 'MANAGER')),
    Department VARCHAR(100),
    ManagerID INTEGER REFERENCES STAFF(StaffID),
    JoinDate DATE DEFAULT CURRENT_DATE,
    EndDate DATE,
    Salary DECIMAL(10,2)
);

-- 5. Designers (Linked to Staff)
CREATE TABLE IF NOT EXISTS DESIGNERS (
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

-- 6. Designer Specialties (Many-to-Many)
CREATE TABLE IF NOT EXISTS DESIGNER_SPECIALTIES (
    DesignerID INTEGER REFERENCES DESIGNERS(DesignerID) ON DELETE CASCADE,
    SpecialtyID INTEGER REFERENCES SPECIALTIES(SpecialtyID) ON DELETE CASCADE,
    ProficiencyLevel VARCHAR(20) DEFAULT 'INTERMEDIATE' CHECK (ProficiencyLevel IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')),
    YearsExperience INTEGER DEFAULT 0,
    PRIMARY KEY (DesignerID, SpecialtyID)
);

-- 7. Designer Schedule
CREATE TABLE IF NOT EXISTS DESIGNER_SCHEDULE (
    ScheduleID SERIAL PRIMARY KEY,
    DesignerID INTEGER REFERENCES DESIGNERS(DesignerID) ON DELETE CASCADE,
    AvailableDate DATE NOT NULL,
    StartTime VARCHAR(5) DEFAULT '09:00',
    EndTime VARCHAR(5) DEFAULT '17:00',
    IsAvailable CHAR(1) DEFAULT '1' CHECK (IsAvailable IN ('0', '1')),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (DesignerID, AvailableDate)
);

-- 8. Bookings
CREATE TABLE IF NOT EXISTS BOOKINGS (
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

-- 9. Booking Categories (Many-to-Many)
CREATE TABLE IF NOT EXISTS BOOKING_CATEGORIES (
    BookingID INTEGER REFERENCES BOOKINGS(BookingID) ON DELETE CASCADE,
    CategoryID INTEGER REFERENCES CATEGORIES(CategoryID) ON DELETE CASCADE,
    PRIMARY KEY (BookingID, CategoryID)
);

-- === SEED DATA ===

-- Referrals
INSERT INTO REFERRALS (ReferralSource) VALUES 
('Website'), ('Social Media'), ('Friend Referral'), ('Advertisement'), ('Other')
ON CONFLICT (ReferralSource) DO NOTHING;

-- Specialties
INSERT INTO SPECIALTIES (SpecialtyName, Description) VALUES
('Modern', 'Contemporary and sleek design'),
('Minimalist', 'Clean lines, simple, functional'),
('Industrial', 'Exposed brick, metal, urban aesthetic'),
('Bohemian', 'Eclectic, colorful, artistic'),
('Traditional', 'Classic, elegant, timeless'),
('Scandinavian', 'Light, airy, functional, cozy')
ON CONFLICT (SpecialtyName) DO NOTHING;

-- Categories
INSERT INTO CATEGORIES (CategoryName, Description) VALUES
('Modern', 'Contemporary and sleek design'),
('Minimalist', 'Clean lines, simple, functional'),
('Industrial', 'Exposed brick, metal, urban aesthetic'),
('Bohemian', 'Eclectic, colorful, artistic'),
('Traditional', 'Classic, elegant, timeless')
ON CONFLICT (CategoryName) DO NOTHING;
