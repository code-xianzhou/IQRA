package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.common.BusinessException;
import com.iqra.mapper.DepartmentMapper;
import com.iqra.model.entity.Department;
import com.iqra.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public List<Department> getDepartmentTree() {
        return baseMapper.getDepartmentTree();
    }

    @Override
    public Department createDepartment(Department department) {
        // 检查父部门是否存在
        if (department.getParentId() != 0) {
            Department parent = this.getById(department.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            // 设置部门级别
            department.setLevel(parent.getLevel() + 1);
        } else {
            // 根部门
            department.setLevel(1);
        }

        department.setStatus(1);
        this.save(department);
        return department;
    }

    @Override
    public Department updateDepartment(Department department) {
        Department existing = this.getById(department.getId());
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        // 根部门不能修改父部门
        if (existing.getParentId() == 0 && department.getParentId() != 0) {
            throw new BusinessException("根部门不能修改父部门");
        }
        // 如果修改了父部门
        if (!existing.getParentId().equals(department.getParentId())) {
            if (department.getParentId() != 0) {
                Department parent = this.getById(department.getParentId());
                if (parent == null) {
                    throw new BusinessException("父部门不存在");
                }
                // 设置部门级别
                department.setLevel(parent.getLevel() + 1);
            } else {
                // 改为根部门
                department.setLevel(1);
            }
        }
        this.updateById(department);
        return department;
    }

    @Override
    public boolean deleteDepartment(Long departmentId) {
        Department department = this.getById(departmentId);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        // 根部门不能删除
        if (department.getParentId() == 0) {
            throw new BusinessException("根部门不能删除");
        }
        // 获取部门及其所有子部门的ID
        List<Long> departmentIds = baseMapper.getDepartmentIdsWithChildren(departmentId);
        // 检查部门下是否有用户
        if (checkUsersInDepartment(departmentId)) {
            throw new BusinessException("部门下有用户，不能删除");
        }
        // 级联删除所有子部门
        return this.removeByIds(departmentIds);
    }

    @Override
    public boolean checkUsersInDepartment(Long departmentId) {
        List<Long> departmentIds = baseMapper.getDepartmentIdsWithChildren(departmentId);
        int count = baseMapper.countUsersInDepartments(departmentIds);
        return count > 0;
    }
}