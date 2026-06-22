package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    private Long id;

    private String username;

    private String passwordHash;

    private String realName;

    private String phone;

    private String role;

    private String status;

    private String avatarPath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
