package com.example.controller.request;

import com.example.entity.Goods;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GoodsRequest(
        @Positive Integer id,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 5000) String description,
        @NotBlank @Size(max = 500) String img,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Size(max = 32) String unit,
        @NotNull @PositiveOrZero Integer count,
        @NotNull @Positive Integer typeId,
        @Positive Integer businessId) {

    public Goods toGoods() {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setName(name);
        goods.setDescription(description);
        goods.setImg(img);
        goods.setPrice(price);
        goods.setUnit(unit);
        goods.setCount(count);
        goods.setTypeId(typeId);
        goods.setBusinessId(businessId);
        return goods;
    }
}
