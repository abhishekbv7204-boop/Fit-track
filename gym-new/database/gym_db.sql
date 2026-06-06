CREATE DATABASE IF NOT EXISTS gym_db;
USE gym_db;

CREATE TABLE IF NOT EXISTS plans (
    plan_id INT AUTO_INCREMENT PRIMARY KEY,
    plan_name VARCHAR(50) NOT NULL,
    duration_months INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    join_date DATE NOT NULL,
    plan_id INT,
    FOREIGN KEY (plan_id) REFERENCES plans(plan_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'admin'
);

CREATE TABLE IF NOT EXISTS trainers (
    trainer_id INT PRIMARY KEY,
    name VARCHAR(100),
    specialization VARCHAR(100),
    phone VARCHAR(20),
    experience_years INT
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY,
    member_id INT,
    amount INT,
    payment_date DATE,
    payment_method VARCHAR(50),
    status VARCHAR(20),
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT PRIMARY KEY,
    member_id INT,
    attendance_date DATE,
    status VARCHAR(20),
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS workout_schedule (
    schedule_id INT PRIMARY KEY,
    member_id INT,
    trainer_id INT,
    workout_plan VARCHAR(255),
    schedule_date DATE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers(trainer_id) ON DELETE SET NULL
);

ALTER TABLE members MODIFY id INT AUTO_INCREMENT;

INSERT IGNORE INTO plans (plan_id, plan_name, duration_months, price) VALUES
(1, 'Basic', 1, 999),
(2, 'Standard', 3, 2499),
(3, 'Premium', 12, 7999);

INSERT IGNORE INTO members (id, name, age, gender, phone, email, join_date, plan_id) VALUES
(1, 'Rahul Sharma', 22, 'Male', '9876543210', 'rahul@example.com', '2026-05-29', 1),
(2, 'Priya Singh', 24, 'Female', '9123456780', 'priya@example.com', '2026-05-29', 2),
(38939, 'abhishek', 19, 'Male', '7204575614', 'abhi@gmail.com', '2026-06-07', 1);

INSERT IGNORE INTO users (id, username, password, role) VALUES
(1, 'admin', 'admin123', 'admin');

INSERT IGNORE INTO trainers (trainer_id, name, specialization, phone, experience_years) VALUES
(1, 'Arjun Mehta', 'Strength Training', '9000011111', 5),
(2, 'Neha Rao', 'Yoga and Fitness', '9000022222', 4);

INSERT IGNORE INTO payments (payment_id, member_id, amount, payment_date, payment_method, status) VALUES
(1, 1, 999, '2026-05-29', 'Cash', 'Paid'),
(2, 2, 2499, '2026-05-29', 'UPI', 'Paid');

INSERT IGNORE INTO attendance (attendance_id, member_id, attendance_date, status) VALUES
(1, 1, '2026-05-29', 'Present'),
(2, 2, '2026-05-29', 'Present');

INSERT IGNORE INTO workout_schedule (schedule_id, member_id, trainer_id, workout_plan, schedule_date) VALUES
(1, 1, 1, 'Chest and Cardio', '2026-05-29'),
(2, 2, 2, 'Yoga and Flexibility', '2026-05-29');
