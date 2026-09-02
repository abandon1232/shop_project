package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.controller.request.OrderStatusRequest;
import com.example.controller.request.PlaceOrderRequest;
import com.example.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/add")
    @RequireRoles(RoleEnum.USER)
    public Result add(@Valid @RequestBody PlaceOrderRequest request) {
        return Result.success(orderService.placeOrder(request));
    }

    @GetMapping("/selectPage")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS, RoleEnum.USER})
    public Result selectPage(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
        return Result.success(orderService.selectPage(pageNum, pageSize));
    }

    @PutMapping("/status")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result updateStatus(@Valid @RequestBody OrderStatusRequest request) {
        orderService.updateStatus(request);
        return Result.success();
    }
}
