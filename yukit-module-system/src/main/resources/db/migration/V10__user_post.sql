-- ----------------------------------------------------------------------------
-- 用户-岗位关联：sys_user_post 支持用户表单多选岗位（多对多）。
-- 沿用 sys_user_role 结构（联合主键，无独立自增/审计列）；一个用户可挂多个岗位。
-- 无外键约束（与库内其它关联表一致，关系完整性由 Service 层维护）：
--   · 删用户 → UserServiceImpl 物理清除其 sys_user_post 行；
--   · 删岗位 → 校验是否仍被"未删除用户"引用（见 PostServiceImpl.deletePost）。
-- 菜单/权限码复用 V2 种子（system:post:* / system:user:*），本迁移无需追加。
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`),
  KEY `idx_sys_user_post_pid` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与岗位关联表';

-- 演示关联：admin(1)→董事长(1)，ry(2)→项目经理(2)。
-- 岗位 se(2) 因此被占用，可用于验证"删除在用岗位被拒"。
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (1, 1);
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (2, 2);
