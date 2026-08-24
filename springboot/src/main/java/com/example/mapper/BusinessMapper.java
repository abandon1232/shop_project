package com.example.mapper;

import com.example.entity.Business;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access operations for merchants.
*/
public interface BusinessMapper {

    /**
      * Create a record.
    */
    int insert(Business business);

    /**
      * Delete a record.
    */
    int deleteById(Integer id);

    /**
      * Update a record.
    */
    int updateById(Business business);

    /**
      * Find a record by ID.
    */
    Business selectById(Integer id);

    /**
      * Find all matching records.
    */
    List<Business> selectAll(Business business);

    @Select("select * from business where username = #{username}")
    Business selectByUsername(String username);
}