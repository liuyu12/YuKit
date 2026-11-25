package cn.lycoder.boot.module.system.service.auth;

import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.lycoder.boot.module.system.controller.admin.auth.vo.AuthUpdatePasswordReqVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 认证 Service 接口
 */
public interface AuthService {

    /**
     * 账号密码登录
     *
     * @param reqVO   登录请求
     * @param request 当前请求（用于记录登录 IP）
     * @return 令牌信息
     */
    AuthLoginRespVO login(AuthLoginReqVO reqVO, HttpServletRequest request);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 当前登录用户自助修改密码：校验旧密码，成功后踢出该账号全部会话（含当前设备），需重新登录
     *
     * @param reqVO 旧/新密码
     */
    void updatePassword(AuthUpdatePasswordReqVO reqVO);

    /**
     * 当前登录用户上传并更新头像，返回可直接用于 img src 的相对访问路径
     *
     * @param file 头像图片文件
     * @return 头像访问 URL
     */
    String updateAvatar(MultipartFile file);
}
