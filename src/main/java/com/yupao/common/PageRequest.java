package com.yupao.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -591371367184508507L;
    protected int pageNum;
    protected int pageSize;
}
