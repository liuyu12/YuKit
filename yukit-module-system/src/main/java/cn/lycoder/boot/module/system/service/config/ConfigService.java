package cn.lycoder.boot.module.system.service.config;

import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.lycoder.boot.module.system.dal.dataobject.config.SystemConfigDO;

/**
 * 参数管理 Service 接口
 * <p>
 * 除 CRUD 外，面向业务代码提供按键名读取入口（{@link #getConfigValue(String)}），
 * 读取走 Spring Cache（cacheNames=config），任何写操作全量清空保证一致性。
 */
public interface ConfigService {

    /**
     * 创建参数
     *
     * @param reqVO 创建请求
     * @return 参数主键
     */
    Long createConfig(ConfigCreateReqVO reqVO);

    /**
     * 更新参数（键名不允许修改）
     *
     * @param reqVO 更新请求，configId 由路径参数覆盖
     */
    void updateConfig(ConfigUpdateReqVO reqVO);

    /**
     * 删除参数（内置参数不允许删除）
     *
     * @param configId 参数主键
     */
    void deleteConfig(Long configId);

    /**
     * 按主键查询参数
     *
     * @param configId 参数主键
     * @return 参数 DO，不存在返回 null
     */
    SystemConfigDO getConfig(Long configId);

    /**
     * 分页查询参数
     *
     * @param reqVO 分页条件
     * @return 参数分页
     */
    PageResult<SystemConfigDO> getConfigPage(ConfigPageReqVO reqVO);

    /**
     * 按键名读取参数值（业务侧统一入口，命中缓存）
     *
     * @param configKey 参数键名
     * @return 参数键值，键名不存在返回 null
     */
    String getConfigValue(String configKey);

    /**
     * 按键名读取整型参数值
     *
     * @param configKey    参数键名
     * @param defaultValue 缺省值（键不存在或值非合法整数时返回）
     * @return 参数值
     */
    int getConfigValueAsInt(String configKey, int defaultValue);

    /**
     * 按键名读取布尔参数值（仅 "true"/"false" 视为有效，大小写不敏感）
     *
     * @param configKey    参数键名
     * @param defaultValue 缺省值（键不存在或值非合法布尔时返回）
     * @return 参数值
     */
    boolean getConfigValueAsBoolean(String configKey, boolean defaultValue);

    /**
     * 手动清空参数缓存（管理端「刷新缓存」入口）
     */
    void refreshCache();
}
