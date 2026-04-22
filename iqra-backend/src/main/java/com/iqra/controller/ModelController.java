package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.model.entity.ModelConfig;
import com.iqra.rag.LlmService;
import com.iqra.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class ModelController {

    private final ModelConfigService modelConfigService;
    private final LlmService llmService;

    @GetMapping("/config")
    public Result<List<ModelConfig>> config() {
        List<ModelConfig> models = modelConfigService.list();
        return Result.success(models);
    }

    @PostMapping("/switch")
    public Result<Void> switchModel(@RequestBody SwitchModelRequest request) {
        modelConfigService.switchModel(request.getModelId());
        llmService.invalidateModel(request.getModelId());
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody ModelConfig config) {
        modelConfigService.updateById(config);
        llmService.invalidateModel(config.getModelId());
        return Result.success();
    }

    public static class SwitchModelRequest {
        private String modelId;

        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
    }
}
