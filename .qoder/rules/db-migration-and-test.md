---
trigger: glob
glob: "**/*.sql,**/*Test.java"
---

# YuKit 数据库迁移与单测约定

## Flyway 迁移

位置 `yukit-module-system/src/main/resources/db/migration/`，命名 `V<N>__<snake_case>.sql`。当前已用至 **V10**，新迁移取下一号；**禁止改动已发布脚本**（校验和不匹配会导致启动失败）。

装配现状（`yukit-server/src/main/resources/application.yml`）：`locations: db/migration`（Flyway 11+ 原生跨 jar 扫描，写 `classpath*:` 会抛 `Unknown prefix for location`）、`clean-disabled: true`、`baseline-on-migrate: true` + `baseline-version: 2`（存量非空库不重跑建表/种子，新空库全量迁移）。依赖须同时有 `spring-boot-starter-flyway` 与 `flyway-mysql`——Boot 4 把自动配置拆成独立模块，只引 `flyway-core` 会静默不执行迁移。

建表风格（参照 V6 config / V7 notice / V9 post）：

- `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` + 表级 `COMMENT`
- 审计列固定 `create_by` / `create_time` / `update_by` / `update_time` + `remark`
- 状态列 `char(1)`，`0` 正常 / `1` 停用，注释标注字典 `sys_normal_disable`
- 唯一约束 `UNIQUE KEY uk_<table>_<col>`；需并发兜底的业务列必须建唯一索引
- 关联表（先例 `sys_user_role` / `sys_role_menu` / `sys_user_post`）用联合主键 + 反查列 `KEY idx_*`，不建外键、不加审计列
- 种子数据要能让"删除被拒"等校验分支在冒烟中可复现

## 逻辑删除语义（两套并存，勿混用）

- 全局 MP 默认：字段 `deleted`，`0` 未删 / `1` 已删。
- RuoYi 遗留表 `sys_user` / `sys_role` / `sys_dept` 用 `del_flag`，DO 上显式 `@TableLogic(value = "0", delval = "2")`。
- `sys_menu`、`sys_post` 无删除标记列，物理删除。
- **引用校验 SQL 必须过滤"未删除"的引用方**（JOIN 主表并加 `del_flag = '0'`），否则逻辑删除的记录会永久占用被引用对象。

## 错误码分段

格式 `1-002-XXX-YYY`，`XXX` 为模块序号：`000` 认证 / `001` 用户 / `002` 角色 / `003` 菜单 / `004` 字典 / `005` 部门 / `006` 定时任务 / `007` 文件 / `008` 参数 / `009` 公告 / `010` 岗位。新模块取 `011` 起。

分配前先在 `SystemErrorCodeConstants` 全文 grep 确认未占用——认证段出现过 `1002000005` 已被 `ONLINE_SESSION_NOT_EXISTS` 占用、后续码顺延的情况。文案不支持 `{0}` 占位，一律静态文案。

## 单元测试

纯 Mockito + `@ExtendWith(MockitoExtension.class)`，**不起 Spring 上下文**；MapStruct 用编译期生成的 `XxxConvertImpl` 真实实现而非 mock。

- 断言错误码：`assertThatThrownBy(...).isInstanceOf(ServiceException.class).extracting(e -> ((ServiceException) e).getCode()).isEqualTo(XXX.getCode())`。
- **严格 stub 陷阱**：`MockitoExtension` 默认 strict stubs，多余打桩直接失败。走拒绝分支的用例里不要打后续步骤才会用到的桩。
- 自增主键由 mock 的 `insert` 回填：`when(mapper.insert(any(XxxDO.class))).thenAnswer(inv -> { inv.getArgument(0, XxxDO.class).setId(10L); return 1; })`。
- **不要用 `Wrappers.lambdaQuery().in(SFunction, coll)`**：MP 在构建 wrapper 阶段即解析 lambda 列名，无 TableInfo 缓存时抛 `can not find lambda cache for this entity`；`.eq` / `.ne` 把解析延迟到 SQL 生成期，所以走 mock 不报错。批量存在性校验改用 `selectBatchIds(ids).size()` 比对。
- 顺序打桩可表达"两次 selectCount"：`when(mapper.selectCount(any())).thenReturn(codeCount, nameCount)`。
- 静态方法用 `try (MockedStatic<T> m = mockStatic(X.class))`（mockito 5.x inline maker 默认可用，无需 mockito-inline）；涉及 `ProceedingJoinPoint.proceed()` 的测试方法签名须 `throws Throwable`。
- 跑测试：改过兄弟模块后先 `install` 再 `test -pl <模块>`，否则会用到 `.m2` 里的陈旧 jar。全模块基线见最近一次回归结果（当前 135 例）。
