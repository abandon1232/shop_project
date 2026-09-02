package com.example.service;

import com.example.common.Constants;
import com.example.common.enums.OrderStatus;
import com.example.common.enums.RoleEnum;
import com.example.controller.request.OrderStatusRequest;
import com.example.controller.request.PlaceOrderRequest;
import com.example.entity.Account;
import com.example.entity.CustomerOrder;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private GoodsService goodsService;
    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService service;

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void customerOrderUsesServerPriceAndDecreasesStock() {
        bindAccount(3, RoleEnum.USER);
        Goods goods = goods(7, 9, "QuietWave Wireless Headphones", "headphones.webp", "1990.00");
        when(goodsService.selectPurchasableById(7)).thenReturn(goods);
        when(goodsMapper.decreaseStock(7, 2)).thenReturn(1);

        CustomerOrder result = service.placeOrder(new PlaceOrderRequest(7, 2));

        ArgumentCaptor<CustomerOrder> captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(orderMapper).insert(captor.capture());
        CustomerOrder inserted = captor.getValue();
        assertEquals(3, inserted.getUserId());
        assertEquals(9, inserted.getBusinessId());
        assertEquals(7, inserted.getGoodsId());
        assertEquals("QuietWave Wireless Headphones", inserted.getProductName());
        assertEquals("headphones.webp", inserted.getProductImg());
        assertEquals(2, inserted.getQuantity());
        assertEquals(new BigDecimal("1990.00"), inserted.getUnitPrice());
        assertEquals(new BigDecimal("3980.00"), inserted.getTotalPrice());
        assertEquals(OrderStatus.PLACED.name(), inserted.getStatus());
        assertTrue(inserted.getOrderNumber().startsWith("NB-"));
        assertEquals(inserted, result);
        verify(goodsMapper).decreaseStock(7, 2);
    }

    @Test
    void insufficientStockDoesNotInsertOrder() {
        bindAccount(3, RoleEnum.USER);
        when(goodsService.selectPurchasableById(7)).thenReturn(
                goods(7, 9, "QuietWave Wireless Headphones", "headphones.webp", "1990.00"));
        when(goodsMapper.decreaseStock(7, 8)).thenReturn(0);

        CustomException error = assertThrows(CustomException.class,
                () -> service.placeOrder(new PlaceOrderRequest(7, 8)));

        assertEquals("4091", error.getCode());
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void nonCustomerCannotPlaceOrder() {
        bindAccount(1, RoleEnum.ADMIN);

        CustomException error = assertThrows(CustomException.class,
                () -> service.placeOrder(new PlaceOrderRequest(7, 1)));

        assertEquals("4031", error.getCode());
        verify(goodsService, never()).selectPurchasableById(7);
    }

    @Test
    void sellerPageIsRestrictedToAuthenticatedSellerId() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(orderMapper.selectAll(any())).thenReturn(List.of());

        service.selectPage(1, 10);

        ArgumentCaptor<CustomerOrder> captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(orderMapper).selectAll(captor.capture());
        assertEquals(7, captor.getValue().getBusinessId());
        assertEquals(null, captor.getValue().getUserId());
    }

    @Test
    void customerPageIsRestrictedToAuthenticatedCustomerId() {
        bindAccount(12, RoleEnum.USER);
        when(orderMapper.selectAll(any())).thenReturn(List.of());

        service.selectPage(1, 10);

        ArgumentCaptor<CustomerOrder> captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(orderMapper).selectAll(captor.capture());
        assertEquals(12, captor.getValue().getUserId());
        assertEquals(null, captor.getValue().getBusinessId());
    }

    @Test
    void sellerCannotUpdateAnotherSellersOrder() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(orderMapper.selectById(41)).thenReturn(order(41, 9, 7, 2, OrderStatus.PLACED));

        CustomException error = assertThrows(CustomException.class,
                () -> service.updateStatus(new OrderStatusRequest(41, OrderStatus.PROCESSING)));

        assertEquals("4031", error.getCode());
        verify(orderMapper, never()).updateStatus(any(), any(), any());
    }

    @Test
    void placedOrderCanMoveToProcessing() {
        bindAccount(1, RoleEnum.ADMIN);
        when(orderMapper.selectById(41)).thenReturn(order(41, 9, 7, 2, OrderStatus.PLACED));
        when(orderMapper.updateStatus(41, OrderStatus.PLACED.name(), OrderStatus.PROCESSING.name()))
                .thenReturn(1);

        service.updateStatus(new OrderStatusRequest(41, OrderStatus.PROCESSING));

        verify(orderMapper).updateStatus(41, OrderStatus.PLACED.name(), OrderStatus.PROCESSING.name());
        verify(goodsMapper, never()).increaseStock(any(), any());
    }

    @Test
    void shippedOrderCannotBeCancelled() {
        bindAccount(1, RoleEnum.ADMIN);
        when(orderMapper.selectById(41)).thenReturn(order(41, 9, 7, 2, OrderStatus.SHIPPED));

        CustomException error = assertThrows(CustomException.class,
                () -> service.updateStatus(new OrderStatusRequest(41, OrderStatus.CANCELLED)));

        assertEquals("4092", error.getCode());
        verify(orderMapper, never()).updateStatus(any(), any(), any());
        verify(goodsMapper, never()).increaseStock(any(), any());
    }

    @Test
    void cancellingPlacedOrderRestoresStockOnce() {
        bindAccount(1, RoleEnum.ADMIN);
        when(orderMapper.selectById(41)).thenReturn(order(41, 9, 7, 2, OrderStatus.PLACED));
        when(orderMapper.updateStatus(41, OrderStatus.PLACED.name(), OrderStatus.CANCELLED.name()))
                .thenReturn(1);

        service.updateStatus(new OrderStatusRequest(41, OrderStatus.CANCELLED));

        InOrder changes = inOrder(goodsMapper, orderMapper);
        changes.verify(orderMapper).updateStatus(41, OrderStatus.PLACED.name(), OrderStatus.CANCELLED.name());
        changes.verify(goodsMapper).increaseStock(7, 2);
    }

    @Test
    void failedConcurrentCancellationDoesNotRestoreStock() {
        bindAccount(1, RoleEnum.ADMIN);
        when(orderMapper.selectById(41)).thenReturn(order(41, 9, 7, 2, OrderStatus.PLACED));
        when(orderMapper.updateStatus(41, OrderStatus.PLACED.name(), OrderStatus.CANCELLED.name()))
                .thenReturn(0);

        CustomException error = assertThrows(CustomException.class,
                () -> service.updateStatus(new OrderStatusRequest(41, OrderStatus.CANCELLED)));

        assertEquals("4092", error.getCode());
        verify(goodsMapper, never()).increaseStock(any(), any());
    }

    private Goods goods(int id, int businessId, String name, String img, String price) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setBusinessId(businessId);
        goods.setName(name);
        goods.setImg(img);
        goods.setPrice(new BigDecimal(price));
        return goods;
    }

    private CustomerOrder order(int id, int businessId, int goodsId, int quantity, OrderStatus status) {
        CustomerOrder order = new CustomerOrder();
        order.setId(id);
        order.setBusinessId(businessId);
        order.setGoodsId(goodsId);
        order.setQuantity(quantity);
        order.setStatus(status.name());
        return order;
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
