package cn.lycoder.boot.module.system.controller.admin.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.module.system.controller.admin.monitor.vo.DatasourceRespVO;
import cn.lycoder.boot.module.system.service.monitor.DatasourceMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据监控：数据源与连接池运行状态（对齐菜单 111，项目用 HikariCP 故非 Druid 控制台）
 */
@Tag(name = "数据监控")
@RestController
@RequestMapping("/monitor/datasource")
@RequiredArgsConstructor
public class DatasourceMonitorController {

    private final DatasourceMonitorService datasourceMonitorService;

    /**
     * 获取数据源监控信息
     */
    @Operation(summary = "获取数据源监控信息")
    @GetMapping
    @SaCheckPermission("monitor:druid:list")
    public Result<DatasourceRespVO> getDatasourceInfo() {
        return Result.success(datasourceMonitorService.getDatasourceInfo());
    }
}
