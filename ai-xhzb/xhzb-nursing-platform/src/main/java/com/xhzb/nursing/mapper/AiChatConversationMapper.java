package com.xhzb.nursing.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhzb.nursing.domain.entity.AiChatConversation;
import com.xhzb.nursing.domain.vo.AiChatConversationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI会话Mapper接口
 *
 * @author mixl
 * @date 2026-11-01
 */
@Mapper
public interface AiChatConversationMapper extends BaseMapper<AiChatConversation>
{
    /**
     * 根据会话ID查询会话
     *
     * @param conversationId 会话ID
     * @return AI会话对象
     */
    public AiChatConversation selectByConversationId(String conversationId);

    /**
     * 查询指定用户的会话列表（会话隔离：仅返回当前用户名下、未删除的会话）
     *
     * @param userId 用户ID
     * @return 会话VO集合（按更新时间倒序）
     */
    public List<AiChatConversationVo> selectConversationVoByUserId(Long userId);

    /**
     * 查询指定用户的会话ID集合
     *
     * @param userId 用户ID
     * @return 会话ID集合（按会话ID倒序，与原Redis实现排序保持一致）
     */
    public List<String> selectConversationIdByUserId(Long userId);

    /**
     * 累加会话消息条数
     *
     * @param conversationId 会话ID
     * @param count 本次新增的消息条数
     * @return 更新条数
     */
    public int incrementMessageCount(@Param("conversationId") String conversationId, @Param("count") Integer count);

    /**
     * 更新会话标题（仅标题为空或默认值时更新，用于取首条用户提问作为标题）
     *
     * @param conversationId 会话ID
     * @param title 会话标题
     * @return 更新条数
     */
    public int updateTitleIfBlank(@Param("conversationId") String conversationId, @Param("title") String title);

    /**
     * 逻辑删除会话（更新状态为已删除）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID（会话隔离：只删除本人会话）
     * @return 更新条数
     */
    public int updateStatusByConversationId(@Param("conversationId") String conversationId,
                                            @Param("userId") Long userId,
                                            @Param("status") String status);
}
