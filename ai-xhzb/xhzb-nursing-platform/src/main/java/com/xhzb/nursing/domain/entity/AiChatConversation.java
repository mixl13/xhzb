package com.xhzb.nursing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xhzb.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI会话对象 ai_chat_conversation
 *
 * @author mixl
 * @date 2026-11-01
 */
@Data
@TableName("ai_chat_conversation")
@Schema(description = "AI会话对象")
public class AiChatConversation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @Schema(title = "主键ID")
    private Long id;

    /** 会话ID（业务唯一标识） */
    @Schema(title = "会话ID")
    private String conversationId;

    /** 所属用户ID（会话隔离维度） */
    @Schema(title = "所属用户ID")
    private Long userId;

    /** 会话标题（默认取首条用户提问） */
    @Schema(title = "会话标题")
    private String title;

    /** 会话消息条数 */
    @Schema(title = "会话消息条数")
    private Integer messageCount;

    /** 状态（0正常 1已删除） */
    @Schema(title = "状态（0正常 1已删除）")
    private String status;
}
