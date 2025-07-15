package com.example.repository;

import com.example.entity.Budget;
import com.example.mapper.BudgetMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class BudgetDAO {
    private final JdbcTemplate jdbc;

    // Thêm ngân sách
    public int insert(Budget b) {
        String sql = "INSERT INTO budgets (id, user_id, category, amount, month, year) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbc.update(sql,
                b.getId().toString(),
                b.getUserId().toString(),
                b.getCategory().name(),
                b.getAmount(),
                b.getMonth(),
                b.getYear());
    }

    // Cập nhật ngân sách
    public int update(Budget b) {
        String sql = "UPDATE budgets SET category = ?, amount = ?, month = ?, year = ? WHERE id = ?";
        return jdbc.update(sql,
                b.getCategory().name(),
                b.getAmount(),
                b.getMonth(),
                b.getYear(),
                b.getId().toString());
    }

    // Xoá ngân sách
    public int delete(UUID id) {
        return jdbc.update("DELETE FROM budgets WHERE id = ?", id.toString());
    }

    // Lấy theo ID
    public Optional<Budget> findById(UUID id) {
        List<Budget> list = jdbc.query("SELECT * FROM budgets WHERE id = ?", new BudgetMapper(), id.toString());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // Lấy tất cả ngân sách theo user
    public List<Budget> findAllByUser(UUID userId) {
        String sql = "SELECT * FROM budgets WHERE user_id = ?";
        return jdbc.query(sql, new BudgetMapper(), userId.toString());
    }

    // Lấy theo user + tháng + năm
    public List<Budget> findByUserAndMonthYear(UUID userId, int month, int year) {
        System.out.println("Filtering for userId=" + userId + ", month=" + month + ", year=" + year);
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND month = ? AND year = ?";
        return jdbc.query(sql, new BudgetMapper(), userId.toString(), month, year);
    }

    // Lấy theo user + tháng + năm + category (dùng để so sánh thực chi)
    public Optional<Budget> findOne(UUID userId, int month, int year, String category) {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND month = ? AND year = ? AND category = ?";
        List<Budget> list = jdbc.query(sql, new BudgetMapper(), userId.toString(), month, year, category);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // Xoá ngân sách theo user ID
    public void deleteByUserId(UUID id) {
        String sql = "DELETE FROM budgets WHERE user_id = ?";
        jdbc.update(sql, id.toString());
    }

    // Kiểm tra ngân sách đã tồn tại theo user, tháng, năm và category
    // Dùng để tránh trùng lặp category khi thêm ngân sách
    public boolean exists(UUID userId, int month, int year, String category) {
        String sql = "SELECT COUNT(*) FROM budgets WHERE user_id = ? AND month = ? AND year = ? AND category = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class,
                userId.toString(), month, year, category);
        return count != null && count > 0;
    }

}
