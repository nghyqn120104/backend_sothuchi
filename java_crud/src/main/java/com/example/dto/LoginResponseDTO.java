package com.example.dto;

import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginResponseDTO {
    private UUID id;
    private String username;
    private String email;
}
