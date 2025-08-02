package com.erp.system.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDto {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean emailVerified;
    private Boolean accountLocked;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private Set<String> roles;
}