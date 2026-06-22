package org.example.lab_equipment_repair.dto;

import org.example.lab_equipment_repair.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String realName,
        String phone,
        String role,
        String status,
        String avatarPath,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getAvatarPath(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
