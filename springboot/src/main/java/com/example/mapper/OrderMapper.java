package com.example.mapper;

import com.example.entity.CustomerOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface OrderMapper {
    int insert(CustomerOrder order);

    CustomerOrder selectById(Integer id);

    List<CustomerOrder> selectAll(CustomerOrder filter);

    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Select("select count(*) from customer_order")
    long countAll();

    @Select("select count(*) from customer_order where business_id = #{businessId}")
    long countByBusinessId(Integer businessId);

    @Select("select coalesce(sum(total_price), 0) from customer_order where status <> 'CANCELLED'")
    BigDecimal revenueAll();

    @Select("select coalesce(sum(total_price), 0) from customer_order where business_id = #{businessId} and status <> 'CANCELLED'")
    BigDecimal revenueByBusinessId(Integer businessId);
}
