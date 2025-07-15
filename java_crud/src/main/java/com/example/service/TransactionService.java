package com.example.service;

import com.example.entity.Transaction;
import com.example.repository.TransactionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
