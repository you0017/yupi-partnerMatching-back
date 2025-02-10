package com.yupao.common;

public enum ErrorCode {
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求参数为空", ""),
    NO_AUTH(40101, "无权限", ""),
    NOT_LOGIN(40100, "未登录", ""),
    SUCCESS(0, "ok", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");

    private final int code;
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public String getDescription() {
        return description;
    }
}
