package com.iqra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {

    private String uploadPath = "./uploads";

    private String[] allowedExtensions = {"pdf", "doc", "docx", "txt"};

    private Long maxFileSize = 50L * 1024 * 1024; // 50MB
}
