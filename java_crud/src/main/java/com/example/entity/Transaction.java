package com.example.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.example.enums.TransactionCategory;
import com.example.enums.TransactionType;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Transaction {
    private UUID id;
    private String description; // mô tả (VD: "Mua đồ ăn")
    private Double amount; // số tiền
    private LocalDate date; // ngày giao dịch
    private TransactionType type;
    private TransactionCategory category; // thể loại (ăn uống, học tập...)
    private UUID userId;
    private UUID accountId;
}
