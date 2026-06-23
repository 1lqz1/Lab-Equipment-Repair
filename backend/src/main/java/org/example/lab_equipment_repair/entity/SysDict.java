package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class SysDict {

    private Long id;

    private String dictCode;

    private String dictName;

    private Boolean enabled;

    private Integer sortOrder;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
