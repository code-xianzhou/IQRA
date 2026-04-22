package com.iqra.skill;

import com.iqra.model.entity.SkillConfig;
import com.iqra.service.SkillConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skill registry and routing
 */
@Slf4j
@Component
public class SkillRegistry {

    private final SkillConfigService skillConfigService;
    private final Map<String, Long> intentSkillMap = new ConcurrentHashMap<>();

    public SkillRegistry(SkillConfigService skillConfigService) {
        this.skillConfigService = skillConfigService;
        init();
    }

    private void init() {
        List<SkillConfig> skills = skillConfigService.getAllEnabled();
        for (SkillConfig skill : skills) {
            if (skill.getEnabled() == 1) {
                // Map intent to skill based on skill type
                switch (skill.getSkillType()) {
                    case "DOC_QA":
                        intentSkillMap.put("DOCUMENT_QA", skill.getId());
                        break;
                    case "PROCESS":
                        intentSkillMap.put("LEAVE_PROCESS", skill.getId());
                        intentSkillMap.put("REIMBURSE_PROCESS", skill.getId());
                        intentSkillMap.put("ONBOARD_PROCESS", skill.getId());
                        break;
                    case "TOOL_QUERY":
                        intentSkillMap.put("ATTENDANCE_QUERY", skill.getId());
                        intentSkillMap.put("SALARY_QUERY", skill.getId());
                        break;
                }
            }
        }
    }

    public Long routeByIntent(String intent) {
        return intentSkillMap.get(intent);
    }

    public void refresh() {
        intentSkillMap.clear();
        init();
    }
}
