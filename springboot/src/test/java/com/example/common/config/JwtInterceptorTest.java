package com.example.common.config;

import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.BusinessService;
import com.example.service.UserService;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtInterceptorTest {

    @Mock
    private AdminService adminService;
    @Mock
    private BusinessService businessService;
    @Mock
    private UserService userService;
    @InjectMocks
    private JwtInterceptor interceptor;

    @Test
    void rejectsTokenProvidedOnlyAsQueryParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(Constants.TOKEN, "query-token");

        CustomException error = assertThrows(CustomException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertEquals(ResultCodeEnum.TOKEN_INVALID_ERROR.code, error.getCode());
        assertEquals(ResultCodeEnum.TOKEN_INVALID_ERROR.msg, error.getMsg());
    }

    @Test
    void storesTheVerifiedAccountForDownstreamAuthorization() {
        Admin account = new Admin();
        account.setId(7);
        account.setRole(RoleEnum.ADMIN.name());
        account.setPassword("stored-password-hash");
        String token = TokenUtils.createToken("7-ADMIN", account.getPassword());
        when(adminService.selectById(7)).thenReturn(account);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.TOKEN, token);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertSame(account, request.getAttribute("currentUser"));
    }
}
