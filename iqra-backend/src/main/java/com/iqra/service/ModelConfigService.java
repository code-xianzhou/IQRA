package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.entity.ModelConfig;

import java.util.List;

public interface ModelConfigService extends IService<ModelConfig> {

    ModelConfig getByModelId(String modelId);

    ModelConfig getDefaultModel();

    List<ModelConfig> getAllEnabledModels();

    boolean switchModel(String modelId);

    void initDefaultModels();
}
