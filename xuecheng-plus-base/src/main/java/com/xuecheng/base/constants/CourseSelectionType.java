package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 选课类型
 * @since 2024/7/27
 */
@Getter
public enum CourseSelectionType {

    FREE("700001", "免费课程"),

    PAID("700002", "收费课程");

    private final String code;
    private final String desc;

    CourseSelectionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseSelectionType fromCode(String code) {
        for (CourseSelectionType type : CourseSelectionType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
