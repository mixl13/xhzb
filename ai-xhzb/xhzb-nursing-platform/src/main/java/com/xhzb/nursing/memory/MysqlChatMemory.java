package com.xhzb.nursing.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.nursing.domain.entity.AiChatMessage;
import com.xhzb.nursing.mapper.AiChatConversationMapper;
import com.xhzb.nursing.mapper.AiChatMessageMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 MySQL 的 Spring AI 会话记忆实现（对应原有 RedisChatMemory）
 *
 * 存储结构：会话消息落到 ai_chat_message 表，按 conversation_id + seq 组织，
 *          读取时按 seq 正序返回，保证 Advisor 拿到的上下文顺序与时间顺序一致。
 *
 * 【重要说明】本类刻意不标注 @Component、也不通过 @Bean 注册到 Spring 容器：
 *            容器内已存在 RedisChatMemory（ChatMemory 实现类），若本类再注册为 Bean，
 *            原有 SpringAIConfig#deepSeekChatClient(ChatMemory) 按类型注入会出现两个候选，
 *            导致 NoUniqueBeanDefinitionException、原功能启动失败。
 *            因此本类由 MysqlChatAiConfig、MysqlChatHistoryServiceImpl 显式 new 出来使用，
 *            既复用同一个 Mapper（数据完全一致），又不污染原有 Bean 体系。
 *
 * @author mixl
 * @date 2026-11-01
 */
public class MysqlChatMemory implements ChatMemory
{
    /** 消息Mapper：会话记忆的读写入口 */
    private final AiChatMessageMapper aiChatMessageMapper;

    /** 会话Mapper：同步维护会话消息条数 */
    private final AiChatConversationMapper aiChatConversationMapper;

    public MysqlChatMemory(AiChatMessageMapper aiChatMessageMapper, AiChatConversationMapper aiChatConversationMapper)
    {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.aiChatConversationMapper = aiChatConversationMapper;
    }

    /**
     * 追加消息到指定会话
     *
     * @param conversationId 会话ID
     * @param messages 本次需要保存的消息（用户提问 / 助手回复）
     */
    @Override
    public void add(String conversationId, List<Message> messages)
    {
        if (CollUtil.isEmpty(messages))
        {
            return;
        }

        // 先取当前会话最大序号，在其基础上递增，保证同一会话内消息严格正序
        int maxSeq = aiChatMessageMapper.selectMaxSeq(conversationId);

        List<AiChatMessage> messageList = new ArrayList<>(messages.size());
        for (Message message : messages)
        {
            AiChatMessage entity = new AiChatMessage();
            entity.setConversationId(conversationId);
            entity.setSeq(++maxSeq);
            entity.setMessageType(message.getMessageType().name());
            entity.setContent(message.getText());
            // 元数据（如工具调用信息）以 JSON 字符串落库，读取时再反序列化
            entity.setMetadata(JSONUtil.toJsonStr(message.getMetadata()));
            messageList.add(entity);
        }

        // 单条 INSERT 批量落库，保证一次对话的多条消息原子写入
        aiChatMessageMapper.batchInsert(messageList);

        // 同步会话表的消息条数与更新时间，便于历史列表展示
        aiChatConversationMapper.incrementMessageCount(conversationId, messages.size());
    }

    /**
     * 获取指定会话的历史消息
     *
     * @param conversationId 会话ID
     * @return 历史消息集合（按时间正序）；无记录时返回空集合
     */
    @Override
    public List<Message> get(String conversationId)
    {
        List<AiChatMessage> messageList = aiChatMessageMapper.selectByConversationId(conversationId);
        if (CollUtil.isEmpty(messageList))
        {
            return List.of();
        }
        return messageList.stream().map(this::toMessage).toList();
    }

    /**
     * 清空指定会话的全部消息
     *
     * @param conversationId 会话ID
     */
    @Override
    public void clear(String conversationId)
    {
        aiChatMessageMapper.deleteByConversationId(conversationId);
    }

    /**
     * 数据库实体转 Spring AI 消息对象
     *
     * @param entity 消息实体
     * @return Spring AI 消息对象
     */
    private Message toMessage(AiChatMessage entity)
    {
        MessageType messageType = MessageType.valueOf(entity.getMessageType());
        Map<String, Object> metadata = StrUtil.isBlank(entity.getMetadata())
                ? Map.of()
                : JSONUtil.toBean(entity.getMetadata(), Map.class);

        return switch (messageType)
        {
            case SYSTEM -> new SystemMessage(entity.getContent());
            case USER -> UserMessage.builder().text(entity.getContent()).metadata(metadata).build();
            // ASSISTANT 与 TOOL 统一按助手消息还原：两者都属于模型侧输出，
            // 这样处理可避免工具调用消息落库后无法还原而抛异常
            case ASSISTANT, TOOL -> AssistantMessage.builder().content(entity.getContent()).properties(metadata).build();
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}
