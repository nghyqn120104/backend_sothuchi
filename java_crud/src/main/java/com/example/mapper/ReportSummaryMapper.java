package com.example.mapper;

import com.example.entity.ReportSummary;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@RequiredArgsConstructor
@Data
public class ReportSummaryMapper implements RowMapper<ReportSummary> {
    private final UUID userId;
    private final int month;
    private final int year;

    @Override
    public ReportSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        double income = rs.getDouble("total_income");
        double expense = rs.getDouble("total_expense");

        ReportSummary r = new ReportSummary();
        r.setUserId(userId);
        r.setMonth(month);
        r.setYear(year);
        r.setTotalIncome(income);
        r.setTotalExpense(expense);
        r.setBalance(income - expense);
        return r;
    }
}
