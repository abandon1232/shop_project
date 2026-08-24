package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.mapper.*;
import com.example.utils.TokenUtils;
import com.example.utils.UserCF;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Product business logic.
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * Create a record.
     */
    public void add(Goods goods) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        goodsMapper.insert(goods);
    }

    /**
     * Delete a record.
     */
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
    }

    /**
     * Delete multiple records.
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            goodsMapper.deleteById(id);
        }
    }

    /**
     * Update a record.
     */
    public void updateById(Goods goods) {
        goodsMapper.updateById(goods);
    }

    /**
     * Find a record by ID.
     */
    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    /**
     * Find all matching records.
     */
    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    /**
     * Find records with pagination.
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            goods.setBusinessId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

    public List<Goods> selectTop15() {
        return goodsMapper.selectTop15();
    }

    public List<Goods> selectByTypeId(Integer id) {
        return goodsMapper.selectByTypeId(id);
    }

    public List<Goods> selectByBusinessId(Integer id) {
        return goodsMapper.selectByBusinessId(id);
    }

    public List<Goods> selectByName(String name) {
        return goodsMapper.selectByName(name);
    }

    public List<Goods> recommend() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (ObjectUtil.isEmpty(currentUser)) {
            // No authenticated user is available.
            return new ArrayList<>();
        }
        // Load all users.
        List<User> allUsers = userMapper.selectAll(null);
        // Load all products.
        List<Goods> allGoods = goodsMapper.selectAll(null);

        // Store the relationship score between every product and user.
        List<RelateDTO> data = new ArrayList<>();
        // Store the products that will be returned to the client.
        List<Goods> result = new ArrayList<>();

        // Calculate relationship data between each product and user.
        for (Goods goods : allGoods) {
            Integer goodsId = goods.getId();
            for (User user : allUsers) {
                Integer userId = user.getId();
                int index = 1;
                RelateDTO relateDTO = new RelateDTO(userId, goodsId, index);
                data.add(relateDTO);
            }
        }

        // Pass the prepared data to the recommendation algorithm.
        List<Integer> goodsIds = UserCF.recommend(currentUser.getId(), data);
        // Convert product IDs to product records.
        List<Goods> recommendResult = goodsIds.stream().map(goodsId -> allGoods.stream()
                        .filter(x -> x.getId().equals(goodsId)).findFirst().orElse(null))
                .limit(10).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(recommendResult)) {
            // Return ten random products as a fallback.
            return getRandomGoods(10);
        }
        if (recommendResult.size() < 10) {
            int num = 10 - recommendResult.size();
            List<Goods> list = getRandomGoods(num);
            result.addAll(list);
        }
        return recommendResult;
    }

    private List<Goods> getRandomGoods(int num) {
        List<Goods> list = new ArrayList<>(num);
        List<Goods> goods = goodsMapper.selectAll(null);
        for (int i = 0; i < num; i++) {
            int index = new Random().nextInt(goods.size());
            list.add(goods.get(index));
        }
        return list;
    }
}