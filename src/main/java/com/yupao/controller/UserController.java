package com.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupao.model.request.UserLoginRequest;
import com.yupao.model.request.UserRegisterRequest;
import com.yupao.common.BaseResponse;
import com.yupao.common.ErrorCode;
import com.yupao.common.ResultUtils;
import com.yupao.constant.UserConstant;
import com.yupao.exception.BusinessException;
import com.yupao.model.domain.User;
import com.yupao.model.vo.UserVO;
import com.yupao.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final RedisTemplate redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long l = userService.userRegister(userAccount, userPassword, checkPassword);
        //return new BaseResponse<Long>(0,l,"注册成功");
        return ResultUtils.success(l);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            return null;
        }
        user = userService.getById(user.getId());
        user = userService.getSafetyUser(user);
        return ResultUtils.success(user);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userRegisterRequest, HttpServletRequest request) {
        if (userRegisterRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        //return new BaseResponse<User>(0, user, "登录成功");
        return ResultUtils.success(user);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            return ResultUtils.error(ErrorCode.NO_AUTH);
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            lambdaQueryWrapper.like(StringUtils.isNotBlank(username), User::getUsername, username);
        }
        return ResultUtils.success(userService.list(lambdaQueryWrapper).stream().peek(user -> {
            userService.getSafetyUser(user);
        }).collect(Collectors.toList()));
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //1.校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.校验权限
        User loginUser = userService.getLoginUser(request);
        //3.触发更新
        return ResultUtils.success(userService.updateUser(user, loginUser));
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (tagNameList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(users);
    }

    @GetMapping("/recommand")
    public BaseResponse<List<User>> recommandUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ValueOperations ops = redisTemplate.opsForValue();
        //如果有缓存，直接拿缓存的
        String redisKey = String.format("yupao:user:recommand:%s", loginUser.getId());
        Page<User> userPage = (Page<User>) ops.get(redisKey);

        if (userPage == null) {
            userPage = userService.page(new Page<>(pageNum, pageSize));
            //ops.set(redisKey,userPage);
            //设置过期时间
            ops.set(redisKey, userPage, 10, TimeUnit.SECONDS);
        }
        return ResultUtils.success(userPage.getRecords().stream().peek(user -> {
            userService.getSafetyUser(user);
        }).collect(Collectors.toList()));
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteUser(Long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.removeById(id));
    }

    @PostMapping("/logOut")
    public BaseResponse<Integer> logOut(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        //return true;
        return ResultUtils.success(1);
    }


    /**
     * 随机匹配用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.matchUsers(num, userService.getLoginUser(request)));
    }
}