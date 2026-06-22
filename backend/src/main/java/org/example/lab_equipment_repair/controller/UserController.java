package org.example.lab_equipment_repair.controller;

import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<List<UserResponse>> listUsers(@RequestParam(required = false) String role) {
        return ApiResponse.success(userService.listUsers(role));
    }
}
