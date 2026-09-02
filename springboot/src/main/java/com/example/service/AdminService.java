package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Administrator business logic.
 **/
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;
    @Resource
    private PasswordService passwordService;

    /**
     * Create a record.
     */
    public void add(Admin admin) {
        if (StrUtil.isBlank(admin.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        admin.setPassword(passwordService.encode(admin.getPassword()));
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        admin.setRole(RoleEnum.ADMIN.name());
        adminMapper.insert(admin);
    }

    /**
     * Delete a record.
     */
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    /**
     * Delete multiple records.
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            adminMapper.deleteById(id);
        }
    }

    /**
     * Update a record.
     */
    public void updateById(Admin admin) {
        if (ObjectUtil.isNotEmpty(admin.getPassword()) && passwordService.needsUpgrade(admin.getPassword())) {
            admin.setPassword(passwordService.encode(admin.getPassword()));
        }
        adminMapper.updateById(admin);
    }

    /**
     * Find a record by ID.
     */
    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    public boolean existsByUsername(String username) {
        return adminMapper.selectByUsername(username) != null;
    }

    /**
     * Find all matching records.
     */
    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }

    /**
     * Find records with pagination.
     */
    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    /**
     * Authenticate an account.
     */
    public Account login(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!passwordService.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        if (passwordService.needsUpgrade(dbAdmin.getPassword())) {
            dbAdmin.setPassword(passwordService.encode(account.getPassword()));
            adminMapper.updateById(dbAdmin);
        }
        // Generate a token.
        String tokenData = dbAdmin.getId() + "-" + RoleEnum.ADMIN.name();
        String token = TokenUtils.createToken(tokenData, dbAdmin.getPassword());
        dbAdmin.setToken(token);
        return dbAdmin;
    }

    /**
     * Register an account.
     */
    public void register(Account account) {
        Admin admin = new Admin();
        BeanUtils.copyProperties(account, admin);
        add(admin);
    }

    /**
     * Change a password.
     */
    public void updatePassword(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!passwordService.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbAdmin.setPassword(passwordService.encode(account.getNewPassword()));
        adminMapper.updateById(dbAdmin);
    }

}
