package com.xhzb.nursing.controller.member;

import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.nursing.domain.dto.MemberLoginDto;
import com.xhzb.nursing.domain.vo.MemberLoginVo;
import com.xhzb.nursing.service.IMemberUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 家属小程序用户Controller
 *
 * 说明：/member/** 在 SecurityConfig 中已允许匿名访问，登录接口无需携带令牌
 *
 * @author mixl
 * @date 2026-11-01
 */
@Tag(name = "家属小程序用户", description = "家属小程序登录")
@RestController
@RequestMapping("/member/user")
public class MemberUserController extends BaseController
{
    @Autowired
    private IMemberUserService memberUserService;

    /**
     * 家属小程序登录
     */
    @Operation(summary = "家属小程序登录")
    @PostMapping("/login")
    public AjaxResult login(@Validated @RequestBody MemberLoginDto loginDto)
    {
        MemberLoginVo loginVo = memberUserService.login(loginDto);
        return AjaxResult.success(loginVo);
    }
}
