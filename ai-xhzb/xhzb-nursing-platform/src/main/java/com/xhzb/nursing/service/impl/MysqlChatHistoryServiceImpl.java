package com.xhzb.nursing.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xhzb.common.exception.ServiceException;
import com.xhzb.common.utils.StringUtils;
import com.xhzb.nursing.domain.entity.AiChatConversation;
import com.xhzb.nursing.domain.vo.AiChatConversationVo;
import com.xhzb.nursing.domain.vo.MessageVO;
import com.xhzb.nursing.mapper.AiChatConversationMapper;
import com.xhzb.nursing.mapper.AiChatMessageMapper;
import com.xhzb.nursing.memory.MysqlChatMemory;
import com.xhzb.nursing.service.IMysqlChatHistoryService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MySQL 版 AI 会话服务实现（会话记忆 + 会话隔离）
 *
 * @author mixl
 * @date 2026-11-01
 */
@Service
public class MysqlChatHistoryServiceImpl implements IMysqlChatHistoryService
{
    /** 会话默认标题 */
    private static final String DEFAULT_TITLE = "新会话";

    /** 会话状态：0-正常 */
    private static final String STATUS_NORMAL = "0";

    /** 会话状态：1-已删除 */
    private static final String STATUS_DELETED = "1";

    /** 会话标题最大长度 */
    private static final int TITLE_MAX_LENGTH = 20;

    @Autowired
    private AiChatConversationMapper aiChatConversationMapper;

    @Autowired
    private AiChatMessageMapper aiChatMessageMapper;

    /**
     * MySQL 版会话记忆实例（非 Spring Bean，此处手动构造，避免与 RedisChatMemory 产生类型注入歧义）
     */
    private final MysqlChatMemory mysqlChatMemory;

    @Autowired
    public MysqlChatHistoryServiceImpl(AiChatMessageMapper aiChatMessageMapper,
                                       AiChatConversationMapper aiChatConversationMapper)
    {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.aiChatConversationMapper = aiChatConversationMapper;
        this.mysqlChatMemory = new MysqlChatMemory(aiChatMessageMapper, aiChatConversationMapper);
    }

    /**
     * 创建会话并绑定到指定用户
     *
     * @param userId 用户ID
     * @return 新会话ID
     */
    @Override
    public String createConversation(Long userId)
    {
        return doCreateConversation(userId, IdUtil.fastSimpleUUID());
    }

    @Override
    public String resolveConversationId(Long userId, String conversationId)
    {
        // 空值、"null"字符串、纯数字（前端新增对话时用 Date.now() 生成的临时ID）统一视为新会话：
        // 临时ID若被当作真实会话持久化，前端再次加载时会判定为临时会话而不拉取历史，导致内容显示不出来
        if (StrUtil.isBlank(conversationId) || "null".equals(conversationId.trim())
                || StrUtil.isNumeric(conversationId.trim()))
        {
            return doCreateConversation(userId, IdUtil.fastSimpleUUID());
        }

        AiChatConversation conversation = aiChatConversationMapper.selectByConversationId(conversationId);
        if (StringUtils.isNull(conversation) || STATUS_DELETED.equals(conversation.getStatus()))
        {
            // 会话ID在库中不存在：按该ID登记到当前用户名下，兼容旧会话ID，避免直接抛错中断对话
            return doCreateConversation(userId, conversationId);
        }
        // 会话隔离：已有会话不属于当前登录用户时拒绝访问
        if (!userId.equals(conversation.getUserId()))
        {
            throw new ServiceException("无权访问该会话");
        }
        return conversationId;
    }

    /**
     * 创建会话并绑定到指定用户
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 会话ID
     */
    private String doCreateConversation(Long userId, String conversationId)
    {
        AiChatConversation conversation = new AiChatConversation();
        conversation.setConversationId(conversationId);
        conversation.setUserId(userId);
        conversation.setTitle(DEFAULT_TITLE);
        conversation.setMessageCount(0);
        conversation.setStatus(STATUS_NORMAL);
        aiChatConversationMapper.insert(conversation);

        return conversationId;
    }

    /**
     * 校验会话归属
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 会话实体
     */
    @Override
    public AiChatConversation checkConversationOwner(Long userId, String conversationId)
    {
        if (StrUtil.isBlank(conversationId))
        {
            throw new ServiceException("会话ID不能为空");
        }
        AiChatConversation conversation = aiChatConversationMapper.selectByConversationId(conversationId);
        if (StringUtils.isNull(conversation) || STATUS_DELETED.equals(conversation.getStatus()))
        {
            throw new ServiceException("会话不存在或已删除");
        }
        // 会话隔离核心校验：会话不属于当前登录用户时直接拒绝，避免越权读取他人聊天记录
        if (!userId.equals(conversation.getUserId()))
        {
            throw new ServiceException("无权访问该会话");
        }
        return conversation;
    }

    /**
     * 查询指定用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话VO集合
     */
    @Override
    public List<AiChatConversationVo> findConversationList(Long userId)
    {
        return aiChatConversationMapper.selectConversationVoByUserId(userId);
    }

    /**
     * 查询指定用户的会话ID集合
     *
     * @param userId 用户ID
     * @return 会话ID集合
     */
    @Override
    public List<String> findConversationIdList(Long userId)
    {
        return aiChatConversationMapper.selectConversationIdByUserId(userId);
    }

    /**
     * 查询指定会话的消息详情
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 消息VO集合
     */
    @Override
    public List<MessageVO> findMessageList(Long userId, String conversationId)
    {
        checkConversationOwner(userId, conversationId);

        List<Message> messageList = mysqlChatMemory.get(conversationId);
        if (CollUtil.isEmpty(messageList))
        {
            return List.of();
        }
        // 复用既有 MessageVO 转换逻辑，与 Redis 版返回结构保持一致
        return messageList.stream().map(MessageVO::new).toList();
    }

    /**
     * 更新会话标题（仅默认标题时更新）
     *
     * @param conversationId 会话ID
     * @param prompt 用户提问内容
     */
    @Override
    public void updateTitleIfBlank(String conversationId, String prompt)
    {
        if (StrUtil.isBlank(prompt))
        {
            return;
        }
        String title = prompt.length() > TITLE_MAX_LENGTH
                ? prompt.substring(0, TITLE_MAX_LENGTH) + "..."
                : prompt;
        aiChatConversationMapper.updateTitleIfBlank(conversationId, title);
    }

    /**
     * 删除会话及其全部消息
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, String conversationId)
    {
        checkConversationOwner(userId, conversationId);

        // 先清空会话记忆（物理删除消息），再逻辑删除会话，保证残留消息不可再被查询
        mysqlChatMemory.clear(conversationId);
        aiChatConversationMapper.updateStatusByConversationId(conversationId, userId, STATUS_DELETED);
    }
}
