package com.example.mapper;

import com.example.entity.SavingGoal;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SavingGoalMapper implements RowMapper<SavingGoal> {
    @Override
    public SavingGoal mapRow(ResultSet rs, int rowNum) throws SQLException {
        SavingGoal sg = new SavingGoal();
        sg.setId(UUID.fromString(rs.getString("id")));
        sg.setUserId(UUID.fromString(rs.getString("user_id")));
        sg.setName(rs.getString("name"));
        sg.setTargetAmount(rs.getDouble("target_amount"));
        sg.setStartDate(rs.getDate("start_date").toLocalDate());
        sg.setEndDate(rs.getDate("end_date").toLocalDate());
        sg.setGoalAccountId(UUID.fromString(rs.getString("account_id")));
        sg.setCompleted(rs.getBoolean("is_completed"));
        return sg;
    }
}
