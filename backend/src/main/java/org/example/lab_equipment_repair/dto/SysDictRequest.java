package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysDictRequest {

    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @NotNull(message = "是否启用不能为空")
    private Boolean enabled = true;

    private Integer sortOrder = 0;

    private String remark;
}
