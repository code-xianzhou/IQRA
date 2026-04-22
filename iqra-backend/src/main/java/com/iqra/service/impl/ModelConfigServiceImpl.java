package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.common.BusinessException;
import com.iqra.mapper.ModelConfigMapper;
import com.iqra.model.entity.ModelConfig;
import com.iqra.service.ModelConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModelConfigServiceImpl extends ServiceImpl<ModelConfigMapper, ModelConfig> implements ModelConfigService {

    @Override
    public ModelConfig getByModelId(String modelId) {
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getModelId, modelId);
        return this.getOne(wrapper);
    }

    @Override
    public ModelConfig getDefaultModel() {
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getIsDefault, 1);
        wrapper.eq(ModelConfig::getEnabled, 1);
        return this.getOne(wrapper);
    }

    @Override
    public List<ModelConfig> getAllEnabledModels() {
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getEnabled, 1);
        wrapper.orderByAsc(ModelConfig::getModelId);
        return this.list(wrapper);
    }

    @Override
    public boolean switchModel(String modelId) {
        ModelConfig targetModel = getByModelId(modelId);
        if (targetModel == null) {
            throw new BusinessException("模型不存在: " + modelId);
        }

        // Reset all models to non-default
        LambdaQueryWrapper<ModelConfig> allWrapper = new LambdaQueryWrapper<>();
        List<ModelConfig> allModels = this.list(allWrapper);
        for (ModelConfig model : allModels) {
            model.setIsDefault(0);
            model.setUpdateTime(LocalDateTime.now());
            this.updateById(model);
        }

        // Set target model as default
        targetModel.setIsDefault(1);
        targetModel.setUpdateTime(LocalDateTime.now());
        return this.updateById(targetModel);
    }

    @Override
    public void initDefaultModels() {
        if (this.count() > 0) {
            return;
        }

        // Qwen2-7B
        ModelConfig qwen2 = new ModelConfig();
        qwen2.setModelId("qwen2-7b");
        qwen2.setModelName("Qwen2-7B");
        qwen2.setModelType("open_source");
        qwen2.setBaseUrl("http://localhost");
        qwen2.setPort(11434);
        qwen2.setApiPath("/v1/chat/completions");
        qwen2.setTimeout(60000);
        qwen2.setTemperature(0.7);
        qwen2.setMaxTokens(4096);
        qwen2.setEnabled(1);
        qwen2.setIsDefault(1);
        qwen2.setStatus("active");
        this.save(qwen2);

        // Llama3-8B
        ModelConfig llama3 = new ModelConfig();
        llama3.setModelId("llama3-8b");
        llama3.setModelName("Llama3-8B");
        llama3.setModelType("open_source");
        llama3.setBaseUrl("http://localhost");
        llama3.setPort(11435);
        llama3.setApiPath("/v1/chat/completions");
        llama3.setTimeout(60000);
        llama3.setTemperature(0.7);
        llama3.setMaxTokens(4096);
        llama3.setEnabled(1);
        llama3.setIsDefault(0);
        llama3.setStatus("active");
        this.save(llama3);

        // ChatGLM3-6B
        ModelConfig chatglm3 = new ModelConfig();
        chatglm3.setModelId("chatglm3-6b");
        chatglm3.setModelName("ChatGLM3-6B");
        chatglm3.setModelType("open_source");
        chatglm3.setBaseUrl("http://localhost");
        chatglm3.setPort(11436);
        chatglm3.setApiPath("/v1/chat/completions");
        chatglm3.setTimeout(60000);
        chatglm3.setTemperature(0.7);
        chatglm3.setMaxTokens(4096);
        chatglm3.setEnabled(1);
        chatglm3.setIsDefault(0);
        chatglm3.setStatus("active");
        this.save(chatglm3);
    }
}
