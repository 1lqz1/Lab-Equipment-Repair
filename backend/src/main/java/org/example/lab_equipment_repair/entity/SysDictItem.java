package org.example.lab_equipment_repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_item")
public class SysDictItem {

    private Long id;

    private Long dictId;

    private String itemValue;

    private String itemLabel;

    private Boolean enabled;

    private Integer sortOrder;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
