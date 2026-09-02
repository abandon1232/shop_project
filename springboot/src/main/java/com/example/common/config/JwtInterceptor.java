package com.example.common.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.BusinessService;
import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Set;

/**
 * JWT authentication interceptor.
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);
    private static final Set<String> PUBLIC_GET_PATHS = Set.of(
            "/goods/featured",
            "/goods/selectById",
            "/goods/selectByTypeId",
            "/goods/selectByName",
            "/type/selectAll",
            "/notice/selectAll");

    @Resource
    private AdminService adminService;
    @Resource
    private BusinessService businessService;
    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublicGetRequest(request)) {
            return true;
        }
        // 1. Read the token from the HTTP request header.
        String token = request.getHeader(Constants.TOKEN);
        // 2. Start authentication.
        if (token == null || token.isEmpty()) {
            throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
        }
        Account account = null;
        try {
            // Decode the identity stored in the token.
            String userRole = JWT.decode(token).getAudience().get(0);
            String userId = userRole.split("-")[0];
            String role = userRole.split("-")[1];
            // Load the account from the database by user ID.
            if (RoleEnum.ADMIN.name().equals(role)) {
                account = adminService.selectById(Integer.valueOf(userId));
            }
            if (RoleEnum.BUSINESS.name().equals(role)) {
                account = businessService.selectById(Integer.valueOf(userId));
            }
            if (RoleEnum.USER.name().equals(role)) {
                account = userService.selectById(Integer.valueOf(userId));
            }
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        if (account == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        try {
            // Verify the token using the stored password hash as the signing key.
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            jwtVerifier.verify(token); // Verify the token.
        } catch (JWTVerificationException e) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        authorize(handler, account);
        request.setAttribute(Constants.CURRENT_USER, account);
        return true;
    }

    private boolean isPublicGetRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String requestUri = request.getRequestURI();
        return requestUri.startsWith("/files/")
                || requestUri.startsWith("/type/selectById/")
                || PUBLIC_GET_PATHS.contains(requestUri);
    }

    private void authorize(Object handler, Account account) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }
        RequireRoles requirement = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireRoles.class);
        if (requirement == null) {
            requirement = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequireRoles.class);
        }
        if (requirement != null && Arrays.stream(requirement.value())
                .noneMatch(role -> role.name().equals(account.getRole()))) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
        }
    }
}
