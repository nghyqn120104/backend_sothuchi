package com.example.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginRequestDTO {
    private String username;
    private String email;
    private String password;
}
