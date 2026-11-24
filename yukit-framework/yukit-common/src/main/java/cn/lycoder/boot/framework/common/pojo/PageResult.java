package cn.lycoder.boot.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页响应结果
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T extends Serializable> implements Serializable {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总量
     */
    private Long total;

    public static <T extends Serializable> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0L);
    }

    /**
     * 分页元素类型转换：如 DO 分页结果 → VO 分页结果
     *
     * @param source    源分页结果
     * @param converter 元素转换函数
     */
    public static <S extends Serializable, T extends Serializable> PageResult<T> convert(
            PageResult<S> source, Function<S, T> converter) {
        if (source == null) {
            return empty();
        }
        return new PageResult<>(source.getList().stream().map(converter).toList(), source.getTotal());
    }

    public PageResult(List<T> list) {
        this.list = list;
        this.total = (long) (list == null ? 0 : list.size());
    }
}