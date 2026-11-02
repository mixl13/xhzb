package com.xhzb.nursing.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xhzb.common.exception.ServiceException;
import com.xhzb.framework.web.service.TokenService;
import com.xhzb.nursing.domain.dto.MemberLoginDto;
import com.xhzb.nursing.domain.entity.FamilyMember;
import com.xhzb.nursing.domain.vo.MemberLoginVo;
import com.xhzb.nursing.service.IFamilyMemberService;
import com.xhzb.nursing.service.IMemberUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家属小程序登录Service实现
 *
 * @author mixl
 * @date 2026-11-01
 */
@Service
public class MemberUserServiceImpl implements IMemberUserService
{
    /** 微信官方 code2Session 接口（临时登录凭证换取 openid、session_key） */
    private static final String CODE_2_SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /** 微信官方 获取接口调用凭据 接口（getPhoneNumber 需要 access_token） */
    private static final String ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

    /** 微信官方 getPhoneNumber 接口（手机号快速验证凭证换取真实手机号） */
    private static final String GET_PHONE_NUMBER_URL =
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";

    /** 随机昵称前缀集合 */
    private static final List<String> NICK_NAME_PREFIX_LIST = Arrays.asList(
            "大吉大利", "今晚吃鸡", "福星高照", "好事发生", "匆匆忙忙", "连滚带爬", "从从容容", "游刃有余");

    /** 小程序appid */
    @Value("${wx.appid}")
    private String appid;

    /** 小程序密钥 */
    @Value("${wx.secret}")
    private String secret;

    @Autowired
    private IFamilyMemberService familyMemberService;

    @Autowired
    private TokenService tokenService;

    /**
     * 家属小程序登录
     *
     * @param loginDto 登录参数
     * @return 令牌与昵称
     */
    @Override
    public MemberLoginVo login(MemberLoginDto loginDto)
    {
        // 1、code 结合 appid、secret 调用 code2Session，换取 openid
        String openId = getOpenId(loginDto.getCode());

        // 2、根据 openid 查询家属信息（openid 是家属在小程序侧的唯一身份标识）
        FamilyMember familyMember = familyMemberService.lambdaQuery()
                .eq(FamilyMember::getOpenId, openId)
                .one();

        // 3、调用 getPhoneNumber 换取真实手机号
        String phone = getPhoneNumber(loginDto.getPhoneCode());

        String nickName;
        // 说明：入参中的 nickName 为前端传入的微信昵称，为保证家属昵称风格统一（随机四字 + 手机号后四位），
        //      此处统一由后端生成；若后续需要使用前端传入的昵称，改为优先取 loginDto.getNickName() 即可
        if (familyMember == null)
        {
            // 4、家属不存在：直接新增，昵称按「随机四字前缀 + 手机号后四位」规则生成
            nickName = generateNickName(phone);
            FamilyMember insertMember = new FamilyMember();
            insertMember.setOpenId(openId);
            insertMember.setPhone(phone);
            insertMember.setName(nickName);
            familyMemberService.save(insertMember);
            familyMember = insertMember;
        }
        else
        {
            // 5、家属已存在：昵称保持历史值不变，仅当手机号变化时更新
            nickName = familyMember.getName();
            if (StrUtil.isBlank(nickName))
            {
                // 历史数据可能没有昵称，此处补齐，避免令牌中的昵称为空
                nickName = generateNickName(phone);
                familyMember.setName(nickName);
            }
            if (!phone.equals(familyMember.getPhone()))
            {
                FamilyMember updateMember = new FamilyMember();
                updateMember.setId(familyMember.getId());
                updateMember.setPhone(phone);
                updateMember.setName(nickName);
                familyMemberService.updateById(updateMember);
                familyMember.setPhone(phone);
            }
        }

        // 6、基于家属ID与昵称生成令牌
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", familyMember.getId());
        paramMap.put("nickName", nickName);
        String token = tokenService.createToken(paramMap);

        MemberLoginVo loginVo = new MemberLoginVo();
        loginVo.setToken(token);
        loginVo.setNickName(nickName);
        return loginVo;
    }

    /**
     * 调用微信 code2Session 接口获取 openid
     *
     * @param code 临时登录凭证
     * @return openid
     */
    private String getOpenId(String code)
    {
        String url = String.format(CODE_2_SESSION_URL, appid, secret, code);
        String result = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(result);

        // 微信接口出错时不返回 openid，只返回 errcode/errmsg，需要提前中断并提示
        Integer errcode = jsonObject.getInt("errcode");
        if (errcode != null && errcode != 0)
        {
            throw new ServiceException("微信登录失败：" + jsonObject.getStr("errmsg"));
        }
        String openId = jsonObject.getStr("openid");
        if (StrUtil.isBlank(openId))
        {
            throw new ServiceException("微信登录失败：未获取到openid");
        }
        return openId;
    }

    /**
     * 调用微信 getPhoneNumber 接口获取真实手机号
     * 该接口需要 access_token，先换取凭据再换取手机号
     *
     * @param phoneCode 获取手机号的临时凭证
     * @return 手机号（不含区号）
     */
    private String getPhoneNumber(String phoneCode)
    {
        String accessTokenUrl = String.format(ACCESS_TOKEN_URL, appid, secret);
        JSONObject accessTokenObj = JSONUtil.parseObj(HttpUtil.get(accessTokenUrl));
        String accessToken = accessTokenObj.getStr("access_token");
        if (StrUtil.isBlank(accessToken))
        {
            throw new ServiceException("获取微信接口调用凭据失败：" + accessTokenObj.getStr("errmsg"));
        }

        String url = String.format(GET_PHONE_NUMBER_URL, accessToken);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("code", phoneCode);
        String result = HttpUtil.createPost(url)
                .body(JSONUtil.toJsonStr(paramMap))
                .contentType(ContentType.JSON.getValue())
                .execute()
                .body();

        JSONObject jsonObject = JSONUtil.parseObj(result);
        Integer errcode = jsonObject.getInt("errcode");
        if (errcode == null || errcode != 0)
        {
            throw new ServiceException("获取手机号失败：" + jsonObject.getStr("errmsg"));
        }

        JSONObject phoneInfo = jsonObject.getJSONObject("phone_info");
        if (phoneInfo == null)
        {
            throw new ServiceException("获取手机号失败：未返回手机号信息");
        }
        // purePhoneNumber 为不带区号的手机号，取不到时再降级取带区号的 phoneNumber
        String phone = phoneInfo.getStr("purePhoneNumber");
        if (StrUtil.isBlank(phone))
        {
            phone = phoneInfo.getStr("phoneNumber");
        }
        if (StrUtil.isBlank(phone))
        {
            throw new ServiceException("获取手机号失败：未返回手机号");
        }
        return phone;
    }

    /**
     * 生成随机昵称：随机四字前缀 + 手机号后四位
     *
     * @param phone 手机号
     * @return 昵称
     */
    private String generateNickName(String phone)
    {
        // 打乱前缀集合后取第一个，保证同一批登录用户昵称不会都相同
        List<String> nickNameList = new ArrayList<>(NICK_NAME_PREFIX_LIST);
        Collections.shuffle(nickNameList);
        String prefix = nickNameList.get(0);
        String postfix = phone.length() > 4 ? phone.substring(phone.length() - 4) : phone;
        return prefix + postfix;
    }
}
