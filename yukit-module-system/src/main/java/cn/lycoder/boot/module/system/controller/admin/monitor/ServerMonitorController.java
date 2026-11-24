package cn.lycoder.boot.module.system.controller.admin.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.ServerInfoRespVO;
import cn.lycoder.boot.module.system.service.monitor.ServerMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控：展示当前实例运行机器的硬件/系统/JVM 指标
 */
@Tag(name = "服务器监控")
@RestController
@RequestMapping("/monitor/server")
@RequiredArgsConstructor
public class ServerMonitorController {

    private final ServerMonitorService serverMonitorService;

    /**
     * 获取服务器监控信息
     */
    @Operation(summary = "获取服务器监控信息")
    @GetMapping
    @SaCheckPermission("monitor:server:list")
    public Result<ServerInfoRespVO> getServerInfo() {
        return Result.success(serverMonitorService.getServerInfo());
    }
}
