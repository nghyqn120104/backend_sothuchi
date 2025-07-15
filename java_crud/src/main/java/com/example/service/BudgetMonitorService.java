package com.example.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
        List<User> users = userDAO.findAll();
        LocalDate now = LocalDate.now();

        for (User user : users) {
            UUID userId = user.getId();
            String email = user.getEmail();

            List<Budget> budgets = budgetDAO.findByUserAndMonthYear(userId, now.getMonthValue(), now.getYear());

            for (Budget b : budgets) {
                double spent = b.getCategory().name().equals("ALL")
                        ? transactionDAO.sumAllExpensesByUserMonth(userId, now.getMonthValue(), now.getYear())
                        : transactionDAO.sumByUserCategoryAndMonth(userId, b.getCategory(), now.getMonthValue(),
                                now.getYear());

                if (spent > b.getAmount()) {
                    // Nếu chưa từng gửi alert
                    boolean alreadyAlerted = alertDAO.exists(b.getId(), userId, now.getMonthValue(), now.getYear());

                    if (!alreadyAlerted) {
                        // Gửi lần đầu
                        emailService.sendBudgetWarning(email,
                                "⚠️ Cảnh báo vượt ngân sách: " + b.getCategory(),
                                String.format("Bạn đã chi %.0f₫ / ngân sách %.0f₫ cho %s (tháng %d/%d)",
                                        spent, b.getAmount(), b.getCategory(), now.getMonthValue(), now.getYear()));

                        // Lưu alert
                        BudgetAlert alert = new BudgetAlert();
                        alert.setId(UUID.randomUUID());
                        alert.setBudgetId(b.getId());
                        alert.setUserId(userId);
                        alert.setCategory(b.getCategory().name());
                        alert.setMonth(now.getMonthValue());
                        alert.setYear(now.getYear());
                        alert.setAlertDate(now);
                        alert.setStillExceeded(true);
                        alertDAO.insert(alert);
                    } else {
                        // Gửi lại nhắc nhở nếu đã gửi rồi
                        emailService.sendBudgetWarning(email,
                                "⚠️ Nhắc lại: Bạn vẫn vượt ngân sách " + b.getCategory(),
                                String.format("Bạn đã chi %.0f₫ / ngân sách %.0f₫ cho %s (tháng %d/%d)",
                                        spent, b.getAmount(), b.getCategory(), now.getMonthValue(), now.getYear()));
                    }
                } else {
                    // Nếu đã từng gửi cảnh báo mà giờ đã hợp lệ → mark resolved
                    Optional<BudgetAlert> existing = alertDAO.findOne(b.getId(), userId, now.getMonthValue(),
                            now.getYear());
                    existing.ifPresent(a -> alertDAO.markAsResolved(a.getId()));
                }
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
