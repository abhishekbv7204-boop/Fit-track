package com.gym.dao;

import com.gym.models.User;
import com.gym.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    public User authenticate(String username, String password) {
        String sql = "SELECT username, role FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(resultSet.getString("username"), resultSet.getString("role"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

