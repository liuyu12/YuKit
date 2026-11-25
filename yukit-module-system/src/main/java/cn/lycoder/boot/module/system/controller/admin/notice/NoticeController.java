package cn.lycoder.boot.module.system.controller.admin.notice;

import cn.lycoder.boot.framework.common.operatelog.OperationLog;
import cn.lycoder.boot.framework.common.operatelog.OperationType;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.framework.common.pojo.Result;
import cn.lycoder.boot.framework.protection.idempotent.Idempotent;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeRespVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.lycoder.boot.module.system.convert.notice.NoticeConvert;
import cn.lycoder.boot.module.system.dal.dataobject.notice.SystemNoticeDO;
import cn.lycoder.boot.module.system.service.notice.NoticeService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知公告管理
 * <p>
 * RESTful 风格 + Sa-Token 细粒度权限，权限码与 V2 种子按钮 1035~1038 对应；
 * list-top 面向所有登录用户（前台铃铛展示），不挂按钮权限。
 */
@Tag(name = "通知公告管理")
@RestController
@RequestMapping("/system/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeConvert noticeConvert;

    /**
     * 创建公告
     */
    @Operation(summary = "创建公告")
    @OperationLog(module = "通知公告", name = "创建公告", type = OperationType.INSERT)
    @Idempotent(timeout = 3, message = "请勿重复提交")
    @PostMapping
    @SaCheckPermission("system:notice:add")
    public Result<Long> createNotice(@RequestBody @Valid NoticeCreateReqVO reqVO) {
        return Result.success(noticeService.createNotice(reqVO));
    }

    /**
     * 更新公告
     */
    @Operation(summary = "更新公告")
    @OperationLog(module = "通知公告", name = "更新公告", type = OperationType.UPDATE)
    @PutMapping("/{noticeId}")
    @SaCheckPermission("system:notice:edit")
    public Result<Boolean> updateNotice(@Parameter(description = "公告主键") @PathVariable("noticeId") Long noticeId,
                                        @RequestBody @Valid NoticeUpdateReqVO reqVO) {
        reqVO.setNoticeId(noticeId);
        noticeService.updateNotice(reqVO);
        return Result.success(true);
    }

    /**
     * 删除公告
     */
    @Operation(summary = "删除公告")
    @OperationLog(module = "通知公告", name = "删除公告", type = OperationType.DELETE)
    @DeleteMapping("/{noticeId}")
    @SaCheckPermission("system:notice:remove")
    public Result<Boolean> deleteNotice(@Parameter(description = "公告主键") @PathVariable("noticeId") Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return Result.success(true);
    }

    /**
     * 获取公告详情
     */
    @Operation(summary = "获取公告详情")
    @GetMapping("/{noticeId}")
    @SaCheckPermission("system:notice:query")
    public Result<NoticeRespVO> getNotice(@Parameter(description = "公告主键") @PathVariable("noticeId") Long noticeId) {
        SystemNoticeDO notice = noticeService.getNotice(noticeId);
        return Result.success(notice == null ? null : noticeConvert.convert(notice));
    }

    /**
     * 分页查询公告
     */
    @Operation(summary = "分页查询公告")
    @GetMapping
    @SaCheckPermission("system:notice:list")
    public Result<PageResult<NoticeRespVO>> getNoticePage(@Valid NoticePageReqVO reqVO) {
        PageResult<SystemNoticeDO> pageResult = noticeService.getNoticePage(reqVO);
        return Result.success(noticeConvert.convertPage(pageResult));
    }

    /**
     * 最新正常状态公告（前台铃铛/弹窗，登录即可访问）
     */
    @Operation(summary = "最新正常状态公告（前台展示）")
    @GetMapping("/list-top")
    public Result<List<NoticeRespVO>> getOpenNoticeList(
            @Parameter(description = "条数，1~20") @RequestParam(value = "limit", defaultValue = "5")
            @Min(value = 1, message = "条数最小为 1") @Max(value = 20, message = "条数最大为 20") Integer limit) {
        return Result.success(noticeConvert.convertList(noticeService.getOpenNoticeList(limit)));
    }
}
