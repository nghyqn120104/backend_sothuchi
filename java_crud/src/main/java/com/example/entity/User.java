package com.example.entity;

import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String password;
    private String email;
}
