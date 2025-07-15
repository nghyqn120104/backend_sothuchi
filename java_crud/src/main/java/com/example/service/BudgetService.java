package com.example.service;

import com.example.entity.Budget;
import com.example.entity.BudgetAlert;
import com.example.enums.TransactionCategory;
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

    // Tạo ngân sách
    public boolean createBudget(Budget b) {
        b.setId(UUID.randomUUID());
        return budgetDAO.insert(b) > 0;
    }

    // Cập nhật ngân sách
    public boolean updateBudget(Budget b) {
        return budgetDAO.update(b) > 0;
    }

    // Xoá ngân sách
    public boolean deleteBudget(UUID id) {
        return budgetDAO.delete(id) > 0;
    }

    // Lấy ngân sách theo ID
    public Optional<Budget> getById(UUID id) {
        return budgetDAO.findById(id);
    }

    // Lấy tất cả ngân sách theo user
    public List<Budget> getAllBudgets(UUID userId) {
        return budgetDAO.findAllByUser(userId);
    }

    // Lọc theo tháng, năm
    public List<Budget> filterBudgetsByUserAndMonthYear(UUID userId, int month, int year) {
        return budgetDAO.findByUserAndMonthYear(userId, month, year);
    }

    // So sánh ngân sách với thực chi
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

        BudgetAlert alert = new BudgetAlert();
        if (spent > budget.getAmount()) {
            boolean alreadyAlerted = alertDAO.exists(budget.getId(), userId, month, year);

            if (!alreadyAlerted) {
                // Gửi email lần đầu
                String email = userDAO.findById(userId)
                        .map(user -> user.getEmail())
                        .orElse(null);

                if (email != null && !email.isEmpty()) {
                    emailService.sendBudgetWarning(email,
                            "⚠️ Cảnh báo vượt ngân sách " + category,
                            "Bạn đã vượt quá ngân sách cho loại chi tiêu " + category +
                                    " trong tháng " + month + "/" + year + ".");
                }

                // Lưu alert
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

        } else {
            // Nếu đã từng cảnh báo => đánh dấu đã giải quyết
            Optional<BudgetAlert> existing = alertDAO.findOne(budget.getId(), userId, month, year);
            existing.ifPresent(a -> alertDAO.markAsResolved(a.getId()));
        }

        return Map.of(
                "budgetedAmount", budget.getAmount(),
                "actualSpent", spent,
                "remaining", budget.getAmount() - spent,
                "usageRate", usageRate,
                "status", spent > budget.getAmount() ? "Vượt ngân sách ⚠️" : "OK ✅");
    }

    // So sánh ngân sách tổng với thực chi
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
                "status", spent > budget.getAmount() ? "Vượt ngân sách tổng ⚠️" : "OK ✅");
    }

}
