package com.xhzb.nursing.config;

import com.xhzb.nursing.constant.SystemConstants;
import com.xhzb.nursing.mapper.AiChatConversationMapper;
import com.xhzb.nursing.mapper.AiChatMessageMapper;
import com.xhzb.nursing.memory.MysqlChatMemory;
import com.xhzb.nursing.util.NursingLevelTool;
import com.xhzb.nursing.util.NursingProjectTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MySQL 版会话记忆的 Spring AI 配置
 *
 * 说明：与原有的 SpringAIConfig（Redis 版）完全独立，只是新增一个 ChatClient Bean，
 *      原有 deepSeekChatClient 仍走 RedisChatMemory，两者互不影响。
 *
 * @author mixl
 * @date 2026-11-01
 */
@Configuration
public class MysqlChatAiConfig
{
    @Autowired
    private AiChatMessageMapper aiChatMessageMapper;

    @Autowired
    private AiChatConversationMapper aiChatConversationMapper;

    /**
     * 创建使用 MySQL 会话记忆的 ChatClient
     * 与 deepSeekChatClient 的唯一区别：MessageChatMemoryAdvisor 底层的 ChatMemory 换成 MysqlChatMemory
     *
     * @param deepSeekChatModel DeepSeek 模型
     * @param nursingProjectTool 护理项目工具
     * @param nursingLevelTool 护理等级工具
     * @param vectorStore 向量存储（RAG 检索）
     * @return ChatClient 实例
     */
    @Bean
    public ChatClient mysqlMemoryChatClient(DeepSeekChatModel deepSeekChatModel,
                                            NursingProjectTool nursingProjectTool,
                                            NursingLevelTool nursingLevelTool,
                                            VectorStore vectorStore)
    {
        // MySQL 版会话记忆不注册为 Bean，此处显式构造，避免与原 RedisChatMemory 产生类型注入歧义
        ChatMemory mysqlChatMemory = new MysqlChatMemory(aiChatMessageMapper, aiChatConversationMapper);

        return ChatClient
                .builder(deepSeekChatModel)
                .defaultSystem(SystemConstants.SYSTEM_PROMPT)
                .defaultTools(nursingProjectTool, nursingLevelTool)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(mysqlChatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.7)
                                .topK(3).build()).build())
                .build();
    }
}
