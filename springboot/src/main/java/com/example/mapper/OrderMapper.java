package com.example.mapper;

import com.example.entity.CustomerOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int insert(CustomerOrder order);

    CustomerOrder selectById(Integer id);

    List<CustomerOrder> selectAll(CustomerOrder filter);

    int updateStatus(@Param("id") Integer id, @Param("status") String status);
}
