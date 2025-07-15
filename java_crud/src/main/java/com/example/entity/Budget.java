package com.example.entity;

import com.example.enums.TransactionCategory;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class Budget {
    private UUID id;
    private UUID userId;
    private TransactionCategory category;
    private double amount;
    private int month;
    private int year;
}
