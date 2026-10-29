package com.xhzb.nursing.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xhzb.nursing.memory.ChatHistoryService;
import com.xhzb.nursing.domain.vo.MessageVO;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_TEMPLATE = "chat:history:%s";
    @Autowired
    private ChatMemory chatMemory;

    @Override
    public void savaHistory(String userId, String conversationId) {
        String key = String.format(KEY_TEMPLATE,userId);
        redisTemplate.opsForSet().add(key,conversationId);
    }

    @Override
    public List<String> findChatIdList(String userId) {
        String key = String.format(KEY_TEMPLATE,userId);
        Set<String> chatIdSet  = redisTemplate.opsForSet().members(key);
        if(CollUtil.isEmpty(chatIdSet)){
            return List.of();
        }
        List<String> chatIdList = chatIdSet.stream()
                .sorted(Comparator.comparing(String::toString).reversed()) //对会话ID进行降序排序
                .toList();
        return chatIdList;
    }

    @Override
    public List<MessageVO> findChatHistoryDetails(String chatId) {
        List<Message> messageList = chatMemory.get(chatId);
        if (CollUtil.isEmpty(messageList)) {
            return List.of();
        }

        List<MessageVO> messageVOList = messageList.stream()
                .map(MessageVO::new) //将SpringAI消息转为MessageVO
                .toList();
        return messageVOList;
    }

    /**
     * 删除会话及聊天信息
     *
     * @param chatId
     */
    @Override
    public void deleteChatHistory(String chatId, String userId) {
        String key = String.format(KEY_TEMPLATE,userId);
        //从第一个缓存中删除用户对应的会话ID
        redisTemplate.opsForSet().remove(key,chatId);

        //从第二个缓存中删除会话ID对应的全部消息
        chatMemory.clear(chatId);
    }
}
