package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentRequest {

    @NotBlank(message = "设备编号不能为空")
    private String code;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private String category;

    @NotNull(message = "实验室不能为空")
    private Long labId;

    private String status;

    private Long responsibleUserId;

    private LocalDate purchaseDate;
}
