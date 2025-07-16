package com.example.service;

import com.example.entity.Budget;
import com.example.entity.BudgetAlert;
import com.example.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetMonitorService {
    private final UserService userService;
    private final BudgetService budgetService;
    private final BudgetAlertService alertService;
    private final TransactionService transactionService;
    private final EmailService emailService;

    public void checkBudgets(LocalDate now) {
        int month = now.getMonthValue();
        int year = now.getYear();

        int pageSize = 100;
        int offset = 0;
        while (true) {
            List<User> users = userService.getPaged(pageSize, offset);
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
        List<Budget> budgets = budgetService.filterBudgetsByUserAndMonthYear(userId, month, year);

        for (Budget b : budgets) {
            double spent = b.getCategory().name().equals("ALL")
                    ? Optional.ofNullable(transactionService.sumAllExpensesByUserMonth(userId, month, year)).orElse(0.0)
                    : Optional
                            .ofNullable(transactionService.sumByUserCategoryAndMonth(userId, b.getCategory().name(),
                                    month, year))
                            .orElse(0.0);

            if (spent > b.getAmount()) {
                boolean alreadyAlerted = alertService.getStillExceededAlerts().stream()
                        .anyMatch(a -> a.getBudgetId().equals(b.getId()) && a.getUserId().equals(userId)
                                && a.getMonth() == month && a.getYear() == year);

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
                    alertService.createAlert(alert);
                } else {
                    emailService.sendBudgetWarning(
                            email,
                            "⚠️ Nhắc lại: Bạn vẫn vượt ngân sách " + b.getCategory().name(),
                            buildBudgetAlertBody(user, b, spent, month, year));
                }

            } else {
                Optional<BudgetAlert> existing = alertService.getAlertsForUser(userId).stream()
                        .filter(a -> a.getBudgetId().equals(b.getId()) && a.getMonth() == month && a.getYear() == year)
                        .findFirst();
                existing.ifPresent(a -> alertService.resolveAlert(a.getId()));
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
