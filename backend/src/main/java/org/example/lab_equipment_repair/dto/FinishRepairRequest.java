package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinishRepairRequest {

    private String faultReason;

    private String solution;

    @NotBlank(message = "维修结果不能为空")
    private String result;

    private BigDecimal cost;
}
