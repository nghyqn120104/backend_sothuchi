package com.example.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.entity.User;
import com.example.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserDAO {
    private final JdbcTemplate jdbcTemplate;

    public int insert(User u) {
        String sql = "INSERT INTO users (id, username, password, email) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                u.getId().toString(),
                u.getUsername(),
                u.getPassword(),
                u.getEmail());
    }

    // READ ALL
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", new UserMapper());
    }

    // READ BY ID
    public Optional<User> findById(UUID id) {
        List<User> result = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", new UserMapper(), id.toString());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new UserMapper(), username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, new UserMapper(), email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // UPDATE
    public int update(User u) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                u.getUsername(),
                u.getPassword(),
                u.getEmail(),
                u.getId().toString());
    }

    // DELETE
    public int delete(UUID id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id.toString());
    }

}