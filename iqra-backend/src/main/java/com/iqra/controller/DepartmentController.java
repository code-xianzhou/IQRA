package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.model.entity.Department;
import com.iqra.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // 获取部门树结构
    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree() {
        List<Department> departments = departmentService.getDepartmentTree();
        return Result.success(departments);
    }

    // 创建部门
    @PostMapping("/create")
    public Result<Department> createDepartment(@RequestBody Department department) {
        Department created = departmentService.createDepartment(department);
        return Result.success(created);
    }

    // 更新部门
    @PutMapping("/update")
    public Result<Department> updateDepartment(@RequestBody Department department) {
        Department updated = departmentService.updateDepartment(department);
        return Result.success(updated);
    }

    // 删除部门
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}