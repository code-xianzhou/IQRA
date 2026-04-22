package com.iqra.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class AskResponse {

    private String answer;

    private List<ReferenceVO> references;

    private Long time;

    private String modelUsed;
}
