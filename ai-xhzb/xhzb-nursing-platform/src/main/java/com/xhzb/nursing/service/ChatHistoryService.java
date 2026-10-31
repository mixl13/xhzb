package com.xhzb.nursing.service;

import com.xhzb.nursing.domain.vo.MessageVO;

import java.util.List;

public interface ChatHistoryService {

    /**
     * 保存用户会话ID
     */
    void savaHistory(String userId,String conversationId);

    List<String> findChatIdList(String userId);

    List<MessageVO> findChatHistoryDetails(String chatId);

    void deleteChatHistory(String chatId, String string);
}
