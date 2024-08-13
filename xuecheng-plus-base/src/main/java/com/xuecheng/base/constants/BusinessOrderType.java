package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 业务订单类型
 * @since 2024/7/27
 */
@Getter
public enum BusinessOrderType {

    COURSE_PURCHASE("60201", "购买课程"),

    STUDY_MATERIAL("60202", "学习资料");

    private final String code;
    private final String desc;

    BusinessOrderType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BusinessOrderType fromCode(String code) {
        for (BusinessOrderType type : BusinessOrderType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
