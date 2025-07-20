package com.example.cronjob;

import com.example.entity.SavingGoal;
import com.example.service.AccountService;
import com.example.service.SavingGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SavingGoalCheckJob {

    private final SavingGoalService savingGoalService;
    private final AccountService accountService;

    // Chạy mỗi ngày lúc 3:00 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void checkAllGoals() {
        log.info("🔄 Kiểm tra toàn bộ mục tiêu tiết kiệm...");

        List<SavingGoal> allGoals = savingGoalService.getAll();
        for (SavingGoal goal : allGoals) {
            accountService.recalculateAndUpdateCurrentBalance(goal.getGoalAccountId());
            savingGoalService.checkGoalCompletion(goal.getGoalAccountId());
        }
    }
}
