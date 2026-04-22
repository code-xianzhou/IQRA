package com.iqra.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHistoryVO {

    private Long id;

    private String sessionId;

    private String question;

    private String answer;

    private String modelUsed;

    private LocalDateTime createTime;
}
