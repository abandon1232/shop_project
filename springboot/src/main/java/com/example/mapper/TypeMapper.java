package com.example.mapper;

import com.example.entity.Type;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access operations for categories.
*/
public interface TypeMapper {

    /**
      * Create a record.
    */
    int insert(Type type);

    /**
      * Delete a record.
    */
    int deleteById(Integer id);

    /**
      * Update a record.
    */
    int updateById(Type type);

    /**
      * Find a record by ID.
    */
    Type selectById(Integer id);

    /**
      * Find all matching records.
    */
    List<Type> selectAll(Type type);

    @Select("select count(*) from type")
    long countAll();

}
