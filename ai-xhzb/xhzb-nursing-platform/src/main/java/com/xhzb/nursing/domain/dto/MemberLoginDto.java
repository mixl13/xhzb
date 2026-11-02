package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 家属小程序登录参数
 *
 * @author mixl
 * @date 2026-11-01
 */
@Data
@Schema(description = "家属小程序登录参数")
public class MemberLoginDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 临时登录凭证code（wx.login 获取，用于换取openid） */
    @NotBlank(message = "临时登录凭证code不能为空")
    @Schema(title = "临时登录凭证code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /** 微信用户昵称 */
    @Schema(title = "微信用户昵称")
    private String nickName;

    /** 获取手机号的临时code（手机号快速验证组件获取） */
    @NotBlank(message = "获取手机号的临时code不能为空")
    @Schema(title = "获取手机号的临时code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneCode;
}
