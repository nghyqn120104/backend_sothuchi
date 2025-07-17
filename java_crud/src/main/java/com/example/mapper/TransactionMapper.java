package com.example.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.example.entity.Transaction;
import com.example.enums.TransactionCategory;
import com.example.enums.TransactionType;

public class TransactionMapper implements RowMapper<Transaction> {

    @Override
    public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        Transaction t = new Transaction();
        t.setId(UUID.fromString(rs.getString("id")));
        t.setDescription(rs.getString("description"));
        t.setAmount(rs.getDouble("amount"));
        t.setDate(rs.getDate("date").toLocalDate());
        t.setType(TransactionType.valueOf(rs.getString("type")));
        t.setCategory(TransactionCategory.valueOf(rs.getString("category")));
        t.setUserId(UUID.fromString(rs.getString("user_id")));

        // Kiểm tra account_id có null không
        String accountIdStr = rs.getString("account_id");
        if (accountIdStr != null) {
            t.setAccountId(UUID.fromString(accountIdStr));
        } else {
            t.setAccountId(null);
        }

        return t;
    }
}
