package cn.lycoder.boot.module.system.service.notice;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.notice.SystemNoticeDO;

import java.util.List;

/**
 * 通知公告 Service 接口
 */
public interface NoticeService {

    /**
     * 创建公告
     *
     * @param reqVO 创建请求
     * @return 公告主键
     */
    Long createNotice(NoticeCreateReqVO reqVO);

    /**
     * 更新公告
     *
     * @param reqVO 更新请求，noticeId 由路径参数覆盖
     */
    void updateNotice(NoticeUpdateReqVO reqVO);

    /**
     * 删除公告
     *
     * @param noticeId 公告主键
     */
    void deleteNotice(Long noticeId);

    /**
     * 按主键查询公告
     *
     * @param noticeId 公告主键
     * @return 公告 DO，不存在返回 null
     */
    SystemNoticeDO getNotice(Long noticeId);

    /**
     * 分页查询公告
     *
     * @param reqVO 分页条件
     * @return 公告分页
     */
    PageResult<SystemNoticeDO> getNoticePage(NoticePageReqVO reqVO);

    /**
     * 最新正常状态公告列表（前台铃铛/弹窗展示用，按创建时间倒序）
     *
     * @param limit 最大条数
     * @return 公告列表
     */
    List<SystemNoticeDO> getOpenNoticeList(int limit);
}
