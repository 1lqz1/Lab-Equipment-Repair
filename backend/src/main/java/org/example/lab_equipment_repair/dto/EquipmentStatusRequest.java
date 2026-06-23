package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EquipmentStatusRequest {

    @NotBlank(message = "设备状态不能为空")
    private String status;
}
