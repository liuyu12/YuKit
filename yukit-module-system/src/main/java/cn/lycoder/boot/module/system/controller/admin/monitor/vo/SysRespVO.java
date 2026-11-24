package cn.lycoder.boot.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作系统信息
 */
@Data
@Schema(description = "管理后台 - 操作系统信息")
public class SysRespVO {

    @Schema(description = "操作系统名称", example = "Mac OS X")
    private String osName;

    @Schema(description = "操作系统版本", example = "26.7.3")
    private String osVersion;

    @Schema(description = "系统架构", example = "aarch64")
    private String osArch;

    @Schema(description = "主机名", example = "liuyu-mbp")
    private String computerName;

    @Schema(description = "主机 IP", example = "192.168.1.10")
    private String computerIp;

    @Schema(description = "项目运行路径", example = "/Users/liuyu/Study/YuKit")
    private String userDir;
}
