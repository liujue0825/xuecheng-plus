package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 课程发布状态
 * @since 2024/7/27
 */
@Getter
public enum CoursePublishStatus {

    NOT_PUBLISHED("203001", "未发布"),

    PUBLISHED("203002", "已发布"),

    OFFLINE("203003", "下线");

    private final String code;
    private final String desc;

    CoursePublishStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CoursePublishStatus fromCode(String code) {
        for (CoursePublishStatus status : CoursePublishStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
