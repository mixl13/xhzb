package com.xhzb.nursing.service;

import com.xhzb.nursing.domain.entity.AiChatConversation;
import com.xhzb.nursing.domain.vo.AiChatConversationVo;
import com.xhzb.nursing.domain.vo.MessageVO;

import java.util.List;

/**
 * MySQL 版 AI 会话服务（会话记忆 + 会话隔离）
 *
 * 与原有 ChatHistoryService（Redis 版）职责一致，但存储改为 MySQL：
 *  - 会话记忆：ai_chat_message 按会话ID存取消息
 *  - 会话隔离：ai_chat_conversation 以 user_id 为维度隔离，所有读写都校验归属
 *
 * @author mixl
 * @date 2026-11-01
 */
public interface IMysqlChatHistoryService
{
    /**
     * 创建会话并绑定到指定用户（会话隔离的入口）
     *
     * @param userId 用户ID
     * @return 新会话ID
     */
    String createConversation(Long userId);

    /**
     * 校验会话归属，防止越权访问他人会话
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 会话实体
     */
    AiChatConversation checkConversationOwner(Long userId, String conversationId);

    /**
     * 解析会话ID：新会话（空值 / 前端临时数字ID）自动创建；已有会话校验归属；
     * 会话ID存在但未登记时按该ID登记到当前用户名下（兼容旧会话ID）
     *
     * @param userId 用户ID
     * @param conversationId 前端传入的会话ID
     * @return 真实会话ID
     */
    String resolveConversationId(Long userId, String conversationId);

    /**
     * 查询指定用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话VO集合
     */
    List<AiChatConversationVo> findConversationList(Long userId);

    /**
     * 查询指定用户的会话ID集合
     *
     * @param userId 用户ID
     * @return 会话ID集合
     */
    List<String> findConversationIdList(Long userId);

    /**
     * 查询指定会话的消息详情
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 消息VO集合
     */
    List<MessageVO> findMessageList(Long userId, String conversationId);

    /**
     * 更新会话标题（仅默认标题时更新，取首条用户提问）
     *
     * @param conversationId 会话ID
     * @param prompt 用户提问内容
     */
    void updateTitleIfBlank(String conversationId, String prompt);

    /**
     * 删除会话及其全部消息
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, String conversationId);
}
