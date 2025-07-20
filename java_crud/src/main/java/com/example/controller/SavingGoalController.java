package com.example.controller;

import com.example.entity.SavingGoal;
import com.example.service.SavingGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/saving-goals")
@RequiredArgsConstructor
public class SavingGoalController {
    private final SavingGoalService service;

    @GetMapping("/user/{userId}")
    public List<SavingGoal> getByUser(@PathVariable UUID userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SavingGoal goal) {
        boolean ok = service.create(goal);
        return ok ? ResponseEntity.ok("Tạo mục tiêu thành công")
                : ResponseEntity.badRequest().body("Tạo thất bại");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody SavingGoal goal) {
        boolean ok = service.update(id, goal);
        return ok ? ResponseEntity.ok("Cập nhật thành công")
                : ResponseEntity.badRequest().body("Không tìm thấy mục tiêu");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = service.delete(id);
        return ok ? ResponseEntity.ok("Đã xoá") : ResponseEntity.badRequest().body("Xoá thất bại");
    }
}
