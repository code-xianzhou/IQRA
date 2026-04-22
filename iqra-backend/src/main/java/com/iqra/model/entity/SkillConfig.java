package com.iqra.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("skill_config")
public class SkillConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String skillName;

    private String skillType;

    private String description;

    private String promptTemplate;

    private Integer enabled;

    private String recallStrategy;

    private Integer topK;

    private Double rerankThreshold;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
