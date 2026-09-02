package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * REST endpoints for merchants.
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * Create a record.
     */
    @PostMapping("/add")
    @RequireRoles(RoleEnum.ADMIN)
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * Delete a record.
     */
    @DeleteMapping("/delete/{id}")
    @RequireRoles(RoleEnum.ADMIN)
    public Result deleteById(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * Delete multiple records.
     */
    @DeleteMapping("/delete/batch")
    @RequireRoles(RoleEnum.ADMIN)
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update a record.
     */
    @PutMapping("/update")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.USER})
    public Result updateById(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * Find a record by ID.
     */
    @GetMapping("/selectById/{id}")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.USER})
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectAccessibleById(id);
        return Result.success(user);
    }

    /**
     * Find all matching records.
     */
    @GetMapping("/selectAll")
    @RequireRoles(RoleEnum.ADMIN)
    public Result selectAll(User user ) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * Find records with pagination.
     */
    @GetMapping("/selectPage")
    @RequireRoles(RoleEnum.ADMIN)
    public Result selectPage(User user,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> page = userService.selectPage(user, pageNum, pageSize);
        return Result.success(page);
    }

}
