---
trigger: always_on
---

# YuKit Sonar 红线自查

生成或修改 Java 代码后必须主动自查以下各项，不要等 IDE/Sonar 告警再返工。

## 硬性规则

1. **魔法数字（S109）**：截断长度、列宽、单位换算等数字一律抽 `private static final` 常量。参照 `OperationLogAspect` 的 `MAX_CONTENT_LENGTH` / `MAX_USER_AGENT_LENGTH`。
2. **懒加载缓存（S3077）**：禁止 `volatile List/Map` 字段（volatile 只保证引用可见性，读取-判断-赋值非原子）。改用 `AtomicReference<T>` + `updateAndGet(cur -> cur != null ? cur : build())`，缓存内容须不可变（`Map.copyOf` / `Stream.toList`）。
3. **多构造器 Bean**：存在两个以上构造器时，注入构造器必须显式 `@Autowired`；只有唯一构造器才依赖隐式注入。
4. **认知复杂度（S3776）**：主流程过长就拆私有 helper（参照 `AuthServiceImpl.login` 拆 `rejectLocked` / `rejectBadCredentials` / `updateLoginInfo` / `record`），既降复杂度又便于单测。
5. **弱哈希（S4790）**：需要摘要/指纹用 SHA-256（`MessageDigest` + `HexFormat`），禁 MD5。
6. **日志**：slf4j 参数化写法 `log.info("x={}", v)`，禁字符串拼接（IDE "Non-constant string" 告警）。
7. **JDK 现成 API 优先**：向上取整用 `Math.ceilDiv`（JDK 21），不手写 `(n + 59) / 60`。
8. **异常文案**：`ServiceException(ErrorCode)` 不做参数化格式化，错误码 message 一律写静态文案，不要放 `{0}` 占位符。

## 例外（勿"优化"）

审计/日志类旁路允许 `catch (Exception)`——这是 `OperationLogAspect` 既定的 fail-open 设计，不要为消除 S2221 告警而改窄成具体异常。

## 交付前自检

对新增/改动文件 grep 逐条核对：`catch (Exception`、裸数字字面量、`volatile` 非原始类型字段、多构造器类、`MD5`、日志字符串拼接。
