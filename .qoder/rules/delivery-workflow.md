---
trigger: model_decision
description: 新增或修改后端功能、需要准备交付验证、收尾清理或提交时
---

# 交付流程与完成定义

## 六步

1. **摸底**：先 grep 同类实现找先例（`notice` / `config` / `post` 是 CRUD 族模板），确认目标表是否已存在、V2 种子已有哪些菜单与权限码。目的是复用惯例而非另立风格。
2. **实现**：Flyway 迁移 → DO / Mapper（+ XML）→ VO → Convert → Service + Impl → Controller。新错误码按段追加；菜单与按钮权限码优先复用既有种子，确需新增才写进迁移。
3. **单测**：定向 `./mvnw -q -pl yukit-module-system test -Dtest='XxxServiceImplTest'`；收尾前跑模块全量回归。
4. **冒烟（固定 8928，避开默认 8888 上 IDE 起的实例）**：

   ```bash
   docker info >/dev/null 2>&1 && docker ps --format '{{.Names}}'   # 需要 mysql8 + redis
   ./mvnw -q -pl yukit-server -am install -DskipTests                # 必须先 install
   ./mvnw -q -pl yukit-server dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
   ```

   启动用工具的后台执行方式，**不要** `nohup ... &`（会随命令结束被进程组回收，日志只见 graceful shutdown）：

   ```bash
   "$JAVA_HOME/bin/java" -cp "yukit-server/target/classes:$(cat /tmp/cp.txt)" \
     -Dserver.port=8928 cn.lycoder.boot.server.YuKitServerApplication
   ```

   裸 `java` 不在 PATH，须用 `JAVA_HOME` 全路径。先 install 再 `-pl`，否则会用到 `.m2` 里的陈旧兄弟模块 jar。
5. **清场**：停进程；删 `data/`、`/tmp/cp.txt`、`/tmp/*.log`；删冒烟数据与 `sys_operate_log`（`id >= 本次起始`）、`sys_login_log`（`create_time >= 起服时刻`）残留；回读各表 `COUNT(*)` / `MAX(id)` 证明已还原。
6. **提交**：`git status --short` 核对改动面无遗漏无多余，再 `git add -A && git commit`，message 写明模块、迁移版本、校验点、错误码段。

## 完成定义（缺一项不算完成）

- 模块全量单测通过；冒烟覆盖 CRUD 全链路与**所有拒绝分支**（断言具体错误码，只看 200 不算验证）
- Flyway 日志出现目标版本；写端点产生的 `sys_operate_log.operator` 非空
- 工作区干净、无残留冒烟进程、无测试数据残留

## 被中断时的协议

任何步骤被打断后重做，必须先复述"哪些已完成、哪些是半成品"，并**实测**当前状态（进程是否活、`git status`、相关表行数）。不要假设"上一步失败 = 什么都没发生"——崩溃点之前的副作用常常还留着。

## 执行细节坑

- **zsh 保留变量**：不要用 `UID` / `GID` / `PID` / `RANDOM` / `SECONDS` 作变量名，`UID=$(...)` 会报 "failed to change user ID" 并中断整条命令、吞掉后续输出。改用 `NEWID` 之类。
- **MySQL 容器中文条件**：`WHERE name IN ('中文值', ...)` 可能因排序规则静默返回 0 行，改按主键 id 查。
- **Docker 守护进程**：可能中途停摆，动库前先 `docker info` 探活。
- **超管权限**：按项目章程红线 2 处理——排查接口调不通时不要去补 `sys_role_menu`。

> 最近核对：`114e4b2` / 2026-11-26
