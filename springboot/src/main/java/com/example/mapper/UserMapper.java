package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access operations for users.
*/
public interface UserMapper {

    /**
      * Create a record.
    */
    int insert(User user);

    /**
      * Delete a record.
    */
    int deleteById(Integer id);

    /**
      * Update a record.
    */
    int updateById(User user);

    /**
      * Find a record by ID.
    */
    User selectById(Integer id);

    /**
      * Find all matching records.
    */
    List<User> selectAll(User user);

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Select("select count(*) from user")
    long countAll();
}
