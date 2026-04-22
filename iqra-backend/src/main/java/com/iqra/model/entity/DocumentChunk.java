package com.iqra.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document_chunk")
public class DocumentChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String chunkId;

    private Long documentId;

    private String content;

    private Integer chunkIndex;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
