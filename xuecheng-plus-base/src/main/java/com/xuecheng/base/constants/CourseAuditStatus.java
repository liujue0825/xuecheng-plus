package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 课程审核状态
 * @since 2024/7/26
 */
@Getter
public enum CourseAuditStatus {

    NOT_PASSED("202001", "审核未通过"),

    NOT_SUBMITTED("202002", "未提交"),

    SUBMITTED("202003", "已提交"),

    PASSED("202004", "审核通过");

    private final String code;
    private final String desc;

    CourseAuditStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseAuditStatus fromCode(String code) {
        for (CourseAuditStatus status : CourseAuditStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
