package com.yupao.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -2222222222222222222L;
    private String userAccount;
    private String userPassword;
}
