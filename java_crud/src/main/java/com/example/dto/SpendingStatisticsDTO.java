package com.example.dto;

import java.util.Map;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SpendingStatisticsDTO {
    private double totalSpent;
    private Map<String, Double> categoryBreakdown;
    private String topSpendingCategory;
    private Map<String, Double> categoryPercentage;
}
