package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 课程作业记录审批状态
 * @since 2024/7/27
 */
@Getter
public enum HomeworkApprovalStatus {

    NOT_SUBMITTED("306001", "未提交"),

    PENDING("306002", "待批改"),

    GRADED("306003", "已批改");

    private final String code;
    private final String desc;

    HomeworkApprovalStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static HomeworkApprovalStatus fromCode(String code) {
        for (HomeworkApprovalStatus status : HomeworkApprovalStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
