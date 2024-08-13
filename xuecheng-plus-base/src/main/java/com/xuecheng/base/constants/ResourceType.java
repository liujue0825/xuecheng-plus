package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 资源类型
 * @since 2024/7/26
 */
@Getter
public enum ResourceType {

    IMAGE("001001", "图片"),

    VIDEO("001002", "视频"),

    OTHER("001003", "其它");

    private final String code;
    private final String desc;

    ResourceType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ResourceType fromCode(String code) {
        for (ResourceType type : ResourceType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
