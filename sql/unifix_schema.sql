-- ===========================================
--  UniFix Database Schema
-- ===========================================

CREATE DATABASE IF NOT EXISTS unifix_db;
USE unifix_db;

-- =============================
--  USERS TABLE
-- =============================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('Student', 'Warden', 'Technician', 'Admin') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================
--  COMPLAINTS TABLE
-- =============================
CREATE TABLE complaints (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    location VARCHAR(100),
    category VARCHAR(100),
    description TEXT,
    image_path VARCHAR(255),
    status ENUM('Pending', 'In Progress', 'Resolved') DEFAULT 'Pending',
    assigned_to VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =============================
--  SOLUTIONS TABLE
-- =============================
CREATE TABLE solutions (
    solution_id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT,
    topic VARCHAR(100),
    resolution TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id)
);