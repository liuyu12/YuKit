package cn.lycoder.boot.module.system.controller.admin.auth.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthCaptchaRespVO {

    /**
     * 验证码唯一标识，登录时随验证码值一并回传，服务端据此从 Redis 取答案比对
     */
    @Schema(description = "验证码唯一标识，登录时随验证码值一并回传", example = "6ac11e7c0d27494882afe548895cc945")
    private String uuid;

    /**
     * 验证码图片，data URI 形式（data:image/png;base64,...），前端可直接用作 img 的 src
     */
    @Schema(description = "验证码图片，data URI 形式（data:image/png;base64,...），前端可直接用作 img 的 src")
    private String imageBase64;

    /**
     * 验证码答案，仅 dev/default 环境回填，方便接口测试直接读码；
     * 生产环境为 null，配合 NON_NULL 不会出现在响应中
     */
    @Schema(description = "验证码答案，仅 dev/default 环境回填，生产为 null 不输出", example = "Hk6c")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;
}
