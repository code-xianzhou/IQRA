package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.entity.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    // 获取部门树结构
    List<Department> getDepartmentTree();

    // 创建部门
    Department createDepartment(Department department);

    // 更新部门
    Department updateDepartment(Department department);

    // 删除部门（级联删除子部门）
    boolean deleteDepartment(Long departmentId);

    // 检查部门下是否有用户
    boolean checkUsersInDepartment(Long departmentId);
}