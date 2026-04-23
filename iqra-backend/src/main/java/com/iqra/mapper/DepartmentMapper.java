package com.iqra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iqra.model.entity.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    // 获取部门树结构
    List<Department> getDepartmentTree();

    // 获取指定部门的所有子部门
    List<Department> getChildDepartments(Long parentId);

    // 获取部门及其所有子部门的ID列表
    List<Long> getDepartmentIdsWithChildren(Long departmentId);

    // 检查部门下是否有用户
    int countUsersInDepartments(List<Long> departmentIds);
}