package cn.lycoder.boot.framework.mybatis.config;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import cn.lycoder.boot.framework.mybatis.handler.DefaultMetaObjectHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 自动配置
 */
@AutoConfiguration
public class YukitMybatisAutoConfiguration {

    /**
     * 分页插件
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 字段自动填充处理器
     * <p>
     * 如果容器中存在 {@link OperatorSupplier} Bean（由安全模块提供），则自动注入；
     * 否则回退为 "system"。
     */
    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public MetaObjectHandler metaObjectHandler(ObjectProvider<OperatorSupplier> operatorSupplierProvider) {
        OperatorSupplier supplier = operatorSupplierProvider.getIfAvailable(OperatorSupplier::system);
        return new DefaultMetaObjectHandler(supplier);
    }
}
