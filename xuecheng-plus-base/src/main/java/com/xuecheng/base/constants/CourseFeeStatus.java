package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 课程收费情况
 * @since 2024/7/27
 */
@Getter
public enum CourseFeeStatus {

    FREE("201000", "免费"),

    PAID("201001", "收费");

    private final String code;
    private final String desc;

    CourseFeeStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseFeeStatus fromCode(String code) {
        for (CourseFeeStatus status : CourseFeeStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
