package com.example.common.enums;

public enum ResultCodeEnum {
    SUCCESS("200", "Success"),

    PARAM_ERROR("400", "Invalid parameters"),
    TOKEN_INVALID_ERROR("401", "Missing or invalid token"),
    TOKEN_CHECK_ERROR("401", "Token verification failed. Please sign in again."),
    FORBIDDEN_ERROR("403", "You do not have permission to perform this action"),
    PARAM_LOST_ERROR("4001", "Required parameters are missing"),

    SYSTEM_ERROR("500", "Unexpected server error"),
    USER_EXIST_ERROR("5001", "Username already exists"),
    USER_NOT_LOGIN("5002", "Please sign in first"),
    USER_ACCOUNT_ERROR("5003", "Incorrect username or password"),
    USER_NOT_EXIST_ERROR("5004", "Account not found"),
    PARAM_PASSWORD_ERROR("5005", "Current password is incorrect"),
    PRODUCT_NOT_FOUND("4041", "Product not found"),
    ORDER_NOT_FOUND("4042", "Order not found"),
    ORDER_ACCESS_DENIED("4031", "You cannot access this order"),
    INSUFFICIENT_STOCK("4091", "Not enough stock is available"),
    INVALID_ORDER_STATUS("4092", "This order status change is not allowed"),
    ;

    public String code;
    public String msg;

    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
