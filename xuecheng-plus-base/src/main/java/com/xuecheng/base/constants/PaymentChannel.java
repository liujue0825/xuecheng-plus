package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 第三方支付渠道编号
 * @since 2024/7/27
 */
@Getter
public enum PaymentChannel {

    WECHAT("603001", "微信支付"),

    ALIPAY("603002", "支付宝");

    private final String code;
    private final String desc;

    PaymentChannel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentChannel fromCode(String code) {
        for (PaymentChannel channel : PaymentChannel.values()) {
            if (channel.code.equals(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

