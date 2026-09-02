package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.controller.request.AddCartItemRequest;
import com.example.controller.request.UpdateCartItemRequest;
import com.example.service.CartService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@Validated
@RequireRoles(RoleEnum.USER)
public class CartController {

    @Resource
    private CartService cartService;

    @GetMapping("/items")
    public Result selectItems() {
        return Result.success(cartService.selectCurrentCart());
    }

    @PostMapping("/items")
    public Result add(@Valid @RequestBody AddCartItemRequest request) {
        return Result.success(cartService.add(request));
    }

    @PutMapping("/items/{id}")
    public Result update(@PathVariable @Positive Integer id,
                         @Valid @RequestBody UpdateCartItemRequest request) {
        return Result.success(cartService.update(id, request));
    }

    @DeleteMapping("/items/{id}")
    public Result delete(@PathVariable @Positive Integer id) {
        cartService.delete(id);
        return Result.success();
    }

    @PostMapping("/checkout")
    public Result checkout() {
        return Result.success(cartService.checkout());
    }
}
