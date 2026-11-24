package cn.lycoder.boot.module.system.framework.job.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link InvokeTargetParser} 字面量解析单元测试
 */
class InvokeTargetParserTest {

    @Test
    @DisplayName("无参：beanName.methodName 与 beanName.methodName() 等价")
    void parsesNoArgForm() {
        InvokeTargetParser.Parsed a = InvokeTargetParser.parse("sampleHeartbeatTask.beat");
        assertThat(a.beanName()).isEqualTo("sampleHeartbeatTask");
        assertThat(a.methodName()).isEqualTo("beat");
        assertThat(a.args()).isEmpty();

        InvokeTargetParser.Parsed b = InvokeTargetParser.parse("sampleHeartbeatTask.beat()");
        assertThat(b.args()).isEmpty();
    }

    @Test
    @DisplayName("带字符串参：单引号 / 双引号 / 含逗号 / 含转义后均识别")
    void parsesStringArg() {
        InvokeTargetParser.Parsed a = InvokeTargetParser.parse("task.echo(\"hello\")");
        assertThat(a.args()).containsExactly("hello");

        InvokeTargetParser.Parsed b = InvokeTargetParser.parse("task.echo('a,b')");
        assertThat(b.args()).containsExactly("a,b");
    }

    @Test
    @DisplayName("多类型字面量：数字 / 布尔 / null / 浮点 自动适配")
    void parsesMixedLiterals() {
        InvokeTargetParser.Parsed p = InvokeTargetParser.parse("task.run(7,true,null,1.5)");
        assertThat(p.args()).containsExactly(7L, Boolean.TRUE, null, 1.5d);
    }

    @Test
    @DisplayName("空参：返回空字符串而非 null（便于 Bean 形参为 String 时使用）")
    void emptyArgBecomesEmptyString() {
        InvokeTargetParser.Parsed p = InvokeTargetParser.parse("task.echo(,)");
        assertThat(p.args()).containsExactly("", "");
    }

    @Test
    @DisplayName("异常：null / 缺分隔点 / 括号错位 均抛 IllegalArgumentException")
    void rejectsInvalidForms() {
        assertThatThrownBy(() -> InvokeTargetParser.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvokeTargetParser.parse("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvokeTargetParser.parse("justBeanName"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvokeTargetParser.parse("bean."))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvokeTargetParser.parse("bean.method(args"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
