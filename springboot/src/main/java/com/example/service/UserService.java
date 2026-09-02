package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * User business logic.
 **/
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordService passwordService;

    /**
     * Create a record.
     */
    public void add(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new CustomException(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (dbUser != null) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        user.setPassword(passwordService.encode(user.getPassword()));
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getUsername());
        }

        user.setRole(RoleEnum.USER.name());
        userMapper.insert(user);
    }

    /**
     * Delete a record.
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * Delete multiple records.
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    /**
     * Update a record.
     */
    public void updateById(User user) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            if (!Objects.equals(currentUser.getId(), user.getId())) {
                throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
            }
            user.setUsername(null);
            user.setPassword(null);
            user.setRole(null);
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(null);
        } else if (passwordService.needsUpgrade(user.getPassword())) {
            user.setPassword(passwordService.encode(user.getPassword()));
        }
        userMapper.updateById(user);
    }

    /**
     * Find a record by ID.
     */
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    public User selectAccessibleById(Integer id) {
        Account current = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(current.getRole())
                && !(RoleEnum.USER.name().equals(current.getRole())
                && Objects.equals(current.getId(), id))) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
        }
        return userMapper.selectById(id);
    }

    /**
     * Find all matching records.
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * Find records with pagination.
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * Authenticate an account.
     */
    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (dbUser == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!passwordService.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        if (passwordService.needsUpgrade(dbUser.getPassword())) {
            dbUser.setPassword(passwordService.encode(account.getPassword()));
            userMapper.updateById(dbUser);
        }
        // Generate a token.
        String tokenData = dbUser.getId() + "-" + RoleEnum.USER.name();
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }

    /**
     * Register an account.
     */
    public void register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);

        add(user);
    }

    /**
     * Change a password.
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (dbUser == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!passwordService.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbUser.setPassword(passwordService.encode(account.getNewPassword()));
        userMapper.updateById(dbUser);
    }

}
