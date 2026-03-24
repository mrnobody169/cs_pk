package com.pokertrack.controller;

import com.pokertrack.dto.request.BatchRegisterRequest;
import com.pokertrack.mapper.UserMapper;
import com.pokertrack.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Admin", description = "Quản lý duyệt tài khoản (chỉ admin)")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Kiểm tra user hiện tại có phải admin không
     */
    private void requireAdmin(UserDetails currentUser) {
        User user = userMapper.findByEmail(currentUser.getUsername());
        if (user == null || !Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ admin mới có quyền thực hiện");
        }
    }

    @Operation(summary = "Danh sách user đang chờ duyệt")
    @GetMapping("/pending-users")
    public ResponseEntity<List<Map<String, Object>>> getPendingUsers(
            @AuthenticationPrincipal UserDetails currentUser) {
        requireAdmin(currentUser);
        List<User> pending = userMapper.findByStatus("PENDING");
        return ResponseEntity.ok(pending.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Danh sách tất cả user")
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @AuthenticationPrincipal UserDetails currentUser) {
        requireAdmin(currentUser);
        // Get all statuses
        List<User> approved = userMapper.findByStatus("APPROVED");
        List<User> pending = userMapper.findByStatus("PENDING");
        List<User> rejected = userMapper.findByStatus("REJECTED");

        List<Map<String, Object>> all = new java.util.ArrayList<>();
        pending.stream().map(this::toDto).forEach(all::add);
        approved.stream().map(this::toDto).forEach(all::add);
        rejected.stream().map(this::toDto).forEach(all::add);
        return ResponseEntity.ok(all);
    }

    @Operation(summary = "Duyệt tài khoản")
    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<Map<String, String>> approveUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {
        requireAdmin(currentUser);
        User user = userMapper.findById(userId);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        userMapper.updateStatus(userId, "APPROVED");

        Map<String, String> resp = new LinkedHashMap<>();
        resp.put("message", "Đã duyệt tài khoản: " + user.getEmail());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Từ chối tài khoản")
    @PostMapping("/users/{userId}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {
        requireAdmin(currentUser);
        User user = userMapper.findById(userId);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        userMapper.updateStatus(userId, "REJECTED");

        Map<String, String> resp = new LinkedHashMap<>();
        resp.put("message", "Đã từ chối tài khoản: " + user.getEmail());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Danh sách user đã được duyệt (cho dropdown thêm thành viên)")
    @GetMapping("/approved-users")
    public ResponseEntity<List<Map<String, Object>>> getApprovedUsers(
            @AuthenticationPrincipal UserDetails currentUser) {
        requireAdmin(currentUser);
        List<User> approved = userMapper.findByStatus("APPROVED");
        return ResponseEntity.ok(approved.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Tạo nhiều tài khoản cùng lúc (auto approve)")
    @PostMapping("/batch-register")
    public ResponseEntity<Map<String, Object>> batchRegister(
            @RequestBody BatchRegisterRequest req,
            @AuthenticationPrincipal UserDetails currentUser) {
        requireAdmin(currentUser);

        List<Map<String, Object>> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (BatchRegisterRequest.UserEntry entry : req.getUsers()) {
            try {
                if (userMapper.findByEmail(entry.getEmail()) != null) {
                    errors.add(entry.getEmail() + " — email đã tồn tại");
                    continue;
                }

                String password = (req.getSharedPassword() != null && !req.getSharedPassword().isEmpty())
                        ? req.getSharedPassword()
                        : entry.getPassword();

                if (password == null || password.isEmpty()) {
                    errors.add(entry.getEmail() + " — thiếu password");
                    continue;
                }

                User user = new User();
                user.setEmail(entry.getEmail());
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setDisplayName(entry.getDisplayName() != null ? entry.getDisplayName() : entry.getEmail());
                user.setStatus("APPROVED");
                user.setIsAdmin(false);
                userMapper.insert(user);

                created.add(toDto(user));
            } catch (Exception e) {
                errors.add(entry.getEmail() + " — " + e.getMessage());
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("created", created);
        resp.put("createdCount", created.size());
        resp.put("errors", errors);
        resp.put("errorCount", errors.size());
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toDto(User user) {
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
