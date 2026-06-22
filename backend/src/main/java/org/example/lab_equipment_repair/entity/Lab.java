package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab")
public class Lab {

    private Long id;

    private String name;

    private String location;

    private Long managerId;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
