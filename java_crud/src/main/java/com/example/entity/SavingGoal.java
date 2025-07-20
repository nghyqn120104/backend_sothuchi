package com.example.entity;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SavingGoal {
    private UUID id;
    private UUID userId;
    private String name;
    private double targetAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID goalAccountId;
    private boolean isCompleted;

}
