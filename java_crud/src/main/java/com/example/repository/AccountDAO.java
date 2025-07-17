package com.example.repository;

import com.example.entity.Account;
import com.example.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountDAO {
    private final JdbcTemplate jdbc;

    public List<Account> findAllByUser(UUID userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY name";
        return jdbc.query(sql, new AccountMapper(), userId.toString());
    }

    public Optional<Account> findById(UUID id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        List<Account> result = jdbc.query(sql, new AccountMapper(), id.toString());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public int insert(Account a) {
        String sql = "INSERT INTO accounts (id, user_id, name, type, balance) VALUES (?, ?, ?, ?, ?)";
        return jdbc.update(sql,
                a.getId().toString(),
                a.getUserId().toString(),
                a.getName(),
                a.getType(),
                a.getBalance());
    }

    public int update(Account a) {
        String sql = "UPDATE accounts SET name = ?, type = ?, balance = ? WHERE id = ?";
        return jdbc.update(sql,
                a.getName(),
                a.getType(),
                a.getBalance(),
                a.getId().toString());
    }

    public int delete(UUID id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        return jdbc.update(sql, id.toString());
    }

    public boolean exists(UUID id) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id.toString());
        return count != null && count > 0;
    }

    public boolean existsByNameAndUser(String name, UUID userId) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE name = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, name, userId.toString());
        return count != null && count > 0;
    }

    public int deleteByUserId(UUID userId) {
        String sql = "DELETE FROM accounts WHERE user_id = ?";
        return jdbc.update(sql, userId.toString());
    }

}
