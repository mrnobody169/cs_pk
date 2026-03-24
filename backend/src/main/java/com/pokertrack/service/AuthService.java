package com.pokertrack.service;

import com.pokertrack.dto.request.LoginRequest;
import com.pokertrack.dto.request.RegisterRequest;
import com.pokertrack.exception.ResourceNotFoundException;
import com.pokertrack.mapper.UserMapper;
import com.pokertrack.model.User;
import com.pokertrack.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = userMapper.findByEmail(request.getEmail());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("user", toUserDto(user));
        return response;
    }

    public Map<String, Object> register(RegisterRequest request) {
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã được sử dụng: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getEmail());
        user.setStatus("PENDING");
        user.setIsAdmin(false);
        userMapper.insert(user);

        // Don't return token — user must wait for admin approval
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "PENDING");
        response.put("message", "Đăng ký thành công! Vui lòng chờ admin duyệt tài khoản.");
        return response;
    }

    public Map<String, Object> getCurrentUser(String email) {
        User user = userMapper.findByEmail(email);
        if (user == null)
            throw new ResourceNotFoundException("User not found");
        return toUserDto(user);
    }

    private Map<String, Object> toUserDto(User user) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("displayName", user.getDisplayName());
        dto.put("status", user.getStatus());
        dto.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        dto.put("createdAt", user.getCreatedAt());
        return dto;
    }
}
