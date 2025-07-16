package com.example.service;

import com.example.entity.Transaction;
import com.example.enums.TransactionCategory;
import com.example.repository.TransactionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionDAO transactionDAO;

    public List<Transaction> getByUser(UUID userId) {
        return transactionDAO.findAllByUser(userId);
    }

    public boolean create(Transaction t) {
        t.setId(UUID.randomUUID());
        return transactionDAO.insert(t) > 0;
    }

    public boolean update(UUID id, Transaction t) {
        if (!transactionDAO.findById(id))
            return false;
        t.setId(id);
        return transactionDAO.update(t) > 0;
    }

    public boolean delete(UUID id) {
        if (!transactionDAO.findById(id))
            return false;
        return transactionDAO.delete(id) > 0;
    }

    public boolean deleteByUser(UUID userId) {
        return transactionDAO.deleteByUserId(userId) > 0;
    }

    public boolean exists(UUID id) {
        return transactionDAO.findById(id);
    }

    public double sumAllExpensesByUserMonth(UUID userId, int month, int year) {
        return transactionDAO.sumAllExpensesByUserMonth(userId, month, year);
    }

    public double sumByUserCategoryAndMonth(UUID userId, String category, int month, int year) {
        TransactionCategory transactionCategory;

        try {
            transactionCategory = TransactionCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return 0.0; // Invalid category string, not in enum
        }

        return transactionDAO.sumByUserCategoryAndMonth(userId, transactionCategory, month, year);
    }

    public List<Transaction> searchTransactions(UUID userId,
            LocalDate startDate, LocalDate endDate,
            Double minAmount, Double maxAmount) {
        return transactionDAO.search(userId, startDate, endDate, minAmount, maxAmount);
    }

}
