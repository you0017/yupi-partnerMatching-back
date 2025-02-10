package com.yupao.model.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户退出登录请求体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamQuitRequest implements Serializable {
    private Long teamId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}