package cn.lycoder.boot.module.system.controller.admin.operatelog.vo;

import cn.lycoder.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志分页查询 Request VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLogPageReqVO extends PageParam {

    /**
     * 所属模块，模糊匹配
     */
    @Schema(description = "所属模块，模糊匹配", example = "用户管理")
    private String module;

    /**
     * 操作名，模糊匹配
     */
    @Schema(description = "操作名，模糊匹配", example = "创建")
    private String name;

    /**
     * 操作人，模糊匹配
     */
    @Schema(description = "操作人，模糊匹配", example = "admin")
    private String operator;

    /**
     * 操作类型码，精确匹配
     */
    @Schema(description = "操作类型码", example = "2")
    private Integer type;

    /**
     * 操作结果：0 成功，1 失败
     */
    @Schema(description = "操作结果：0 成功，1 失败", example = "0")
    private Integer status;

    /**
     * 操作时间范围 - 起
     */
    @Schema(description = "操作时间范围-起")
    private LocalDateTime beginTime;

    /**
     * 操作时间范围 - 止
     */
    @Schema(description = "操作时间范围-止")
    private LocalDateTime endTime;
}
