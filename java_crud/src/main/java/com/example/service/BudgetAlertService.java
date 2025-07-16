package com.example.service;

import com.example.entity.BudgetAlert;
import com.example.repository.BudgetAlertDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetAlertService {

    private final BudgetAlertDAO budgetAlertDAO;

    /**
     * Tạo cảnh báo nếu chưa tồn tại alert trùng (cùng user, budget, tháng, năm)
     */
    public void createAlert(BudgetAlert alert) {
        boolean alreadyExists = budgetAlertDAO.exists(
                alert.getBudgetId(),
                alert.getUserId(),
                alert.getMonth(),
                alert.getYear()
        );

        if (!alreadyExists) {
            budgetAlertDAO.insert(alert);
        }
        // Nếu đã tồn tại, không insert để tránh trùng lặp
    }

    /**
     * Đánh dấu alert đã được xử lý
     */
    public void resolveAlert(UUID alertId) {
        budgetAlertDAO.markAsResolved(alertId);
    }

    /**
     * Lấy toàn bộ alert của 1 user (dù đã resolved hay chưa)
     */
    public List<BudgetAlert> getAlertsForUser(UUID userId) {
        return budgetAlertDAO.findByUserId(userId);
    }

    /**
     * (Tuỳ chọn mở rộng) Lấy các alert vẫn đang vi phạm ngân sách
     */
    public List<BudgetAlert> getStillExceededAlerts() {
        return budgetAlertDAO.findAllStillExceeded();
    }
}
