package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LabRequest {

    @NotBlank(message = "实验室名称不能为空")
    private String name;

    private String location;

    private Long managerId;

    private String description;
}
