package com.iqra.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private Long departmentId; // 部门ID
    private String departmentName; // 部门名称
    private String email;
    private String phone;
    private String role;
    private Integer firstLogin;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}