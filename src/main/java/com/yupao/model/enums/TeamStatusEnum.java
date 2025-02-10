package com.yupao.model.enums;

/**
 * 队伍状态枚举
 */
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    private Integer value;
    private String text;

    TeamStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
    public static TeamStatusEnum getEnumByValue(Integer value){
        for (TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            if(teamStatusEnum.value.equals(value)){
                return teamStatusEnum;
            }
        }
        return null;
    }
}
