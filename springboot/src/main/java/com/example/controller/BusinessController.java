package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.entity.Business;
import com.example.service.BusinessService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * REST endpoints for merchants.
 **/
@RestController
@RequestMapping("/business")
public class BusinessController {

    @Resource
    private BusinessService businessService;

    /**
     * Create a record.
     */
    @PostMapping("/add")
    @RequireRoles(RoleEnum.ADMIN)
    public Result add(@RequestBody Business business) {
        businessService.add(business);
        return Result.success();
    }

    /**
     * Delete a record.
     */
    @DeleteMapping("/delete/{id}")
    @RequireRoles(RoleEnum.ADMIN)
    public Result deleteById(@PathVariable Integer id) {
        businessService.deleteById(id);
        return Result.success();
    }

    /**
     * Delete multiple records.
     */
    @DeleteMapping("/delete/batch")
    @RequireRoles(RoleEnum.ADMIN)
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        businessService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update a record.
     */
    @PutMapping("/update")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result updateById(@RequestBody Business business) {
        businessService.updateById(business);
        return Result.success();
    }

    /**
     * Find a record by ID.
     */
    @GetMapping("/selectById/{id}")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result selectById(@PathVariable Integer id) {
        Business business = businessService.selectAccessibleById(id);
        return Result.success(business);
    }

    /**
     * Find all matching records.
     */
    @GetMapping("/selectAll")
    @RequireRoles(RoleEnum.ADMIN)
    public Result selectAll(Business business ) {
        List<Business> list = businessService.selectAll(business);
        return Result.success(list);
    }

    /**
     * Find records with pagination.
     */
    @GetMapping("/selectPage")
    @RequireRoles(RoleEnum.ADMIN)
    public Result selectPage(Business business,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Business> page = businessService.selectPage(business, pageNum, pageSize);
        return Result.success(page);
    }

}
