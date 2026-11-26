---
trigger: manual
---

# YuKit 功能交付与冒烟流程

新增或改动后端功能时按此顺序推进，每步都要有产出，不得跳步。

## 1. 摸底

先找同类既有模块当模板（`notice` / `config` / `post` 是最贴近的 CRUD 族），确认三件事：目标表是否已存在、V2 种子里的菜单与权限码、既有分层与命名风格。目的是复用惯例而非另立一套。

## 2. 实现

Flyway 迁移 → DO / Mapper（+ XML）→ VO → Convert → Service + Impl → Controller → 单测。

- 新错误码按段追加进 `SystemErrorCodeConstants`（分段见"数据库迁移与单测约定"）。
- 菜单与按钮权限码优先复用 V2 种子已有条目，确需新增才写进迁移脚本。
- 超管（`userId = 1`）在 `StpInterfaceImpl` 返回 `"*"` 通配符，`hasElement` 直接放行任意权限码，**与 `sys_role_menu` 无关**。因此 role 1 在该表零行是设计常态，不要为"admin 缺某权限"补 migration，也不要用"插一行后能调通"来反证缺权限。

## 3. 单测

```bash
./mvnw -q -pl yukit-module-system test -Dtest='XxxServiceImplTest'   # 定向
./mvnw -q -pl yukit-module-system test                                # 模块回归
```

## 4. 冒烟（固定用 8928，避开默认端口 8888 上 IDE 起的实例）

```bash
# 探活：需要 mysql8 与 redis 容器
docker info >/dev/null 2>&1 && docker ps --format '{{.Names}}'

# 打包 + 生成运行时 classpath（必须先 install，否则 -pl 单模块会用 .m2 里的陈旧兄弟模块 jar）
./mvnw -q -pl yukit-server -am install -DskipTests
./mvnw -q -pl yukit-server dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
```

启动命令（用工具的后台执行方式，**不要** `nohup ... &`——那样会随命令结束被进程组回收，日志里只见 graceful shutdown）：

```bash
"$JAVA_HOME/bin/java" -cp "yukit-server/target/classes:$(cat /tmp/cp.txt)" \
  -Dserver.port=8928 cn.lycoder.boot.server.YuKitServerApplication
```

裸 `java` 不在 PATH，须用 `JAVA_HOME` 全路径。

登录：默认 profile 是 `dev`，`/system/auth/captcha` 会把答案回填在 `code` 字段；若返回 `captchaEnabled=false` 则直接 `POST /system/auth/login`。账号 `admin` / `admin123`，之后请求带头 `satoken: <tokenValue>`。

覆盖点：CRUD 全链路；唯一性与引用校验的**拒绝分支**（断言具体错误码，不能只看 200）；分页与 `simple-list` 过滤停用；`@InDict` 非法值 → 400；`sys_operate_log.operator` 非空；Flyway 日志出现目标版本。

## 5. 清理现场

- 停掉 8928 进程，删除 `data/`、`/tmp/cp.txt`、`/tmp/*.log`。
- 删除冒烟数据与日志：测试用户/记录行；`DELETE FROM sys_operate_log WHERE id >= <本次起始 id>`；`DELETE FROM sys_login_log WHERE create_time >= '<本次起服时刻>'`。
- 回读各表 `COUNT(*)` / `MAX(id)` 复述还原结果，证明回到干净状态。

## 6. 提交

`git status --short` 核对改动面无遗漏无多余 → `git add -A && git commit`。message 说明模块、迁移版本、校验点与错误码段。

## 已知坑

- **zsh 保留变量**：脚本里别用 `UID` / `GID` / `PID` / `RANDOM` / `SECONDS` 作变量名，`UID=$(...)` 会报 "failed to change user ID" 并中断整条命令、吞掉后续输出。改用 `NEWID` 之类。命令崩溃点之前的副作用会残留，重跑前先查现状（复用已存在的 id），不要假设"失败 = 什么都没发生"。
- **MySQL 容器中文条件**：`WHERE name IN ('创建岗位', ...)` 可能因排序规则静默返回 0 行，按主键 id 查询规避。
- **Docker 守护进程**：可能中途停摆，动库前先 `docker info` 探活。
