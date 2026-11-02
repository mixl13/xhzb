package com.xhzb.nursing.service;

import com.xhzb.nursing.domain.dto.MemberLoginDto;
import com.xhzb.nursing.domain.vo.MemberLoginVo;

/**
 * 家属小程序登录Service接口
 *
 * @author mixl
 * @date 2026-11-01
 */
public interface IMemberUserService
{
    /**
     * 家属小程序登录
     * 流程：code换取openid -> 查询家属 -> phoneCode换取手机号 -> 新增或更新家属 -> 生成令牌
     *
     * @param loginDto 登录参数
     * @return 令牌与昵称
     */
    MemberLoginVo login(MemberLoginDto loginDto);
}
