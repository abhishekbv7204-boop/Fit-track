package com.gym.dao;

import com.gym.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseTableDAO {
    private static final Set<String> ALLOWED_TABLES = new HashSet<>(Arrays.asList(
        "attendance",
        "members",
        "payments",
        "plans",
        "trainers",
        "users",
        "workout_schedule"
    ));

    public DatabaseTable findTable(String tableName) throws Exception {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Table is not allowed");
        }

        String sql = "SELECT * FROM " + tableName;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<String> columns = new ArrayList<>();
            List<Map<String, String>> rows = new ArrayList<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columns.add(metaData.getColumnName(i));
            }

            while (resultSet.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String column : columns) {
                    Object value = resultSet.getObject(column);
                    row.put(column, value == null ? "" : value.toString());
                }
                rows.add(row);
            }

            return new DatabaseTable(tableName, columns, rows);
        }
    }

    public void insertRow(String tableName, Map<String, String> values) throws Exception {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Table is not allowed");
        }

        List<String> tableColumns = findTable(tableName).getColumns();
        List<String> insertColumns = new ArrayList<>();
        List<String> insertValues = new ArrayList<>();

        for (String column : tableColumns) {
            String value = values.get(column);
            if (value != null && !value.isBlank()) {
                insertColumns.add(column);
                insertValues.add(value);
            }
        }

        if (insertColumns.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }

        String placeholders = String.join(",", insertColumns.stream().map(column -> "?").toList());
        String sql = "INSERT INTO " + tableName + " (" + String.join(",", insertColumns) + ") VALUES (" + placeholders + ")";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < insertValues.size(); i++) {
                statement.setString(i + 1, insertValues.get(i));
            }
            statement.executeUpdate();
        }
    }

    public void deleteRow(String tableName, String columnName, String value) throws Exception {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Table is not allowed");
        }

        List<String> tableColumns = findTable(tableName).getColumns();
        if (!tableColumns.contains(columnName)) {
            throw new IllegalArgumentException("Invalid column name");
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.executeUpdate();
        }
    }

    public List<String> allowedTables() {
        return new ArrayList<>(Arrays.asList(
            "members",
            "plans",
            "trainers",
            "payments",
            "attendance",
            "workout_schedule",
            "users"
        ));
    }

    public static class DatabaseTable {
        private final String name;
        private final List<String> columns;
        private final List<Map<String, String>> rows;

        public DatabaseTable(String name, List<String> columns, List<Map<String, String>> rows) {
            this.name = name;
            this.columns = columns;
            this.rows = rows;
        }

        public String getName() { return name; }
        public List<String> getColumns() { return columns; }
        public List<Map<String, String>> getRows() { return rows; }
    }
}
