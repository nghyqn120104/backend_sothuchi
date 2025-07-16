package com.example.service;

import com.example.entity.Budget;
import com.example.entity.BudgetAlert;
import com.example.entity.User;
import com.example.repository.BudgetAlertDAO;
import com.example.repository.BudgetDAO;
import com.example.repository.TransactionDAO;
import com.example.repository.UserDAO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetMonitorService {
    private final UserDAO userDAO;
    private final BudgetDAO budgetDAO;
    private final BudgetAlertDAO alertDAO;
    private final TransactionDAO transactionDAO;
    private final EmailService emailService;

    public void checkBudgets(LocalDate now) {
        int month = now.getMonthValue();
        int year = now.getYear();

        int pageSize = 100;
        int offset = 0;
        while (true) {
            List<User> users = userDAO.findPaged(pageSize, offset);
            if (users.isEmpty())
                break;

            for (User user : users) {
                checkBudgetForUser(user, now, month, year);
            }

            offset += pageSize;
        }

    }

    private void checkBudgetForUser(User user, LocalDate now, int month, int year) {
        UUID userId = user.getId();
        String email = user.getEmail();
        List<Budget> budgets = budgetDAO.findByUserAndMonthYear(userId, month, year);

        for (Budget b : budgets) {
            double spent = b.getCategory().name().equals("ALL")
                    ? Optional.ofNullable(transactionDAO.sumAllExpensesByUserMonth(userId, month, year)).orElse(0.0)
                    : Optional
                            .ofNullable(transactionDAO.sumByUserCategoryAndMonth(userId, b.getCategory(), month, year))
                            .orElse(0.0);

            if (spent > b.getAmount()) {
                boolean alreadyAlerted = alertDAO.exists(b.getId(), userId, month, year);

                if (!alreadyAlerted) {
                    emailService.sendBudgetWarning(
                            email,
                            "⚠️ Bạn đã vượt ngân sách: " + b.getCategory().name(),
                            buildBudgetAlertBody(user, b, spent, month, year));

                    BudgetAlert alert = new BudgetAlert();
                    alert.setId(UUID.randomUUID());
                    alert.setBudgetId(b.getId());
                    alert.setUserId(userId);
                    alert.setCategory(b.getCategory().name());
                    alert.setMonth(month);
                    alert.setYear(year);
                    alert.setAlertDate(now);
                    alert.setStillExceeded(true);
                    alertDAO.insert(alert);
                } else {
                    emailService.sendBudgetWarning(
                            email,
                            "⚠️ Nhắc lại: Bạn vẫn vượt ngân sách " + b.getCategory().name(),
                            buildBudgetAlertBody(user, b, spent, month, year));
                }

            } else {
                Optional<BudgetAlert> existing = alertDAO.findOne(b.getId(), userId, month, year);
                existing.ifPresent(a -> alertDAO.markAsResolved(a.getId()));
            }
        }
    }

    private String buildBudgetAlertBody(User user, Budget b, double spent, int month, int year) {
        return String.format("""
                Xin chào %s,

                Bạn đã vượt ngân sách %s trong tháng %d/%d.
                - Ngân sách: %.0fđ
                - Đã chi: %.0fđ
                - Chênh lệch: %.0fđ

                Vui lòng kiểm soát lại chi tiêu nhé!

                Trân trọng,
                Sổ Thu Chi
                """, user.getUsername(), b.getCategory().name(), month, year, b.getAmount(), spent,
                spent - b.getAmount());
    }
}
