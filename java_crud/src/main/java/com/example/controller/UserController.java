package com.example.controller;

import com.example.dto.LoginRequestDTO;
import com.example.dto.LoginResponseDTO;
import com.example.dto.SpendingStatisticsDTO;
import com.example.entity.User;
import com.example.service.BudgetService;
import com.example.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final BudgetService budgetService;

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User u) {
        String result = userService.createUser(u);
        if ("Thêm user thành công".equals(result)) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User u) {
        return userService.updateUser(id, u)
                ? ResponseEntity.ok("Cập nhật thành công")
                : ResponseEntity.badRequest().body("Cập nhật thất bại");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id)
                ? ResponseEntity.ok("Đã xoá user")
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        Optional<LoginResponseDTO> loginResult = userService.login(request);
        if (loginResult.isPresent()) {
            return ResponseEntity.ok("Đăng nhập thành công");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu");
        }
    }

    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<?> getSpendingStatistics(
            @PathVariable UUID userId,
            @RequestParam int month,
            @RequestParam int year) {
        SpendingStatisticsDTO stats = budgetService.getSpendingStatistics(userId, month, year);
        return ResponseEntity.ok(stats);
    }

}
