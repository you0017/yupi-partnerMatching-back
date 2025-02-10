package com.yupao.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求参数
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -3377145813278548077L;
    private long id;
}
