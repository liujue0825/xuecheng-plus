package com.xuecheng.base.constants;


import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 对象的审核状态
 * @since 2024/7/26
 */
@Getter
public enum AuditStatus {

    NOT_PASSED("002001", "审核未通过"),

    NOT_REVIEWED("002002", "未审核"),

    PASSED("002003", "审核通过");

    private final String code;
    private final String desc;

    AuditStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AuditStatus fromCode(String code) {
        for (AuditStatus status : AuditStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
