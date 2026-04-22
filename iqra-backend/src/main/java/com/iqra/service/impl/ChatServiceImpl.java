package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqra.common.BusinessException;
import com.iqra.mapper.ChatHistoryMapper;
import com.iqra.model.dto.AskRequest;
import com.iqra.model.entity.ChatHistory;
import com.iqra.model.entity.ModelConfig;
import com.iqra.model.vo.AskResponse;
import com.iqra.model.vo.ChatHistoryVO;
import com.iqra.rag.RagEngine;
import com.iqra.service.ChatService;
import com.iqra.service.ModelConfigService;
import com.iqra.workflow.WorkflowContext;
import com.iqra.workflow.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatService {

    private final RagEngine ragEngine;
    private final WorkflowEngine workflowEngine;
    private final ModelConfigService modelConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AskResponse ask(AskRequest request) {
        // Validate request
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new BusinessException("用户ID不能为空");
        }
        if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
            throw new BusinessException("问题不能为空");
        }

        // Get model: use request model or default
        String modelId = request.getModelId();
        if (modelId == null || modelId.isEmpty()) {
            ModelConfig defaultModel = modelConfigService.getDefaultModel();
            if (defaultModel == null) {
                throw new BusinessException("默认模型未配置");
            }
            modelId = defaultModel.getModelId();
        }

        // Build workflow context
        WorkflowContext context = new WorkflowContext();
        context.setUserId(request.getUserId());
        context.setQuestion(request.getQuestion());
        context.setSkillId(request.getSkillId());
        context.setModelId(modelId);
        context.setSessionId(request.getSessionId());

        // Get recent history for this session
        String history = buildHistory(request.getUserId(), request.getSessionId());
        context.setHistory(history);

        // Execute workflow
        AskResponse response = workflowEngine.execute(context);

        // Save to history
        saveHistory(request, response);

        return response;
    }

    @Override
    public List<ChatHistoryVO> getHistory(String userId, String sessionId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId);
        if (sessionId != null && !sessionId.isEmpty()) {
            wrapper.eq(ChatHistory::getSessionId, sessionId);
        }
        wrapper.orderByDesc(ChatHistory::getCreateTime);
        wrapper.last("LIMIT 50");

        List<ChatHistory> records = this.list(wrapper);
        List<ChatHistoryVO> result = new ArrayList<>();
        for (ChatHistory record : records) {
            ChatHistoryVO vo = new ChatHistoryVO();
            BeanUtils.copyProperties(record, vo);
            result.add(vo);
        }
        return result;
    }

    @Override
    public void saveHistory(AskRequest request, AskResponse response) {
        ChatHistory history = new ChatHistory();
        history.setUserId(request.getUserId());
        history.setSessionId(request.getSessionId());
        history.setQuestion(request.getQuestion());
        history.setAnswer(response.getAnswer());
        history.setModelUsed(response.getModelUsed());

        if (response.getReferences() != null) {
            try {
                history.setReferencesJson(objectMapper.writeValueAsString(response.getReferences()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize references", e);
            }
        }

        this.save(history);
    }

    @Override
    public void deleteHistoryBySession(String userId, String sessionId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId);
        wrapper.eq(ChatHistory::getSessionId, sessionId);
        this.remove(wrapper);
    }

    private String buildHistory(String userId, String sessionId) {
        List<ChatHistoryVO> historyList = getHistory(userId, sessionId);
        if (historyList.isEmpty()) {
            return "无";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(5, historyList.size());
        for (int i = limit - 1; i >= 0; i--) {
            ChatHistoryVO h = historyList.get(i);
            sb.append("用户: ").append(h.getQuestion()).append("\n");
            sb.append("助手: ").append(h.getAnswer()).append("\n\n");
        }
        return sb.toString();
    }
}
