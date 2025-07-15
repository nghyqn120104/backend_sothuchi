package com.example.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class BudgetAlert {
    private UUID id;
    private UUID budgetId;
    private UUID userId;
    private String category;
    private int month;
    private int year;
    private LocalDate alertDate;
    private boolean stillExceeded;
}
