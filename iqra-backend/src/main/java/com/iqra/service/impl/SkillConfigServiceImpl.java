package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.common.Constants;
import com.iqra.mapper.SkillConfigMapper;
import com.iqra.model.entity.SkillConfig;
import com.iqra.service.SkillConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SkillConfigServiceImpl extends ServiceImpl<SkillConfigMapper, SkillConfig> implements SkillConfigService {

    @Override
    public List<SkillConfig> getAllEnabled() {
        LambdaQueryWrapper<SkillConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillConfig::getEnabled, 1);
        return this.list(wrapper);
    }

    @Override
    public SkillConfig getByName(String skillName) {
        LambdaQueryWrapper<SkillConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillConfig::getSkillName, skillName);
        return this.getOne(wrapper);
    }

    public void initDefaultSkills() {
        if (this.count() > 0) {
            return;
        }

        // Document QA skill
        SkillConfig docQa = new SkillConfig();
        docQa.setSkillName("文档问答");
        docQa.setSkillType(Constants.SKILL_TYPE_DOC_QA);
        docQa.setDescription("基于文档库进行智能问答");
        docQa.setPromptTemplate("你是企业知识助手，请基于以下参考文档回答问题。\n\n参考文档：{{context}}\n问题：{{question}}\n\n请给出准确、简洁的回答。");
        docQa.setEnabled(1);
        docQa.setRecallStrategy("multi");
        docQa.setTopK(5);
        docQa.setRerankThreshold(0.5);
        docQa.setCreateTime(LocalDateTime.now());
        docQa.setUpdateTime(LocalDateTime.now());
        this.save(docQa);

        // Process skill
        SkillConfig process = new SkillConfig();
        process.setSkillName("流程处理");
        process.setSkillType(Constants.SKILL_TYPE_PROCESS);
        process.setDescription("处理请假、报销、入职等流程查询");
        process.setPromptTemplate("你是流程指引助手。用户问题：{{question}}\n\n请基于参考文档提供详细的流程步骤和注意事项。\n\n参考文档：{{context}}");
        process.setEnabled(1);
        process.setRecallStrategy("multi");
        process.setTopK(5);
        process.setRerankThreshold(0.4);
        process.setCreateTime(LocalDateTime.now());
        process.setUpdateTime(LocalDateTime.now());
        this.save(process);

        // Tool query skill
        SkillConfig toolQuery = new SkillConfig();
        toolQuery.setSkillName("工具查询");
        toolQuery.setSkillType(Constants.SKILL_TYPE_TOOL_QUERY);
        toolQuery.setDescription("查询员工信息、考勤等数据");
        toolQuery.setPromptTemplate("你是数据查询助手。\n用户问题：{{question}}\n请基于系统提供的数据回答。");
        toolQuery.setEnabled(1);
        toolQuery.setRecallStrategy("keyword");
        toolQuery.setTopK(3);
        toolQuery.setRerankThreshold(0.6);
        toolQuery.setCreateTime(LocalDateTime.now());
        toolQuery.setUpdateTime(LocalDateTime.now());
        this.save(toolQuery);
    }
}
