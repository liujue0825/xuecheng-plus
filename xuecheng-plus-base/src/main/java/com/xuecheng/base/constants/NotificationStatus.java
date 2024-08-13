package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 消息通知状态
 * @since 2024/7/27
 */
@Getter
public enum NotificationStatus {

    NOT_NOTIFIED("003001", "未通知"),

    SUCCESS("003002", "成功");

    private final String code;
    private final String desc;

    NotificationStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotificationStatus fromCode(String code) {
        for (NotificationStatus status : NotificationStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}