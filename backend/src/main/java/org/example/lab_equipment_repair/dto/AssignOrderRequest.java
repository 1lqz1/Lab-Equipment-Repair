package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignOrderRequest {

    @NotNull(message = "维修人员不能为空")
    private Long repairerId;

    private String remark;
}
