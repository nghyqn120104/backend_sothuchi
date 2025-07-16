package com.example.cronjob;

import com.example.service.BudgetMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BudgetMonitorJob {
    private final BudgetMonitorService budgetMonitorService;

    // Chạy lúc 8h sáng mỗi ngày
    // @Scheduled(cron = "0 0 8 * * ?")
    // Dùng để test
    @Scheduled(fixedRate = 60000)
    public void runDailyBudgetCheck() {
        budgetMonitorService.checkBudgets(LocalDate.now());
    }
}
