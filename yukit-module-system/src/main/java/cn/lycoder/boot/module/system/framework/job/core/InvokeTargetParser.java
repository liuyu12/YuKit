package cn.lycoder.boot.module.system.framework.job.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析 {@code sys_job.invoke_target}：形如 {@code beanName.methodName(arg1,arg2,...)}
 * <p>
 * 支持的参数字面量：
 * <ul>
 *   <li>字符串：{@code "abc"} / {@code 'abc'}</li>
 *   <li>布尔：{@code true} / {@code false}</li>
 *   <li>空值：{@code null}</li>
 *   <li>整型：默认解析为 Long；如需 Integer 在调用方做类型匹配</li>
 *   <li>浮点：包含小数点时解析为 Double</li>
 * </ul>
 * 无参形式可省略括号：{@code beanName.methodName}。
 */
public final class InvokeTargetParser {

    private static final char LPAREN = '(';
    private static final char RPAREN = ')';
    private static final char COMMA = ',';
    private static final char DOT = '.';
    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';
    private static final String NULL_LITERAL = "null";
    private static final String TRUE_LITERAL = "true";
    private static final String FALSE_LITERAL = "false";

    private InvokeTargetParser() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 解析为不可变记录。失败抛 IllegalArgumentException，由 Service 转业务异常。
     */
    public static Parsed parse(String invokeTarget) {
        if (invokeTarget == null || invokeTarget.isBlank()) {
            throw new IllegalArgumentException("调用目标不能为空");
        }
        String trimmed = invokeTarget.trim();
        int lparen = trimmed.indexOf(LPAREN);
        int rparen = trimmed.lastIndexOf(RPAREN);
        String methodExpr;
        String argsExpr;
        if (lparen < 0 && rparen < 0) {
            methodExpr = trimmed;
            argsExpr = "";
        } else if (lparen > 0 && rparen == trimmed.length() - 1 && lparen < rparen) {
            methodExpr = trimmed.substring(0, lparen);
            argsExpr = trimmed.substring(lparen + 1, rparen).trim();
        } else {
            throw new IllegalArgumentException("调用目标格式错误，应为 beanName.methodName 或 beanName.methodName(args)");
        }
        int dot = methodExpr.lastIndexOf(DOT);
        if (dot <= 0 || dot == methodExpr.length() - 1) {
            throw new IllegalArgumentException("调用目标缺少 beanName.methodName 分隔点");
        }
        String beanName = methodExpr.substring(0, dot);
        String methodName = methodExpr.substring(dot + 1);
        List<Object> args = parseArgs(argsExpr);
        return new Parsed(beanName, methodName, args.toArray());
    }

    private static List<Object> parseArgs(String argsExpr) {
        List<Object> result = new ArrayList<>();
        if (argsExpr.isEmpty()) {
            return result;
        }
        for (String raw : splitTopLevel(argsExpr)) {
            result.add(parseValue(raw.trim()));
        }
        return result;
    }

    /**
     * 按逗号切分，但忽略引号内的逗号（如字符串参数含逗号）。
     */
    private static List<String> splitTopLevel(String argsExpr) {
        List<String> parts = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < argsExpr.length(); i++) {
            char c = argsExpr.charAt(i);
            if (quote == 0 && (c == SINGLE_QUOTE || c == DOUBLE_QUOTE)) {
                quote = c;
                buf.append(c);
            } else if (quote != 0 && c == quote) {
                quote = 0;
                buf.append(c);
            } else if (quote == 0 && c == COMMA) {
                parts.add(buf.toString());
                buf.setLength(0);
            } else {
                buf.append(c);
            }
        }
        parts.add(buf.toString());
        return parts;
    }

    private static Object parseValue(String token) {
        if (token.isEmpty()) {
            return "";
        }
        char first = token.charAt(0);
        if ((first == SINGLE_QUOTE || first == DOUBLE_QUOTE) && token.length() >= 2
                && token.charAt(token.length() - 1) == first) {
            return token.substring(1, token.length() - 1);
        }
        if (NULL_LITERAL.equalsIgnoreCase(token)) {
            return null;
        }
        if (TRUE_LITERAL.equalsIgnoreCase(token)) {
            return Boolean.TRUE;
        }
        if (FALSE_LITERAL.equalsIgnoreCase(token)) {
            return Boolean.FALSE;
        }
        try {
            if (token.indexOf('.') >= 0) {
                return Double.valueOf(token);
            }
            return Long.valueOf(token);
        } catch (NumberFormatException ignored) {
            // 非数字字面量：原样作为字符串处理（如枚举名 / SpEL 留给调用方）
            return token;
        }
    }

    /**
     * 解析结果：bean 名 + 方法名 + 实参数组
     */
    public record Parsed(String beanName, String methodName, Object[] args) {
    }
}
