package cn.lycoder.boot.module.system.service.notice;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.lycoder.boot.module.system.convert.notice.NoticeConvert;
import cn.lycoder.boot.module.system.dal.dataobject.notice.SystemNoticeDO;
import cn.lycoder.boot.module.system.dal.mapper.notice.SystemNoticeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.NOTICE_NOT_EXISTS;

/**
 * 通知公告 Service 实现
 * <p>
 * 公告量级小、读写频率低，不引入缓存（区别于 dict/config 的读多写少场景）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    /**
     * 前台展示条数上限，防止 limit 参数被放大拖垮查询
     */
    private static final int OPEN_LIST_MAX_LIMIT = 20;

    private final SystemNoticeMapper noticeMapper;
    private final NoticeConvert noticeConvert;

    @Override
    public Long createNotice(NoticeCreateReqVO reqVO) {
        SystemNoticeDO notice = noticeConvert.convert(reqVO);
        notice.setStatus(StringUtils.hasText(reqVO.getStatus())
                ? reqVO.getStatus() : SystemNoticeDO.STATUS_NORMAL);
        noticeMapper.insert(notice);
        return notice.getNoticeId();
    }

    @Override
    public void updateNotice(NoticeUpdateReqVO reqVO) {
        validateNoticeExists(reqVO.getNoticeId());
        noticeMapper.updateById(noticeConvert.convert(reqVO));
    }

    @Override
    public void deleteNotice(Long noticeId) {
        validateNoticeExists(noticeId);
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public SystemNoticeDO getNotice(Long noticeId) {
        return noticeMapper.selectById(noticeId);
    }

    @Override
    public PageResult<SystemNoticeDO> getNoticePage(NoticePageReqVO reqVO) {
        LambdaQueryWrapper<SystemNoticeDO> wrapper = Wrappers.<SystemNoticeDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getNoticeTitle()), SystemNoticeDO::getNoticeTitle, reqVO.getNoticeTitle())
                .eq(StringUtils.hasText(reqVO.getNoticeType()), SystemNoticeDO::getNoticeType, reqVO.getNoticeType())
                .eq(StringUtils.hasText(reqVO.getStatus()), SystemNoticeDO::getStatus, reqVO.getStatus())
                .ge(reqVO.getBeginTime() != null, SystemNoticeDO::getCreateTime, reqVO.getBeginTime())
                .le(reqVO.getEndTime() != null, SystemNoticeDO::getCreateTime, reqVO.getEndTime())
                .orderByDesc(SystemNoticeDO::getNoticeId);
        Page<SystemNoticeDO> page = noticeMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<SystemNoticeDO> getOpenNoticeList(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), OPEN_LIST_MAX_LIMIT);
        return noticeMapper.selectList(Wrappers.<SystemNoticeDO>lambdaQuery()
                .eq(SystemNoticeDO::getStatus, SystemNoticeDO.STATUS_NORMAL)
                .orderByDesc(SystemNoticeDO::getCreateTime)
                .orderByDesc(SystemNoticeDO::getNoticeId)
                .last("LIMIT " + safeLimit));
    }

    private void validateNoticeExists(Long noticeId) {
        if (noticeMapper.selectById(noticeId) == null) {
            throw new ServiceException(NOTICE_NOT_EXISTS);
        }
    }
}
