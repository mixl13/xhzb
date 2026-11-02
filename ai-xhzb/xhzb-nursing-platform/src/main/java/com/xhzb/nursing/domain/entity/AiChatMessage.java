package com.xhzb.nursing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xhzb.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI会话消息对象 ai_chat_message
 *
 * @author mixl
 * @date 2026-11-01
 */
@Data
@TableName("ai_chat_message")
@Schema(description = "AI会话消息对象")
public class AiChatMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @Schema(title = "主键ID")
    private Long id;

    /** 所属会话ID */
    @Schema(title = "所属会话ID")
    private String conversationId;

    /** 消息序号（同一会话内递增，保证上下文按时间正序） */
    @Schema(title = "消息序号")
    private Integer seq;

    /** 消息类型（user/assistant/system/tool） */
    @Schema(title = "消息类型")
    private String messageType;

    /** 消息内容 */
    @Schema(title = "消息内容")
    private String content;

    /** 消息元数据（JSON字符串） */
    @Schema(title = "消息元数据")
    private String metadata;
}
