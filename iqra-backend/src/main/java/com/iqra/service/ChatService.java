package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.dto.AskRequest;
import com.iqra.model.entity.ChatHistory;
import com.iqra.model.vo.AskResponse;
import com.iqra.model.vo.ChatHistoryVO;

import java.util.List;

public interface ChatService extends IService<ChatHistory> {

    AskResponse ask(AskRequest request);

    List<ChatHistoryVO> getHistory(String userId, String sessionId);

    void saveHistory(AskRequest request, AskResponse response);

    void deleteHistoryBySession(String userId, String sessionId);
}
