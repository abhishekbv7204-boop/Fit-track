package com.gym.dao;

import com.gym.models.Plan;
import com.gym.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlanDAO {
    public List<Plan> findAll() {
        List<Plan> plans = new ArrayList<>();
        String sql = "SELECT plan_id, plan_name, duration_months, price FROM plans ORDER BY plan_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                plans.add(new Plan(
                    resultSet.getInt("plan_id"),
                    resultSet.getString("plan_name"),
                    resultSet.getInt("duration_months"),
                    resultSet.getBigDecimal("price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return plans;
    }
}

