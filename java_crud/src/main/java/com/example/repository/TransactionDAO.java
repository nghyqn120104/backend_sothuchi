package com.example.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.Transaction;
import com.example.enums.TransactionCategory;
import com.example.mapper.TransactionMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransactionDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<Transaction> findAllByUser(UUID userId) {
        String sql = "SELECT * FROM transactions where user_id = ?";
        return jdbcTemplate.query(sql, new TransactionMapper(), userId.toString());
    }

    public int insert(Transaction t) {
        String sql = "INSERT INTO transactions (id, description, amount, date, type, category, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                t.getId().toString(),
                t.getDescription(),
                t.getAmount(),
                t.getDate(),
                t.getType().name(),
                t.getCategory().name(),
                t.getUserId().toString());
    }

    public int update(Transaction t) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, date = ?, type = ?, category = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                t.getDescription(),
                t.getAmount(),
                t.getDate(),
                t.getType().name(),
                t.getCategory().name(),
                t.getId().toString());
    }

    public int delete(UUID id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        return jdbcTemplate.update(sql, id.toString());
    }

    public int deleteByUserId(UUID userId) {
        String sql = "DELETE FROM transactions WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId.toString());
    }

    public boolean findById(UUID id) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.toString());
        return count != null && count > 0;
    }

    public double sumByUserCategoryAndMonth(UUID userId, TransactionCategory category, int month, int year) {
        String sql = """
                    SELECT SUM(amount) AS total
                    FROM transactions
                    WHERE user_id = ? AND category = ? AND type = 'EXPENSE'
                          AND MONTH(date) = ? AND YEAR(date) = ?
                """;

        Double total = jdbcTemplate.queryForObject(sql, Double.class, userId.toString(), category.name(), month, year);
        return total != null ? total : 0.0;
    }

    public double sumAllExpensesByUserMonth(UUID userId, int month, int year) {
        String sql = """
                    SELECT SUM(amount) AS total
                    FROM transactions
                    WHERE user_id = ? AND type = 'EXPENSE'
                          AND MONTH(date) = ? AND YEAR(date) = ?
                """;

        Double total = jdbcTemplate.queryForObject(sql, Double.class, userId.toString(), month, year);
        return total != null ? total : 0.0;
    }

}
