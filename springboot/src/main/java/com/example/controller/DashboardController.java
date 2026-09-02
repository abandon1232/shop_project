package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public Result summary() {
        return Result.success(dashboardService.summary());
    }
}
