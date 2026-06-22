-- 实验室设备报修管理系统数据库初始化脚本
-- 数据库名包含短横线，使用时必须用反引号包裹。

CREATE SCHEMA IF NOT EXISTS `Lab-Equipment-Repair`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `Lab-Equipment-Repair`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
  `role` VARCHAR(30) NOT NULL COMMENT '用户角色：ADMIN 系统管理员，LAB_MANAGER 实验室管理员，REPAIRER 维修人员，REPORTER 报修人',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：PENDING 待审核，ACTIVE 启用，REJECTED 已拒绝，DISABLED 禁用',
  `avatar_path` VARCHAR(255) DEFAULT NULL COMMENT '头像文件访问路径',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_role_status` (`role`, `status`),
  KEY `idx_user_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS `lab` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '实验室名称',
  `location` VARCHAR(150) DEFAULT NULL COMMENT '实验室位置',
  `manager_id` BIGINT DEFAULT NULL COMMENT '实验室管理员用户ID',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '实验室说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_name` (`name`),
  KEY `idx_lab_manager_id` (`manager_id`),
  KEY `idx_lab_location` (`location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实验室信息表';

CREATE TABLE IF NOT EXISTS `equipment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(50) NOT NULL COMMENT '设备编号',
  `name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '设备类别',
  `lab_id` BIGINT NOT NULL COMMENT '所属实验室ID',
  `status` VARCHAR(30) NOT NULL DEFAULT 'NORMAL' COMMENT '设备状态：NORMAL 正常，REPAIRING 维修中，SCRAPPED 已报废，DISABLED 停用',
  `responsible_user_id` BIGINT DEFAULT NULL COMMENT '设备负责人用户ID',
  `purchase_date` DATE DEFAULT NULL COMMENT '购置日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_code` (`code`),
  KEY `idx_equipment_name` (`name`),
  KEY `idx_equipment_category` (`category`),
  KEY `idx_equipment_lab_id` (`lab_id`),
  KEY `idx_equipment_status` (`status`),
  KEY `idx_equipment_lab_status` (`lab_id`, `status`),
  KEY `idx_equipment_responsible_user_id` (`responsible_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备档案表';

CREATE TABLE IF NOT EXISTS `repair_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
  `equipment_id` BIGINT NOT NULL COMMENT '报修设备ID',
  `lab_id` BIGINT NOT NULL COMMENT '所属实验室ID，冗余保存便于查询和统计',
  `reporter_id` BIGINT NOT NULL COMMENT '报修人用户ID',
  `fault_description` TEXT NOT NULL COMMENT '故障描述',
  `urgency` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '紧急程度：LOW 低，NORMAL 普通，HIGH 高，URGENT 紧急',
  `location` VARCHAR(150) DEFAULT NULL COMMENT '故障地点',
  `contact` VARCHAR(50) DEFAULT NULL COMMENT '报修人联系方式',
  `status` VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED' COMMENT '工单状态：SUBMITTED 待审核，REJECTED 已退回，ASSIGNED 已派单，IN_PROGRESS 维修中，REPAIRED 待验收，ACCEPTED 已验收，CLOSED 已归档，CANCELLED 已取消',
  `assigned_to` BIGINT DEFAULT NULL COMMENT '指派维修人员用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_repair_order_order_no` (`order_no`),
  KEY `idx_repair_order_equipment_id` (`equipment_id`),
  KEY `idx_repair_order_lab_id` (`lab_id`),
  KEY `idx_repair_order_reporter_id` (`reporter_id`),
  KEY `idx_repair_order_assigned_to` (`assigned_to`),
  KEY `idx_repair_order_status` (`status`),
  KEY `idx_repair_order_urgency` (`urgency`),
  KEY `idx_repair_order_created_at` (`created_at`),
  KEY `idx_repair_order_status_created_at` (`status`, `created_at`),
  KEY `idx_repair_order_reporter_status_time` (`reporter_id`, `status`, `created_at`),
  KEY `idx_repair_order_assigned_status_time` (`assigned_to`, `status`, `created_at`),
  KEY `idx_repair_order_lab_status_time` (`lab_id`, `status`, `created_at`),
  KEY `idx_repair_order_equipment_status` (`equipment_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报修工单主表';

CREATE TABLE IF NOT EXISTS `maintenance_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '工单ID',
  `repairer_id` BIGINT NOT NULL COMMENT '维修人员用户ID',
  `fault_reason` TEXT DEFAULT NULL COMMENT '故障原因',
  `solution` TEXT DEFAULT NULL COMMENT '处理措施',
  `result` TEXT DEFAULT NULL COMMENT '维修结果',
  `cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '维修费用',
  `started_at` DATETIME DEFAULT NULL COMMENT '开始维修时间',
  `finished_at` DATETIME DEFAULT NULL COMMENT '完成维修时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_maintenance_order_id` (`order_id`),
  KEY `idx_maintenance_repairer_id` (`repairer_id`),
  KEY `idx_maintenance_order_repairer` (`order_id`, `repairer_id`),
  KEY `idx_maintenance_started_at` (`started_at`),
  KEY `idx_maintenance_finished_at` (`finished_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维修记录表';

CREATE TABLE IF NOT EXISTS `acceptance_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '工单ID',
  `accepter_id` BIGINT NOT NULL COMMENT '验收人用户ID',
  `accepted` TINYINT(1) NOT NULL COMMENT '是否验收通过：1 通过，0 不通过',
  `comment` TEXT DEFAULT NULL COMMENT '验收意见',
  `accepted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '验收时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_acceptance_order_id` (`order_id`),
  KEY `idx_acceptance_accepter_id` (`accepter_id`),
  KEY `idx_acceptance_accepted` (`accepted`),
  KEY `idx_acceptance_accepted_at` (`accepted_at`),
  KEY `idx_acceptance_order_time` (`order_id`, `accepted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验收记录表';

CREATE TABLE IF NOT EXISTS `order_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '工单ID',
  `from_status` VARCHAR(30) DEFAULT NULL COMMENT '变更前工单状态',
  `to_status` VARCHAR(30) NOT NULL COMMENT '变更后工单状态',
  `operator_id` BIGINT NOT NULL COMMENT '操作人用户ID',
  `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `remark` TEXT DEFAULT NULL COMMENT '操作备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_status_log_order_id` (`order_id`),
  KEY `idx_order_status_log_operator_id` (`operator_id`),
  KEY `idx_order_status_log_to_status` (`to_status`),
  KEY `idx_order_status_log_created_at` (`created_at`),
  KEY `idx_order_status_log_order_time` (`order_id`, `created_at`),
  KEY `idx_order_status_log_operator_time` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单状态流转日志表';
