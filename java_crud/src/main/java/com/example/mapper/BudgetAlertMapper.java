package com.example.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.example.entity.BudgetAlert;

public class BudgetAlertMapper implements RowMapper<BudgetAlert> {
    @Override
    public BudgetAlert mapRow(ResultSet rs, int rowNum) throws SQLException {
        BudgetAlert alert = new BudgetAlert();
        alert.setId(UUID.fromString(rs.getString("id")));
        alert.setBudgetId(UUID.fromString(rs.getString("budget_id")));
        alert.setUserId(UUID.fromString(rs.getString("user_id")));
        alert.setCategory(rs.getString("category"));
        alert.setMonth(rs.getInt("month"));
        alert.setYear(rs.getInt("year"));
        alert.setAlertDate(rs.getDate("alert_date").toLocalDate());
        alert.setStillExceeded(rs.getBoolean("still_exceeded"));
        return alert;
    }
}

