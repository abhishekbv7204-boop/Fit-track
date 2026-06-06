package com.gym.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class SqlFileSync {
    private static final List<String> TABLES = Arrays.asList(
        "plans",
        "members",
        "users",
        "trainers",
        "payments",
        "attendance",
        "workout_schedule"
    );

    public static void syncFromDatabase() throws Exception {
        Path sqlFile = locateProjectRoot().resolve("database").resolve("gym_db.sql");
        String newline = System.lineSeparator();
        StringBuilder sql = new StringBuilder();

        sql.append(schemaSql(newline));

        try (Connection connection = DBConnection.getConnection()) {
            for (String table : TABLES) {
                appendTableRows(connection, sql, table, newline);
            }
        }

        Files.writeString(sqlFile, sql.toString(), StandardCharsets.UTF_8);
    }

    private static void appendTableRows(Connection connection, StringBuilder sql, String table, String newline) throws Exception {
        String query = "SELECT * FROM " + table;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            boolean hasRows = false;

            while (resultSet.next()) {
                if (!hasRows) {
                    sql.append(newline)
                        .append("INSERT IGNORE INTO ")
                        .append(table)
                        .append(" (");

                    for (int i = 1; i <= columnCount; i++) {
                        sql.append(metaData.getColumnName(i));
                        if (i < columnCount) sql.append(", ");
                    }

                    sql.append(") VALUES").append(newline);
                    hasRows = true;
                } else {
                    sql.append(",").append(newline);
                }

                sql.append("(");
                for (int i = 1; i <= columnCount; i++) {
                    sql.append(toSqlValue(resultSet.getObject(i)));
                    if (i < columnCount) sql.append(", ");
                }
                sql.append(")");
            }

            if (hasRows) {
                sql.append(";").append(newline);
            }
        }
    }

    private static String toSqlValue(Object value) {
        if (value == null) return "NULL";

        if (value instanceof Number || value instanceof BigDecimal) {
            return value.toString();
        }

        if (value instanceof Date || value instanceof Time || value instanceof Timestamp) {
            return "'" + value + "'";
        }

        return "'" + value.toString().replace("\\", "\\\\").replace("'", "''") + "'";
    }

    private static Path locateProjectRoot() throws Exception {
        Path current = Path.of("").toAbsolutePath().normalize();

        while (current != null) {
            if (Files.exists(current.resolve("database").resolve("gym_db.sql"))) {
                return current;
            }
            if (Files.exists(current.resolve("gym-new").resolve("database").resolve("gym_db.sql"))) {
                return current.resolve("gym-new");
            }
            current = current.getParent();
        }

        throw new IllegalStateException("Could not find database/gym_db.sql from the current folder.");
    }

    private static String schemaSql(String newline) {
        return String.join(newline,
            "CREATE DATABASE IF NOT EXISTS gym_db;",
            "USE gym_db;",
            "",
            "CREATE TABLE IF NOT EXISTS plans (",
            "    plan_id INT AUTO_INCREMENT PRIMARY KEY,",
            "    plan_name VARCHAR(50) NOT NULL,",
            "    duration_months INT NOT NULL,",
            "    price DECIMAL(10, 2) NOT NULL",
            ");",
            "",
            "CREATE TABLE IF NOT EXISTS members (",
            "    id INT AUTO_INCREMENT PRIMARY KEY,",
            "    name VARCHAR(100) NOT NULL,",
            "    age INT NOT NULL,",
            "    gender VARCHAR(20) NOT NULL,",
            "    phone VARCHAR(20) NOT NULL,",
            "    email VARCHAR(100) NOT NULL,",
            "    join_date DATE NOT NULL,",
            "    plan_id INT,",
            "    FOREIGN KEY (plan_id) REFERENCES plans(plan_id) ON DELETE SET NULL",
            ");",
            "",
            "CREATE TABLE IF NOT EXISTS users (",
            "    id INT AUTO_INCREMENT PRIMARY KEY,",
            "    username VARCHAR(50) UNIQUE NOT NULL,",
            "    password VARCHAR(100) NOT NULL,",
            "    role VARCHAR(20) NOT NULL DEFAULT 'admin'",
            ");",
            "",
            "CREATE TABLE IF NOT EXISTS trainers (",
            "    trainer_id INT PRIMARY KEY,",
            "    name VARCHAR(100),",
            "    specialization VARCHAR(100),",
            "    phone VARCHAR(20),",
            "    experience_years INT",
            ");",
            "",
            "CREATE TABLE IF NOT EXISTS payments (",
            "    payment_id INT PRIMARY KEY,",
            "    member_id INT,",
            "    amount INT,",
            "    payment_date DATE,",
            "    payment_method VARCHAR(50),",
            "    status VARCHAR(20),",
            "    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE",
            ");",
            "",
            "CREATE TABLE IF NOT EXISTS attendance (",
            "    attendance_id INT PRIMARY KEY,",
            "    member_id INT,",
            "    attendance_date DATE,",
            "    status VARCHAR(20),",
            "    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE",
            ");",
            "",
            "CREATE TABLE IF NOT EXISTS workout_schedule (",
            "    schedule_id INT PRIMARY KEY,",
            "    member_id INT,",
            "    trainer_id INT,",
            "    workout_plan VARCHAR(255),",
            "    schedule_date DATE,",
            "    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,",
            "    FOREIGN KEY (trainer_id) REFERENCES trainers(trainer_id) ON DELETE SET NULL",
            ");",
            "",
            "ALTER TABLE members MODIFY id INT AUTO_INCREMENT;",
            ""
        );
    }
}
