package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {

    private Long id;

    private String orderNo;

    private Long equipmentId;

    private Long labId;

    private Long reporterId;

    private String faultDescription;

    private String urgency;

    private String location;

    private String contact;

    private String status;

    private Long assignedTo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
