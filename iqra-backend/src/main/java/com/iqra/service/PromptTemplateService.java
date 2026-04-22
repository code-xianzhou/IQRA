package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.entity.PromptTemplate;

public interface PromptTemplateService extends IService<PromptTemplate> {

    PromptTemplate getSystemPrompt(String modelId, Long skillId);

    PromptTemplate getBySkillId(Long skillId, String modelId);

    void initDefaultTemplates();
}
