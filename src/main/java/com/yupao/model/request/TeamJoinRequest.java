package com.yupao.model.request;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamJoinRequest implements Serializable {
    private Long teamId;

    /**
     * 密码
     */
    private String password;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}