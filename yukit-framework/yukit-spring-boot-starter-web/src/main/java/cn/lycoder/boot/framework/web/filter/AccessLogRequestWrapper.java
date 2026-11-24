package cn.lycoder.boot.framework.web.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 有界窥探请求体包装
 * <p>
 * 输入流照常透传给下游读取，仅额外窥探前 {@code maxBytes} 个字节用于日志打印，不全量缓冲。
 * 仅对 application/json 请求包装（由过滤器判断），multipart/表单请求不走此包装以避免与容器参数解析冲突。
 */
public class AccessLogRequestWrapper extends HttpServletRequestWrapper {

    private final int maxBytes;
    private byte[] head = new byte[0];
    private int captured = 0;
    private ServletInputStream inputStream;
    private BufferedReader reader;

    public AccessLogRequestWrapper(HttpServletRequest request, int maxBytes) {
        super(request);
        this.maxBytes = Math.max(0, maxBytes);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new TeeServletInputStream(super.getInputStream());
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null || encoding.isBlank()
                    ? StandardCharsets.UTF_8 : Charset.forName(encoding);
            reader = new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
        return reader;
    }

    /**
     * 已窥探到的请求体头部字节（最多 maxBytes）
     */
    public byte[] getContentHead() {
        return head;
    }

    /**
     * 头部缓冲已满，body 可能还有未窥探部分（打印时标注截断）
     */
    public boolean isTruncated() {
        return captured >= maxBytes;
    }

    private synchronized void capture(byte[] bytes, int off, int len) {
        if (captured >= maxBytes || len <= 0) {
            return;
        }
        int take = Math.min(maxBytes - captured, len);
        head = Arrays.copyOf(head, captured + take);
        System.arraycopy(bytes, off, head, captured, take);
        captured += take;
    }

    private final class TeeServletInputStream extends ServletInputStream {

        private final ServletInputStream delegate;

        private TeeServletInputStream(ServletInputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b >= 0) {
                capture(new byte[]{(byte) b}, 0, 1);
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = delegate.read(b, off, len);
            if (n > 0) {
                capture(b, off, n);
            }
            return n;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            delegate.setReadListener(listener);
        }
    }
}
