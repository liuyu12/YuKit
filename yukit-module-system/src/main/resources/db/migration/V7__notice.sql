-- ----------------------------------------------------------------------------
-- 公告管理：sys_notice 存系统通知/公告（RuoYi 标准结构）
-- notice_type 对应字典 sys_notice_type（1 通知 / 2 公告），
-- status 对应字典 sys_notice_status（0 正常 / 1 关闭），关闭后前台不再展示。
-- 菜单 107 与按钮 1035~1038 权限码已在 V2 种子中，本迁移无需追加菜单。
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '公告内容（纯文本/富文本 HTML，RuoYi 原生 longblob+base64 系历史包被，REST API 改存文本）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- 演示公告：一条正常通知 + 一条已关闭公告，覆盖两种类型与状态
INSERT INTO `sys_notice` (`notice_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '维护通知：系统将于本周末凌晨停机升级', '1', '升级期间服务不可用，预计 2 小时，请提前保存工作。', '0', 'admin', NOW(), '停机维护'),
(2, '公告：参数管理模块已上线', '2', '新增系统参数在线维护能力，详见「参数设置」。', '1', 'admin', NOW(), '历史公告已关闭');
