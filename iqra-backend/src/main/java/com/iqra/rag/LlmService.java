package com.iqra.rag;

import com.iqra.common.BusinessException;
import com.iqra.model.entity.ModelConfig;
import com.iqra.service.ModelConfigService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM service: manages multiple chat models and provides model switching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final ModelConfigService modelConfigService;

    private final ConcurrentHashMap<String, ChatLanguageModel> modelCache = new ConcurrentHashMap<>();

    public ChatLanguageModel getChatModel(String modelId) {
        return modelCache.computeIfAbsent(modelId, this::createChatModel);
    }

    public ChatLanguageModel createChatModel(String modelId) {
        ModelConfig config = modelConfigService.getByModelId(modelId);
        if (config == null) {
            throw new BusinessException("模型配置不存在: " + modelId);
        }

        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException("模型未启用: " + modelId);
        }

        log.info("Creating chat model: {} at {}:{}", modelId, config.getBaseUrl(), config.getPort());

        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl() + ":" + config.getPort())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .timeout(java.time.Duration.ofMillis(config.getTimeout()))
                .build();
    }

    public void invalidateModel(String modelId) {
        modelCache.remove(modelId);
    }

    public void invalidateAllModels() {
        modelCache.clear();
    }
}
