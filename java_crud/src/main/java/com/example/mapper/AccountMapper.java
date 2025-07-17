package com.example.mapper;

import com.example.entity.Account;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountMapper implements RowMapper<Account> {
    @Override
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
        Account account = new Account();
        account.setId(UUID.fromString(rs.getString("id")));
        account.setUserId(UUID.fromString(rs.getString("user_id")));
        account.setName(rs.getString("name"));
        account.setType(rs.getString("type"));
        account.setBalance(rs.getDouble("balance"));
        return account;
    }
}