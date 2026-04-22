package com.iqra.model.dto;

import lombok.Data;

@Data
public class AskRequest {

    private String userId;

    private String question;

    private Long skillId;

    private String modelId;

    private String sessionId;
}
