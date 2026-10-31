package com.xhzb.nursing.controller;


import cn.hutool.core.util.StrUtil;
import com.xhzb.nursing.service.ChatHistoryService;
import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.SecurityUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChatController {

    //容器中存在两个 ChatClient（通用对话 / 健康评估），必须按 Bean 名称指定，否则按类型注入会歧义
    @Autowired
    @Qualifier("deepSeekChatClient")
    private ChatClient deepSeekChatClient;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(String prompt, String chatId) {

        if(StrUtil.isBlank(chatId) || "null".equals(chatId.trim())) {
            throw new BaseException("请先创建会话");
        }
        // 将会话登记到当前登录用户下，便于「历史记录」拉取；
        // Redis Set 天然去重，重复登记不会产生副作用
        chatHistoryService.savaHistory(SecurityUtils.getUserId().toString(), chatId);

        Flux<String> content = deepSeekChatClient
                .prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                .stream()
                .content();
        return content;
    }
}
