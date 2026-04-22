package com.iqra.rag;

import com.iqra.model.entity.PromptTemplate;
import com.iqra.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt template management and generation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptEngine {

    private final PromptTemplateService promptTemplateService;

    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是企业内部智能助手，必须基于提供的参考文档回答问题，不能编造信息。\n\n" +
            "参考文档：{{context}}\n\n" +
            "用户问题：{{question}}\n\n" +
            "历史对话：{{history}}\n\n" +
            "请用简洁、正式、准确的中文回答。";

    public String buildPrompt(String question, String context, String history, String modelId, Long skillId) {
        String template = getTemplate(modelId, skillId);

        Map<String, String> variables = new HashMap<>();
        variables.put("context", context);
        variables.put("question", question);
        variables.put("history", history != null ? history : "无");

        return replaceVariables(template, variables);
    }

    public String buildSystemPrompt(String modelId, Long skillId) {
        PromptTemplate template = promptTemplateService.getSystemPrompt(modelId, skillId);
        if (template != null && template.getContent() != null) {
            return template.getContent();
        }
        return DEFAULT_SYSTEM_PROMPT;
    }

    private String getTemplate(String modelId, Long skillId) {
        if (skillId != null) {
            PromptTemplate template = promptTemplateService.getBySkillId(skillId, modelId);
            if (template != null) {
                return template.getContent();
            }
        }
        return DEFAULT_SYSTEM_PROMPT;
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    public String buildContext(List<String> segments) {
        if (segments == null || segments.isEmpty()) {
            return "无参考文档。";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            sb.append("[文档片段").append(i + 1).append("]\n");
            sb.append(segments.get(i));
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
