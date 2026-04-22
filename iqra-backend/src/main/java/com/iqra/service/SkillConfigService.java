package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.entity.SkillConfig;

import java.util.List;

public interface SkillConfigService extends IService<SkillConfig> {

    List<SkillConfig> getAllEnabled();

    SkillConfig getByName(String skillName);
}
