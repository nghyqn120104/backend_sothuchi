package com.example.service;

import com.example.dto.SpendingStatisticsDTO;
import com.example.entity.Budget;
import com.example.entity.BudgetAlert;
import com.example.entity.User;
import com.example.enums.TransactionCategory;
import com.example.exception.BudgetAlreadyExistsException;
import com.example.repository.BudgetAlertDAO;
import com.example.repository.BudgetDAO;
import com.example.repository.TransactionDAO;
import com.example.repository.UserDAO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final BudgetAlertDAO alertDAO;
    private final TransactionDAO transactionDAO;
    private final EmailService emailService;

    public boolean createBudget(Budget b) {
        boolean exists = budgetDAO.exists(b.getUserId(), b.getMonth(), b.getYear(), b.getCategory().name());
        if (exists) {
            throw new BudgetAlreadyExistsException("Ngân sách đã tồn tại cho tháng " + b.getMonth()
                    + "/" + b.getYear() + " - loại: " + b.getCategory());
        }

        b.setId(UUID.randomUUID());
        return budgetDAO.insert(b) > 0;
    }

    public boolean updateBudget(Budget b) {
        return budgetDAO.update(b) > 0;
    }

    public boolean deleteBudget(UUID id) {
        return budgetDAO.delete(id) > 0;
    }

    public Optional<Budget> getById(UUID id) {
        return budgetDAO.findById(id);
    }

    public List<Budget> getAllBudgets(UUID userId) {
        return budgetDAO.findAllByUser(userId);
    }

    public List<Budget> filterBudgetsByUserAndMonthYear(UUID userId, int month, int year) {
        return budgetDAO.findByUserAndMonthYear(userId, month, year);
    }

    public Map<String, Object> compareWithActual(UUID userId, TransactionCategory category, int month, int year) {
        Optional<Budget> budgetOpt = budgetDAO.findOne(userId, month, year, category.name());

        if (budgetOpt.isEmpty()) {
            return Map.of("error", "Chưa đặt ngân sách cho loại chi tiêu này");
        }

        Budget budget = budgetOpt.get();
        double spent = transactionDAO.sumByUserCategoryAndMonth(userId, category, month, year);

        if (spent < 0) {
            return Map.of("error", "Không có giao dịch nào cho loại chi tiêu này trong tháng này");
        }

        double usageRate = budget.getAmount() > 0
                ? Math.round((spent / budget.getAmount()) * 10000.0) / 100.0
                : 0;

        if (spent > budget.getAmount()) {
            handleBudgetExceeded(userId, budget, category, month, year);
        } else {
            resolveAlertIfAny(budget.getId(), userId, month, year);
        }

        return Map.of(
                "budgetedAmount", budget.getAmount(),
                "actualSpent", spent,
                "remaining", budget.getAmount() - spent,
                "usageRate", usageRate,
                "status", spent == 0 ? "Chưa có giao dịch ❓"
                        : (spent > budget.getAmount() ? "Vượt ngân sách ⚠️" : "OK ✅"));
    }

    public Map<String, Object> compareAllBudget(UUID userId, int month, int year) {
        Optional<Budget> budgetOpt = budgetDAO.findOne(userId, month, year, "ALL");

        if (budgetOpt.isEmpty()) {
            return Map.of("error", "Chưa đặt ngân sách tổng (ALL) cho tháng này");
        }

        Budget budget = budgetOpt.get();
        double spent = transactionDAO.sumAllExpensesByUserMonth(userId, month, year);
        double usageRate = budget.getAmount() > 0
                ? Math.round((spent / budget.getAmount()) * 10000.0) / 100.0
                : 0;

        return Map.of(
                "budgetedAmount", budget.getAmount(),
                "actualSpent", spent,
                "remaining", budget.getAmount() - spent,
                "usageRate", usageRate,
                "status", spent == 0 ? "Chưa có giao dịch ❓"
                        : (spent > budget.getAmount() ? "Vượt ngân sách tổng ⚠️" : "OK ✅"));
    }

    public SpendingStatisticsDTO getSpendingStatistics(UUID userId, int month, int year) {
        return transactionDAO.getSpendingStatistics(userId, month, year);
    }

    // ==== PRIVATE HELPERS ====

    private void handleBudgetExceeded(UUID userId, Budget budget, TransactionCategory category, int month, int year) {
        boolean alreadyAlerted = alertDAO.exists(budget.getId(), userId, month, year);
        if (alreadyAlerted)
            return;

        userDAO.findById(userId).map(User::getEmail).ifPresent(email -> {
            if (!email.isEmpty()) {
                emailService.sendBudgetWarning(email,
                        "⚠️ Cảnh báo vượt ngân sách " + category,
                        "Bạn đã vượt quá ngân sách cho loại chi tiêu " + category +
                                " trong tháng " + month + "/" + year + ".");
            }
        });

        BudgetAlert alert = new BudgetAlert();
        alert.setId(UUID.randomUUID());
        alert.setBudgetId(budget.getId());
        alert.setUserId(userId);
        alert.setCategory(category.name());
        alert.setMonth(month);
        alert.setYear(year);
        alert.setAlertDate(LocalDate.now());
        alert.setStillExceeded(true);

        alertDAO.insert(alert);
    }

    private void resolveAlertIfAny(UUID budgetId, UUID userId, int month, int year) {
        alertDAO.findOne(budgetId, userId, month, year)
                .ifPresent(a -> alertDAO.markAsResolved(a.getId()));
    }
}
