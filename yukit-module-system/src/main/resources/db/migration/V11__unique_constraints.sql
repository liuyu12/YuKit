-- ----------------------------------------------------------------------------
-- 唯一性的数据库层兜底。此前只有 sys_config.config_key / sys_dict_type.dict_type /
-- sys_job(job_name,job_group) / sys_post.post_code 建了唯一索引，其余业务唯一列
-- （账号、角色标识、部门名、菜单名、字典值、岗位名称）只有 Service 层 selectCount 校验，
-- 存在"查—插"竞态窗口：并发重复提交可插入重复行。本迁移补齐约束。
--
-- 分两类处理，依据是**是否存在逻辑删除**：全库只有 sys_user / sys_role / sys_dept 有
-- del_flag（DO 上 @TableLogic(value="0", delval="2")），其余表物理删除。
--   · 物理删除表 → 直接建全局唯一索引，与 Service 校验语义完全一致。
--   · 逻辑删除表 → 不能建全局唯一索引。Service 的 selectCount 被 @TableLogic 自动过滤，
--     只数未删行，即"账号删除后名字允许重用"是现行语义；全局唯一索引会让已删行永久占位，
--     静默反转这条规则（且"创建→删除→重建同名"直接撞库）。
--     改用 STORED 生成列：未删行取到列值、已删行取到 NULL，而 MySQL 唯一索引允许多个 NULL
--     共存，于是约束恰好只作用于未删行，语义零变化。生成列业务侧一律不读写（DO 不映射、
--     全库 INSERT 均带显式列清单），仅承担约束职责；需要它纯粹是 MySQL 不支持部分/过滤索引。
--
-- 存量数据已逐列查过重复组数，均为 0，索引可直接创建（含已删行也不冲突）。
-- 已知不做：sys_menu.perms 不加唯一索引——V2 种子里 113「缓存监控」与 114「缓存列表」
--   共用 monitor:cache:list（RuoYi 遗留），另有 5 行空串，建索引会直接失败。
-- ----------------------------------------------------------------------------

-- ========== 一、物理删除表：直接全局唯一索引 ==========

-- 对应 PostServiceImpl.validateCodeAndNameUnique 的名称那半（编码已有 uk）
ALTER TABLE `sys_post`
  ADD UNIQUE KEY `uk_sys_post_name` (`post_name`);

-- 对应 DictDataServiceImpl.validateDictValueUnique：同一字典类型下取值唯一
ALTER TABLE `sys_dict_data`
  ADD UNIQUE KEY `uk_sys_dict_data_type_value` (`dict_type`, `dict_value`);

-- 对应 MenuServiceImpl.validateMenuNameUnique：同一父级下菜单名唯一
ALTER TABLE `sys_menu`
  ADD UNIQUE KEY `uk_sys_menu_parent_name` (`parent_id`, `menu_name`);

-- ========== 二、逻辑删除表：生成列，仅未删行参与唯一约束 ==========

-- 对应 UserServiceImpl.validateUserNameUnique
ALTER TABLE `sys_user`
  ADD COLUMN `user_name_active` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `user_name` END) STORED
    COMMENT '仅未删除行取到值，供唯一索引使用；业务不读写',
  ADD UNIQUE KEY `uk_sys_user_active_name` (`user_name_active`);

-- 对应 RoleServiceImpl.validateRoleKeyUnique
ALTER TABLE `sys_role`
  ADD COLUMN `role_key_active` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `role_key` END) STORED
    COMMENT '仅未删除行取到值，供唯一索引使用；业务不读写',
  ADD UNIQUE KEY `uk_sys_role_active_key` (`role_key_active`);

-- 对应 DeptServiceImpl.validateDeptNameUnique：同一父部门下部门名唯一。
-- 两个生成列同时为 NULL（已删行）时不参与约束，因此不需要拼分隔符，无歧义风险。
ALTER TABLE `sys_dept`
  ADD COLUMN `parent_id_active` bigint
    GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `parent_id` END) STORED
    COMMENT '仅未删除行取到值，供唯一索引使用；业务不读写',
  ADD COLUMN `dept_name_active` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `dept_name` END) STORED
    COMMENT '仅未删除行取到值，供唯一索引使用；业务不读写',
  ADD UNIQUE KEY `uk_sys_dept_active_parent_name` (`parent_id_active`, `dept_name_active`);
