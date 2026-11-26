---
trigger: glob
glob: "**/*.java"
---

# YuKit Sonar 红线自查

> 分工：能被 lint / CI 稳定判定的规则应下沉到构建（项目已有 Sonar `sonar` profile + JaCoCo）。本文件只收反复触发、需要在生成时就规避的条目；条目本身仅在写 Java 时相关，故触发方式用 glob 而非 always_on（章程里已留指针兜底）。

改完 Java 主动自查，不要等告警：

1. **S109 魔法数字**：截断长度、列宽、单位换算抽 `private static final` 常量（参照 `OperationLogAspect.MAX_CONTENT_LENGTH`）。
2. **S3077 懒加载缓存**：禁用 `volatile` 集合字段，改 `AtomicReference<T>` + `updateAndGet(cur -> cur != null ? cur : build())`，缓存内容须不可变（`Map.copyOf` / `Stream.toList`）。
3. **多构造器 Bean**：两个以上构造器时，注入构造器必须显式 `@Autowired`。
4. **S3776 认知复杂度**：主流程过长就拆私有 helper（参照 `AuthServiceImpl.login`）。
5. **S4790 弱哈希**：摘要用 SHA-256（`MessageDigest` + `HexFormat`），禁 MD5。
6. **日志**：slf4j 参数化 `log.info("x={}", v)`，禁字符串拼接。
7. **JDK API 优先**：向上取整用 `Math.ceilDiv`，不手写 `(n + 59) / 60`。
8. **异常文案**：`ServiceException(ErrorCode)` 不做参数化格式化，错误码 message 用静态文案，不放 `{0}` 占位符。

**例外勿改**：审计/日志类旁路允许 `catch (Exception)`——这是 `OperationLogAspect` 既定的 fail-open 设计，不要为消除 S2221 告警而改窄。

交付前对改动文件 grep：`catch (Exception`、裸数字字面量、`volatile` 非原始类型字段、多构造器类、`MD5`、日志拼接。

> 最近核对：`114e4b2` / 2026-11-26
