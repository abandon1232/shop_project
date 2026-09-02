package com.example.common.enums;

public enum StatusEnum {
    PENDING,
    APPROVED,
    REJECTED;

    public String code() {
        return name();
    }
}
