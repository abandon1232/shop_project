package com.example.utils;

import com.auth0.jwt.JWT;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenUtilsTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void returnsTheAccountStoredByTheInterceptor() {
        Account account = new Account();
        account.setId(7);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUser", account);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertSame(account, TokenUtils.getCurrentUser());
    }

    @Test
    void failsClosedWhenNoAuthenticatedAccountExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        CustomException error = assertThrows(CustomException.class, TokenUtils::getCurrentUser);

        assertEquals(ResultCodeEnum.USER_NOT_LOGIN.code, error.getCode());
    }

    @Test
    void createsATokenThatExpiresAfterAboutTwoHours() {
        Instant earliestExpectedExpiry = Instant.now().plusSeconds(119 * 60);

        String token = TokenUtils.createToken("7-USER", "test-signing-key");
        Instant expiry = JWT.decode(token).getExpiresAtAsInstant();

        assertTrue(expiry.isAfter(earliestExpectedExpiry));
        assertTrue(expiry.isBefore(Instant.now().plusSeconds(121 * 60)));
    }
}
