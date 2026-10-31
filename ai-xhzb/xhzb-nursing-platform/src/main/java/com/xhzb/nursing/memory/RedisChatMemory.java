package com.xhzb.nursing.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.nursing.domain.vo.Msg;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class RedisChatMemory implements ChatMemory {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_TEMPLATE = "chat:memory:%s";

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }

        List<String> messageList = messages.stream().map(Msg::new).map(x -> JSONUtil.toJsonStr(x)).toList();

        String key = String.format(KEY_TEMPLATE, conversationId);
        // 追加到列表尾部：保证按时间正序存放，
        // 否则历史详情会倒序展示，且 Advisor 取到的上下文顺序也是反的
        redisTemplate.opsForList().rightPushAll(key, messageList);
     }


    @Override
    public List<Message> get(String conversationId) {
        String key = String.format(KEY_TEMPLATE, conversationId);
        List<String> listMessage = redisTemplate.opsForList().range(key, 0, -1);
        if (CollUtil.isEmpty(listMessage)) {
            return List.of();
        }
        List<Message> messageList = listMessage.stream()
                .map(x -> JSONUtil.toBean(x,Msg.class))
                .map(Msg :: toMessage)
                .toList();
        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        String key = String.format(KEY_TEMPLATE, conversationId);
        redisTemplate.delete(key);

    }
}
