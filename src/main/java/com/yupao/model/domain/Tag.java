package com.yupao.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签
 * @TableName tag
 */
@TableName(value ="tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 0不是,1是
     */
    private Integer isParent;

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
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}