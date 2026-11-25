-- ----------------------------------------------------------------------------
-- 岗位管理：sys_post 存公司维度全局岗位（RuoYi 标准结构，不挂部门——
-- 岗位是全员可选字典而非部门从属数据，与项目/部门维度解耦）。
-- status 对应字典 sys_normal_disable（0 正常 / 1 停用），停用岗位不再出现在下拉选项。
-- 菜单 104 与按钮 1020~1025 权限码已在 V2 种子中，本迁移无需追加菜单。
-- 用户-岗位关联（sys_user_post）本次不做：删岗无法校验在用用户，属已知边界。
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位主键',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编码（全局唯一）',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `post_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '岗位类别编码（预留分组维度）',
  `post_sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用，字典 sys_normal_disable）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`),
  UNIQUE KEY `uk_sys_post_code` (`post_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位信息表';

-- 演示岗位：与 RuoYi 默认对齐的四个经典岗位 + 一条停用岗位用于状态过滤演示
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_category`, `post_sort`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, 'ceo',    '董事长',     'senior', 1, '0', 'admin', NOW(), '最高管理岗'),
(2, 'se',     '项目经理',   'manage', 2, '0', 'admin', NOW(), '一线管理岗'),
(3, 'hr',     '人力资源',   'support', 3, '0', 'admin', NOW(), '职能岗'),
(4, 'user',   '普通员工',   'basic',  4, '0', 'admin', NOW(), '默认岗位'),
(5, 'intern', '实习生（停用）', 'basic', 9, '1', 'admin', NOW(), '停用演示：不再生成新的实习岗位');
