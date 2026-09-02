package com.example.controller.request;

import com.example.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderStatusRequest(
        @NotNull @Positive Integer id,
        @NotNull OrderStatus status) {
}
