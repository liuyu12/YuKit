---
trigger: glob
glob: "**/*.java"
---

# YuKit 分层与代码归属约定

包根 `cn.lycoder.boot`。通用能力由 `yukit-spring-boot-starter-{web,security,mybatis,cache,protection}` 提供（模块全貌见项目章程）。

## 模块归属原则

- **能力提供方**：starter 模块封装通用能力，在自己的 pom 里声明所需三方依赖，不含业务逻辑，不假设宿主的存储拓扑。
- **组合根**：`yukit-server` 按需引入 starter 并做装配。依赖方向决定了归属——某个配置类要用的库（如 alone-redis / spring-data-redis）不在目标 starter 的 classpath 上，就装不进去，只能放 server。
- 运行期装配类放 `yukit-server/.../config`（先例：`SaTokenAloneRedisConfig`、`KeyPrefixSaTokenDao`）。`@ConditionalOnProperty` 这类 opt-in 开关属部署配置，归 server；库行为归 starter。
- 父工程不"喂饭"：依赖声明的具体规则（BOM 归属、子模块不写 version）见「构建与配置约定」。

## 业务模块内分层（以 system 为例）

```text
controller/admin/<domain>/XxxController.java     # 端点 + 权限 + 审计注解
controller/admin/<domain>/vo/Xxx*ReqVO.java      # 入参与出参
service/<domain>/XxxService.java / XxxServiceImpl.java
convert/<domain>/XxxConvert.java                 # MapStruct
dal/dataobject/<domain>/SystemXxxDO.java         # @TableName
dal/mapper/<domain>/SystemXxxMapper.java         # BaseMapper
resources/mapper/<domain>/XxxMapper.xml          # 自定义 SQL
enums/SystemErrorCodeConstants.java              # 错误码集中登记
```

## 硬性约定

1. **参与分页转换的 RespVO 必须 `implements Serializable`**：`PageResult.convert` 的泛型上界是 `<S extends Serializable, T extends Serializable>`，漏了编译报"类型参数不在范围 F 内"。
2. `XxxUpdateReqVO extends XxxCreateReqVO`（先例：`PostUpdateReqVO`、`JobUpdateReqVO`），避免字段重复声明。
3. 业务异常统一 `throw new ServiceException(XXX_YYY)`，错误码从 `SystemErrorCodeConstants` 引用，禁止内联数字码。
4. 唯一性校验：Service 层 `selectCount` 并在更新时用 `.ne(excludeId != null, id, excludeId)` 排除自身，DB 唯一索引兜底并发窗口；先校验编码再校验名称。
5. 引用完整性校验放 Service 层（如 `deletePost` 统计关联表后抛 `POST_IN_USE`），关联表不建外键。
6. 全量覆盖式多对多保存：`@Transactional` 下先 `deleteByXxxId` 再 `insertBatch`；引用校验的删除位过滤口径见项目章程红线 1。
7. Controller 只读端点不记 `@OperationLog`；写端点配 `@SaCheckPermission`，字典型 CRUD 的 create 加 `@Idempotent`（先例：config / dict / notice / post）。敏感接口用 `@OperationLog(recordParams = false)` 防明文入库。
8. 自定义 SQL 与批量插入写进 `resources/mapper/<domain>/*.xml`，不在注解里拼 SQL；纯 CRUD 可只用 `BaseMapper`。
9. 只读跨全表校验加 `@IgnoreDataPermission`（理由见项目章程红线 5）。

## 库版本禁忌（写 Java 时就生效，别等测试阶段才发现）

- **MyBatis-Plus**：不要用 `Wrappers.lambdaQuery().in(SFunction, coll)`——它在构建 wrapper 阶段就解析 lambda 列名，纯 Mockito 单测无 TableInfo 缓存会抛 `can not find lambda cache for this entity`（而 `.eq` / `.ne` 惰性到 SQL 生成期，所以不报错）。批量存在性校验改用 `selectBatchIds(ids).size()` 比对。
- **Sa-Token 1.46**：`SaSession` 无 `getExpirationTime()`、`SaTokenDao` 无 `getTimeToLive()`；会话存活走 `SaManager.getSaTokenDao().getSessionTimeout(key)`，`NOT_VALUE_EXPIRE = -2` 表永不过期。key 用裸逻辑名（前缀由覆写的 `wrapKey` 负责），自带前缀会双重包裹导致永远 miss。
- **Spring Boot 4**：自动配置已按技术拆包——Flyway 需 `spring-boot-starter-flyway`（只引 `flyway-core` 会静默不迁移）；不再托管 `spring-boot-starter-aop`，要 AOP 直接依赖 `org.aspectj:aspectjweaver`。
- **事务代理边界**：`@Transactional` 只在 public 方法且走代理时生效，同类内部调用无效（全量覆盖写法见上方硬性约定 6）。
- **时间源**：取当前时间走统一注入的 `Clock` Bean（由 web starter 提供），不直写 `LocalDate.now()` / `new Date()`。

> 最近核对：`114e4b2` / 2026-11-26
