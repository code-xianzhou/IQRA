package com.iqra.controller;

import com.iqra.common.Result;
import com.iqra.model.dto.ChangePasswordRequest;
import com.iqra.model.dto.LoginRequest;
import com.iqra.model.entity.User;
import com.iqra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = userService.login(request.getUsername(), request.getPassword());
        session.setAttribute("user", user);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("firstLogin", user.getFirstLogin() != null && user.getFirstLogin() == 1);
        return Result.success(result);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }
        userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        // 清除 session，要求重新登录
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(user);
    }
}
