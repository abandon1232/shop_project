package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.exception.CustomException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JWT utility methods.
 */
public class TokenUtils {

    /**
     * Generate a token.
     */
    public static String createToken(String data, String sign) {
        return JWT.create().withAudience(data) // Store userId-role in the token audience claim.
                .withExpiresAt(Date.from(Instant.now().plus(Duration.ofHours(2)))) // Expire after two hours.
                .sign(Algorithm.HMAC256(sign)); // Use the stored password hash as the token signing key.
    }

    /**
     * Get the currently authenticated account.
     */
    public static Account getCurrentUser() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        Object account = attributes.getRequest().getAttribute(Constants.CURRENT_USER);
        if (account instanceof Account authenticated) {
            return authenticated;
        }
        throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
    }
}

