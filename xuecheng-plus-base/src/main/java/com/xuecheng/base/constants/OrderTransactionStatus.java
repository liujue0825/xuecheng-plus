package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 订单交易类型状态
 * @since 2024/7/27
 */
@Getter
public enum OrderTransactionStatus {

    NOT_PAID("600001", "未支付"),

    PAID("600002", "已支付"),

    CLOSED("600003", "已关闭"),

    REFUNDED("600004", "已退款"),

    COMPLETED("600005", "已完成");

    private final String code;
    private final String desc;

    OrderTransactionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderTransactionStatus fromCode(String code) {
        for (OrderTransactionStatus status : OrderTransactionStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
