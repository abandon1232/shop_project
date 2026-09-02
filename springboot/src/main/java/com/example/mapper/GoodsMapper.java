package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access operations for products.
*/
public interface GoodsMapper {

    /**
      * Create a record.
    */
    int insert(Goods goods);

    /**
      * Delete a record.
    */
    int deleteById(Integer id);

    /**
      * Update a record.
    */
    int updateById(Goods goods);

    /**
      * Find a record by ID.
    */
    Goods selectById(Integer id);

    /**
      * Find all matching records.
    */
    List<Goods> selectAll(Goods goods);

    List<Goods> selectFeatured(@Param("limit") int limit);

    @Select("select * from goods where type_id = #{id}")
    List<Goods> selectByTypeId(Integer id);

    @Select("select * from goods where business_id = #{id}")
    List<Goods> selectByBusinessId(Integer id);

    @Select("select * from goods where name like concat('%', #{name}, '%')")
    List<Goods> selectByName(String name);
}
