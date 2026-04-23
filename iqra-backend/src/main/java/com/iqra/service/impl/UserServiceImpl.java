package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.common.BusinessException;
import com.iqra.mapper.UserMapper;
import com.iqra.model.dto.CreateUserRequest;
import com.iqra.model.entity.User;
import com.iqra.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getPassword, password);
        wrapper.eq(User::getStatus, 1);

        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public User createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setDepartment(request.getDepartment());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setFirstLogin(1); // 新用户首次登录标志
        user.setStatus(1);
        this.save(user);
        return user;
    }

    @Override
    public boolean deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("admin 管理员账户不可删除");
        }
        return this.removeById(id);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!oldPassword.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        user.setPassword(newPassword);
        user.setFirstLogin(0); // 修改密码后清除首次登录标志
        return this.updateById(user);
    }

    @Override
    public boolean resetFirstLogin(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setFirstLogin(1);
        return this.updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("admin 管理员账户不可重置密码");
        }
        // 重置密码为用户名
        user.setPassword(user.getUsername());
        user.setFirstLogin(1); // 重置密码后需要首次登录修改密码
        return this.updateById(user);
    }
}