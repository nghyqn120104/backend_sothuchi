package com.example.service;

import com.example.entity.Account;
import com.example.entity.SavingGoal;
import com.example.repository.AccountDAO;
import com.example.repository.SavingGoalDAO;
import com.example.repository.TransactionDAO;
import com.example.repository.UserDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavingGoalService {
    private final SavingGoalDAO savingGoalDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    private final EmailService emailService;

    public Optional<SavingGoal> getById(UUID id) {
        return savingGoalDAO.findById(id);
    }

    public List<SavingGoal> getByUser(UUID userId) {
        return savingGoalDAO.findByUser(userId);
    }

    public List<SavingGoal> getAll() {
        return savingGoalDAO.findAll();
    }

    public boolean create(SavingGoal goal) {
        UUID goalId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        // Tạo tài khoản đi kèm goal
        Account account = new Account();
        account.setId(accountId);
        account.setUserId(goal.getUserId());
        account.setName(goal.getName());
        account.setType("Saving");
        account.setInitialBalance(0.0);

        // Lưu account vào DB
        int accountResult = accountDAO.insert(account);
        if (accountResult <= 0)
            return false;

        // Gán accountId cho goal
        goal.setId(goalId);
        goal.setGoalAccountId(accountId);

        return savingGoalDAO.insert(goal) > 0;
    }

    public boolean update(UUID id, SavingGoal updated) {
        Optional<SavingGoal> goalOpt = savingGoalDAO.findById(id);
        if (goalOpt.isEmpty())
            return false;

        SavingGoal existing = goalOpt.get();

        updated.setId(id);
        updated.setGoalAccountId(existing.getGoalAccountId());

        // Nếu tên SavingGoal thay đổi → cập nhật tên Account
        if (!existing.getName().equalsIgnoreCase(updated.getName())) {
            Optional<Account> accOpt = accountDAO.findById(existing.getGoalAccountId());
            accOpt.ifPresent(acc -> {
                acc.setName(updated.getName()); // đồng bộ tên
                accountDAO.update(acc);
            });
        }

        return savingGoalDAO.update(updated) > 0;
    }

    public boolean delete(UUID id) {
        Optional<SavingGoal> existingOpt = savingGoalDAO.findById(id);
        if (existingOpt.isEmpty())
            return false;

        SavingGoal sg = existingOpt.get();

        // Xóa saving_goal trước
        boolean deleted = savingGoalDAO.delete(id) > 0;

        if (deleted) {
            // Sau đó xóa luôn account đi kèm
            String note = "Trước đây thuộc goal: " + sg.getName();
            transactionDAO.detachAccountAndNote(sg.getGoalAccountId(), note);
            accountDAO.delete(sg.getGoalAccountId());
        }

        return deleted;
    }

    public void checkGoalCompletion(UUID accountId) {
        Optional<SavingGoal> sgOpt = savingGoalDAO.findByAccountId(accountId);
        if (sgOpt.isEmpty())
            return;

        SavingGoal goal = sgOpt.get();

        // Nếu đã hoàn thành → bỏ qua
        if (goal.isCompleted())
            return;

        Optional<Account> accOpt = accountDAO.findById(accountId);
        if (accOpt.isEmpty())
            return;

        double currentBalance = accOpt.get().getCurrentBalance(); // dùng balance đã tính trước
        if (currentBalance >= goal.getTargetAmount()) {
            String userEmail = userDAO.findById(goal.getUserId())
                    .map(user -> user.getEmail())
                    .orElse("");
            if (userEmail.isBlank()) {
                log.warn("⚠ Không có email để gửi cho userId: " + goal.getUserId());
                return;
            }

            String subject = "🎯 Mục tiêu \"" + goal.getName() + "\" đã hoàn thành";
            String body = "Bạn đã tiết kiệm được " + currentBalance + " VNĐ cho mục tiêu \"" + goal.getName() + "\".";

            emailService.sendBudgetWarning(userEmail, subject, body);

            // ✅ Đánh dấu đã gửi
            goal.setCompleted(true);
            savingGoalDAO.updateCompletionStatus(goal.getId(), true);
        }
    }

}
