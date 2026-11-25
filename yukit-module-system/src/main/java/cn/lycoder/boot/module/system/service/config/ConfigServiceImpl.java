package cn.lycoder.boot.module.system.service.config;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.framework.common.pojo.PageResult;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigPageReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.lycoder.boot.module.system.convert.config.ConfigConvert;
import cn.lycoder.boot.module.system.dal.dataobject.config.SystemConfigDO;
import cn.lycoder.boot.module.system.dal.mapper.config.SystemConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.CONFIG_BUILT_IN_FORBIDDEN;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.CONFIG_KEY_EXISTS;
import static cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants.CONFIG_NOT_EXISTS;

/**
 * 参数管理 Service 实现
 * <p>
 * 缓存：按键名缓存参数值（cacheNames=config，规格见 application.yml）。
 * 参数变更频率极低、业务读取高频，任何写操作全量清空 config 缓存保证一致性；
 * 键不存在返回 null 且不缓存空值（unless），避免缓存穿透固化。
 * 类型化读取（int/boolean）经自身代理调用缓存方法，避免同类内部调用绕过缓存切面。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigMapper configMapper;
    private final ConfigConvert configConvert;
    /**
     * 自身代理：类型化读取经由代理调用 {@link #getConfigValue(String)}，保证 @Cacheable 生效
     */
    private final ObjectProvider<ConfigService> selfProvider;

    @Override
    @CacheEvict(cacheNames = "config", allEntries = true)
    public Long createConfig(ConfigCreateReqVO reqVO) {
        // 键名全局唯一（表上另有 uk 兜底，此处提前校验给出友好提示）
        validateConfigKeyUnique(reqVO.getConfigKey(), null);
        SystemConfigDO config = configConvert.convert(reqVO);
        config.setConfigType(StringUtils.hasText(reqVO.getConfigType())
                ? reqVO.getConfigType() : SystemConfigDO.TYPE_CUSTOM);
        configMapper.insert(config);
        return config.getConfigId();
    }

    @Override
    @CacheEvict(cacheNames = "config", allEntries = true)
    public void updateConfig(ConfigUpdateReqVO reqVO) {
        validateConfigExists(reqVO.getConfigId());
        // convert 产物不含 configKey/configType（null 字段 updateById 自动跳过），键名与内置标记不可改
        configMapper.updateById(configConvert.convert(reqVO));
    }

    @Override
    @CacheEvict(cacheNames = "config", allEntries = true)
    public void deleteConfig(Long configId) {
        SystemConfigDO config = validateConfigExists(configId);
        if (SystemConfigDO.TYPE_BUILT_IN.equals(config.getConfigType())) {
            throw new ServiceException(CONFIG_BUILT_IN_FORBIDDEN);
        }
        configMapper.deleteById(configId);
    }

    @Override
    public SystemConfigDO getConfig(Long configId) {
        return configMapper.selectById(configId);
    }

    @Override
    public PageResult<SystemConfigDO> getConfigPage(ConfigPageReqVO reqVO) {
        LambdaQueryWrapper<SystemConfigDO> wrapper = Wrappers.<SystemConfigDO>lambdaQuery()
                .like(StringUtils.hasText(reqVO.getConfigName()), SystemConfigDO::getConfigName, reqVO.getConfigName())
                .like(StringUtils.hasText(reqVO.getConfigKey()), SystemConfigDO::getConfigKey, reqVO.getConfigKey())
                .eq(StringUtils.hasText(reqVO.getConfigType()), SystemConfigDO::getConfigType, reqVO.getConfigType())
                .orderByAsc(SystemConfigDO::getConfigId);
        Page<SystemConfigDO> page = configMapper.selectPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    @Cacheable(cacheNames = "config", key = "#configKey", unless = "#result == null")
    public String getConfigValue(String configKey) {
        SystemConfigDO config = configMapper.selectOne(Wrappers.<SystemConfigDO>lambdaQuery()
                .eq(SystemConfigDO::getConfigKey, configKey));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public int getConfigValueAsInt(String configKey, int defaultValue) {
        String value = self().getConfigValue(configKey);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("[getConfigValueAsInt] 参数 {} 值 [{}] 非整型，回退缺省值 {}", configKey, value, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public boolean getConfigValueAsBoolean(String configKey, boolean defaultValue) {
        String value = self().getConfigValue(configKey);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return defaultValue;
    }

    @Override
    @CacheEvict(cacheNames = "config", allEntries = true)
    public void refreshCache() {
        log.info("[refreshCache] 参数缓存已全量清空");
    }

    private ConfigService self() {
        return selfProvider.getObject();
    }

    private SystemConfigDO validateConfigExists(Long configId) {
        SystemConfigDO config = configMapper.selectById(configId);
        if (config == null) {
            throw new ServiceException(CONFIG_NOT_EXISTS);
        }
        return config;
    }

    private void validateConfigKeyUnique(String configKey, Long excludeId) {
        Long count = configMapper.selectCount(Wrappers.<SystemConfigDO>lambdaQuery()
                .eq(SystemConfigDO::getConfigKey, configKey)
                .ne(excludeId != null, SystemConfigDO::getConfigId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException(CONFIG_KEY_EXISTS);
        }
    }
}
