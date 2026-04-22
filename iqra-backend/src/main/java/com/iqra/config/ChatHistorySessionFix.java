package com.iqra.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iqra.mapper.ChatHistoryMapper;
import com.iqra.model.entity.ChatHistory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Data initialization: fix null sessionId in chat_history
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHistorySessionFix {

    private final ChatHistoryMapper chatHistoryMapper;

    @PostConstruct
    public void fixNullSessionIds() {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(ChatHistory::getSessionId);
        wrapper.orderByAsc(ChatHistory::getCreateTime);
        List<ChatHistory> nullSessionRecords = chatHistoryMapper.selectList(wrapper);

        if (nullSessionRecords.isEmpty()) {
            return;
        }

        log.info("Found {} chat history records with null sessionId, fixing...", nullSessionRecords.size());

        // Group consecutive null-session records: each record becomes its own session
        // since we can't know which ones belonged together
        int fixed = 0;
        for (ChatHistory record : nullSessionRecords) {
            String newSessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);
            record.setSessionId(newSessionId);
            chatHistoryMapper.updateById(record);
            fixed++;
        }

        log.info("Fixed {} chat history records with generated sessionIds", fixed);
    }
}
