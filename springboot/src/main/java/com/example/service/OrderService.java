package com.example.service;

import com.example.common.enums.OrderStatus;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.controller.request.OrderStatusRequest;
import com.example.controller.request.PlaceOrderRequest;
import com.example.entity.Account;
import com.example.entity.CustomerOrder;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PLACED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Resource
    private GoodsService goodsService;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private OrderMapper orderMapper;

    @Transactional
    public CustomerOrder placeOrder(PlaceOrderRequest request) {
        Account account = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(account.getRole())) {
            throw new CustomException(ResultCodeEnum.ORDER_ACCESS_DENIED);
        }
        if (request.quantity() == null || request.quantity() < 1 || request.quantity() > 99) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }

        Goods goods = goodsService.selectPurchasableById(request.goodsId());
        if (goodsMapper.decreaseStock(goods.getId(), request.quantity()) != 1) {
            throw new CustomException(ResultCodeEnum.INSUFFICIENT_STOCK);
        }

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(createOrderNumber());
        order.setGoodsId(goods.getId());
        order.setUserId(account.getId());
        order.setBusinessId(goods.getBusinessId());
        order.setProductName(goods.getName());
        order.setProductImg(goods.getImg());
        order.setQuantity(request.quantity());
        order.setUnitPrice(goods.getPrice());
        order.setTotalPrice(goods.getPrice().multiply(BigDecimal.valueOf(request.quantity())));
        order.setStatus(OrderStatus.PLACED.name());
        orderMapper.insert(order);
        return order;
    }

    public PageInfo<CustomerOrder> selectPage(Integer pageNum, Integer pageSize) {
        Account account = TokenUtils.getCurrentUser();
        CustomerOrder filter = new CustomerOrder();
        if (RoleEnum.USER.name().equals(account.getRole())) {
            filter.setUserId(account.getId());
        } else if (RoleEnum.BUSINESS.name().equals(account.getRole())) {
            filter.setBusinessId(account.getId());
        } else if (!RoleEnum.ADMIN.name().equals(account.getRole())) {
            throw new CustomException(ResultCodeEnum.ORDER_ACCESS_DENIED);
        }

        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(orderMapper.selectAll(filter));
    }

    @Transactional
    public void updateStatus(OrderStatusRequest request) {
        Account account = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(account.getRole())
                && !RoleEnum.BUSINESS.name().equals(account.getRole())) {
            throw new CustomException(ResultCodeEnum.ORDER_ACCESS_DENIED);
        }

        CustomerOrder order = orderMapper.selectById(request.id());
        if (order == null) {
            throw new CustomException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        if (RoleEnum.BUSINESS.name().equals(account.getRole())
                && !Objects.equals(account.getId(), order.getBusinessId())) {
            throw new CustomException(ResultCodeEnum.ORDER_ACCESS_DENIED);
        }

        OrderStatus current;
        try {
            current = OrderStatus.valueOf(order.getStatus());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new CustomException(ResultCodeEnum.INVALID_ORDER_STATUS);
        }
        if (!ALLOWED_TRANSITIONS.get(current).contains(request.status())) {
            throw new CustomException(ResultCodeEnum.INVALID_ORDER_STATUS);
        }

        if (request.status() == OrderStatus.CANCELLED && order.getGoodsId() != null) {
            goodsMapper.increaseStock(order.getGoodsId(), order.getQuantity());
        }
        orderMapper.updateStatus(order.getId(), request.status().name());
    }

    private String createOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "NB-" + date + "-" + random;
    }
}
