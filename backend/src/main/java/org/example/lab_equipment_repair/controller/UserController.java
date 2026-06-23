package org.example.lab_equipment_repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.CreateUserRequest;
import org.example.lab_equipment_repair.dto.ResetPasswordRequest;
import org.example.lab_equipment_repair.dto.UpdateUserRequest;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(userService.listUsers(role, status, keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.updateUser(id, request));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.password());
        return ApiResponse.success();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> approveUser(@PathVariable Long id) {
        return ApiResponse.success(userService.approve(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> rejectUser(@PathVariable Long id) {
        return ApiResponse.success(userService.reject(id));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> disableUser(@PathVariable Long id) {
        return ApiResponse.success(userService.disable(id));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> enableUser(@PathVariable Long id) {
        return ApiResponse.success(userService.enable(id));
    }
}
