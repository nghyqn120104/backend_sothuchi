package com.example.service;

import com.example.entity.Account;
import com.example.repository.AccountDAO;
import com.example.repository.TransactionDAO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

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
        // Xoá tài khoản → gỡ liên kết trong transaction
        transactionDAO.clearAccountId(id); // 👇 bước này cần thêm
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

        if (from.getBalance() < amount)
            return false;

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        accountDAO.update(from);
        accountDAO.update(to);
        return true;
    }
}
