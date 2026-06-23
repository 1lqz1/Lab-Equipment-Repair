package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 50, message = "真实姓名不能超过 50 个字符")
        String realName,

        @Size(max = 30, message = "联系电话不能超过 30 个字符")
        String phone,

        @NotBlank(message = "用户角色不能为空")
        String role,

        @NotBlank(message = "用户状态不能为空")
        String status
) {
}
