-- 用户注册审批与个人资料头像增量脚本
-- 适用于已经创建好的 `lab-equipment-repair` 数据库。

USE `lab-equipment-repair`;

ALTER TABLE `user`
  MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：PENDING 待审核，ACTIVE 启用，REJECTED 已拒绝，DISABLED 禁用';

SET @avatar_path_column_count := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND COLUMN_NAME = 'avatar_path'
);
SET @avatar_path_sql := IF(
  @avatar_path_column_count = 0,
  'ALTER TABLE `user` ADD COLUMN `avatar_path` VARCHAR(255) DEFAULT NULL COMMENT ''头像文件访问路径'' AFTER `status`',
  'SELECT 1'
);
PREPARE avatar_path_stmt FROM @avatar_path_sql;
EXECUTE avatar_path_stmt;
DEALLOCATE PREPARE avatar_path_stmt;

SET @idx_user_created_at_count := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user'
    AND INDEX_NAME = 'idx_user_created_at'
);
SET @idx_user_created_at_sql := IF(
  @idx_user_created_at_count = 0,
  'CREATE INDEX `idx_user_created_at` ON `user` (`created_at`)',
  'SELECT 1'
);
PREPARE idx_user_created_at_stmt FROM @idx_user_created_at_sql;
EXECUTE idx_user_created_at_stmt;
DEALLOCATE PREPARE idx_user_created_at_stmt;
