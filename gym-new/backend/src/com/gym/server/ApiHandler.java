package com.gym.server;

import com.gym.dao.MemberDAO;
import com.gym.dao.PlanDAO;
import com.gym.dao.UserDAO;
import com.gym.dao.DatabaseTableDAO;
import com.gym.dao.DatabaseTableDAO.DatabaseTable;
import com.gym.models.Member;
import com.gym.models.Plan;
import com.gym.models.User;
import com.gym.utils.SqlFileSync;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler {
    private final UserDAO userDAO = new UserDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final PlanDAO planDAO = new PlanDAO();
    private final DatabaseTableDAO databaseTableDAO = new DatabaseTableDAO();

    public void handleLogin(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> body = parseJsonObject(readBody(exchange));
        User user = userDAO.authenticate(body.get("username"), body.get("password"));

        if (user == null) {
            sendJson(exchange, 401, "{\"success\":false,\"message\":\"Invalid username or password\"}");
            return;
        }

        sendJson(exchange, 200, "{\"success\":true,\"role\":\"" + escape(user.getRole()) + "\"}");
    }

    public void handlePlans(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        List<Plan> plans = planDAO.findAll();
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < plans.size(); i++) {
            Plan plan = plans.get(i);
            json.append("{")
                .append("\"id\":").append(plan.getId()).append(",")
                .append("\"name\":\"").append(escape(plan.getName())).append("\",")
                .append("\"durationMonths\":").append(plan.getDurationMonths()).append(",")
                .append("\"price\":").append(plan.getPrice())
                .append("}");

            if (i < plans.size() - 1) json.append(",");
        }

        json.append("]");
        sendJson(exchange, 200, json.toString());
    }

    public void handleMembers(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        try {
            String method = exchange.getRequestMethod().toUpperCase();

            switch (method) {
                case "GET" -> sendMembers(exchange);
                case "POST" -> createMember(exchange);
                case "PUT" -> updateMember(exchange);
                case "DELETE" -> deleteMember(exchange);
                default -> sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"Server error\"}");
        }
    }

    public void handleDatabaseTables(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendDatabaseTables(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            insertDatabaseRow(exchange);
            return;
        }

        if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            deleteDatabaseRow(exchange);
            return;
        }

        sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
    }

    private void sendDatabaseTables(HttpExchange exchange) throws IOException {
        try {
            StringBuilder json = new StringBuilder("[");
            List<String> tableNames = databaseTableDAO.allowedTables();

            for (int i = 0; i < tableNames.size(); i++) {
                DatabaseTable table = databaseTableDAO.findTable(tableNames.get(i));
                json.append(tableToJson(table));
                if (i < tableNames.size() - 1) json.append(",");
            }

            json.append("]");
            sendJson(exchange, 200, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"Could not load database tables\"}");
        }
    }

    private void insertDatabaseRow(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String tableName = query.get("table");

            if (isBlank(tableName)) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Table name is required\"}");
                return;
            }

            Map<String, String> body = parseJsonObject(readBody(exchange));
            String validationError = validateNameFields(body);
            if (validationError == null) {
                validationError = validateGenderFields(body);
            }
            if (validationError != null) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + validationError + "\"}");
                return;
            }

            databaseTableDAO.insertRow(tableName, body);
            syncSqlFile();
            sendJson(exchange, 201, "{\"success\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"Could not insert tuple. Check required IDs, dates, and foreign keys.\"}");
        }
    }

    private void deleteDatabaseRow(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String tableName = query.get("table");
            String columnName = query.get("column");
            String value = query.get("value");

            if (isBlank(tableName) || isBlank(columnName) || isBlank(value)) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Table, column, and value are required\"}");
                return;
            }

            databaseTableDAO.deleteRow(tableName, columnName, value);
            syncSqlFile();
            sendJson(exchange, 200, "{\"success\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"Could not delete row. Check foreign key constraints.\"}");
        }
    }

    private void sendMembers(HttpExchange exchange) throws IOException {
        List<Member> members = memberDAO.findAll();
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            json.append("{")
                .append("\"id\":").append(member.getId()).append(",")
                .append("\"name\":\"").append(escape(member.getName())).append("\",")
                .append("\"age\":").append(member.getAge()).append(",")
                .append("\"gender\":\"").append(escape(member.getGender())).append("\",")
                .append("\"phone\":\"").append(escape(member.getPhone())).append("\",")
                .append("\"email\":\"").append(escape(member.getEmail())).append("\",")
                .append("\"joinDate\":\"").append(member.getJoinDate()).append("\",")
                .append("\"planId\":").append(member.getPlanId() == null ? "null" : member.getPlanId()).append(",")
                .append("\"planName\":\"").append(escape(member.getPlanName() == null ? "No Plan" : member.getPlanName())).append("\"")
                .append("}");

            if (i < members.size() - 1) json.append(",");
        }

        json.append("]");
        sendJson(exchange, 200, json.toString());
    }

    private void createMember(HttpExchange exchange) throws Exception {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        String validationError = validateMember(body, false);

        if (validationError != null) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + validationError + "\"}");
            return;
        }

        int id = memberDAO.create(
            body.get("name"),
            Integer.parseInt(body.get("age")),
            body.get("gender"),
            body.get("phone"),
            body.get("email"),
            Integer.parseInt(body.get("planId"))
        );

        syncSqlFile();
        sendJson(exchange, 201, "{\"success\":true,\"id\":" + id + "}");
    }

    private void updateMember(HttpExchange exchange) throws Exception {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        String validationError = validateMember(body, true);

        if (validationError != null) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + validationError + "\"}");
            return;
        }

        boolean updated = memberDAO.update(
            Integer.parseInt(body.get("id")),
            body.get("name"),
            Integer.parseInt(body.get("age")),
            body.get("gender"),
            body.get("phone"),
            body.get("email"),
            Integer.parseInt(body.get("planId"))
        );

        if (updated) {
            syncSqlFile();
        }
        sendJson(exchange, updated ? 200 : 404, "{\"success\":" + updated + "}");
    }

    private void deleteMember(HttpExchange exchange) throws Exception {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
        String id = query.get("id");

        if (id == null || id.isBlank()) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"Member id is required\"}");
            return;
        }

        boolean deleted = memberDAO.delete(Integer.parseInt(id));
        if (deleted) {
            syncSqlFile();
        }
        sendJson(exchange, deleted ? 200:404, "{\"success\":" + deleted + "}");
    }

    private void syncSqlFile() throws Exception {
        SqlFileSync.syncFromDatabase();
    }

    private String validateNameFields(Map<String, String> body) {
        for (Map.Entry<String, String> entry : body.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isNameField(key) && !isBlank(value) && !isValidName(value)) {
                return "The " + key + " field must contain only letters and spaces";
            }
        }
        return null;
    }

    private String validateMember(Map<String, String> body, boolean requireId) {
        if (requireId && isBlank(body.get("id"))) return "Member id is required";
        String name = body.get("name");
        if (isBlank(name)) return "Name is required";
        if (!isValidName(name)) return "Name must contain only letters and spaces";
        if (isBlank(body.get("age"))) return "Age is required";
        String gender = body.get("gender");
        if (isBlank(gender)) return "Gender is required";
        if (!isValidGender(gender)) return "Gender must be Male, Female, or Other";
        if (isBlank(body.get("phone"))) return "Phone is required";
        if (isBlank(body.get("email"))) return "Email is required";
        if (isBlank(body.get("planId"))) return "Plan is required";
        return null;
    }

    private String validateGenderFields(Map<String, String> body) {
        for (Map.Entry<String, String> entry : body.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isGenderField(key) && !isBlank(value) && !isValidGender(value)) {
                return "The " + key + " field must be Male, Female, or Other";
            }
        }
        return null;
    }

    private boolean isGenderField(String key) {
        if (isBlank(key)) return false;
        String lower = key.toLowerCase();
        return lower.equals("gender") || lower.endsWith("_gender") || key.endsWith("Gender");
    }

    private boolean isNameField(String key) {
        if (isBlank(key)) return false;
        String lower = key.toLowerCase();
        if (lower.equals("name")) return true;
        if (lower.endsWith("_name")) return true;
        if (key.endsWith("Name") && !lower.endsWith("username")) return true;
        return false;
    }

    private boolean isValidGender(String value) {
        if (isBlank(value)) return false;
        String normalized = value.trim().toLowerCase();
        return normalized.equals("male") || normalized.equals("female") || normalized.equals("other");
    }

    private boolean isValidName(String value) {
        return !isBlank(value) && value.trim().matches("^[A-Za-z ]+$");
    }

    private String tableToJson(DatabaseTable table) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"name\":\"").append(escape(table.getName())).append("\",");
        json.append("\"columns\":[");

        for (int i = 0; i < table.getColumns().size(); i++) {
            json.append("\"").append(escape(table.getColumns().get(i))).append("\"");
            if (i < table.getColumns().size() - 1) json.append(",");
        }

        json.append("],\"rows\":[");

        for (int i = 0; i < table.getRows().size(); i++) {
            Map<String, String> row = table.getRows().get(i);
            json.append("{");

            for (int j = 0; j < table.getColumns().size(); j++) {
                String column = table.getColumns().get(j);
                json.append("\"").append(escape(column)).append("\":")
                    .append("\"").append(escape(row.get(column))).append("\"");
                if (j < table.getColumns().size() - 1) json.append(",");
            }

            json.append("}");
            if (i < table.getRows().size() - 1) json.append(",");
        }

        json.append("]}");
        return json.toString();
    }

    private boolean handleOptions(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }

        return false;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private Map<String, String> parseJsonObject(String json) {
        Map<String, String> values = new HashMap<>();
        String text = json.trim();

        if (text.startsWith("{")) text = text.substring(1);
        if (text.endsWith("}")) text = text.substring(0, text.length() - 1);

        for (String pair : text.split(",")) {
            int colon = pair.indexOf(":");
            if (colon == -1) continue;

            String key = cleanJsonValue(pair.substring(0, colon));
            String value = cleanJsonValue(pair.substring(colon + 1));
            values.put(key, value);
        }

        return values;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> values = new HashMap<>();
        if (query == null || query.isBlank()) return values;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                values.put(
                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                );
            }
        }

        return values;
    }

    private String cleanJsonValue(String value) {
        String result = value.trim();
        if (result.startsWith("\"")) result = result.substring(1);
        if (result.endsWith("\"")) result = result.substring(0, result.length() - 1);
        return result.replace("\\\"", "\"");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
