package com.iqra.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId; // 父部门ID，根部门为0

    private Integer level; // 部门级别，根部门为1

    private Integer status; // 状态：1-正常，0-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}