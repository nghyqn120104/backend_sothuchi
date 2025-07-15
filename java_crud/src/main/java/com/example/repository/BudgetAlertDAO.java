package com.example.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.BudgetAlert;
import com.example.mapper.BudgetAlertMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BudgetAlertDAO {

    private final JdbcTemplate jdbc;

    public boolean exists(UUID budgetId, UUID userId, int month, int year) {
        String sql = """
                    SELECT COUNT(*) FROM budget_alerts
                    WHERE budget_id = ? AND user_id = ? AND month = ? AND year = ? AND still_exceeded = TRUE
                """;
        Integer count = jdbc.queryForObject(sql, Integer.class,
                budgetId.toString(), userId.toString(), month, year);
        return count != null && count > 0;
    }

    public void insert(BudgetAlert alert) {
        String sql = """
                    INSERT INTO budget_alerts (id, budget_id, user_id, category, month, year, alert_date, still_exceeded)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql,
                alert.getId().toString(),
                alert.getBudgetId().toString(),
                alert.getUserId().toString(),
                alert.getCategory(),
                alert.getMonth(),
                alert.getYear(),
                alert.getAlertDate(),
                alert.isStillExceeded());
    }

    public List<BudgetAlert> findAllStillExceeded() {
        String sql = "SELECT * FROM budget_alerts WHERE still_exceeded = TRUE";
        return jdbc.query(sql, new BudgetAlertMapper());
    }

    public void markAsResolved(UUID id) {
        String sql = "UPDATE budget_alerts SET still_exceeded = FALSE WHERE id = ?";
        jdbc.update(sql, id.toString());
    }

    public Optional<BudgetAlert> findOne(UUID budgetId, UUID userId, int month, int year) {
        String sql = """
                    SELECT * FROM budget_alerts
                    WHERE budget_id = ? AND user_id = ? AND month = ? AND year = ? AND still_exceeded = TRUE
                    LIMIT 1
                """;
        List<BudgetAlert> result = jdbc.query(sql, new BudgetAlertMapper(),
                budgetId.toString(), userId.toString(), month, year);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

}
