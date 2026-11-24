-- ----------------------------------------------------------------------------
-- 登录日志表：记录每次登录尝试（成功/失败及原因），供安全审计与风控排查
-- 与 sys_operate_log 同为不可变运行期数据，不随种子导入
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `user_id` bigint DEFAULT NULL COMMENT '登录用户ID（账号不存在时为 NULL）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号名',
  `result` int NOT NULL COMMENT '登录结果，见 LoginResultTypeEnum（0成功 1密码错误 2账号禁用 3账号锁定 4验证码失败 5系统异常）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '结果描述（失败原因/剩余尝试次数等）',
  `user_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '客户端IP',
  `user_agent` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '浏览器UA',
  `create_time` datetime DEFAULT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_uname` (`username`),
  KEY `idx_sys_login_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志记录';
