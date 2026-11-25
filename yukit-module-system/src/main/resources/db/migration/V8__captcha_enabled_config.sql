-- ----------------------------------------------------------------------------
-- 登录验证码开关：sys.account.captchaEnabled 参数（与 RuoYi 键名对齐）
-- 运行期可在「参数管理」直接改值，配合 config 缓存写即清，即时生效：
--   true  = 登录必须通过图形验证码校验
--   false = 跳过验证码校验（如内网临时关闭）
-- 注意：yml 的 yukit.captcha.enabled 是部署级总闸（false 时本参数无法反开），
-- 实际生效 = 总闸 AND 本参数。当前环境总闸为 false，改本参数不影响现网行为。
-- ----------------------------------------------------------------------------
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`) VALUES
(4, '登录-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', NOW(), '登录图形验证码校验开关（true 开启 / false 关闭），与部署级总闸 yukit.captcha.enabled 取与');
