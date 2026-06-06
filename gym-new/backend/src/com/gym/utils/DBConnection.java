package com.gym.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gym_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Abhi@9141";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found on classpath! Make sure lib/mysql-connector-java-8.0.30.jar is included.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static Connection getServerConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found on classpath! Make sure lib/mysql-connector-java-8.0.30.jar is included.");
        }
        return DriverManager.getConnection(serverUrl(), USER, PASSWORD);
    }

    private static String serverUrl() {
        return URL.replaceFirst("/gym_db(?=\\?|$)", "/");
    }
}
