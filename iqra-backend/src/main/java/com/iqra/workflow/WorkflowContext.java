package com.iqra.workflow;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Workflow context for passing data between nodes
 */
@Data
public class WorkflowContext {

    private String userId;
    private String question;
    private String intent;
    private Long skillId;
    private String modelId;
    private String sessionId;
    private String history;
    private Map<String, Object> attributes = new HashMap<>();

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
