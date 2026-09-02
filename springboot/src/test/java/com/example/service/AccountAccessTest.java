package com.example.service;

import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.Business;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.mapper.BusinessMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAccessTest {

    @Mock
    private AdminMapper adminMapper;
    @Mock
    private BusinessMapper businessMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AdminService adminService;
    @InjectMocks
    private BusinessService businessService;
    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void userCannotReadAnotherUserProfile() {
        bindCurrentAccount(4, RoleEnum.USER);

        CustomException error = assertThrows(CustomException.class,
                () -> userService.selectAccessibleById(5));

        assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
        verify(userMapper, never()).selectById(5);
    }

    @Test
    void userCanReadOwnProfile() {
        bindCurrentAccount(4, RoleEnum.USER);
        User stored = new User();
        stored.setId(4);
        when(userMapper.selectById(4)).thenReturn(stored);

        assertSame(stored, userService.selectAccessibleById(4));
    }

    @Test
    void administratorCanReadAnyBusinessProfile() {
        bindCurrentAccount(1, RoleEnum.ADMIN);
        Business stored = new Business();
        stored.setId(8);
        when(businessMapper.selectById(8)).thenReturn(stored);

        assertSame(stored, businessService.selectAccessibleById(8));
    }

    @Test
    void businessCannotReadAnotherBusinessProfile() {
        bindCurrentAccount(8, RoleEnum.BUSINESS);

        CustomException error = assertThrows(CustomException.class,
                () -> businessService.selectAccessibleById(9));

        assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
        verify(businessMapper, never()).selectById(9);
    }

    @Test
    void accountCreationRequiresSuppliedPassword() {
        Admin admin = new Admin();
        admin.setUsername("admin2");
        Business business = new Business();
        business.setUsername("shop2");
        User user = new User();
        user.setUsername("user2");

        assertMissingPassword(() -> adminService.add(admin));
        assertMissingPassword(() -> businessService.add(business));
        assertMissingPassword(() -> userService.add(user));
    }

    private void assertMissingPassword(Runnable action) {
        CustomException error = assertThrows(CustomException.class, action::run);
        assertEquals(ResultCodeEnum.PARAM_LOST_ERROR.code, error.getCode());
    }

    private void bindCurrentAccount(int id, RoleEnum role) {
        Account account = new Account();
        account.setId(id);
        account.setRole(role.name());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(Constants.CURRENT_USER, account);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
