package com.xhzb.nursing.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI会话列表VO（MySQL版会话隔离返回结构）
 *
 * @author mixl
 * @date 2026-11-01
 */
@Data
@Schema(description = "AI会话列表VO")
public class AiChatConversationVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @Schema(title = "会话ID")
    private String conversationId;

    /** 会话标题 */
    @Schema(title = "会话标题")
    private String title;

    /** 会话消息条数 */
    @Schema(title = "会话消息条数")
    private Integer messageCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(title = "创建时间")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(title = "更新时间")
    private Date updateTime;
}
