package com.example.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ReportSummary {
    private UUID userId;
    private int month;
    private int year;
    private double totalIncome;
    private double totalExpense;
    private double balance;
}
