---
trigger: model_decision
description: 需要盘点项目已有哪些模块、端点、权限码、表或 SPI，或判断某功能是否已实现时
---

# 能力盘点入口（只放指针，不放结论）

功能清单不写死在本文件——它会腐烂。需要时**现查**，以实查结果为唯一依据：

| 要查什么 | 从哪查 |
| --- | --- |
| 有哪些对外端点 | `find . -name '*Controller.java'`，看类上 `@RequestMapping` + 方法上 `@(Get\|Post\|Put\|Delete)Mapping` |
| 权限码全集 | `grep -rho '@SaCheckPermission("[^"]*")' --include='*.java' . \| sort -u` |
| 菜单与按钮种子 | `yukit-module-system/src/main/resources/db/migration/V2__system_data.sql` 里的 `sys_menu` INSERT |
| 表结构与索引 | 同目录全部 `V*.sql`（Flyway 版本序列即建表历史，最新号见目录排序） |
| 业务错误码 | `yukit-module-system/.../enums/SystemErrorCodeConstants.java`，按模块分块注释 |
| 跨模块 SPI 契约 | `yukit-framework/**`：`OperatorSupplier`、`DataPermissionRule`、`OperationLogRecorder`、`DictValueProvider`、`ArrayValuable` |
| 运行参数与安全白名单 | `yukit-server/src/main/resources/application.yml` |
| 某个域的完整实现 | `yukit-module-system/.../{controller/admin,service,dal/dataobject,dal/mapper,convert}/<domain>/` 五个同名目录 |
| 已有测试基线 | `./mvnw -q -pl yukit-module-system test` 的实际输出（不引用文档里的历史数字） |

判重原则：某功能"是否已存在"必须落到上面这些实查证据上。若需要长期稳定的功能规格，应另建 per-module capability manifest（表 / 端点 / 校验 / 错误码 / 行为语义与边界 / 验收用例），本文件不代替它。

> 最近核对：`114e4b2` / 2026-11-26（仅核对入口路径有效，不代表清单内容）
