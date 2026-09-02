package com.example.controller.response;

import java.math.BigDecimal;

public record DashboardSummary(
        long products,
        long categories,
        long orders,
        long customers,
        long sellers,
        long lowStockProducts,
        BigDecimal revenue) {
}
