package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.model.dto.CreateUserRequest;
import com.iqra.model.entity.User;
import com.iqra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userService.getAllUsers();
        // 隐藏密码
        users.forEach(u -> u.setPassword("******"));
        return Result.success(users);
    }

    @PostMapping("/create")
    public Result<User> create(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        user.setPassword("******");
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PostMapping("/{id}/reset-first-login")
    public Result<Void> resetFirstLogin(@PathVariable Long id) {
        userService.resetFirstLogin(id);
        return Result.success();
    }

    @PostMapping("/reset-password/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
}