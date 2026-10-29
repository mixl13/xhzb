package com.xhzb.nursing.controller;

import com.xhzb.nursing.memory.ChatHistoryService;
import com.xhzb.nursing.domain.vo.MessageVO;
import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ai/history")
public class ChatHistoryController extends BaseController {
    @Autowired
    private ChatHistoryService chatHistoryService;

    /**
     * 获取当前登录用户在 Redis 中所有会话ID
     * 前端：GET /ai/history/list
     */
    @GetMapping
    public AjaxResult getChatIdList(){
        List<String> chatIdList = chatHistoryService.findChatIdList(SecurityUtils.getUserId().toString());
        return AjaxResult.success(chatIdList);
    }

    /**
     * 获取聊天记录详情
     * 前端：GET /ai/history/{chatId}
     */
    @GetMapping("/{chatId}")
    public AjaxResult findChatHistoryDetails(@PathVariable String chatId){
        List<MessageVO> messageVOList = chatHistoryService.findChatHistoryDetails(chatId);
        return AjaxResult.success(messageVOList);
    }

    /**
     * 删除会话及聊天信息
     * @param chatId
     * @return
     */
    @DeleteMapping("/{chatId}")
    public AjaxResult deleteChatHistory(@PathVariable String chatId) {
        chatHistoryService.deleteChatHistory(chatId, SecurityUtils.getUserId().toString());
        return success();
    }
}
