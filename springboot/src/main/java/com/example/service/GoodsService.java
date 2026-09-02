package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.common.enums.StatusEnum;
import com.example.entity.Account;
import com.example.entity.Business;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.BusinessMapper;
import com.example.mapper.GoodsMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Product business logic.
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private BusinessMapper businessMapper;

    /**
     * Create a record.
     */
    public void add(Goods goods) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            requireApprovedBusiness(currentUser);
            goods.setBusinessId(currentUser.getId());
        }
        goodsMapper.insert(goods);
    }

    /**
     * Delete a record.
     */
    public void deleteById(Integer id) {
        requireBusinessOwnership(id);
        goodsMapper.deleteById(id);
    }

    /**
     * Delete multiple records.
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            requireBusinessOwnership(id);
        }
        for (Integer id : ids) {
            goodsMapper.deleteById(id);
        }
    }

    /**
     * Update a record.
     */
    public void updateById(Goods goods) {
        requireBusinessOwnership(goods.getId());
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        goodsMapper.updateById(goods);
    }

    /**
     * Find a record by ID.
     */
    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    /**
     * Find all matching records.
     */
    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    /**
     * Find records with pagination.
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

    public List<Goods> selectByTypeId(Integer id) {
        return goodsMapper.selectByTypeId(id);
    }

    public List<Goods> selectByBusinessId(Integer id) {
        return goodsMapper.selectByBusinessId(id);
    }

    public List<Goods> selectByName(String name) {
        return goodsMapper.selectByName(name);
    }

    public List<Goods> featured() {
        return goodsMapper.selectFeatured(10);
    }

    private void requireBusinessOwnership(Integer goodsId) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            return;
        }
        requireApprovedBusiness(currentUser);
        Goods storedGoods = goodsMapper.selectById(goodsId);
        if (storedGoods == null || !Objects.equals(storedGoods.getBusinessId(), currentUser.getId())) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
        }
    }

    private void requireApprovedBusiness(Account currentUser) {
        Business business = businessMapper.selectById(currentUser.getId());
        if (business == null || !StatusEnum.APPROVED.code().equals(business.getStatus())) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
        }
    }
}
