package com.example.repository;

import com.example.entity.SavingGoal;
import com.example.mapper.SavingGoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class SavingGoalDAO {
    private final JdbcTemplate jdbc;

    public int insert(SavingGoal sg) {
        String sql = """
                    INSERT INTO saving_goals (id, user_id, name, target_amount, start_date, end_date, account_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbc.update(sql,
                sg.getId().toString(),
                sg.getUserId().toString(),
                sg.getName(),
                sg.getTargetAmount(),
                sg.getStartDate(),
                sg.getEndDate(),
                sg.getGoalAccountId().toString());
    }

    public int update(SavingGoal sg) {
        String sql = """
                    UPDATE saving_goals SET name = ?, target_amount = ?, start_date = ?, end_date = ?, account_id = ?
                    WHERE id = ?
                """;
        return jdbc.update(sql,
                sg.getName(),
                sg.getTargetAmount(),
                sg.getStartDate(),
                sg.getEndDate(),
                sg.getGoalAccountId().toString(),
                sg.getId().toString());
    }

    public int delete(UUID id) {
        return jdbc.update("DELETE FROM saving_goals WHERE id = ?", id.toString());
    }

    public int deleteByUserId(UUID userId) {
        return jdbc.update("DELETE FROM saving_goals WHERE user_id = ?", userId.toString());
    }

    public Optional<SavingGoal> findById(UUID id) {
        List<SavingGoal> list = jdbc.query("SELECT * FROM saving_goals WHERE id = ?",
                new SavingGoalMapper(), id.toString());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<SavingGoal> findByUser(UUID userId) {
        String sql = "SELECT * FROM saving_goals WHERE user_id = ? ORDER BY end_date";
        return jdbc.query(sql, new SavingGoalMapper(), userId.toString());
    }

    public Optional<SavingGoal> findByAccountId(UUID accountId) {
        List<SavingGoal> list = jdbc.query("SELECT * FROM saving_goals WHERE account_id = ?",
                new SavingGoalMapper(), accountId.toString());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<SavingGoal> findAll() {
        String sql = "SELECT * FROM saving_goals";
        return jdbc.query(sql, new SavingGoalMapper());
    }

    public int updateCompletionStatus(UUID goalId, boolean isCompleted) {
        String sql = "UPDATE saving_goals SET is_completed = ? WHERE id = ?";
        return jdbc.update(sql, isCompleted, goalId.toString());
    }

}
