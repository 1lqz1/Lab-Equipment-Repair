-- 横向功能扩展数据库脚本
-- 适用于已经创建好的 `Lab-Equipment-Repair` 数据库。
-- 说明：本项目按需求不增加外键约束，仅增加字段、表和常用索引。

USE `Lab-Equipment-Repair`;

SET @lab_status_column_count = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'lab'
    AND COLUMN_NAME = 'status'
);
SET @lab_status_sql = IF(
  @lab_status_column_count = 0,
  'ALTER TABLE `lab` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT ''ACTIVE'' COMMENT ''实验室状态：ACTIVE 启用，DISABLED 停用'' AFTER `manager_id`',
  'SELECT ''lab.status already exists'''
);
PREPARE lab_status_stmt FROM @lab_status_sql;
EXECUTE lab_status_stmt;
DEALLOCATE PREPARE lab_status_stmt;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典项表';

INSERT INTO `sys_dict` (`dict_code`, `dict_name`, `enabled`, `sort_order`, `remark`)
VALUES
  ('user_role', '用户角色', 1, 10, '系统账号角色'),
  ('user_status', '用户状态', 1, 20, '账号审批与启停状态'),
  ('equipment_status', '设备状态', 1, 30, '设备资产状态'),
  ('order_status', '工单状态', 1, 40, '报修工单流程状态'),
  ('urgency_level', '紧急程度', 1, 50, '工单紧急程度'),
  ('equipment_category', '设备分类', 1, 60, '设备资产分类')
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `enabled` = VALUES(`enabled`),
  `sort_order` = VALUES(`sort_order`),
  `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_item` (`dict_id`, `item_value`, `item_label`, `enabled`, `sort_order`)
SELECT d.id, x.item_value, x.item_label, 1, x.sort_order
FROM `sys_dict` d
JOIN (
  SELECT 'user_role' dict_code, 'ADMIN' item_value, '系统管理员' item_label, 10 sort_order
  UNION ALL SELECT 'user_role', 'LAB_MANAGER', '实验室管理员', 20
  UNION ALL SELECT 'user_role', 'REPAIRER', '维修人员', 30
  UNION ALL SELECT 'user_role', 'REPORTER', '报修人员', 40
  UNION ALL SELECT 'user_status', 'PENDING', '待审核', 10
  UNION ALL SELECT 'user_status', 'ACTIVE', '启用', 20
  UNION ALL SELECT 'user_status', 'REJECTED', '已拒绝', 30
  UNION ALL SELECT 'user_status', 'DISABLED', '禁用', 40
  UNION ALL SELECT 'equipment_status', 'NORMAL', '正常', 10
  UNION ALL SELECT 'equipment_status', 'REPAIRING', '维修中', 20
  UNION ALL SELECT 'equipment_status', 'DISABLED', '停用', 30
  UNION ALL SELECT 'equipment_status', 'SCRAPPED', '已报废', 40
  UNION ALL SELECT 'order_status', 'SUBMITTED', '待审核', 10
  UNION ALL SELECT 'order_status', 'REJECTED', '已退回', 20
  UNION ALL SELECT 'order_status', 'ASSIGNED', '已派单', 30
  UNION ALL SELECT 'order_status', 'IN_PROGRESS', '维修中', 40
  UNION ALL SELECT 'order_status', 'REPAIRED', '待验收', 50
  UNION ALL SELECT 'order_status', 'ACCEPTED', '已验收', 60
  UNION ALL SELECT 'order_status', 'CLOSED', '已归档', 70
  UNION ALL SELECT 'order_status', 'CANCELLED', '已取消', 80
  UNION ALL SELECT 'urgency_level', 'LOW', '低', 10
  UNION ALL SELECT 'urgency_level', 'NORMAL', '普通', 20
  UNION ALL SELECT 'urgency_level', 'HIGH', '高', 30
  UNION ALL SELECT 'urgency_level', 'URGENT', '紧急', 40
  UNION ALL SELECT 'equipment_category', '显微分析', '显微分析', 10
  UNION ALL SELECT 'equipment_category', '样品制备', '样品制备', 20
  UNION ALL SELECT 'equipment_category', '生物实验', '生物实验', 30
  UNION ALL SELECT 'equipment_category', '电子测试', '电子测试', 40
  UNION ALL SELECT 'equipment_category', '教学设备', '教学设备', 50
) x ON d.dict_code = x.dict_code
ON DUPLICATE KEY UPDATE
  `item_label` = VALUES(`item_label`),
  `enabled` = VALUES(`enabled`),
  `sort_order` = VALUES(`sort_order`);
