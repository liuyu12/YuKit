---
trigger: glob
glob: "pom.xml,**/pom.xml,application*.yml,**/application*.yml"
---

# 构建与配置约定

改 `pom.xml` 或 `application*.yml` 时适用。此前这类文件**没有任何规则覆盖**，而版本配对错配的代价是直接启动失败。

## 依赖与版本管理

- 第三方库与内部模块的版本一律进 `yukit-dependencies`（独立 BOM，`packaging: pom`，**不继承根 POM**，否则与根 POM 形成环形引用）；根 POM 以 `<type>pom</type><scope>import</scope>` 引入。
- 子模块 `<dependency>` **不写 `<version>`**（实测唯一允许出现 version 的位置是 `<parent>`，取 `${revision}`）。
- 根 POM 的 `<dependencies>` 只保留 `optional` 的 lombok——父工程不喂饭，其余依赖由各模块自行声明。
- 编译器 `annotationProcessorPaths` 用的 lombok / mapstruct 版本在**根 POM properties 里另有一份**（插件配置不吃 `dependencyManagement`）。根 POM 已注释提醒"需与 BOM 保持同步"，改 BOM 必须同步它，否则注解处理器与实际库版本错位。
- **版本配对是硬约束，错配直接起不来**：Sa-Token 须用 `sa-token-spring-boot4-starter`（boot3 版不兼容 Boot 4）；`springdoc 3.x` 对应 Boot 4（2.x 仅支持 Boot 3）。新增 Boot 生态依赖前先确认其 Boot 4 兼容坐标。
- 内部 starter 在根 POM `dependencyManagement` 中以 `${revision}` 声明，供子模块无版本引用。
- 已知安全债记录在 BOM 注释内（Boot 4.1.3 托管的 logback 1.5.38 / tomcat 11.0.27 有 CVE）：升级 Boot 补丁版本时顺带处理，必要时在 BOM 覆盖版本。别在代码里另找地方记录这类待办。

## 运行配置（`yukit-server/src/main/resources/application.yml`）

- 单一 `application.yml` + `spring.profiles.active: dev`。**dev / prod 的差异主要落在 `logback-spring.xml` 的 `<springProfile>` 分支**，不要新建 `application-dev.yml` 分段文件。
- MyBatis：`mapper-locations: classpath*:/mapper/**/*.xml`——`classpath*:` **必须带**，否则扫不到各模块 jar 内的 mapper。
- Flyway：`locations: db/migration`——这里**不能写 `classpath*:`**（Flyway 11 抛 `Unknown prefix for location`，它原生支持跨 jar）。两条规则相反，改配置时不要互相"修正"。
- 白名单有两处且互不相通：`yukit.security.exclude-paths`（放行登录校验）与 `yukit.access-log.exclude-paths`（忽略访问日志）。安全拦截默认覆盖全部路由，**漏配前者会导致全站 401**（`/actuator/**` 也靠它放行）；新增免登录端点只需改前者，两者别想当然合并。
- 自定义配置统一挂 `yukit.*` 前缀（含 `yukit.zone-id`），kebab-case 命名，并在对应 `@ConfigurationProperties` 类里给默认值，不依赖 yml 一定存在。

## 失效条件

- Sa-Token / springdoc 的"版本配对"条目在升级到 Boot 5 或依赖大版本后需重新核实，不自动成立。
- CVE 待办条目在 BOM 注释被处理掉后即作废。

> 最近核对：`114e4b2` / 2026-11-26
