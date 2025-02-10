package com.yupao.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 
     */
    @TableLogic//表示逻辑删除
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private Integer userRole;

    private String planetCode;

    private String tags;

    @TableField("profile")
    private String profile;
}