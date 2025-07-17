package com.example.controller;

import com.example.entity.Transaction;
import com.example.service.TransactionReportService;
import com.example.service.TransactionService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionReportService reportService;

    @GetMapping("/user/{userId}")
    public List<Transaction> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return transactionService.getByUserPaged(userId, page, size);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Transaction t) {
        boolean ok = transactionService.create(t);
        return ok
                ? ResponseEntity.ok("Đã thêm giao dịch")
                : ResponseEntity.badRequest().body("Tài khoản không hợp lệ hoặc không thuộc về người dùng");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Transaction transaction) {
        boolean ok = transactionService.update(id, transaction);
        return ok
                ? ResponseEntity.ok("Cập nhật thành công")
                : ResponseEntity.badRequest().body("Tài khoản không hợp lệ hoặc không thuộc về người dùng");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = transactionService.delete(id);
        return ok ? ResponseEntity.ok("Đã xoá giao dịch") : ResponseEntity.badRequest().body("Xoá thất bại");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTransactions(
            @RequestParam UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        List<Transaction> results = transactionService.searchTransactions(userId, startDate, endDate, minAmount,
                maxAmount);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam UUID userId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "pdf") String format) {
        byte[] data = reportService.generateSpendingReport(userId, month, year, format);

        String contentType = format.equals("excel")
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/pdf";
        String fileName = "spending-report-" + month + "-" + year + "." + format;

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping("/user/{userId}/account/{accountId}")
    public List<Transaction> getByUserAndAccount(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return transactionService.getByUserAndAccountPaged(userId, accountId, page, size);
    }

}
