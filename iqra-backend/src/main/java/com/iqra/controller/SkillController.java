package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.model.entity.SkillConfig;
import com.iqra.service.SkillConfigService;
import com.iqra.skill.SkillRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillConfigService skillConfigService;
    private final SkillRegistry skillRegistry;

    @GetMapping("/list")
    public Result<List<SkillConfig>> list() {
        List<SkillConfig> skills = skillConfigService.list();
        return Result.success(skills);
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody SkillConfig config) {
        skillConfigService.updateById(config);
        skillRegistry.refresh();
        return Result.success();
    }

    @PostMapping("/batchUpdate")
    public Result<Void> batchUpdate(@RequestBody List<SkillConfig> configs) {
        skillConfigService.updateBatchById(configs);
        skillRegistry.refresh();
        return Result.success();
    }
}
