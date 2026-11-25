package cn.lycoder.boot.module.system.dal.mapper.userpost;

import cn.lycoder.boot.module.system.dal.dataobject.userpost.SystemUserPostDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户和岗位关联 Mapper
 * <p>
 * 自定义 SQL 见 resources/mapper/userpost/SystemUserPostMapper.xml
 */
public interface SystemUserPostMapper extends BaseMapper<SystemUserPostDO> {

    /**
     * 删除某用户的全部岗位关联
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户岗位关联
     */
    int insertBatch(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * 查询某用户已关联的岗位ID集合
     */
    List<Long> selectPostIdsByUserId(@Param("userId") Long userId);

    /**
     * 统计某岗位被"未删除用户"引用的关联数（JOIN sys_user 过滤 del_flag='0'），
     * 供删岗前置校验：逻辑删除的用户不应永久占用岗位
     */
    long countActiveByPostId(@Param("postId") Long postId);
}
