-- ----------------------------------------------------------------------------
-- 参数管理：sys_config 存运行时可改的系统参数（键值对），供后端业务读取
-- 沿用 RuoYi 表结构与字段命名；config_type='Y' 为内置参数，不允许删除。
-- 菜单 106 与按钮 1030~1034 权限码已在 V2 种子中，本迁移无需追加菜单。
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '参数键名（全局唯一）',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '系统内置（Y 是 N 否，内置不允许删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参数配置表';

-- 初始参数：与 RuoYi 默认对齐的用户/界面参数 + 本项目演示参数
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`) VALUES
(1, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', NOW(), '重置密码功能使用的默认密码'),
(2, '界面-右侧工单开关', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', NOW(), '深色主题theme-dark，浅色主题theme-light'),
(3, '心跳演示-间隔说明', 'sys.demo.heartbeat', 'on', 'N', 'admin', NOW(), '演示任务 sampleHeartbeatTask 的参数示例');
