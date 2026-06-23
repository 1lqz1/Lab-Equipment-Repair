package org.example.lab_equipment_repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysDictItemRequest {

    @NotBlank(message = "字典项值不能为空")
    private String itemValue;

    @NotBlank(message = "字典项名称不能为空")
    private String itemLabel;

    @NotNull(message = "是否启用不能为空")
    private Boolean enabled = true;

    private Integer sortOrder = 0;

    private String remark;
}
