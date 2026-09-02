package com.example.service;

import com.example.common.Constants;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
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
        var sequence = inOrder(cartMapper);
        sequence.verify(cartMapper).lockUserById(3);
        sequence.verify(cartMapper).selectByUserAndGoods(3, 7);
        verify(cartMapper).updateQuantity(14, 3, 5);
        verify(cartMapper, never()).insert(any());
    }

    @Test
    void addingLocksTheCustomerBeforeLoadingProductState() {
        bindAccount(3, RoleEnum.USER);
        Goods goods = goods(7, 8, "QuietWave Headphones", "1990.00");
        CartItem existing = cartItem(14, 3, 7, 2);
        when(goodsService.selectPurchasableById(7)).thenReturn(goods);
        when(cartMapper.selectByUserAndGoods(3, 7)).thenReturn(existing);
        when(cartMapper.updateQuantity(14, 3, 3)).thenReturn(1);
        when(cartMapper.selectOwnedById(14, 3)).thenReturn(cartItem(14, 3, 7, 3));

        service.add(new AddCartItemRequest(7, 1));

        var sequence = inOrder(cartMapper, goodsService);
        sequence.verify(cartMapper).lockUserById(3);
        sequence.verify(goodsService).selectPurchasableById(7);
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
        verify(cartMapper).lockUserById(3);
        verify(cartMapper, never()).selectByUserAndGoods(any(), any());
    }

    @Test
    void sellerCannotReadCustomerCart() {
        bindAccount(7, RoleEnum.BUSINESS);

        CustomException error = assertThrows(CustomException.class, service::selectCurrentCart);

        assertEquals("4032", error.getCode());
        verifyNoInteractions(cartMapper);
    }

    @Test
    void customerReadsOnlyTheirCurrentCart() {
        bindAccount(3, RoleEnum.USER);
        List<CartItem> savedCart = List.of(cartItem(14, 3, 7, 2));
        when(cartMapper.selectByUserId(3)).thenReturn(savedCart);

        List<CartItem> result = service.selectCurrentCart();

        assertSame(savedCart, result);
        verify(cartMapper).selectByUserId(3);
        verify(cartMapper, never()).lockUserById(any());
    }

    @Test
    void addingNewProductInsertsForCurrentCustomerAndReturnsGeneratedLine() {
        bindAccount(3, RoleEnum.USER);
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 8, "QuietWave Headphones", "1990.00"));
        when(cartMapper.selectByUserAndGoods(3, 7)).thenReturn(null);
        doAnswer(invocation -> {
            invocation.getArgument(0, CartItem.class).setId(41);
            return 1;
        }).when(cartMapper).insert(any(CartItem.class));
        CartItem persisted = cartItem(41, 3, 7, 2);
        when(cartMapper.selectOwnedById(41, 3)).thenReturn(persisted);

        CartItem result = service.add(new AddCartItemRequest(7, 2));

        ArgumentCaptor<CartItem> inserted = ArgumentCaptor.forClass(CartItem.class);
        verify(cartMapper).insert(inserted.capture());
        assertEquals(3, inserted.getValue().getUserId());
        assertEquals(7, inserted.getValue().getGoodsId());
        assertEquals(2, inserted.getValue().getQuantity());
        verify(cartMapper).selectOwnedById(41, 3);
        assertSame(persisted, result);
    }

    @Test
    void customerCanUpdateTheirOwnedLineWithinStock() {
        bindAccount(3, RoleEnum.USER);
        CartItem existing = cartItem(99, 3, 7, 1);
        CartItem updated = cartItem(99, 3, 7, 2);
        when(cartMapper.selectOwnedById(99, 3)).thenReturn(existing, updated);
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 8, "QuietWave Headphones", "1990.00"));
        when(cartMapper.updateQuantity(99, 3, 2)).thenReturn(1);

        CartItem result = service.update(99, new UpdateCartItemRequest(2));

        assertSame(updated, result);
        var sequence = inOrder(cartMapper);
        sequence.verify(cartMapper).lockUserById(3);
        sequence.verify(cartMapper).selectOwnedById(99, 3);
        verify(cartMapper).updateQuantity(99, 3, 2);
    }

    @Test
    void updatingBeyondCurrentStockDoesNotMutateOwnedLine() {
        bindAccount(3, RoleEnum.USER);
        when(cartMapper.selectOwnedById(99, 3)).thenReturn(cartItem(99, 3, 7, 1));
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 1, "QuietWave Headphones", "1990.00"));

        CustomException error = assertThrows(CustomException.class,
                () -> service.update(99, new UpdateCartItemRequest(2)));

        assertEquals("4091", error.getCode());
        verify(cartMapper).selectOwnedById(99, 3);
        verify(cartMapper, never()).updateQuantity(any(), any(), any());
    }

    @Test
    void customerCanDeleteTheirOwnedLine() {
        bindAccount(3, RoleEnum.USER);
        when(cartMapper.deleteOwned(99, 3)).thenReturn(1);

        service.delete(99);

        var sequence = inOrder(cartMapper);
        sequence.verify(cartMapper).lockUserById(3);
        sequence.verify(cartMapper).deleteOwned(99, 3);
        verify(cartMapper).deleteOwned(99, 3);
    }

    @Test
    void deletingMissingOrForeignLineReturnsNotFound() {
        bindAccount(3, RoleEnum.USER);
        when(cartMapper.deleteOwned(99, 3)).thenReturn(0);

        CustomException error = assertThrows(CustomException.class, () -> service.delete(99));

        assertEquals("4043", error.getCode());
        verify(cartMapper).deleteOwned(99, 3);
    }

    @Test
    void checkoutCreatesOneOrderPerLineThenClearsCart() {
        bindAccount(3, RoleEnum.USER);
        CartItem first = cartItem(14, 3, 7, 2);
        CartItem second = cartItem(15, 3, 8, 1);
        when(cartMapper.selectByUserId(3)).thenReturn(List.of(first, second));
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 6, "Headphones", "1990.00"));
        when(goodsService.selectPurchasableById(8)).thenReturn(goods(8, 4, "Keyboard", "990.00"));
        CustomerOrder firstOrder = new CustomerOrder();
        CustomerOrder secondOrder = new CustomerOrder();
        when(orderService.placeOrder(new PlaceOrderRequest(7, 2))).thenReturn(firstOrder);
        when(orderService.placeOrder(new PlaceOrderRequest(8, 1))).thenReturn(secondOrder);
        when(cartMapper.deleteByUserId(3)).thenReturn(2);

        List<CustomerOrder> result = service.checkout();

        assertEquals(List.of(firstOrder, secondOrder), result);
        var sequence = inOrder(orderService, cartMapper);
        sequence.verify(cartMapper).lockUserById(3);
        sequence.verify(cartMapper).selectByUserId(3);
        sequence.verify(orderService).placeOrder(new PlaceOrderRequest(7, 2));
        sequence.verify(orderService).placeOrder(new PlaceOrderRequest(8, 1));
        sequence.verify(cartMapper).deleteByUserId(3);
    }

    @Test
    void checkoutRejectsADeleteCountThatDoesNotMatchItsClaimedSnapshot() {
        bindAccount(3, RoleEnum.USER);
        CartItem line = cartItem(14, 3, 7, 1);
        when(cartMapper.selectByUserId(3)).thenReturn(List.of(line));
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 6, "Headphones", "1990.00"));
        when(orderService.placeOrder(new PlaceOrderRequest(7, 1))).thenReturn(new CustomerOrder());
        when(cartMapper.deleteByUserId(3)).thenReturn(0);

        CustomException error = assertThrows(CustomException.class, service::checkout);

        assertEquals("4094", error.getCode());
    }

    @Test
    void failedCheckoutDoesNotClearCart() {
        bindAccount(3, RoleEnum.USER);
        CartItem line = cartItem(14, 3, 7, 3);
        when(cartMapper.selectByUserId(3)).thenReturn(List.of(line));
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 2, "Headphones", "1990.00"));

        CustomException error = assertThrows(CustomException.class, service::checkout);

        assertEquals("4091", error.getCode());
        verifyNoInteractions(orderService);
        verify(cartMapper, never()).deleteByUserId(any());
    }

    @Test
    void checkoutPrevalidatesEveryLineBeforePlacingAnyOrder() {
        bindAccount(3, RoleEnum.USER);
        CartItem first = cartItem(14, 3, 7, 1);
        CartItem unavailableSecond = cartItem(15, 3, 8, 2);
        when(cartMapper.selectByUserId(3)).thenReturn(List.of(first, unavailableSecond));
        when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 1, "Headphones", "1990.00"));
        when(goodsService.selectPurchasableById(8)).thenReturn(goods(8, 1, "Keyboard", "990.00"));

        CustomException error = assertThrows(CustomException.class, service::checkout);

        assertEquals("4091", error.getCode());
        verifyNoInteractions(orderService);
        verify(cartMapper, never()).deleteByUserId(any());
    }

    @Test
    void emptyCartCannotCheckout() {
        bindAccount(3, RoleEnum.USER);
        when(cartMapper.selectByUserId(3)).thenReturn(List.of());

        CustomException error = assertThrows(CustomException.class, service::checkout);

        assertEquals("4093", error.getCode());
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

    private void bindAccount(int id, RoleEnum role) {
        Account account = new Account();
        account.setId(id);
        account.setRole(role.name());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(Constants.CURRENT_USER, account);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
