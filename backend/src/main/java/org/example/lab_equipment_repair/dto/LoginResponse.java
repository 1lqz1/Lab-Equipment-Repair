package org.example.lab_equipment_repair.dto;

public record LoginResponse(
        String token,
        UserResponse user
) {
}
