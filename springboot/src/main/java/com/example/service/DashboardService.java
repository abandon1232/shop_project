package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.controller.response.DashboardSummary;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.mapper.BusinessMapper;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.TypeMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private BusinessMapper businessMapper;

    public DashboardSummary summary() {
        Account account = TokenUtils.getCurrentUser();
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            return new DashboardSummary(
                    goodsMapper.countAll(),
                    typeMapper.countAll(),
                    orderMapper.countAll(),
                    userMapper.countAll(),
                    businessMapper.countAll(),
                    goodsMapper.countLowStock(),
                    zeroIfNull(orderMapper.revenueAll()));
        }
        if (RoleEnum.BUSINESS.name().equals(account.getRole())) {
            Integer businessId = account.getId();
            return new DashboardSummary(
                    goodsMapper.countByBusinessId(businessId),
                    0,
                    orderMapper.countByBusinessId(businessId),
                    0,
                    0,
                    goodsMapper.countLowStockByBusinessId(businessId),
                    zeroIfNull(orderMapper.revenueByBusinessId(businessId)));
        }
        throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
