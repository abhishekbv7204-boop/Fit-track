package com.gym.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() throws Exception {
        try (Connection connection = DBConnection.getServerConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS gym_db");
        }

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS plans (
                    plan_id INT AUTO_INCREMENT PRIMARY KEY,
                    plan_name VARCHAR(50) NOT NULL,
                    duration_months INT NOT NULL,
                    price DECIMAL(10, 2) NOT NULL
                )
                """);

            statement.executeUpdate("""
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
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'admin'
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS trainers (
                    trainer_id INT PRIMARY KEY,
                    name VARCHAR(100),
                    specialization VARCHAR(100),
                    phone VARCHAR(20),
                    experience_years INT
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    payment_id INT PRIMARY KEY,
                    member_id INT,
                    amount INT,
                    payment_date DATE,
                    payment_method VARCHAR(50),
                    status VARCHAR(20),
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS attendance (
                    attendance_id INT PRIMARY KEY,
                    member_id INT,
                    attendance_date DATE,
                    status VARCHAR(20),
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS workout_schedule (
                    schedule_id INT PRIMARY KEY,
                    member_id INT,
                    trainer_id INT,
                    workout_plan VARCHAR(255),
                    schedule_date DATE,
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                    FOREIGN KEY (trainer_id) REFERENCES trainers(trainer_id) ON DELETE SET NULL
                )
                """);

            statement.executeUpdate("""
                INSERT INTO plans (plan_id, plan_name, duration_months, price) VALUES
                (1, 'Basic', 1, 999.00),
                (2, 'Standard', 3, 2499.00),
                (3, 'Premium', 12, 7999.00)
                ON DUPLICATE KEY UPDATE
                    plan_name = VALUES(plan_name),
                    duration_months = VALUES(duration_months),
                    price = VALUES(price)
                """);

            statement.executeUpdate("""
                INSERT INTO users (id, username, password, role)
                VALUES (1, 'admin', 'admin123', 'admin')
                ON DUPLICATE KEY UPDATE
                    username = VALUES(username),
                    password = VALUES(password),
                    role = VALUES(role)
                """);

            statement.executeUpdate("""
                INSERT IGNORE INTO members (id, name, age, gender, phone, email, join_date, plan_id) VALUES
                (1, 'Rahul Sharma', 22, 'Male', '9876543210', 'rahul@example.com', CURDATE(), 1),
                (2, 'Priya Singh', 24, 'Female', '9123456780', 'priya@example.com', CURDATE(), 2)
                """);

            statement.executeUpdate("""
                INSERT IGNORE INTO trainers (trainer_id, name, specialization, phone, experience_years) VALUES
                (1, 'Arjun Mehta', 'Strength Training', '9000011111', 5),
                (2, 'Neha Rao', 'Yoga and Fitness', '9000022222', 4)
                """);

            statement.executeUpdate("""
                INSERT IGNORE INTO payments (payment_id, member_id, amount, payment_date, payment_method, status) VALUES
                (1, 1, 999, CURDATE(), 'Cash', 'Paid'),
                (2, 2, 2499, CURDATE(), 'UPI', 'Paid')
                """);

            statement.executeUpdate("""
                INSERT IGNORE INTO attendance (attendance_id, member_id, attendance_date, status) VALUES
                (1, 1, CURDATE(), 'Present'),
                (2, 2, CURDATE(), 'Present')
                """);

            statement.executeUpdate("""
                INSERT IGNORE INTO workout_schedule (schedule_id, member_id, trainer_id, workout_plan, schedule_date) VALUES
                (1, 1, 1, 'Chest and Cardio', CURDATE()),
                (2, 2, 2, 'Yoga and Flexibility', CURDATE())
                """);
        }

        SqlFileSync.syncFromDatabase();
    }
}
