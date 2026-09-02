package com.example.service;

import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.controller.request.AddCartItemRequest;
import com.example.controller.request.UpdateCartItemRequest;
import com.example.entity.Account;
import com.example.entity.CartItem;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.CartMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock private CartMapper cartMapper;
    @Mock private GoodsService goodsService;
    @Mock private OrderService orderService;
    @InjectMocks private CartService service;

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void addingExistingProductIncrementsOwnedLine() {
        bindAccount(3, RoleEnum.USER);
        Goods goods = goods(7, 8, "QuietWave Headphones", "1990.00");
        CartItem existing = cartItem(14, 3, 7, 2);
        when(goodsService.selectPurchasableById(7)).thenReturn(goods);
        when(cartMapper.selectByUserAndGoods(3, 7)).thenReturn(existing);
        when(cartMapper.updateQuantity(14, 3, 5)).thenReturn(1);
        when(cartMapper.selectOwnedById(14, 3)).thenReturn(cartItem(14, 3, 7, 5));

        CartItem result = service.add(new AddCartItemRequest(7, 3));

        assertEquals(5, result.getQuantity());
        verify(cartMapper).updateQuantity(14, 3, 5);
        verify(cartMapper, never()).insert(any());
    }

    @Test
    void customerCannotUpdateAnotherCustomersLine() {
        bindAccount(3, RoleEnum.USER);
        when(cartMapper.selectOwnedById(99, 3)).thenReturn(null);

        CustomException error = assertThrows(CustomException.class,
                () -> service.update(99, new UpdateCartItemRequest(2)));

        assertEquals("4043", error.getCode());
        verify(cartMapper, never()).updateQuantity(any(), any(), any());
    }

    @Test
    void addingMoreThanCurrentStockIsRejected() {
        bindAccount(3, RoleEnum.USER);
        when(goodsService.selectPurchasableById(7))
                .thenReturn(goods(7, 2, "QuietWave Headphones", "1990.00"));

        CustomException error = assertThrows(CustomException.class,
                () -> service.add(new AddCartItemRequest(7, 3)));

        assertEquals("4091", error.getCode());
        verifyNoInteractions(cartMapper);
    }

    @Test
    void sellerCannotReadCustomerCart() {
        bindAccount(7, RoleEnum.BUSINESS);

        CustomException error = assertThrows(CustomException.class, service::selectCurrentCart);

        assertEquals("4032", error.getCode());
        verifyNoInteractions(cartMapper);
    }

    private Goods goods(int id, int stock, String name, String price) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setCount(stock);
        goods.setName(name);
        goods.setPrice(new BigDecimal(price));
        return goods;
    }

    private CartItem cartItem(int id, int userId, int goodsId, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setGoodsId(goodsId);
        item.setQuantity(quantity);
        return item;
    }

    private CartItem detailedCartItem(int id, int userId, int goodsId, int quantity, int stock) {
        CartItem item = cartItem(id, userId, goodsId, quantity);
        item.setStock(stock);
        return item;
    }

    private void bindAccount(int id, RoleEnum role) {
        Account account = new Account();
        account.setId(id);
        account.setRole(role.name());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(Constants.CURRENT_USER, account);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
