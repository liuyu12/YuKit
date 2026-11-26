-- ----------------------------------------------------------------------------
-- 两项补齐，均为「Service 校验与 DB 约束/种子数据自相矛盾」的收尾。
--
-- 一、sys_role.role_name 补唯一兜底。
--   RoleServiceImpl 此前只校验 role_key（validateRoleKeyUnique），ROLE_NAME_EXISTS
--   错误码 1002002002 定义了却从未被使用——即角色名称既不校验也无约束，可以建出任意多个
--   同名角色。岗位模块已是 code+name 双唯一（PostServiceImpl + uk_sys_post_code/name），
--   本次对齐到角色。存量已查：sys_role 仅 2 行（超级管理员/普通角色），无重名，索引可直接建。
--   sys_role 带 del_flag，因此沿用 V11 的 STORED 生成列方案而非全局唯一索引——
--   全局索引会让已删行永久占位，静默反转「删除后名字可重用」这条现行语义。
--
-- 二、删除 V2 遗留的冗余菜单 114「缓存列表」。
--   CacheMonitorController 的 4 个端点全部只校验 monitor:cache:list，后端不存在与 114
--   对应的独立能力；113/114 共用同一 perms 是 RuoYi 遗留，而 MenuServiceImpl
--   .validatePermsUnique 要求 perms 全局唯一，于是这两条菜单**任何编辑保存都必然失败**
--   （种子直写绕过了校验，冲突才得以入库）。
--   处理取舍：保留 113 持有 monitor:cache:list（权限由 role_menu→menu.perms 聚合而来，
--   两条都删会让普通角色 role_id=2 失去缓存监控访问权）；114 改独立码会留下一条
--   「授予了但后端无人校验」的空权限，故按实际能力删除。
--   全库无外键，靠先删引用再删菜单避免留下孤儿 role_menu。
-- ----------------------------------------------------------------------------

ALTER TABLE `sys_role`
  ADD COLUMN `role_name_active` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `role_name` END) STORED
    COMMENT '仅未删除行取到值，供唯一索引使用；业务不读写',
  ADD UNIQUE KEY `uk_sys_role_active_name` (`role_name_active`);

DELETE FROM `sys_role_menu` WHERE `menu_id` = 114;
DELETE FROM `sys_menu` WHERE `menu_id` = 114;
