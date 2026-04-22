package com.iqra.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iqra.mapper.SkillConfigMapper;
import com.iqra.model.entity.SkillConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Data initialization: updates skill descriptions to Chinese
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final SkillConfigMapper skillConfigMapper;

    @PostConstruct
    public void init() {
        updateSkillDescriptions();
    }

    private void updateSkillDescriptions() {
        List<SkillConfig> skills = skillConfigMapper.selectList(null);
        for (SkillConfig skill : skills) {
            boolean needUpdate = false;

            if ("DocQA".equals(skill.getSkillName()) && skill.getDescription() != null && skill.getDescription().contains("Document based")) {
                skill.setDescription("基于文档的智能问答，通过检索企业内部知识库提供准确答案");
                skill.setPromptTemplate("你是一个知识助手。基于以下参考文档：\n\n参考内容：{{context}}\n问题：{{question}}\n\n请给出准确简洁的回答。");
                needUpdate = true;
            } else if ("Process".equals(skill.getSkillName()) && skill.getDescription() != null && skill.getDescription().contains("Process handling")) {
                skill.setDescription("流程办理助手，支持请假、报销、入职等流程咨询");
                skill.setPromptTemplate("你是流程引导助手。问题：{{question}}\n\n请基于参考文档提供详细的流程步骤。\n\n参考内容：{{context}}");
                needUpdate = true;
            } else if ("ToolQuery".equals(skill.getSkillName()) && skill.getDescription() != null && skill.getDescription().contains("Query employee")) {
                skill.setDescription("工具调用查询，支持员工信息、考勤等数据查询");
                skill.setPromptTemplate("你是数据查询助手。\n问题：{{question}}\n请基于系统提供的数据回答。");
                needUpdate = true;
            }

            if (needUpdate) {
                skillConfigMapper.updateById(skill);
                log.info("Updated skill description to Chinese: {}", skill.getSkillName());
            }
        }
    }
}
