package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度应为 6 到 50 位")
        String password
) {
}
