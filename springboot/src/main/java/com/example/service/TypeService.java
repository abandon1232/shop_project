package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Account;
import com.example.entity.Type;
import com.example.mapper.TypeMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Category business logic.
 **/
@Service
public class TypeService {

    @Resource
    private TypeMapper typeMapper;

    /**
     * Create a record.
     */
    public void add(Type type) {
        typeMapper.insert(type);
    }

    /**
     * Delete a record.
     */
    public void deleteById(Integer id) {
        typeMapper.deleteById(id);
    }

    /**
     * Delete multiple records.
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            typeMapper.deleteById(id);
        }
    }

    /**
     * Update a record.
     */
    public void updateById(Type type) {
        typeMapper.updateById(type);
    }

    /**
     * Find a record by ID.
     */
    public Type selectById(Integer id) {
        return typeMapper.selectById(id);
    }

    /**
     * Find all matching records.
     */
    public List<Type> selectAll(Type type) {
        return typeMapper.selectAll(type);
    }

    /**
     * Find records with pagination.
     */
    public PageInfo<Type> selectPage(Type type, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Type> list = typeMapper.selectAll(type);
        return PageInfo.of(list);
    }

}
