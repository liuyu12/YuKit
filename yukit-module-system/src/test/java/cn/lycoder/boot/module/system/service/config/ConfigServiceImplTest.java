package cn.lycoder.boot.module.system.service.config;

import cn.lycoder.boot.framework.common.exception.ServiceException;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigCreateReqVO;
import cn.lycoder.boot.module.system.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.lycoder.boot.module.system.convert.config.ConfigConvertImpl;
import cn.lycoder.boot.module.system.dal.dataobject.config.SystemConfigDO;
import cn.lycoder.boot.module.system.dal.mapper.config.SystemConfigMapper;
import cn.lycoder.boot.module.system.enums.SystemErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ConfigServiceImpl 单测：纯 Mockito，不起 Spring 上下文
 * <p>
 * Convert 使用 MapStruct 编译期生成的真实实现；selfProvider 回指被测实例，
 * 覆盖类型化读取的取值/回退分支（无缓存代理，直落 mapper）。
 */
@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private SystemConfigMapper configMapper;
    @Mock
    private ObjectProvider<ConfigService> selfProvider;

    private ConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(configMapper, new ConfigConvertImpl(), selfProvider);
    }

    @Test
    void createShouldDefaultCustomTypeAndInsert() {
        when(configMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(configMapper.insert(any(SystemConfigDO.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, SystemConfigDO.class).setConfigId(100L);
            return 1;
        });
        ConfigCreateReqVO reqVO = new ConfigCreateReqVO();
        reqVO.setConfigName("测试参数");
        reqVO.setConfigKey("sys.test.key");
        reqVO.setConfigValue("v1");

        Long configId = configService.createConfig(reqVO);

        assertThat(configId).isEqualTo(100L);
        ArgumentCaptor<SystemConfigDO> captor = ArgumentCaptor.forClass(SystemConfigDO.class);
        verify(configMapper).insert(captor.capture());
        assertThat(captor.getValue().getConfigType()).isEqualTo(SystemConfigDO.TYPE_CUSTOM);
        assertThat(captor.getValue().getConfigKey()).isEqualTo("sys.test.key");
    }

    @Test
    void createShouldRejectDuplicateKey() {
        when(configMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        ConfigCreateReqVO reqVO = new ConfigCreateReqVO();
        reqVO.setConfigName("重复键");
        reqVO.setConfigKey("sys.user.initPassword");
        reqVO.setConfigValue("x");

        assertThatThrownBy(() -> configService.createConfig(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.CONFIG_KEY_EXISTS.getCode());
        verify(configMapper, never()).insert(any(SystemConfigDO.class));
    }

    @Test
    void updateShouldRejectNotExists() {
        when(configMapper.selectById(9L)).thenReturn(null);
        ConfigUpdateReqVO reqVO = new ConfigUpdateReqVO();
        reqVO.setConfigId(9L);
        reqVO.setConfigName("n");
        reqVO.setConfigValue("v");

        assertThatThrownBy(() -> configService.updateConfig(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.CONFIG_NOT_EXISTS.getCode());
    }

    @Test
    void updateShouldPersistWithoutKeyAndType() {
        when(configMapper.selectById(1L)).thenReturn(buildDO(1L, SystemConfigDO.TYPE_CUSTOM));
        ConfigUpdateReqVO reqVO = new ConfigUpdateReqVO();
        reqVO.setConfigId(1L);
        reqVO.setConfigName("新名称");
        reqVO.setConfigValue("new-value");

        configService.updateConfig(reqVO);

        ArgumentCaptor<SystemConfigDO> captor = ArgumentCaptor.forClass(SystemConfigDO.class);
        verify(configMapper).updateById(captor.capture());
        SystemConfigDO updated = captor.getValue();
        assertThat(updated.getConfigId()).isEqualTo(1L);
        assertThat(updated.getConfigName()).isEqualTo("新名称");
        // 键名与内置标记不可改：转换产物中为 null，updateById 跳过对应列
        assertThat(updated.getConfigKey()).isNull();
        assertThat(updated.getConfigType()).isNull();
    }

    @Test
    void deleteShouldForbidBuiltIn() {
        when(configMapper.selectById(1L)).thenReturn(buildDO(1L, SystemConfigDO.TYPE_BUILT_IN));

        assertThatThrownBy(() -> configService.deleteConfig(1L))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.CONFIG_BUILT_IN_FORBIDDEN.getCode());
        verify(configMapper, never()).deleteById(1L);
    }

    @Test
    void deleteCustomShouldRemove() {
        when(configMapper.selectById(2L)).thenReturn(buildDO(2L, SystemConfigDO.TYPE_CUSTOM));

        configService.deleteConfig(2L);

        verify(configMapper).deleteById(2L);
    }

    @Test
    void deleteShouldRejectNotExists() {
        when(configMapper.selectById(3L)).thenReturn(null);

        assertThatThrownBy(() -> configService.deleteConfig(3L))
                .isInstanceOf(ServiceException.class)
                .extracting(e -> ((ServiceException) e).getCode())
                .isEqualTo(SystemErrorCodeConstants.CONFIG_NOT_EXISTS.getCode());
    }

    @Test
    void getConfigValueShouldReturnNullOrStoredValue() {
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(buildDO(1L, SystemConfigDO.TYPE_CUSTOM));
        assertThat(configService.getConfigValue("sys.test.key")).isEqualTo("v");

        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        assertThat(configService.getConfigValue("sys.missing.key")).isNull();
    }

    @Test
    void getConfigValueAsIntShouldParseOrFallback() {
        when(selfProvider.getObject()).thenReturn(configService);
        SystemConfigDO numeric = buildDO(1L, SystemConfigDO.TYPE_CUSTOM);
        numeric.setConfigValue("42");
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(numeric);
        assertThat(configService.getConfigValueAsInt("sys.test.key", 7)).isEqualTo(42);

        SystemConfigDO notNumeric = buildDO(1L, SystemConfigDO.TYPE_CUSTOM);
        notNumeric.setConfigValue("abc");
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(notNumeric);
        assertThat(configService.getConfigValueAsInt("sys.test.key", 7)).isEqualTo(7);

        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        assertThat(configService.getConfigValueAsInt("sys.missing.key", 7)).isEqualTo(7);
    }

    @Test
    void getConfigValueAsBooleanShouldBeLenientOnTrueFalseOnly() {
        when(selfProvider.getObject()).thenReturn(configService);
        SystemConfigDO yes = buildDO(1L, SystemConfigDO.TYPE_CUSTOM);
        yes.setConfigValue("TRUE");
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(yes);
        assertThat(configService.getConfigValueAsBoolean("sys.test.key", false)).isTrue();

        // 非 true/false 字面量（如 on/off）不回退为字符串比较直觉值，走 defaultValue
        SystemConfigDO ambiguous = buildDO(1L, SystemConfigDO.TYPE_CUSTOM);
        ambiguous.setConfigValue("on");
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(ambiguous);
        assertThat(configService.getConfigValueAsBoolean("sys.test.key", true)).isTrue();
        assertThat(configService.getConfigValueAsBoolean("sys.test.key", false)).isFalse();
    }

    @Test
    void refreshCacheShouldNotTouchDatabase() {
        configService.refreshCache();

        verify(configMapper, never()).selectList(any(Wrapper.class));
    }

    private SystemConfigDO buildDO(Long configId, String configType) {
        SystemConfigDO config = new SystemConfigDO();
        config.setConfigId(configId);
        config.setConfigName("测试参数");
        config.setConfigKey("sys.test.key");
        config.setConfigValue("v");
        config.setConfigType(configType);
        return config;
    }
}
