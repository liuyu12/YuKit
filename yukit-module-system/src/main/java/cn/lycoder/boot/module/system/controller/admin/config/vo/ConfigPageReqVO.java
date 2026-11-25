package cn.lycoder.boot.module.system.controller.admin.config.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 参数分页查询 Request VO")
public class ConfigPageReqVO extends PageParam {

    /**
     * 参数名称，模糊匹配
     */
    @Schema(description = "参数名称，模糊匹配", example = "密码")
    private String configName;

    /**
     * 参数键名，模糊匹配
     */
    @Schema(description = "参数键名，模糊匹配", example = "sys.user")
    private String configKey;

    /**
     * 是否内置：Y 是 / N 否
     */
    @Schema(description = "是否内置：Y 是 / N 否", example = "N")
    private String configType;
}
