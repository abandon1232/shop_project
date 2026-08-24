package com.example.mapper;

import com.example.entity.Admin;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access operations for administrators.
*/
public interface AdminMapper {

    /**
      * Create a record.
    */
    int insert(Admin admin);

    /**
      * Delete a record.
    */
    int deleteById(Integer id);

    /**
      * Update a record.
    */
    int updateById(Admin admin);

    /**
      * Find a record by ID.
    */
    Admin selectById(Integer id);

    /**
      * Find all matching records.
    */
    List<Admin> selectAll(Admin admin);

    @Select("select * from admin where username = #{username}")
    Admin selectByUsername(String username);
}