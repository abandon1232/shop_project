package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.controller.request.AddCartItemRequest;
import com.example.controller.request.PlaceOrderRequest;
import com.example.controller.request.UpdateCartItemRequest;
import com.example.entity.Account;
import com.example.entity.CartItem;
import com.example.entity.CustomerOrder;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.CartMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private static final int MAX_QUANTITY = 99;

    @Resource
    private CartMapper cartMapper;
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;

    public List<CartItem> selectCurrentCart() {
        Account account = currentCustomer();
        return cartMapper.selectByUserId(account.getId());
    }

    @Transactional
    public CartItem add(AddCartItemRequest request) {
        Account account = currentCustomer();
        validateQuantity(request == null ? null : request.quantity());
        Goods goods = goodsService.selectPurchasableById(request.goodsId());
        validateStock(goods, request.quantity());

        cartMapper.lockUserById(account.getId());
        CartItem existing = cartMapper.selectByUserAndGoods(account.getId(), goods.getId());
        int quantity = request.quantity();
        if (existing != null) {
            quantity += existing.getQuantity();
            validateStock(goods, quantity);
            if (cartMapper.updateQuantity(existing.getId(), account.getId(), quantity) != 1) {
                throw new CustomException(ResultCodeEnum.CART_ITEM_NOT_FOUND);
            }
            return selectOwned(existing.getId(), account.getId());
        }

        CartItem item = new CartItem();
        item.setUserId(account.getId());
        item.setGoodsId(goods.getId());
        item.setQuantity(quantity);
        if (cartMapper.insert(item) != 1) {
            throw new CustomException(ResultCodeEnum.CART_ITEM_NOT_FOUND);
        }
        return selectOwned(item.getId(), account.getId());
    }

    @Transactional
    public CartItem update(Integer id, UpdateCartItemRequest request) {
        Account account = currentCustomer();
        validateQuantity(request == null ? null : request.quantity());
        cartMapper.lockUserById(account.getId());
        CartItem item = selectOwned(id, account.getId());
        Goods goods = goodsService.selectPurchasableById(item.getGoodsId());
        validateStock(goods, request.quantity());
        if (cartMapper.updateQuantity(id, account.getId(), request.quantity()) != 1) {
            throw new CustomException(ResultCodeEnum.CART_ITEM_NOT_FOUND);
        }
        return selectOwned(id, account.getId());
    }

    @Transactional
    public void delete(Integer id) {
        Account account = currentCustomer();
        cartMapper.lockUserById(account.getId());
        if (cartMapper.deleteOwned(id, account.getId()) != 1) {
            throw new CustomException(ResultCodeEnum.CART_ITEM_NOT_FOUND);
        }
    }

    @Transactional
    public List<CustomerOrder> checkout() {
        Account account = currentCustomer();
        cartMapper.lockUserById(account.getId());
        List<CartItem> items = cartMapper.selectByUserId(account.getId());
        if (items.isEmpty()) {
            throw new CustomException(ResultCodeEnum.CART_EMPTY);
        }

        for (CartItem item : items) {
            Goods goods = goodsService.selectPurchasableById(item.getGoodsId());
            validateStock(goods, item.getQuantity());
        }

        List<CustomerOrder> orders = new ArrayList<>();
        for (CartItem item : items) {
            orders.add(orderService.placeOrder(new PlaceOrderRequest(item.getGoodsId(), item.getQuantity())));
        }
        if (cartMapper.deleteByUserId(account.getId()) != items.size()) {
            throw new CustomException(ResultCodeEnum.CART_CONFLICT);
        }
        return orders;
    }

    private Account currentCustomer() {
        Account account = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(account.getRole())) {
            throw new CustomException(ResultCodeEnum.CART_ACCESS_DENIED);
        }
        return account;
    }

    private CartItem selectOwned(Integer id, Integer userId) {
        CartItem item = cartMapper.selectOwnedById(id, userId);
        if (item == null) {
            throw new CustomException(ResultCodeEnum.CART_ITEM_NOT_FOUND);
        }
        return item;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > MAX_QUANTITY) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    private void validateStock(Goods goods, int quantity) {
        if (quantity > MAX_QUANTITY || goods.getCount() == null || quantity > goods.getCount()) {
            throw new CustomException(ResultCodeEnum.INSUFFICIENT_STOCK);
        }
    }
}
