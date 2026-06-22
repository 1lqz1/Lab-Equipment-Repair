package org.example.lab_equipment_repair.dto;

import lombok.Data;

@Data
public class RepairOrderQuery {

    private String status;

    private Long equipmentId;

    private Long labId;

    private Long reporterId;

    private Long assignedTo;
}
