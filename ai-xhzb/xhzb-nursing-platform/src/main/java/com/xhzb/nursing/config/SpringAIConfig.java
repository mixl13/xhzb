package com.xhzb.nursing.config;

import com.xhzb.nursing.constant.SystemConstants;
import com.xhzb.nursing.util.NursingLevelTool;
import com.xhzb.nursing.util.NursingProjectTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class SpringAIConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.ai.vectorstore.redis.index-name:spring-ai-index}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix:doc:}")
    private String prefix;


    /**
     * 创建并返回一个ChatClient的Spring Bean实例。
     * @param deepSeekChatModel
     * @return
     */
    @Bean
    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel, NursingProjectTool nursingProjectTool,
                                         ChatMemory chatMemory, NursingLevelTool nursingLevelTool,VectorStore vectorStore) {
        return ChatClient
                .builder(deepSeekChatModel)
                .defaultSystem(SystemConstants.PROMPT)
                .defaultTools(nursingProjectTool,nursingLevelTool)
                .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.7)
                                .topK(3).build()).build())  //添加默认的advisor 记录日志
                .build();
    }

    @Bean
    public VectorStore redisVectorStore(OpenAiEmbeddingModel openAiEmbeddingModel) {
        JedisPooled jedisPooled = new JedisPooled("192.168.100.168", 6379);
        return RedisVectorStore.builder(jedisPooled, openAiEmbeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(true)
                .build();
    }
}
