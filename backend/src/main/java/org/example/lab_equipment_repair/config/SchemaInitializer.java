package org.example.lab_equipment_repair.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        addLabStatusColumnIfMissing();
        createDictTablesIfMissing();
    }

    private void addLabStatusColumnIfMissing() {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'lab'
                          AND COLUMN_NAME = 'status'
                        """,
                Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("""
                ALTER TABLE `lab`
                  ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                  COMMENT '实验室状态：ACTIVE 启用，DISABLED 停用' AFTER `manager_id`
                """);
    }

    private void createDictTablesIfMissing() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `sys_dict` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                  `dict_code` VARCHAR(80) NOT NULL COMMENT '字典编码',
                  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
                  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1 启用，0 停用',
                  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
                  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_sys_dict_code` (`dict_code`),
                  KEY `idx_sys_dict_enabled` (`enabled`),
                  KEY `idx_sys_dict_sort_order` (`sort_order`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表'
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `sys_dict_item` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                  `dict_id` BIGINT NOT NULL COMMENT '字典ID',
                  `item_value` VARCHAR(100) NOT NULL COMMENT '字典项值',
                  `item_label` VARCHAR(100) NOT NULL COMMENT '字典项显示名称',
                  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1 启用，0 停用',
                  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
                  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_sys_dict_item_value` (`dict_id`, `item_value`),
                  KEY `idx_sys_dict_item_dict_id` (`dict_id`),
                  KEY `idx_sys_dict_item_enabled` (`enabled`),
                  KEY `idx_sys_dict_item_sort_order` (`sort_order`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典项表'
                """);
    }
}
