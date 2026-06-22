package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {

    private Long id;

    private String code;

    private String name;

    private String category;

    private Long labId;

    private String status;

    private Long responsibleUserId;

    private LocalDate purchaseDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
