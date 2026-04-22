package com.iqra.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("function_info")
public class FunctionInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String functionName;

    private String functionDesc;

    private String parameters;

    private String returnType;

    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
