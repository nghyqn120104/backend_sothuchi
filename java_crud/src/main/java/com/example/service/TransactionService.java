package com.example.service;

import com.example.entity.Transaction;
import com.example.enums.TransactionCategory;
import com.example.repository.AccountDAO;
import com.example.repository.TransactionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    private final AccountService accountService;

    public List<Transaction> getByUserPaged(UUID userId, int page, int size) {
        int offset = page * size;
        return transactionDAO.findAllByUserPaged(userId, size, offset);
    }

    public boolean create(Transaction t) {
        t.setId(UUID.randomUUID());

        if (t.getAccountId() != null) {
            boolean isValidAccount = accountDAO
                    .findById(t.getAccountId())
                    .map(acc -> acc.getUserId().equals(t.getUserId()))
                    .orElse(false);
            if (!isValidAccount)
                return false;
        }

        boolean ok = transactionDAO.insert(t) > 0;
        if (ok && t.getAccountId() != null)
            accountService.recalculateAndUpdateCurrentBalance(t.getAccountId());

        return ok;
    }

    @Transactional
    public boolean update(UUID id, Transaction newT) {
        Optional<Transaction> oldOpt = transactionDAO.findByIdReturnEntity(id);
        if (oldOpt.isEmpty())
            return false;

        Transaction oldT = oldOpt.get();

        if (newT.getAccountId() != null) {
            boolean isValidAccount = accountDAO
                    .findById(newT.getAccountId())
                    .map(acc -> acc.getUserId().equals(newT.getUserId()))
                    .orElse(false);
            if (!isValidAccount)
                return false;
        }

        newT.setId(id);
        boolean ok = transactionDAO.update(newT) > 0;

        // Cập nhật balance cả tài khoản cũ và mới nếu khác
        if (ok) {
            if (oldT.getAccountId() != null)
                accountService.recalculateAndUpdateCurrentBalance(oldT.getAccountId());
            if (newT.getAccountId() != null && !newT.getAccountId().equals(oldT.getAccountId()))
                accountService.recalculateAndUpdateCurrentBalance(newT.getAccountId());
        }

        return ok;
    }

    @Transactional
    public boolean delete(UUID id) {
        Optional<Transaction> transactionOpt = transactionDAO.findByIdReturnEntity(id);
        if (transactionOpt.isEmpty())
            return false;

        Transaction transaction = transactionOpt.get();
        boolean ok = transactionDAO.delete(id) > 0;

        if (ok && transaction.getAccountId() != null) {
            accountService.recalculateAndUpdateCurrentBalance(transaction.getAccountId());
        }

        return ok;
    }

    public boolean deleteByUser(UUID userId) {
        // Lấy toàn bộ giao dịch của user
        List<Transaction> transactions = transactionDAO.findAllByUser(userId);

        // Ghi nhận các accountId bị ảnh hưởng
        Set<UUID> affectedAccountIds = new HashSet<>();
        for (Transaction t : transactions) {
            if (t.getAccountId() != null)
                affectedAccountIds.add(t.getAccountId());
        }

        // Xoá tất cả giao dịch
        boolean ok = transactionDAO.deleteByUserId(userId) > 0;

        // Cập nhật lại balance cho các account liên quan
        if (ok) {
            for (UUID accountId : affectedAccountIds) {
                accountService.recalculateAndUpdateCurrentBalance(accountId);
            }
        }

        return ok;
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
            return 0.0;
        }

        return transactionDAO.sumByUserCategoryAndMonth(userId, transactionCategory, month, year);
    }

    public List<Transaction> searchTransactions(UUID userId,
            LocalDate startDate, LocalDate endDate,
            Double minAmount, Double maxAmount) {
        return transactionDAO.search(userId, startDate, endDate, minAmount, maxAmount);
    }

    public List<Transaction> getByUserAndAccountPaged(UUID userId, UUID accountId, int page, int size) {
        int offset = page * size;
        return transactionDAO.findAllByUserAndAccountPaged(userId, accountId, size, offset);
    }
}