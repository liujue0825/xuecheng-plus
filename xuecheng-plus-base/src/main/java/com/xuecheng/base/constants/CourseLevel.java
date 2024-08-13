package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 课程等级
 * @since 2024/7/27
 */
@Getter
public enum CourseLevel {

    BEGINNER("204001", "初级"),

    INTERMEDIATE("204002", "中级"),

    ADVANCED("204003", "高级");

    private final String code;
    private final String desc;

    CourseLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseLevel fromCode(String code) {
        for (CourseLevel level : CourseLevel.values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
