package com.example.mapper;

import com.example.entity.CartItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    Integer lockUserById(Integer userId);
    List<CartItem> selectByUserId(Integer userId);
    CartItem selectByUserAndGoods(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
    CartItem selectOwnedById(@Param("id") Integer id, @Param("userId") Integer userId);
    int insert(CartItem item);
    int updateQuantity(@Param("id") Integer id, @Param("userId") Integer userId, @Param("quantity") Integer quantity);
    int deleteOwned(@Param("id") Integer id, @Param("userId") Integer userId);
    int deleteByUserId(Integer userId);
}
