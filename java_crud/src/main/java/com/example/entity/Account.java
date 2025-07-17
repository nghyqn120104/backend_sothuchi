package com.example.entity;

import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Account {
    private UUID id;
    private UUID userId;
    private String name;
    private String type;
    private double balance;
}
