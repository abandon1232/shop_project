package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
     * Find a product that may be shown and purchased on the public storefront.
     */
    Goods selectPurchasableById(Integer id);

    /**
      * Find all matching records.
    */
    List<Goods> selectAll(Goods goods);

    List<Goods> selectFeatured(@Param("limit") int limit);

    @Select("""
            select goods.*, type.name as typeName, business.name as businessName
            from goods
            left join business on goods.business_id = business.id
            left join type on goods.type_id = type.id
            where goods.type_id = #{id}
              and (goods.business_id is null or business.status = 'APPROVED')
            order by goods.id desc
            """)
    List<Goods> selectByTypeId(Integer id);

    @Select("select * from goods where business_id = #{id}")
    List<Goods> selectByBusinessId(Integer id);

    @Select("""
            select goods.*, type.name as typeName, business.name as businessName
            from goods
            left join business on goods.business_id = business.id
            left join type on goods.type_id = type.id
            where goods.name like concat('%', #{name}, '%')
              and (goods.business_id is null or business.status = 'APPROVED')
            order by goods.id desc
            """)
    List<Goods> selectByName(String name);

    @Update("update goods set count = count - #{quantity} where id = #{id} and count >= #{quantity}")
    int decreaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    @Update("update goods set count = count + #{quantity} where id = #{id}")
    int increaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    @Select("select count(*) from goods")
    long countAll();

    @Select("select count(*) from goods where business_id = #{businessId}")
    long countByBusinessId(Integer businessId);

    @Select("select count(*) from goods where count <= 5")
    long countLowStock();

    @Select("select count(*) from goods where business_id = #{businessId} and count <= 5")
    long countLowStockByBusinessId(Integer businessId);
}
