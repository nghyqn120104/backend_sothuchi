package com.example.controller;

import com.example.entity.Account;
import com.example.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/user/{userId}")
    public List<Account> getByUser(@PathVariable UUID userId) {
        return accountService.getByUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return accountService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Account account) {
        boolean ok = accountService.create(account);
        return ok
                ? ResponseEntity.ok("Tạo tài khoản thành công")
                : ResponseEntity.badRequest().body("Tên tài khoản đã tồn tại");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Account account) {
        boolean ok = accountService.update(id, account);
        return ok
                ? ResponseEntity.ok("Cập nhật tài khoản thành công")
                : ResponseEntity.badRequest().body("Tên tài khoản đã tồn tại");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = accountService.delete(id);
        return ok ? ResponseEntity.ok("Đã xoá tài khoản") : ResponseEntity.badRequest().body("Xoá thất bại");
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestParam UUID from,
            @RequestParam UUID to,
            @RequestParam double amount) {
        boolean ok = accountService.transfer(from, to, amount);
        return ok ? ResponseEntity.ok("Chuyển tiền thành công")
                : ResponseEntity.badRequest().body("Chuyển tiền thất bại");
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getBalanceInfo(id));
    }

}
