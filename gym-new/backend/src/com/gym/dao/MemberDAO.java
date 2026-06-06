package com.gym.dao;

import com.gym.models.Member;
import com.gym.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        String sql = """
            SELECT m.id, m.name, m.age, m.gender, m.phone, m.email, m.join_date,
                   m.plan_id, p.plan_name
            FROM members m
            LEFT JOIN plans p ON m.plan_id = p.plan_id
            ORDER BY m.id DESC
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                members.add(toMember(resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    public int create(String name, int age, String gender, String phone, String email, int planId) throws Exception {
        String sql = """
            INSERT INTO members (name, age, gender, phone, email, join_date, plan_id)
            VALUES (?, ?, ?, ?, ?, CURDATE(), ?)
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setInt(2, age);
            statement.setString(3, gender);
            statement.setString(4, phone);
            statement.setString(5, email);
            statement.setInt(6, planId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return 0;
    }

    public boolean update(int id, String name, int age, String gender, String phone, String email, int planId) throws Exception {
        String sql = """
            UPDATE members
            SET name = ?, age = ?, gender = ?, phone = ?, email = ?, plan_id = ?
            WHERE id = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, age);
            statement.setString(3, gender);
            statement.setString(4, phone);
            statement.setString(5, email);
            if (planId > 0) {
                statement.setInt(6, planId);
            } else {
                statement.setNull(6, Types.INTEGER);
            }
            statement.setInt(7, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM members WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Member toMember(ResultSet resultSet) throws Exception {
        int planIdValue = resultSet.getInt("plan_id");
        Integer planId = resultSet.wasNull() ? null : planIdValue;
        Date joinDate = resultSet.getDate("join_date");

        return new Member(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getInt("age"),
            resultSet.getString("gender"),
            resultSet.getString("phone"),
            resultSet.getString("email"),
            joinDate,
            planId,
            resultSet.getString("plan_name")
        );
    }
}

