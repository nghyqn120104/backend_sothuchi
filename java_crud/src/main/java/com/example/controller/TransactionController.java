package com.example.controller;

import com.example.entity.Transaction;
import com.example.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/user/{userId}")
    public List<Transaction> getByUser(@PathVariable UUID userId) {
        return transactionService.getByUser(userId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Transaction t) {
        boolean ok = transactionService.create(t);
        return ok ? ResponseEntity.ok("Đã thêm giao dịch") : ResponseEntity.badRequest().body("Thêm thất bại");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Transaction transaction) {
        boolean ok = transactionService.update(id, transaction);
        return ok ? ResponseEntity.ok("Cập nhật thành công") : ResponseEntity.badRequest().body("Cập nhật thất bại");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = transactionService.delete(id);
        return ok ? ResponseEntity.ok("Đã xoá giao dịch") : ResponseEntity.badRequest().body("Xoá thất bại");
    }
}
