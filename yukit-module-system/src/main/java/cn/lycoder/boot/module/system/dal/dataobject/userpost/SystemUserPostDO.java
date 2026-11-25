package cn.lycoder.boot.module.system.dal.dataobject.userpost;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户和岗位关联 DO，对应表 sys_user_post
 * <p>
 * 联合主键（user_id, post_id），无独立自增主键与审计字段，故不继承 BaseEntity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_post")
public class SystemUserPostDO implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;
}
