package cn.lycoder.boot.module.system.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 参数更新 Request VO
 * <p>
 * configId 由路径参数传入并覆盖，请求体可省略；configKey 不允许修改（业务侧按 key 读取，改 key 等同删除重建）
 */
@Data
public class ConfigUpdateReqVO {

    /**
     * 参数主键（由路径参数传入并覆盖）
     */
    @Schema(description = "参数主键（由路径参数传入并覆盖）", example = "1")
    private Long configId;

    /**
     * 参数名称
     */
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称长度不能超过 100")
    @Schema(description = "参数名称", example = "账号初始密码")
    private String configName;

    /**
     * 参数键值
     */
    @NotBlank(message = "参数键值不能为空")
    @Size(max = 500, message = "参数键值长度不能超过 500")
    @Schema(description = "参数键值", example = "123456")
    private String configValue;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500")
    @Schema(description = "备注", example = "重置密码功能使用的默认密码")
    private String remark;
}
