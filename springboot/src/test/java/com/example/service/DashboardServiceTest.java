package com.example.service;

import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.controller.response.DashboardSummary;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.mapper.BusinessMapper;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.TypeMapper;
import com.example.mapper.UserMapper;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private TypeMapper typeMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private BusinessMapper businessMapper;

    @InjectMocks
    private DashboardService service;

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void administratorReceivesGlobalSummary() {
        bindAccount(1, RoleEnum.ADMIN);
        when(goodsMapper.countAll()).thenReturn(18L);
        when(typeMapper.countAll()).thenReturn(6L);
        when(orderMapper.countAll()).thenReturn(12L);
        when(userMapper.countAll()).thenReturn(8L);
        when(businessMapper.countAll()).thenReturn(2L);
        when(goodsMapper.countLowStock()).thenReturn(3L);
        when(orderMapper.revenueAll()).thenReturn(new BigDecimal("12590.00"));

        DashboardSummary summary = service.summary();

        assertEquals(18L, summary.products());
        assertEquals(6L, summary.categories());
        assertEquals(12L, summary.orders());
        assertEquals(8L, summary.customers());
        assertEquals(2L, summary.sellers());
        assertEquals(3L, summary.lowStockProducts());
        assertEquals(new BigDecimal("12590.00"), summary.revenue());
    }

    @Test
    void sellerReceivesOnlyOwnCommercialSummary() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(goodsMapper.countByBusinessId(7)).thenReturn(5L);
        when(orderMapper.countByBusinessId(7)).thenReturn(4L);
        when(goodsMapper.countLowStockByBusinessId(7)).thenReturn(1L);
        when(orderMapper.revenueByBusinessId(7)).thenReturn(new BigDecimal("2498.00"));

        DashboardSummary summary = service.summary();

        assertEquals(5L, summary.products());
        assertEquals(0L, summary.categories());
        assertEquals(4L, summary.orders());
        assertEquals(0L, summary.customers());
        assertEquals(0L, summary.sellers());
        assertEquals(1L, summary.lowStockProducts());
        assertEquals(new BigDecimal("2498.00"), summary.revenue());
        verifyNoInteractions(typeMapper, userMapper, businessMapper);
    }

    @Test
    void customerCannotReadManagementSummary() {
        bindAccount(9, RoleEnum.USER);

        CustomException error = assertThrows(CustomException.class, service::summary);

        assertEquals("403", error.getCode());
        verifyNoInteractions(goodsMapper, typeMapper, orderMapper, userMapper, businessMapper);
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
