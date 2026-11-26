package cn.lycoder.boot.module.system.framework.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 唯一索引与 {@link DuplicateKeyExceptionHandler} 登记表的配对守卫测试
 * <p>
 * 纯 JVM：只读迁移脚本与处理器内的映射表，不起 Spring 上下文、不连数据库。
 * <p>
 * 存在的理由：处理器靠「MySQL 报文里的索引名 → 业务错误码」把并发撞库还原成与预校验一致的提示，
 * 这条链路没有任何编译期或运行期约束——加了 {@code UNIQUE KEY} 却忘了登记，代码照样跑，
 * 用户只会看到兜底码；反过来删了索引却留着登记，映射表就成了幽灵。两种漂移都只在真实撞库时才暴露，
 * 所以把「两边必须相等」固化成测试，而不是写进文档靠人自觉。
 * <p>
 * 兜底能力本身（未登记索引不得回 500）由 {@link DuplicateKeyExceptionHandlerTest} 覆盖，本类不重复。
 */
class UniqueIndexRegistrationTest {

    /**
     * 迁移脚本里声明唯一索引的两种写法：
     * {@code UNIQUE KEY `x` (cols)}（建表内）与 {@code ADD UNIQUE KEY `x` (cols)}（ALTER）
     */
    private static final Pattern UNIQUE_INDEX_PATTERN =
            Pattern.compile("(?i)\\bUNIQUE\\s+(?:KEY|INDEX)\\s+`?([A-Za-z0-9_]+)`?");

    /**
     * 迁移脚本所在 classpath 目录（Flyway 与应用读同一份，测试不再拼相对路径，避免 IDE 工作目录差异）
     */
    private static final String MIGRATION_PATTERN = "classpath*:db/migration/V*.sql";

    @Test
    @DisplayName("迁移脚本声明的每个唯一索引都已登记，且登记项都真实存在")
    void declaredIndexesAndRegistrationsMustMatch() throws Exception {
        Set<String> declared = declaredIndexes();
        Set<String> registered = registeredIndexes();

        // 先防「两边都空所以相等」的假通过：抽取器写坏或资源没找到时，declared 会静默变成空集
        assertThat(declared)
                .as("未能从 %s 抽取到唯一索引，抽取正则或脚本路径已失效", MIGRATION_PATTERN)
                .isNotEmpty();
        assertThat(registered)
                .as("处理器的索引映射表为空，字段名或结构已变更")
                .isNotEmpty();

        Set<String> unregistered = diff(declared, registered);
        assertThat(unregistered)
                .as("这些唯一索引在迁移脚本里声明了，却没登记进 DuplicateKeyExceptionHandler.INDEX_ERROR_CODES，"
                        + "并发撞库时会退化成通用码而不是精确的业务提示")
                .isEmpty();

        Set<String> stale = diff(registered, declared);
        assertThat(stale)
                .as("这些登记项在迁移脚本里已找不到对应的唯一索引声明，映射表陈旧，应随索引一起删除")
                .isEmpty();
    }

    /**
     * 从全部迁移脚本抽取唯一索引名，跳过 {@code --} 注释行，避免设计说明里的字面量被当成声明
     */
    private static Set<String> declaredIndexes() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MIGRATION_PATTERN);
        Set<String> names = new TreeSet<>();
        for (Resource resource : resources) {
            String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (String line : sql.split("\n")) {
                if (line.trim().startsWith("--")) {
                    continue;
                }
                Matcher matcher = UNIQUE_INDEX_PATTERN.matcher(line);
                while (matcher.find()) {
                    names.add(matcher.group(1));
                }
            }
        }
        return names;
    }

    /**
     * 反射读取处理器的私有映射表键集：宁可在此处用反射，也不为测试放宽生产类的可见性。
     * 字段被改名会抛 NoSuchFieldException 让测试红灯，而不是静默通过。
     */
    private static Set<String> registeredIndexes() throws Exception {
        Field field = DuplicateKeyExceptionHandler.class.getDeclaredField("INDEX_ERROR_CODES");
        field.setAccessible(true);
        return ((Map<?, ?>) field.get(null)).keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Set<String> diff(Set<String> source, Set<String> other) {
        Set<String> result = new TreeSet<>(source);
        result.removeAll(other);
        return result;
    }
}
