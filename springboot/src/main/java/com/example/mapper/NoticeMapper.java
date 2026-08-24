package com.example.mapper;

import com.example.entity.Notice;
import java.util.List;

/**
 * Data access operations for notices.
*/
public interface NoticeMapper {

    /**
      * Create a record.
    */
    int insert(Notice notice);

    /**
      * Delete a record.
    */
    int deleteById(Integer id);

    /**
      * Update a record.
    */
    int updateById(Notice notice);

    /**
      * Find a record by ID.
    */
    Notice selectById(Integer id);

    /**
      * Find all matching records.
    */
    List<Notice> selectAll(Notice notice);

}