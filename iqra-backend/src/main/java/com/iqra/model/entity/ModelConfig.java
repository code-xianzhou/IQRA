package com.iqra.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_config")
public class ModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String modelId;

    private String modelName;

    private String modelType;

    private String baseUrl;

    private Integer port;

    private String apiPath;

    private Integer timeout;

    private Double temperature;

    private Integer maxTokens;

    private Integer enabled;

    private Integer isDefault;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
