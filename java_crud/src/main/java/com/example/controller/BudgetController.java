package com.example.controller;

import com.example.entity.Budget;
import com.example.service.BudgetService;

import lombok.RequiredArgsConstructor;

import com.example.enums.TransactionCategory;
import com.example.exception.BudgetAlreadyExistsException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    // Tạo ngân sách
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Budget b) {
        try {
            boolean ok = budgetService.createBudget(b);
            return ok ? ResponseEntity.ok("Tạo ngân sách thành công")
                    : ResponseEntity.badRequest().body("Tạo thất bại");
        } catch (BudgetAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật ngân sách
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Budget b) {
        Optional<Budget> existing = budgetService.getById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        b.setId(id);
        boolean ok = budgetService.updateBudget(b);
        return ok
                ? ResponseEntity.ok("Cập nhật ngân sách thành công")
                : ResponseEntity.badRequest().body("Cập nhật thất bại");
    }

    // Xoá ngân sách
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = budgetService.deleteBudget(id);
        return ok
                ? ResponseEntity.ok("Xoá ngân sách thành công")
                : ResponseEntity.status(404).body("Không tìm thấy ngân sách để xoá");
    }

    // Lấy ngân sách theo ID
    @GetMapping("/user/{userId}/budget/{id}")
    public ResponseEntity<Budget> getBudgetDetail(
            @PathVariable UUID userId,
            @PathVariable UUID id) {
        Optional<Budget> found = budgetService.getById(id);

        if (found.isEmpty() || !found.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(found.get());
    }

    // Lấy tất cả ngân sách của người dùng
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<Budget>> getAllBudgetsOfUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                budgetService.getAllBudgets(userId));
    }

    // Lọc ngân sách theo tháng và năm
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getBudgetsByUserAndMonthYear(
            @PathVariable UUID userId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                budgetService.filterBudgetsByUserAndMonthYear(userId, month, year));
    }

    // So sánh ngân sách với thực chi
    @GetMapping("/compare")
    public ResponseEntity<?> compareBudget(
            @RequestParam UUID userId,
            @RequestParam TransactionCategory category,
            @RequestParam int month,
            @RequestParam int year) {
        Map<String, Object> result = budgetService.compareWithActual(userId, category, month, year);
        return result.containsKey("error") ? ResponseEntity.badRequest().body(result.get("error"))
                : ResponseEntity.ok(result);
    }

    // So sánh ngân sách tổng với thực chi
    @GetMapping("/compare/all")
    public ResponseEntity<?> compareBudgetAll(
            @RequestParam UUID userId,
            @RequestParam int month,
            @RequestParam int year) {
        Map<String, Object> result = budgetService.compareAllBudget(userId, month, year);
        return result.containsKey("error")
                ? ResponseEntity.badRequest().body(result.get("error"))
                : ResponseEntity.ok(result);
    }

}
