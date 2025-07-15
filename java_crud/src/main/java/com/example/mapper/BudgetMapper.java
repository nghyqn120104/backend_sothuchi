package com.example.mapper;

import com.example.entity.Budget;
import com.example.enums.TransactionCategory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BudgetMapper implements RowMapper<Budget> {
    @Override
    public Budget mapRow(ResultSet rs, int rowNum) throws SQLException {
        Budget b = new Budget();
        b.setId(UUID.fromString(rs.getString("id")));
        b.setUserId(UUID.fromString(rs.getString("user_id")));
        b.setCategory(TransactionCategory.valueOf(rs.getString("category")));
        b.setAmount(rs.getDouble("amount"));
        b.setMonth(rs.getInt("month"));
        b.setYear(rs.getInt("year"));
        return b;
    }
}
