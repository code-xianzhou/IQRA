package com.iqra.model.dto;

import lombok.Data;

@Data
public class DocumentUploadRequest {

    private String department;

    private String tags;

    private String uploadBy;
}
