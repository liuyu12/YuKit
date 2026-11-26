---
trigger: glob
glob: "**/*.java"
---

# YuKit 分层与代码归属约定

包根 `cn.lycoder.boot`。模块：`yukit-dependencies`（BOM）/ `yukit-framework`（`yukit-common` + `yukit-spring-boot-starter-{web,security,mybatis,cache,protection}`）/ `yukit-module-system`（业务）/ `yukit-server`（组合根）。

## 模块归属原则

- **能力提供方**：starter 模块封装通用能力，在自己的 pom 里声明所需三方依赖，不含业务逻辑，不假设宿主的存储拓扑。
- **组合根**：`yukit-server` 按需引入 starter 并做装配。依赖方向决定了归属——某个配置类要用的库（如 alone-redis / spring-data-redis）不在目标 starter 的 classpath 上，就装不进去，只能放 server。
- 运行期装配类放 `yukit-server/.../config`（先例：`SaTokenAloneRedisConfig`、`KeyPrefixSaTokenDao`）。`@ConditionalOnProperty` 这类 opt-in 开关属部署配置，归 server；库行为归 starter。
- 父工程不"喂饭"：不在根 POM 的 `<dependencies>` 里堆通用依赖，每个模块只声明自己真正用到的。

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
6. 全量覆盖式多对多保存：`@Transactional` 下先 `deleteByXxxId` 再 `insertBatch`；被引用对象删除须只统计未逻辑删除的引用方。
7. Controller 只读端点不记 `@OperationLog`；写端点配 `@SaCheckPermission`，字典型 CRUD 的 create 加 `@Idempotent`（先例：config / dict / notice / post）。敏感接口用 `@OperationLog(recordParams = false)` 防明文入库。
8. 自定义 SQL 与批量插入写进 `resources/mapper/<domain>/*.xml`，不在注解里拼 SQL；纯 CRUD 可只用 `BaseMapper`。
9. 只读跨全表校验（账号唯一等）加 `@IgnoreDataPermission`，避免被部门数据范围干扰。
