package com.xhzb.nursing.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhzb.nursing.domain.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI会话消息Mapper接口
 *
 * @author mixl
 * @date 2026-11-01
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage>
{
    /**
     * 查询指定会话的全部消息
     *
     * @param conversationId 会话ID
     * @return 消息集合（按消息序号正序，保证上下文顺序正确）
     */
    public List<AiChatMessage> selectByConversationId(String conversationId);

    /**
     * 查询指定会话当前最大消息序号
     *
     * @param conversationId 会话ID
     * @return 最大序号，无消息时返回0
     */
    public Integer selectMaxSeq(String conversationId);

    /**
     * 批量新增会话消息
     *
     * @param messageList 消息集合
     * @return 新增条数
     */
    public int batchInsert(@Param("messageList") List<AiChatMessage> messageList);

    /**
     * 删除指定会话的全部消息
     *
     * @param conversationId 会话ID
     * @return 删除条数
     */
    public int deleteByConversationId(String conversationId);
}
