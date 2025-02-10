package com.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupao.common.BaseResponse;
import com.yupao.model.vo.UserVO;
import com.yupao.service.UserService;
import com.yupao.common.ErrorCode;
import com.yupao.constant.UserConstant;
import com.yupao.exception.BusinessException;
import com.yupao.model.domain.User;
import com.yupao.mapper.UserMapper;
import com.yupao.utils.AlgorithmUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.list.FixedSizeList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 0.0
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-17 15:18:53
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new RuntimeException("参数为空");
        }
        if (userAccount.length() < 4) {
            throw new RuntimeException("用户名小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new RuntimeException("密码小于8位");
        }

        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new RuntimeException("账户不能包含特殊字符");
        }

        //密码和确认密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new RuntimeException("密码和确认密码不一致");
        }

        //账户不能重复
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new RuntimeException("账户不能重复");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());

        //3.插入数据
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .build();
        boolean save = this.save(user);
        if (!save) {
            throw new RuntimeException("插入数据失败");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            log.info("参数为空");
            throw new RuntimeException("参数为空");
        }
        if (userAccount.length() < 4) {
            log.info("用户名小于4位");
            throw new RuntimeException("用户名小于4位");
        }
        if (userPassword.length() < 8) {
            log.info("密码小于8位");
            throw new RuntimeException("密码小于8位");
        }

        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            log.info("账户包含特殊字符");
            throw new RuntimeException("账户包含特殊字符");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());

        //账户不能重复
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = userMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            log.info("账户不存在");
            throw new RuntimeException("账户不存在");
        }

        //4.用户脱敏
        User safetyUser = getSafetyUser(user);

        //3.记录用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        return safetyUser;
    }

    @Override
    public User getSafetyUser(User user) {
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setPlanetCode(user.getPlanetCode());
        safetyUser.setTags(user.getTags());
        safetyUser.setProfile(user.getProfile());
        return safetyUser;
    }

    /**
     * 用户拥有的标签      内存过滤
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (tagNameList.isEmpty()) {
            throw new RuntimeException("标签为空");
        }
        Gson gson = new Gson();
        //先查所有用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<User> list = this.list(lambdaQueryWrapper);
        //2.内存中判断是否包含要求的标签
        return list.stream().filter(user -> {
            //false删除，true保存
            String tags = user.getTags();
            if (tags == null) {
                return false;
            }
            //Set<String> tempTags = (Set<String>) Optional.ofNullable(gson.fromJson(tags, new TypeToken<Set<String>>() {}.getType())).orElse(new HashSet<>());
            Set<String> tempTags = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            tempTags = Optional.ofNullable(tempTags).orElse(new HashSet<>());
            for (String tempTag : tagNameList) {
                if (tempTags.contains(tempTag)) {
                    return true;
                }
            }
            return false;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员允许更新其他的任意用户
        //如果不是管理员，只允许更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    /**
     * 用户拥有的标签      sql查询版本
     *
     * @param tagNameList
     * @return
     */
    @Deprecated
    public List<User> searchUserByTagsBySQL(List<String> tagNameList) {
        if (tagNameList.isEmpty()) {
            throw new RuntimeException("标签为空");
        }
        /*LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            lambdaQueryWrapper.like(User::getTags, tagName);
        }
        List<User> list = this.list(lambdaQueryWrapper);*/
        Gson gson = new Gson();
        //先查所有用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<User> list = this.list(lambdaQueryWrapper);
        //2.内存中判断是否包含要求的标签
        /*Iterator<User> iterator = list.iterator();
        User user = null;
        while (iterator.hasNext()) {
            user = iterator.next();
            String tags = user.getTags();
            if (tags==null){
                iterator.remove();
                continue;
            }
            Set<String> tempTags = gson.fromJson(tags, new TypeToken<Set<String>>() {}.getType());
            for (String tempTag : tagNameList) {
                if (!tempTags.contains(tempTag)){
                    iterator.remove();
                }
            }
        }*/
        return list.stream().filter(user -> {
            //false删除，true保存
            String tags = user.getTags();
            if (tags == null) {
                return false;
            }
            Set<String> tempTags = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            for (String tempTag : tagNameList) {
                if (!tempTags.contains(tempTag)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //鉴权，仅限管理员查询
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null || !user.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAdmin(User userLogin) {
        //鉴权，仅限管理员查询
        if (userLogin == null || !userLogin.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            return false;
        }
        return true;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.isNotNull(User::getTags);
        lambdaQueryWrapper.select(User::getId, User::getTags);
        List<User> list = this.list(lambdaQueryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        //用户列表下标 => 相似度
        List<Pair<User,Long>> list1 = new ArrayList<>();
        SortedMap<Integer, Long> map = new TreeMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (StringUtils.isBlank(list.get(i).getTags()) || list.get(i).getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(list.get(i).getTags(), new TypeToken<List<String>>() {
            }.getType());
            //排除空标签
            /*if (userTagList == null || userTagList.size() <= 0){
                continue;
            }*/
            //计算分数
            list1.add(new Pair<>(list.get(i), (long) AlgorithmUtils.CalculateDistance(tagList, userTagList)));
        }
        //筛选出前num个最匹配的用户,由小到大排序
        List<Pair<User, Long>> collect = list1.stream().sorted((a, b) -> (int) (a.getValue() - b.getValue())).limit(num).collect(Collectors.toList());
        List<User> users = collect.stream().map(Pair::getKey).collect(Collectors.toList());
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(User::getId, users.stream().map(User::getId).collect(Collectors.toList()));
        Map<Long,List<User>> userIdUserListMap = this.list(lambdaQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (User user : list) {
            finalUserList.add(userIdUserListMap.get(user.getId()).get(0));
        }
        return list;
    }
}




