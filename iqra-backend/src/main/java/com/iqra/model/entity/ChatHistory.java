package com.iqra.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_history")
public class ChatHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String sessionId;

    private String question;

    private String answer;

    private String referencesJson;

    private String modelUsed;

    private String skillUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
