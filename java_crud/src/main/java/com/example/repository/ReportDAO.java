package com.example.repository;

import com.example.entity.ReportSummary;
import com.example.mapper.ReportSummaryMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ReportDAO {

    @Autowired
    private JdbcTemplate jdbc;

    public ReportSummary calculateMonthlyReport(UUID userId, int month, int year) {
        String sql = """
                    SELECT
                        SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS total_income,
                        SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS total_expense
                    FROM transactions
                    WHERE user_id = ? AND MONTH(date) = ? AND YEAR(date) = ?
                """;

        return jdbc.queryForObject(
                sql,
                new ReportSummaryMapper(userId, month, year),
                userId.toString(), month, year);
    }

}
