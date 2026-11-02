package com.xhzb.nursing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 家属小程序登录结果
 *
 * @author mixl
 * @date 2026-11-01
 */
@Data
@Schema(description = "家属小程序登录结果")
public class MemberLoginVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 令牌 */
    @Schema(title = "令牌")
    private String token;

    /** 家属昵称 */
    @Schema(title = "家属昵称")
    private String nickName;
}
