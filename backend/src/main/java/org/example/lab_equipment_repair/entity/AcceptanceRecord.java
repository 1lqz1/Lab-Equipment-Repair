package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("acceptance_record")
public class AcceptanceRecord {

    private Long id;

    private Long orderId;

    private Long accepterId;

    private Boolean accepted;

    private String comment;

    private LocalDateTime acceptedAt;

    private LocalDateTime createdAt;
}
