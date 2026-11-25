package cn.lycoder.boot.module.system.dal.dataobject.file;

import cn.lycoder.boot.framework.mybatis.pojo.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件元数据 DO，对应表 sys_file
 * <p>
 * 二进制内容不落库，存于 {@code FileStorage}（由 bizPath 定位）；
 * fileHash 为内容 SHA-256（hex），预留秒传/完整性校验。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SystemFileDO extends BaseEntity {

    /**
     * 文件ID
     */
    @TableId
    private Long fileId;

    /**
     * 存储文件名（uuid.扩展名）
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储相对路径（如 2026/11/25/xxx.png）
     */
    private String bizPath;

    /**
     * 存储类型（local 等，对应 FileStorage.getType()）
     */
    private String storageType;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件内容 SHA-256（hex）
     */
    private String fileHash;

    /**
     * 备注
     */
    private String remark;
}
