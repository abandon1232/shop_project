package com.example.service;

import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Business;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.BusinessMapper;
import com.example.mapper.GoodsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {

    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private BusinessMapper businessMapper;

    @InjectMocks
    private GoodsService service;

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void batchDeleteChecksEveryProductBeforeDeletingAny() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(businessMapper.selectById(7)).thenReturn(business(7, "APPROVED"));
        when(goodsMapper.selectById(11)).thenReturn(goods(11, 7));
        when(goodsMapper.selectById(12)).thenReturn(goods(12, 9));

        CustomException error = assertThrows(CustomException.class,
                () -> service.deleteBatch(List.of(11, 12)));

        assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
        verify(goodsMapper, never()).deleteById(anyInt());
    }

    @Test
    void pendingBusinessCannotCreateProduct() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(businessMapper.selectById(7)).thenReturn(business(7, "PENDING"));

        CustomException error = assertThrows(CustomException.class,
                () -> service.add(new Goods()));

        assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
        verify(goodsMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approvedBusinessCreatesProductUnderOwnAccount() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(businessMapper.selectById(7)).thenReturn(business(7, "APPROVED"));
        Goods goods = new Goods();

        service.add(goods);

        assertEquals(7, goods.getBusinessId());
        verify(goodsMapper).insert(goods);
    }

    @Test
    void pendingBusinessCannotDeleteOwnedProduct() {
        bindAccount(7, RoleEnum.BUSINESS);
        when(businessMapper.selectById(7)).thenReturn(business(7, "PENDING"));

        CustomException error = assertThrows(CustomException.class,
                () -> service.deleteById(11));

        assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
        verify(goodsMapper, never()).deleteById(anyInt());
    }

    @Test
    void featuredProductsUseSimpleDeterministicQuery() {
        List<Goods> stored = List.of(goods(20, 7), goods(19, 7));
        when(goodsMapper.selectFeatured(10)).thenReturn(stored);

        assertSame(stored, service.featured());
        verify(goodsMapper).selectFeatured(10);
    }

    @Test
    void publicDetailUsesPurchasableProductQuery() {
        Goods stored = goods(20, 7);
        when(goodsMapper.selectPurchasableById(20)).thenReturn(stored);

        assertSame(stored, service.selectPurchasableById(20));
        verify(goodsMapper).selectPurchasableById(20);
    }

    @Test
    void publicDetailRejectsUnavailableProduct() {
        when(goodsMapper.selectPurchasableById(20)).thenReturn(null);

        CustomException error = assertThrows(CustomException.class,
                () -> service.selectPurchasableById(20));

        assertEquals("4041", error.getCode());
    }

    private Goods goods(int id, int businessId) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setBusinessId(businessId);
        return goods;
    }

    private Business business(int id, String status) {
        Business business = new Business();
        business.setId(id);
        business.setStatus(status);
        return business;
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
