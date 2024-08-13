package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 支付记录交易状态
 * @since 2024/7/27
 */
@Getter
public enum PaymentTransactionStatus {

    NOT_PAID("601001", "未支付"),

    PAID("601002", "已支付"),

    REFUNDED("601003", "已退款");

    private final String code;
    private final String desc;

    PaymentTransactionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentTransactionStatus fromCode(String code) {
        for (PaymentTransactionStatus status : PaymentTransactionStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}