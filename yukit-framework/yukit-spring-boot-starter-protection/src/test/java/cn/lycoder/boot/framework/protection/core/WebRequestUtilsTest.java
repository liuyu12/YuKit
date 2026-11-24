package cn.lycoder.boot.framework.protection.core;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebRequestUtils} 单元测试：入参指纹、客户端 IP 解析、操作人回退
 */
class WebRequestUtilsTest {

    @Test
    @DisplayName("fingerprint：同输入稳定、不同输入不同、null 安全、结果为 64 位十六进制")
    void fingerprint_shouldBeStableAndSensitive() {
        Object[] args = {"a", 1};
        String one = WebRequestUtils.fingerprint(args);
        String two = WebRequestUtils.fingerprint(new Object[]{"a", 1});

        assertThat(one).hasSize(64).matches("[0-9a-f]{64}");
        // 内容相同的入参得到相同指纹
        assertThat(one).isEqualTo(two);
        // 内容不同得到不同指纹
        assertThat(one).isNotEqualTo(WebRequestUtils.fingerprint(new Object[]{"b", 2}));
        // null 入参按空串处理，稳定且仍为合法十六进制摘要
        assertThat(WebRequestUtils.fingerprint(null))
                .hasSize(64)
                .isEqualTo(WebRequestUtils.fingerprint(null));
    }

    @Test
    @DisplayName("resolveClientIp：null 请求返回 unknown")
    void resolveClientIp_null_returnsUnknown() {
        assertThat(WebRequestUtils.resolveClientIp(null)).isEqualTo("unknown");
    }

    @Test
    @DisplayName("resolveClientIp：优先取 X-Forwarded-For，多 IP 取首个")
    void resolveClientIp_prefersForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
        request.addHeader("X-Real-IP", "9.9.9.9");
        request.setRemoteAddr("127.0.0.1");
        assertThat(WebRequestUtils.resolveClientIp(request)).isEqualTo("1.2.3.4");
    }

    @Test
    @DisplayName("resolveClientIp：无代理头时回退 remoteAddr")
    void resolveClientIp_fallbackRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        assertThat(WebRequestUtils.resolveClientIp(request)).isEqualTo("10.0.0.5");
    }

    @Test
    @DisplayName("resolveOperator：供应商有值用其值，空/异常回退客户端 IP")
    void resolveOperator_fallbackToIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.9");

        OperatorSupplier present = () -> "admin";
        assertThat(WebRequestUtils.resolveOperator(present, request)).isEqualTo("admin");

        OperatorSupplier blank = () -> "  ";
        assertThat(WebRequestUtils.resolveOperator(blank, request)).isEqualTo("10.0.0.9");

        OperatorSupplier broken = () -> {
            throw new IllegalStateException("not login");
        };
        assertThat(WebRequestUtils.resolveOperator(broken, request)).isEqualTo("10.0.0.9");

        assertThat(WebRequestUtils.resolveOperator(null, request)).isEqualTo("10.0.0.9");
    }
}
