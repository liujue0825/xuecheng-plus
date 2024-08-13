package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 课程模式状态
 * @since 2024/7/27
 */
@Getter
public enum CourseModeStatus {

    RECORDED("200002", "录播"),

    LIVE("200003", "直播");

    private final String code;
    private final String desc;

    CourseModeStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseModeStatus fromCode(String code) {
        for (CourseModeStatus status : CourseModeStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
