package com.example.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.entity.Budget;
import com.example.entity.BudgetAlert;
import com.example.entity.User;
import com.example.repository.BudgetAlertDAO;
import com.example.repository.BudgetDAO;
import com.example.repository.TransactionDAO;
import com.example.repository.UserDAO;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudgetMonitorService {
    private final UserDAO userDAO;
    private final BudgetDAO budgetDAO;
    private final BudgetAlertDAO alertDAO;
    private final TransactionDAO transactionDAO;
    private final EmailService emailService;

    // Chạy 8h sáng mỗi ngày
    @Scheduled(cron = "0 0 8 * * ?")
    // Dùng để test mỗi 60s
    // @Scheduled(fixedRate = 60000)
    public void autoCheckBudgetsDaily() {
        List<User> users = userDAO.findAll();
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        for (User user : users) {
            UUID userId = user.getId();
            String email = user.getEmail();
            List<Budget> budgets = budgetDAO.findByUserAndMonthYear(userId, month, year);

            for (Budget b : budgets) {
                double spent = b.getCategory().name().equals("ALL")
                        ? transactionDAO.sumAllExpensesByUserMonth(userId, month, year)
                        : transactionDAO.sumByUserCategoryAndMonth(userId, b.getCategory(), month, year);

                if (spent > b.getAmount()) {
                    boolean alreadyAlerted = alertDAO.exists(b.getId(), userId, month, year);

                    if (!alreadyAlerted) {
                        // Gửi cảnh báo lần đầu
                        emailService.sendBudgetWarning(
                                email,
                                "⚠️ Bạn đã vượt ngân sách: " + b.getCategory().name(),
                                buildBudgetAlertBody(user, b, spent, month, year));

                        // Ghi nhận alert
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
                        // Gửi nhắc lại nếu đã từng cảnh báo
                        emailService.sendBudgetWarning(
                                email,
                                "⚠️ Nhắc lại: Bạn vẫn vượt ngân sách " + b.getCategory().name(),
                                buildBudgetAlertBody(user, b, spent, month, year));
                    }

                } else {
                    // Nếu chi tiêu đã hợp lý => đánh dấu alert đã resolved
                    Optional<BudgetAlert> existing = alertDAO.findOne(b.getId(), userId, month, year);
                    existing.ifPresent(a -> alertDAO.markAsResolved(a.getId()));
                }
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
