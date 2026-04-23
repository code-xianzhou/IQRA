package com.iqra.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.dto.CreateUserRequest;
import com.iqra.model.entity.User;
import com.iqra.model.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    User login(String username, String password);

    List<User> getAllUsers();

    List<UserVO> getAllUserVOs();

    User createUser(CreateUserRequest request);

    User updateUser(User user);

    boolean deleteUser(Long id);

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    boolean resetFirstLogin(Long userId);

    boolean resetPassword(Long userId);
}