package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Size(min = 6, max = 50)
        String password,

        @NotBlank
        @Size(max = 50)
        String realName,

        @Size(max = 30)
        String phone
) {
}
