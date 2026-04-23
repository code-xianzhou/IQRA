package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.dto.CreateUserRequest;
import com.iqra.model.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User login(String username, String password);

    List<User> getAllUsers();

    User createUser(CreateUserRequest request);

    boolean deleteUser(Long id);

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    boolean resetFirstLogin(Long userId);

    boolean resetPassword(Long userId);
}