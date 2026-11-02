package com.xhzb;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xhzb.framework.web.service.TokenService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class weChatTest {
    /**
     * 测试调用微信小程序登录接口
     */
    @Test
    public void testInvokeWechat(){
        String url = "https://api.weixin.qq.com/sns/jscode2session";

        HttpResponse response = HttpUtil.createGet(url)
                .form(Map.of(
                        "appid","wx8b95197250e53ae3",
                        "secret","20f233c427e4ba0cfb87798af01997ff",
                        "js_code","0d12I9000h5KVW1adc3009mKRh42ABCD",
                        "grant_type","authorization_code"
                ))
                .execute();

        int status = response.getStatus();
        System.out.println(status);

        if(response.isOk()) {
            String result = response.body();

            JSONObject object = JSONUtil.parseObj(result);
            //获取用户唯一身份标识
            String openid = object.getStr("openid");
            System.out.println(openid);
        }
    }

    @Autowired
    private TokenService tokenService;



    /**
     * 测试生成令牌
     */
    @Test
    public void testCreateToken(){
        List<String> NICK_NAME_LIST = Arrays.asList("好事发生", "大吉大利", "今晚吃鸡", "福星高照", "兴高采烈", "妙笔生花");

        //打乱集合中元素顺序
        Collections.shuffle(NICK_NAME_LIST);
        String prefix = NICK_NAME_LIST.get(0); //随机昵称前缀
        String postfix = RandomStringUtils.randomNumeric(4); //随机4位数字
        String nickName = prefix +postfix; //随机昵称
        System.out.println(nickName);


        String token = tokenService.createToken(Map.of("userId", 1, "nickName", nickName));
        System.out.println(token);
    }

}
