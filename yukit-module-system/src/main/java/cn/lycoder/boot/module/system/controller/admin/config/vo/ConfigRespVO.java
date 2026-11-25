package cn.lycoder.boot.module.system.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参数 Response VO
 */
@Data
@Schema(description = "管理后台 - 参数 Response VO")
public class ConfigRespVO implements Serializable {

    /**
     * 参数主键
     */
    @Schema(description = "参数主键", example = "1")
    private Long configId;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称", example = "账号初始密码")
    private String configName;

    /**
     * 参数键名
     */
    @Schema(description = "参数键名", example = "sys.user.initPassword")
    private String configKey;

    /**
     * 参数键值
     */
    @Schema(description = "参数键值", example = "123456")
    private String configValue;

    /**
     * 是否内置：Y 是 / N 否
     */
    @Schema(description = "是否内置：Y 是 / N 否", example = "Y")
    private String configType;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "重置密码功能使用的默认密码")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-01-02 12:00:00")
    private LocalDateTime updateTime;
}
