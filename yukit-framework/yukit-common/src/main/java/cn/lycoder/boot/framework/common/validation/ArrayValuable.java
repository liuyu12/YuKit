package cn.lycoder.boot.framework.common.validation;

/**
 * 可枚举值接口：枚举类实现后配合 {@link InEnum} 做参数白名单校验
 * <p>
 * 典型实现：
 * <pre>{@code
 * public enum CommonStatusEnum implements ArrayValuable<Integer> {
 *     ENABLE(0), DISABLE(1);
 *
 *     public static final Integer[] ARRAYS = Arrays.stream(values())
 *             .map(CommonStatusEnum::getStatus).toArray(Integer[]::new);
 *
 *     @Override
 *     public Integer[] array() {
 *         return ARRAYS;
 *     }
 * }
 * }</pre>
 */
public interface ArrayValuable<T> {

    /**
     * @return 该类型全部合法值，作为校验白名单
     */
    T[] array();
}
