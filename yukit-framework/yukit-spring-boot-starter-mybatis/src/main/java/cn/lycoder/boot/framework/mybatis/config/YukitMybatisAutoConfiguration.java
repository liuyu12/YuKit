package cn.lycoder.boot.framework.mybatis.config;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.mybatis.datapermission.aop.IgnoreDataPermissionAspect;
import cn.lycoder.boot.framework.mybatis.datapermission.core.DataPermissionRule;
import cn.lycoder.boot.framework.mybatis.datapermission.core.YukitDataPermissionHandler;
import cn.lycoder.boot.framework.mybatis.handler.DefaultMetaObjectHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * MyBatis-Plus 自动配置
 */
@AutoConfiguration
public class YukitMybatisAutoConfiguration {

    /**
     * 分页插件 + 数据权限插件
     * <p>
     * 数据权限基于 MyBatis-Plus 官方 {@link DataPermissionInterceptor} + JSqlParser，在 SQL 执行前
     * 解析 SELECT 并按业务规则（{@link DataPermissionRule}）自动改写 WHERE，对 Mapper 全透明、连表也生效。
     * 仅当容器中存在至少一个 {@link DataPermissionRule} Bean 时才装配，避免无谓的 SQL 解析开销。
     * <p>
     * 注意：{@link DataPermissionInterceptor} 必须加在 {@link PaginationInnerInterceptor} <b>之前</b>，
     * 否则分页 count 语句不会被追加数据权限条件。
     * <p>
     * 关键：装配阶段<b>不能实例化</b> {@link DataPermissionRule}（其依赖 Mapper → SqlSessionFactory →
     * 本拦截器，会成环）。故「是否存在规则」用 bean 定义判定（allowEagerInit=false），规则交给 handler 首次查询时懒解析。
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<DataPermissionRule> ruleProvider,
            ConfigurableListableBeanFactory beanFactory) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // allowEagerInit=false：仅按类型/定义判定是否存在规则，不触发其实例化
        String[] ruleBeanNames = beanFactory.getBeanNamesForType(DataPermissionRule.class, true, false);
        if (ruleBeanNames.length > 0) {
            DataPermissionInterceptor dataPermission = new DataPermissionInterceptor();
            dataPermission.setDataPermissionHandler(
                    new YukitDataPermissionHandler(() -> ruleProvider.orderedStream().toList()));
            interceptor.addInnerInterceptor(dataPermission);
        }

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * {@code @IgnoreDataPermission} 切面：为全局数据权限提供方法级关闭能力
     */
    @Bean
    @ConditionalOnMissingBean
    public IgnoreDataPermissionAspect ignoreDataPermissionAspect() {
        return new IgnoreDataPermissionAspect();
    }

    /**
     * 字段自动填充处理器
     * <p>
     * 如果容器中存在 {@link OperatorSupplier} Bean（由安全模块提供），则自动注入；否则回退为 "system"。
     * 时间源优先复用全局 {@link Clock} Bean（yukit.zone-id 统一配置），
     * 独立使用本 starter 时回退为处理器内置默认。
     */
    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public MetaObjectHandler metaObjectHandler(ObjectProvider<OperatorSupplier> operatorSupplierProvider,
            ObjectProvider<Clock> clockProvider) {
        OperatorSupplier supplier = operatorSupplierProvider.getIfAvailable(OperatorSupplier::system);
        Clock clock = clockProvider.getIfAvailable();
        return clock != null
                ? new DefaultMetaObjectHandler(clock, supplier)
                : new DefaultMetaObjectHandler(supplier);
    }
}
