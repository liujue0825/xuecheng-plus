package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 选课状态
 * @since 2024/7/27
 */

@Getter
public enum CourseSelectionStatus {

    SUCCESS("701001", "选课成功"),

    PENDING_PAYMENT("701002", "待支付");

    private final String code;
    private final String desc;

    CourseSelectionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseSelectionStatus fromCode(String code) {
        for (CourseSelectionStatus status : CourseSelectionStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

