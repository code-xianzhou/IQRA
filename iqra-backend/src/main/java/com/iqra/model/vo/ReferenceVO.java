package com.iqra.model.vo;

import lombok.Data;

@Data
public class ReferenceVO {

    private String documentId;

    private String documentName;

    private String content;

    private Double score;

    private Integer chunkIndex;
}
