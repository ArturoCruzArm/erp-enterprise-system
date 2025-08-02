package com.erp.system.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String refreshToken;
    private UserDto user;
    private Set<String> permissions;
    private LocalDateTime lastLogin;
}