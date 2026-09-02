package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.common.enums.StatusEnum;
import com.example.entity.Account;
import com.example.entity.Business;
import com.example.exception.CustomException;
import com.example.mapper.BusinessMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Merchant business logic.
 **/
@Service
public class BusinessService {

    @Resource
    private BusinessMapper businessMapper;
    @Resource
    private PasswordService passwordService;

    /**
     * Create a record.
     */
    public void add(Business business) {
        if (business.getPassword() == null || business.getPassword().isBlank()) {
            throw new CustomException(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        Business dbBusiness = businessMapper.selectByUsername(business.getUsername());
        if (dbBusiness != null) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        business.setPassword(passwordService.encode(business.getPassword()));
        if (business.getName() == null || business.getName().isEmpty()) {
            business.setName(business.getUsername());
        }
        if (business.getStatus() == null || business.getStatus().isEmpty()) {
            business.setStatus(StatusEnum.PENDING.code());
        }
        business.setRole(RoleEnum.BUSINESS.name());
        businessMapper.insert(business);
    }

    /**
     * Delete a record.
     */
    public void deleteById(Integer id) {
        businessMapper.deleteById(id);
    }

    /**
     * Delete multiple records.
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            businessMapper.deleteById(id);
        }
    }

    /**
     * Update a record.
     */
    public void updateById(Business business) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {
            if (!Objects.equals(currentUser.getId(), business.getId())) {
                throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
            }
            business.setUsername(null);
            business.setPassword(null);
            business.setRole(null);
            business.setStatus(null);
        }
        if (business.getPassword() == null || business.getPassword().isBlank()) {
            business.setPassword(null);
        } else if (passwordService.needsUpgrade(business.getPassword())) {
            business.setPassword(passwordService.encode(business.getPassword()));
        }
        businessMapper.updateById(business);
    }

    /**
     * Find a record by ID.
     */
    public Business selectById(Integer id) {
        return businessMapper.selectById(id);
    }

    public Business selectAccessibleById(Integer id) {
        Account current = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(current.getRole())
                && !(RoleEnum.BUSINESS.name().equals(current.getRole())
                && Objects.equals(current.getId(), id))) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
        }
        return businessMapper.selectById(id);
    }

    /**
     * Find all matching records.
     */
    public List<Business> selectAll(Business business) {
        return businessMapper.selectAll(business);
    }

    /**
     * Find records with pagination.
     */
    public PageInfo<Business> selectPage(Business business, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Business> list = businessMapper.selectAll(business);
        return PageInfo.of(list);
    }

    /**
     * Authenticate an account.
     */
    public Account login(Account account) {
        Business dbBusiness = businessMapper.selectByUsername(account.getUsername());
        if (dbBusiness == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!passwordService.matches(account.getPassword(), dbBusiness.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        if (passwordService.needsUpgrade(dbBusiness.getPassword())) {
            dbBusiness.setPassword(passwordService.encode(account.getPassword()));
            businessMapper.updateById(dbBusiness);
        }
        // Generate a token.
        String tokenData = dbBusiness.getId() + "-" + RoleEnum.BUSINESS.name();
        String token = TokenUtils.createToken(tokenData, dbBusiness.getPassword());
        dbBusiness.setToken(token);
        return dbBusiness;
    }

    /**
     * Register an account.
     */
    public void register(Account account) {
        Business business = new Business();
        BeanUtils.copyProperties(account, business);

        add(business);
    }

    /**
     * Change a password.
     */
    public void updatePassword(Account account) {
        Business dbBusiness = businessMapper.selectByUsername(account.getUsername());
        if (dbBusiness == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!passwordService.matches(account.getPassword(), dbBusiness.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbBusiness.setPassword(passwordService.encode(account.getNewPassword()));
        businessMapper.updateById(dbBusiness);
    }

}
