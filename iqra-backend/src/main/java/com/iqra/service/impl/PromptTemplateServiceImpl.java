package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.mapper.PromptTemplateMapper;
import com.iqra.model.entity.PromptTemplate;
import com.iqra.service.PromptTemplateService;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateServiceImpl extends ServiceImpl<PromptTemplateMapper, PromptTemplate> implements PromptTemplateService {

    @Override
    public PromptTemplate getSystemPrompt(String modelId, Long skillId) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getTemplateType, "SYSTEM");

        if (skillId != null) {
            wrapper.eq(PromptTemplate::getSkillType, String.valueOf(skillId));
        }

        if (modelId != null) {
            wrapper.eq(PromptTemplate::getModelId, modelId);
        }

        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public PromptTemplate getBySkillId(Long skillId, String modelId) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getSkillType, String.valueOf(skillId));
        if (modelId != null) {
            wrapper.eq(PromptTemplate::getModelId, modelId);
        }
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public void initDefaultTemplates() {
        if (this.count() > 0) {
            return;
        }

        PromptTemplate template = new PromptTemplate();
        template.setTemplateName("默认系统提示词");
        template.setTemplateType("SYSTEM");
        template.setContent("你是企业内部智能助手，必须基于提供的参考文档回答问题，不能编造信息。\n\n参考文档：{{context}}\n\n用户问题：{{question}}\n\n历史对话：{{history}}\n\n请用简洁、正式、准确的中文回答。");
        this.save(template);
    }
}
