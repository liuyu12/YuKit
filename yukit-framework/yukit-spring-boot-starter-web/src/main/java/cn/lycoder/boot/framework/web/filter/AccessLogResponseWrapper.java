package cn.lycoder.boot.framework.web.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 有界缓存响应包装
 * <p>
 * 字节流照常透传输出，仅额外窥探前 {@code maxBytes} 个字节用于解析统一 {@code Result} 的业务 code/msg，
 * 不全量缓冲大响应体，也无需 copyBodyToResponse。
 */
public class AccessLogResponseWrapper extends HttpServletResponseWrapper {

    private final int maxBytes;
    private byte[] head = new byte[0];
    private int captured = 0;
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public AccessLogResponseWrapper(HttpServletResponse response, int maxBytes) {
        super(response);
        this.maxBytes = Math.max(0, maxBytes);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new TeeServletOutputStream(super.getOutputStream());
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null || encoding.isBlank()
                    ? StandardCharsets.UTF_8 : Charset.forName(encoding);
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), charset));
        }
        return writer;
    }

    /**
     * 返回已窥探到的响应体头部字节（最多 maxBytes），供解析业务 code/msg
     */
    public byte[] getContentHead() {
        return head;
    }

    /**
     * 头部缓冲已满，响应可能还有未窥探部分（打印时标注截断）
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

    private final class TeeServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream delegate;

        private TeeServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            capture(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            capture(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            delegate.setWriteListener(listener);
        }
    }
}
