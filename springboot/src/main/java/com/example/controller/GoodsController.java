package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.controller.request.GoodsRequest;
import com.example.entity.Goods;
import com.example.service.GoodsService;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * REST endpoints for products.
 **/
@RestController
@RequestMapping("/goods")
@Validated
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * Create a record.
     */
    @PostMapping("/add")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result add(@Valid @RequestBody GoodsRequest request) {
        goodsService.add(request.toGoods());
        return Result.success();
    }

    /**
     * Delete a record.
     */
    @DeleteMapping("/delete/{id}")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result deleteById(@PathVariable Integer id) {
        goodsService.deleteById(id);
        return Result.success();
    }

    /**
     * Delete multiple records.
     */
    @DeleteMapping("/delete/batch")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        goodsService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update a record.
     */
    @PutMapping("/update")
    @RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
    public Result updateById(@Valid @RequestBody GoodsRequest request) {
        if (request.id() == null) {
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }
        goodsService.updateById(request.toGoods());
        return Result.success();
    }

    @GetMapping("/selectByTypeId")
    public Result selectByTypeId(@RequestParam Integer id) {
        List<Goods> list = goodsService.selectByTypeId(id);
        return Result.success(list);
    }
    @GetMapping("/selectByName")
    public Result selectByName(@RequestParam String name) {
        List<Goods> list = goodsService.selectByName(name);
        return Result.success(list);
    }

    @GetMapping("/selectByBusinessId")
    public Result selectByBusinessId(@RequestParam Integer id) {
        List<Goods> list = goodsService.selectByBusinessId(id);
        return Result.success(list);
    }
    /**
     * Find a record by ID.
     */
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        Goods goods = goodsService.selectById(id);
        return Result.success(goods);
    }

    /**
     * Find all matching records.
     */
    @GetMapping("/selectAll")
    public Result selectAll(Goods goods ) {
        List<Goods> list = goodsService.selectAll(goods);
        return Result.success(list);
    }
    @GetMapping("/featured")
    public Result featured(){
        List<Goods> list = goodsService.featured();
        return Result.success(list);
    }
    /**
     * Find records with pagination.
     */
    @GetMapping("/selectPage")
    public Result selectPage(Goods goods,
                             @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
                             @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
        PageInfo<Goods> page = goodsService.selectPage(goods, pageNum, pageSize);
        return Result.success(page);
    }

}
