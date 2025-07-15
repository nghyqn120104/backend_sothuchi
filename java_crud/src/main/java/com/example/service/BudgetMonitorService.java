package com.example.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.example.entity.User;
import com.example.enums.TransactionCategory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.entity.Budget;
import com.example.entity.BudgetAlert;
import com.example.repository.BudgetAlertDAO;
import com.example.repository.BudgetDAO;
import com.example.repository.TransactionDAO;
import com.example.repository.UserDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetMonitorService {
    private final UserDAO userDAO;
    private final BudgetDAO budgetDAO;
    private final BudgetAlertDAO alertDAO;
    private final TransactionDAO transactionDAO;
    private final EmailService emailService;

    // @Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(fixedRate = 60000)
    public void autoCheckBudgetsDaily() {
        List<BudgetAlert> alerts = alertDAO.findAllStillExceeded();

        for (BudgetAlert alert : alerts) {
            double spent = alert.getCategory().equals("ALL")
                    ? transactionDAO.sumAllExpensesByUserMonth(alert.getUserId(), alert.getMonth(), alert.getYear())
                    : transactionDAO.sumByUserCategoryAndMonth(alert.getUserId(),
                            TransactionCategory.valueOf(alert.getCategory()), alert.getMonth(), alert.getYear());

            if (spent > 0) {
                // Gửi nhắc lại
                String email = userDAO.findById(alert.getUserId()).get().getEmail();
                emailService.sendBudgetWarning(
                        email,
                        "⚠️ Nhắc lại: Bạn vẫn đang vượt ngân sách " + alert.getCategory(),
                        "... nội dung tương tự ...");
            } else {
                alertDAO.markAsResolved(alert.getId());
            }
        }
    }

    public void checkBudgetsAndNotifyAllUsers(int month, int year) {
        List<User> users = userDAO.findAll();

        for (User user : users) {
            UUID userId = user.getId();
            String email = user.getEmail();

            List<Budget> budgets = budgetDAO.findByUserAndMonthYear(userId, month, year);

            for (Budget b : budgets) {
                double spent;
                if (b.getCategory().name().equals("ALL")) {
                    spent = transactionDAO.sumAllExpensesByUserMonth(userId, month, year);
                } else {
                    spent = transactionDAO.sumByUserCategoryAndMonth(userId, b.getCategory(), month, year);
                }

                if (spent > b.getAmount()) {
                    String subject = "⚠️ Bạn đã vượt ngân sách: " + b.getCategory().name();
                    String body = String.format("""
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

                    emailService.sendBudgetWarning(email, subject, body);
                }
            }
        }
    }
}
