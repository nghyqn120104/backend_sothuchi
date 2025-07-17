package com.example.service;

import com.example.dto.LoginRequestDTO;
import com.example.dto.LoginResponseDTO;
import com.example.entity.User;
import com.example.repository.AccountDAO;
import com.example.repository.BudgetDAO;
import com.example.repository.TransactionDAO;
import com.example.repository.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDAO userDao;
    private final TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final AccountDAO accountDAO;

    public List<User> getAll() {
        return userDao.findAll();
    }

    public Optional<User> getById(UUID id) {
        return userDao.findById(id);
    }

    public String createUser(User u) {
        // Kiểm tra username đã tồn tại
        if (userDao.findByUsername(u.getUsername()).isPresent()) {
            return "Tên tài khoản đã tồn tại";
        }
        // Kiểm tra email đã tồn tại (nếu muốn)
        if (userDao.findByEmail(u.getEmail()).isPresent()) {
            return "Email đã tồn tại";
        }
        u.setId(UUID.randomUUID());
        return userDao.insert(u) > 0 ? "Thêm user thành công" : "Thêm thất bại";
    }

    public boolean updateUser(UUID id, User u) {
        if (!userDao.findById(id).isPresent())
            return false;
        u.setId(id);
        return userDao.update(u) > 0;
    }

    public boolean deleteUser(UUID id) {
        transactionDAO.deleteByUserId(id);
        budgetDAO.deleteByUserId(id);
        accountDAO.deleteByUserId(id);
        return userDao.delete(id) > 0;
    }

    public Optional<LoginResponseDTO> login(LoginRequestDTO request) {
        String usernameOrEmail = request.getUsername();
        if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
            usernameOrEmail = request.getEmail();
        }
        String password = request.getPassword();

        Optional<User> userOpt = userDao.findByUsername(usernameOrEmail);
        if (!userOpt.isPresent()) {
            userOpt = userDao.findByEmail(usernameOrEmail);
        }

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            LoginResponseDTO response = new LoginResponseDTO();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            return Optional.of(response);
        }
        return Optional.empty();
    }

    public List<User> getPaged(int limit, int offset) {
        return userDao.findPaged(limit, offset);
    }

}
