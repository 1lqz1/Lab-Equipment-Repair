package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitOrderRequest {

    @NotNull(message = "报修设备不能为空")
    private Long equipmentId;

    @NotBlank(message = "故障描述不能为空")
    private String faultDescription;

    private String urgency;

    private String location;

    private String contact;
}
