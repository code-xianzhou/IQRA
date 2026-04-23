package com.iqra.config;

import com.iqra.mapper.DepartmentMapper;
import com.iqra.mapper.UserMapper;
import com.iqra.model.entity.Department;
import com.iqra.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public void run(String... args) throws Exception {
        // 初始化根部门
        if (departmentMapper.selectCount(null) == 0) {
            Department rootDept = new Department();
            rootDept.setName("根部门");
            rootDept.setParentId(0L);
            rootDept.setLevel(1);
            rootDept.setStatus(1);
            departmentMapper.insert(rootDept);
        }

        // 初始化默认管理员账户
        if (userMapper.selectCount(null) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setRealName("管理员");
            admin.setDepartment("根部门");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800138000");
            admin.setRole("ADMIN");
            admin.setFirstLogin(1);
            admin.setStatus(1);
            userMapper.insert(admin);
        }
    }
}