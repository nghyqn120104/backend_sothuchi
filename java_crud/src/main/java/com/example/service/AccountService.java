package com.example.service;

import com.example.entity.Account;
import com.example.repository.AccountDAO;
import com.example.repository.TransactionDAO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final SavingGoalService savingGoalService;

    public List<Account> getByUser(UUID userId) {
        return accountDAO.findAllByUser(userId);
    }

    public Optional<Account> getById(UUID id) {
        return accountDAO.findById(id);
    }

    public boolean create(Account account) {
        if (accountDAO.existsByNameAndUser(account.getName(), account.getUserId())) {
            return false; // báo trùng tên
        }
        account.setId(UUID.randomUUID());
        return accountDAO.insert(account) > 0;
    }

    public boolean update(UUID id, Account account) {
        Optional<Account> existingOpt = accountDAO.findById(id);
        if (existingOpt.isEmpty())
            return false;

        Account existing = existingOpt.get();

        // Nếu tên mới khác tên cũ → kiểm tra trùng
        if (!existing.getName().equalsIgnoreCase(account.getName())) {
            if (accountDAO.existsByNameAndUser(account.getName(), account.getUserId())) {
                return false; // Trùng tên với account khác cùng user
            }
        }

        account.setId(id);
        return accountDAO.update(account) > 0;
    }

    public boolean delete(UUID id) {
        Optional<Account> accOpt = accountDAO.findById(id);
        if (accOpt.isEmpty())
            return false;

        Account acc = accOpt.get();

        // Thêm ghi chú mô tả
        String note = "Trước đây thuộc tài khoản: " + acc.getName();
        transactionDAO.detachAccountAndNote(id, note);

        return accountDAO.delete(id) > 0;
    }

    @Transactional
    public boolean transfer(UUID fromAccountId, UUID toAccountId, double amount) {
        if (amount <= 0)
            return false;

        Optional<Account> fromOpt = accountDAO.findById(fromAccountId);
        Optional<Account> toOpt = accountDAO.findById(toAccountId);

        if (fromOpt.isEmpty() || toOpt.isEmpty())
            return false;

        Account from = fromOpt.get();
        Account to = toOpt.get();

        if (from.getInitialBalance() < amount)
            return false;

        from.setInitialBalance(from.getInitialBalance() - amount);
        to.setInitialBalance(to.getInitialBalance() + amount);

        accountDAO.update(from);
        accountDAO.update(to);
        return true;
    }

    // Lấy thông tin số dư tài khoản
    // Bao gồm: số dư ban đầu, tổng thu nhập, tổng chi tiêu, số dư hiện tại
    // Số dư hiện tại = Số dư ban đầu + Tổng thu nhập - Tổng
    public Map<String, Object> getBalanceInfo(UUID accountId) {
        Optional<Account> accOpt = accountDAO.findById(accountId);
        if (accOpt.isEmpty())
            return Map.of("error", "Account not found");

        Account acc = accOpt.get();
        double income = transactionDAO.sumIncomeByAccount(accountId);
        double expense = transactionDAO.sumExpenseByAccount(accountId);
        double currentBalance = acc.getInitialBalance() + income - expense;

        savingGoalService.checkGoalCompletion(accountId);

        return Map.of(
                "initialBalance", acc.getInitialBalance(),
                "totalIncome", income,
                "totalExpense", expense,
                "currentBalance", currentBalance);
    }

    public void recalculateAndUpdateCurrentBalance(UUID accountId) {
        Optional<Account> accOpt = accountDAO.findById(accountId);
        if (accOpt.isEmpty())
            return;

        double income = transactionDAO.sumIncomeByAccount(accountId);
        double expense = transactionDAO.sumExpenseByAccount(accountId);
        double initial = accOpt.get().getInitialBalance();

        double current = initial + income - expense;
        accountDAO.updateCurrentBalance(accountId, current);
    }

}
