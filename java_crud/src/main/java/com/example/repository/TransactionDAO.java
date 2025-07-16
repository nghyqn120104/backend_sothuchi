package com.example.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.dto.SpendingStatisticsDTO;
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

    public SpendingStatisticsDTO getSpendingStatistics(UUID userId, int month, int year) {
        String sql = "SELECT category, SUM(amount) AS total " +
                "FROM transactions " +
                "WHERE user_id = ? AND type = 'EXPENSE' " +
                "AND MONTH(date) = ? AND YEAR(date) = ? " +
                "GROUP BY category";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId.toString(), month, year);

        double totalSpent = 0;
        Map<String, Double> breakdown = new HashMap<>();
        String topCategory = null;
        double maxAmount = 0;

        for (Map<String, Object> row : rows) {
            String category = (String) row.get("category");
            double amount = ((Number) row.get("total")).doubleValue();

            breakdown.put(category, amount);
            totalSpent += amount;

            if (amount > maxAmount) {
                maxAmount = amount;
                topCategory = category;
            }
        }

        // Tính tỷ lệ phần trăm
        Map<String, Double> percentageBreakdown = new HashMap<>();
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            double percent = totalSpent > 0 ? (entry.getValue() / totalSpent) * 100 : 0;
            percentageBreakdown.put(entry.getKey(), Math.round(percent * 100.0) / 100.0); // làm tròn 2 số
        }

        SpendingStatisticsDTO dto = new SpendingStatisticsDTO();
        dto.setTotalSpent(totalSpent);
        dto.setCategoryBreakdown(breakdown);
        dto.setTopSpendingCategory(topCategory);
        dto.setCategoryPercentage(percentageBreakdown); // cần bổ sung field này trong DTO nếu chưa có

        return dto;
    }

    public List<Transaction> search(UUID userId,
            LocalDate startDate, LocalDate endDate,
            Double minAmount, Double maxAmount) {

        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE user_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId.toString());

        if (startDate != null) {
            sql.append("AND date >= ? ");
            params.add(startDate);
        }

        if (endDate != null) {
            sql.append("AND date <= ? ");
            params.add(endDate);
        }

        if (minAmount != null) {
            sql.append("AND amount >= ? ");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            sql.append("AND amount <= ? ");
            params.add(maxAmount);
        }

        sql.append("ORDER BY date DESC");

        return jdbcTemplate.query(sql.toString(), new TransactionMapper(), params.toArray());
    }

}
