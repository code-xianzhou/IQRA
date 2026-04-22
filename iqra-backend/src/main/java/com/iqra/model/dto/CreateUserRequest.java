package com.iqra.model.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String realName;
    private String department;
    private String email;
    private String phone;
    private String role; // ADMIN / USER
}
