package com.iqra.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentVO {

    private Long id;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String uploadBy;

    private String status;

    private String department;

    private String tags;

    private String errorMessage;

    private LocalDateTime createTime;
}