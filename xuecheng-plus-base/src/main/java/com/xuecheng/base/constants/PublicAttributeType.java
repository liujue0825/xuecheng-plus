package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 公共属性类型
 * @since 2024/7/27
 */
@Getter
public enum PublicAttributeType {

    ACTIVE("1", 1, "使用态"),

    DELETED("0", 0, "删除态"),

    TEMPORARY("-1", -1, "暂时态");

    private final String code;
    private final int codeInt;
    private final String desc;

    PublicAttributeType(String code, int codeInt, String desc) {
        this.code = code;
        this.codeInt = codeInt;
        this.desc = desc;
    }

    public static PublicAttributeType fromCode(String code) {
        for (PublicAttributeType type : PublicAttributeType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    public static PublicAttributeType fromCodeInt(int codeInt) {
        for (PublicAttributeType type : PublicAttributeType.values()) {
            if (type.codeInt == codeInt) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown codeInt: " + codeInt);
    }
}
