package cn.lycoder.boot.module.system.service.auth;

/**
 * 登录失败计数 / 账号锁定 Service
 * <p>
 * 以用户名为维度在 Redis 维护连续失败次数，达到阈值即视为锁定，窗口到期自动解锁。
 */
public interface LoginAttemptService {

    /**
     * 账号当前是否处于锁定状态
     */
    boolean isLocked(String username);

    /**
     * 记录一次登录失败
     *
     * @return 记录后已累计的失败次数（用于提示剩余尝试次数）
     */
    int recordFailure(String username);

    /**
     * 锁定剩余时间（分钟，向上取整）；未锁定返回 0
     */
    long getLockRemainMinutes(String username);

    /**
     * 清除失败计数（登录成功或管理员解锁时调用）
     */
    void reset(String username);
}
