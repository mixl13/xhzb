package com.xhzb.nursing.config;

import com.xhzb.nursing.constant.SystemConstants;
import com.xhzb.nursing.util.NursingProjectTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    /**
     * 创建并返回一个ChatClient的Spring Bean实例。
     * @param deepSeekChatModel
     * @return
     */
    @Bean
    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel, NursingProjectTool nursingProjectTool, ChatMemory chatMemory) {
        return ChatClient
                .builder(deepSeekChatModel)
                .defaultSystem(SystemConstants.PROMPT)
                .defaultTools(nursingProjectTool)
                .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build())  //添加默认的advisor 记录日志
                .build();
    }
}
