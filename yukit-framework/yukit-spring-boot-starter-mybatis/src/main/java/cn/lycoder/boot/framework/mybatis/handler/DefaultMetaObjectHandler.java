package cn.lycoder.boot.framework.mybatis.handler;

import cn.lycoder.boot.framework.common.security.OperatorSupplier;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 自动填充 BaseEntity 中的 createBy、createTime、updateBy、updateTime 字段
 */
public class DefaultMetaObjectHandler implements MetaObjectHandler {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final Clock clock;
    private final OperatorSupplier operatorSupplier;

    public DefaultMetaObjectHandler() {
        this(Clock.system(ZONE_ID), OperatorSupplier.system());
    }

    public DefaultMetaObjectHandler(OperatorSupplier operatorSupplier) {
        this(Clock.system(ZONE_ID), operatorSupplier);
    }

    public DefaultMetaObjectHandler(Clock clock, OperatorSupplier operatorSupplier) {
        this.clock = clock;
        this.operatorSupplier = operatorSupplier;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(clock);
        String operator = getCurrentUser();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, operator);
        this.strictInsertFill(metaObject, "updateBy", String.class, operator);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now(clock));
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    /**
     * 获取当前操作用户
     */
    private String getCurrentUser() {
        String operator = operatorSupplier.get();
        return operator != null ? operator : "system";
    }
}
