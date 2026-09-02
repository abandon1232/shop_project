package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.controller.request.AuthRequest;
import com.example.controller.request.PasswordChangeRequest;
import com.example.service.AdminService;
import com.example.service.BusinessService;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * Public authentication endpoints.
 */
@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private BusinessService businessService;

    @Resource
    private UserService userService;

    @GetMapping("/")
    public Result hello() {
        return Result.success("访问成功");
    }

    /**
     * Authenticate an account.
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody AuthRequest request) {
        return switch (request.role()) {
            case "ADMIN" -> Result.success(adminService.login(request.toAccount()));
            case "BUSINESS" -> Result.success(businessService.login(request.toAccount()));
            case "USER" -> Result.success(userService.login(request.toAccount()));
            default -> Result.error(ResultCodeEnum.PARAM_ERROR);
        };
    }

    /**
     * Register an account.
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody AuthRequest request) {
        return switch (request.role()) {
            case "ADMIN" -> Result.error(ResultCodeEnum.FORBIDDEN_ERROR);
            case "BUSINESS" -> {
                businessService.register(request.toAccount());
                yield Result.success();
            }
            case "USER" -> {
                userService.register(request.toAccount());
                yield Result.success();
            }
            default -> Result.error(ResultCodeEnum.PARAM_ERROR);
        };
    }

    /**
     * Change a password.
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return switch (request.role()) {
            case "ADMIN" -> {
                adminService.updatePassword(request.toAccount());
                yield Result.success();
            }
            case "BUSINESS" -> {
                businessService.updatePassword(request.toAccount());
                yield Result.success();
            }
            case "USER" -> {
                userService.updatePassword(request.toAccount());
                yield Result.success();
            }
            default -> Result.error(ResultCodeEnum.PARAM_ERROR);
        };
    }

}
