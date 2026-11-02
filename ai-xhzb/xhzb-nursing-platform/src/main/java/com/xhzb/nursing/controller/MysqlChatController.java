package com.xhzb.nursing.controller;

import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.exception.ServiceException;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.domain.vo.MessageVO;
import com.xhzb.nursing.service.IMysqlChatHistoryService;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 会话 Controller（MySQL 版会话记忆 + 会话隔离）
 *
 * 对外地址与前端 src/api/nursing/ai.js 完全对齐（/ai/chat、/ai/history），前端无需改动；
 * 原 Redis 版实现已迁移到 /ai/redis/**，逻辑保持不变。
 * 会话隔离：所有会话读写都以登录用户ID为准并校验归属，用户之间互不可见。
 *
 * @author mixl
 * @date 2026-11-01
 */
@Tag(name = "AI会话", description = "基于MySQL的会话记忆与会话隔离")
@RestController
@RequestMapping("/ai")
public class MysqlChatController extends BaseController
{
    // 容器中存在多个 ChatClient（Redis版 / 健康评估 / MySQL版），必须按 Bean 名称指定，否则按类型注入会歧义
    @Autowired
    @Qualifier("mysqlMemoryChatClient")
    private ChatClient mysqlMemoryChatClient;

    @Autowired
    private IMysqlChatHistoryService mysqlChatHistoryService;

    /**
     * AI 对话（会话记忆走 MySQL）
     * 不限制请求方法：前端 sendChatMessage 使用 POST，需与原 /ai/chat 行为保持一致，否则会 405
     * 参数名必须为 chatId：前端以 ?prompt=xx&chatId=xx 传参，命名不一致会导致绑定为空值，
     * 进而每轮对话都创建新会话，历史记忆失效
     */
    @Operation(summary = "AI对话（MySQL会话记忆）")
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(String prompt, String chatId, HttpServletResponse response)
    {
        Long userId = SecurityUtils.getUserId();
        // /ai/chat 在 SecurityConfig 中允许匿名访问，但会话必须归属到具体用户，
        // 未登录时直接拒绝，避免写入 user_id 为空的脏数据
        if (userId == null)
        {
            throw new ServiceException("请先登录");
        }

        // 新对话（空会话ID或前端临时数字ID）由后端创建真实会话，并完成「用户 -> 会话」绑定
        String conversationId = mysqlChatHistoryService.resolveConversationId(userId, chatId);

        // 通过响应头回传真实会话ID：前端新增对话时依赖 X-Chat-Id 把临时ID替换为真实会话ID
        response.setHeader("X-Chat-Id", conversationId);

        // 首条提问作为会话标题，便于历史列表回显
        mysqlChatHistoryService.updateTitleIfBlank(conversationId, prompt);

        return mysqlMemoryChatClient
                .prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * 获取当前登录用户的会话ID列表（会话隔离：仅返回本人会话）
     */
    @Operation(summary = "查询当前用户的会话ID列表")
    @GetMapping("/history")
    public AjaxResult chatIdList()
    {
        List<String> chatIdList = mysqlChatHistoryService.findConversationIdList(SecurityUtils.getUserId());
        return AjaxResult.success(chatIdList);
    }

    /**
     * 获取指定会话的聊天记录详情
     */
    @Operation(summary = "查询会话聊天记录")
    @GetMapping("/history/{chatId}")
    public AjaxResult chatHistoryDetail(@PathVariable String chatId)
    {
        List<MessageVO> list = mysqlChatHistoryService.findMessageList(SecurityUtils.getUserId(), chatId);
        return AjaxResult.success(list);
    }

    /**
     * 删除会话及其全部聊天记录
     */
    @Operation(summary = "删除会话及聊天记录")
    @DeleteMapping("/history/{chatId}")
    public AjaxResult deleteChatHistory(@PathVariable String chatId)
    {
        if (StrUtil.isBlank(chatId))
        {
            return AjaxResult.error("会话ID不能为空");
        }
        mysqlChatHistoryService.deleteConversation(SecurityUtils.getUserId(), chatId);
        return success();
    }
}
