package com.yupao.service;

import com.yupao.common.BaseResponse;
import com.yupao.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupao.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 0.0
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-17 15:18:53
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User user);

    List<User> searchUserByTags(List<String> tagList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User userLogin);

    List<User> matchUsers(long num, User loginUser);
}
