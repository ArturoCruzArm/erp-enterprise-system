package com.erp.system.user.service;

import com.erp.system.user.dto.LoginRequest;
import com.erp.system.user.dto.LoginResponse;
import com.erp.system.user.dto.UserDto;
import com.erp.system.user.entity.Permission;
import com.erp.system.user.entity.Role;
import com.erp.system.user.entity.User;
import com.erp.system.user.repository.UserRepository;
import com.erp.system.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        if (user.getAccountLocked()) {
            throw new BadCredentialsException("Account is locked");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }
        
        handleSuccessfulLogin(user);
        
        Set<String> permissions = getUserPermissions(user);
        String accessToken = jwtUtil.generateToken(user.getUsername(), permissions);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        return new LoginResponse(
                accessToken,
                "Bearer",
                3600L, // 1 hour
                refreshToken,
                mapToUserDto(user),
                permissions,
                user.getLastLogin()
        );
    }
    
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        userRepository.updateFailedLoginAttempts(user.getId(), attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            userRepository.updateAccountLocked(user.getId(), true);
            log.warn("Account locked for user: {}", user.getUsername());
        }
    }
    
    private void handleSuccessfulLogin(User user) {
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
        userRepository.updateFailedLoginAttempts(user.getId(), 0);
    }
    
    private Set<String> getUserPermissions(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
    
    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setAccountLocked(user.getAccountLocked());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}