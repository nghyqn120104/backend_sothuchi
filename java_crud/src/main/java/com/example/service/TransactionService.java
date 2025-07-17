package com.example.service;

import com.example.entity.Transaction;
import com.example.enums.TransactionCategory;
import com.example.enums.TransactionType;
import com.example.repository.AccountDAO;
import com.example.repository.TransactionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    public List<Transaction> getByUserPaged(UUID userId, int page, int size) {
        int offset = page * size;
        return transactionDAO.findAllByUserPaged(userId, size, offset);
    }

    public boolean create(Transaction t) {
        t.setId(UUID.randomUUID());

        // 🔒 Kiểm tra accountId thuộc về user
        if (t.getAccountId() != null) {
            boolean isValidAccount = accountDAO
                    .findById(t.getAccountId())
                    .map(acc -> acc.getUserId().equals(t.getUserId()))
                    .orElse(false);
            if (!isValidAccount)
                return false;
        }

        boolean ok = transactionDAO.insert(t) > 0;

        if (ok && t.getAccountId() != null) {
            accountDAO.findById(t.getAccountId()).ifPresent(account -> {
                double delta = t.getType() == TransactionType.INCOME ? t.getAmount() : -t.getAmount();
                account.setBalance(account.getBalance() + delta);
                accountDAO.update(account);
            });
        }

        return ok;
    }

    @Transactional
    public boolean update(UUID id, Transaction newT) {
        Optional<Transaction> oldOpt = transactionDAO.findByIdReturnEntity(id);
        if (oldOpt.isEmpty())
            return false;

        Transaction old = oldOpt.get();

        // 🔒 Kiểm tra account mới (nếu có) có thuộc user không
        if (newT.getAccountId() != null) {
            boolean isValidAccount = accountDAO
                    .findById(newT.getAccountId())
                    .map(acc -> acc.getUserId().equals(newT.getUserId()))
                    .orElse(false);
            if (!isValidAccount)
                return false;
        }

        // ✅ Hoàn lại tiền cho account cũ
        if (old.getAccountId() != null) {
            accountDAO.findById(old.getAccountId()).ifPresent(account -> {
                double delta = switch (old.getType()) {
                    case INCOME -> -old.getAmount();
                    case EXPENSE -> old.getAmount();
                    default -> 0;
                };
                account.setBalance(account.getBalance() + delta);
                accountDAO.update(account);
            });
        }

        // ✅ Áp dụng tiền cho account mới
        if (newT.getAccountId() != null) {
            accountDAO.findById(newT.getAccountId()).ifPresent(account -> {
                double delta = switch (newT.getType()) {
                    case INCOME -> newT.getAmount();
                    case EXPENSE -> -newT.getAmount();
                    default -> 0;
                };
                account.setBalance(account.getBalance() + delta);
                accountDAO.update(account);
            });
        }

        newT.setId(id);
        return transactionDAO.update(newT) > 0;
    }

    @Transactional
    public boolean delete(UUID id) {
        Optional<Transaction> transactionOpt = transactionDAO.findByIdReturnEntity(id);
        if (transactionOpt.isEmpty())
            return false;

        Transaction t = transactionOpt.get();

        // Xóa transaction trước
        boolean ok = transactionDAO.delete(id) > 0;

        if (ok && t.getAccountId() != null) {
            accountDAO.findById(t.getAccountId()).ifPresent(account -> {
                double delta = switch (t.getType()) {
                    case INCOME -> -t.getAmount(); // Hoàn lại tiền đã cộng
                    case EXPENSE -> t.getAmount(); // Hoàn lại tiền đã trừ
                    default -> 0; // Nếu type không rõ, không thay đổi
                };

                account.setBalance(account.getBalance() + delta);
                accountDAO.update(account);
            });
        }

        return ok;
    }

    public boolean deleteByUser(UUID userId) {
        List<Transaction> transactions = transactionDAO.findAllByUser(userId);

        // Hoàn lại số dư
        for (Transaction t : transactions) {
            if (t.getAccountId() != null) {
                accountDAO.findById(t.getAccountId()).ifPresent(account -> {
                    double delta = t.getType() == TransactionType.INCOME ? -t.getAmount() : t.getAmount();
                    account.setBalance(account.getBalance() + delta);
                    accountDAO.update(account);
                });
            }
        }

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
