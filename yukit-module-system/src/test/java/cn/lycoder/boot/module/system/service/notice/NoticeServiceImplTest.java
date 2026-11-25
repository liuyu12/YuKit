package cn.lycoder.boot.module.system.service.notice;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.lycoder.boot.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.lycoder.boot.module.system.convert.notice.NoticeConvertImpl;
import cn.lycoder.boot.module.system.dal.dataobject.notice.SystemNoticeDO;
import cn.lycoder.boot.module.system.dal.mapper.notice.SystemNoticeMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NoticeServiceImpl 单测：纯 Mockito，不起 Spring 上下文
 * <p>
 * Convert 使用 MapStruct 编译期生成的真实实现。
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    private SystemNoticeMapper noticeMapper;

    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        noticeService = new NoticeServiceImpl(noticeMapper, new NoticeConvertImpl());
    }

    @Test
    void createShouldDefaultStatusNormalAndInsert() {
        when(noticeMapper.insert(any(SystemNoticeDO.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, SystemNoticeDO.class).setNoticeId(10L);
            return 1;
        });
        NoticeCreateReqVO reqVO = new NoticeCreateReqVO();
        reqVO.setNoticeTitle("维护通知");
        reqVO.setNoticeType("1");
        reqVO.setNoticeContent("周六凌晨停机");

        Long noticeId = noticeService.createNotice(reqVO);

        assertThat(noticeId).isEqualTo(10L);
        ArgumentCaptor<SystemNoticeDO> captor = ArgumentCaptor.forClass(SystemNoticeDO.class);
        verify(noticeMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SystemNoticeDO.STATUS_NORMAL);
        assertThat(captor.getValue().getNoticeType()).isEqualTo("1");
    }

    @Test
    void createShouldKeepExplicitStatus() {
        when(noticeMapper.insert(any(SystemNoticeDO.class))).thenReturn(1);
        NoticeCreateReqVO reqVO = new NoticeCreateReqVO();
        reqVO.setNoticeTitle("已关闭公告");
        reqVO.setNoticeType("2");
        reqVO.setStatus(SystemNoticeDO.STATUS_CLOSED);

        noticeService.createNotice(reqVO);

        ArgumentCaptor<SystemNoticeDO> captor = ArgumentCaptor.forClass(SystemNoticeDO.class);
        verify(noticeMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SystemNoticeDO.STATUS_CLOSED);
    }

    @Test
    void updateShouldRejectNotExists() {
        when(noticeMapper.selectById(99L)).thenReturn(null);
        NoticeUpdateReqVO reqVO = new NoticeUpdateReqVO();
        reqVO.setNoticeId(99L);
        reqVO.setNoticeTitle("n");
        reqVO.setNoticeType("1");
        reqVO.setStatus(SystemNoticeDO.STATUS_NORMAL);

        assertThatThrownBy(() -> noticeService.updateNotice(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.NOTICE_NOT_EXISTS.getCode());
        verify(noticeMapper, never()).updateById(any(SystemNoticeDO.class));
    }

    @Test
    void updateShouldPersistFullFields() {
        when(noticeMapper.selectById(1L)).thenReturn(buildDO(1L));
        NoticeUpdateReqVO reqVO = new NoticeUpdateReqVO();
        reqVO.setNoticeId(1L);
        reqVO.setNoticeTitle("新标题");
        reqVO.setNoticeType("2");
        reqVO.setStatus(SystemNoticeDO.STATUS_CLOSED);

        noticeService.updateNotice(reqVO);

        ArgumentCaptor<SystemNoticeDO> captor = ArgumentCaptor.forClass(SystemNoticeDO.class);
        verify(noticeMapper).updateById(captor.capture());
        assertThat(captor.getValue().getNoticeTitle()).isEqualTo("新标题");
        assertThat(captor.getValue().getStatus()).isEqualTo(SystemNoticeDO.STATUS_CLOSED);
    }

    @Test
    void deleteShouldRemoveExisting() {
        when(noticeMapper.selectById(2L)).thenReturn(buildDO(2L));

        noticeService.deleteNotice(2L);

        verify(noticeMapper).deleteById(2L);
    }

    @Test
    void deleteShouldRejectNotExists() {
        when(noticeMapper.selectById(3L)).thenReturn(null);

        assertThatThrownBy(() -> noticeService.deleteNotice(3L))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.NOTICE_NOT_EXISTS.getCode());
        verify(noticeMapper, never()).deleteById(3L);
    }

    @Test
    void getNoticePageShouldConvertRecordsAndTotal() {
        Page<SystemNoticeDO> mpPage = new Page<>(1, 10);
        mpPage.setRecords(List.of(buildDO(1L)));
        mpPage.setTotal(1L);
        when(noticeMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mpPage);
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setNoticeTitle("维护");

        PageResult<SystemNoticeDO> result = noticeService.getNoticePage(reqVO);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).singleElement()
                .satisfies(doItem -> assertThat(doItem.getNoticeId()).isEqualTo(1L));
    }

    @Test
    void getOpenNoticeListShouldQueryWithClampedLimit() {
        when(noticeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildDO(1L)));

        List<SystemNoticeDO> list = noticeService.getOpenNoticeList(100);

        // 超限 limit 被钳制到上限后仍正常回源，结果透传
        assertThat(list).hasSize(1);
        verify(noticeMapper).selectList(any(Wrapper.class));
    }

    private SystemNoticeDO buildDO(Long noticeId) {
        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setNoticeId(noticeId);
        notice.setNoticeTitle("维护通知");
        notice.setNoticeType("1");
        notice.setNoticeContent("周六凌晨停机");
        notice.setStatus(SystemNoticeDO.STATUS_NORMAL);
        return notice;
    }
}
