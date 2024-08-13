package com.xuecheng.base.constants;

import lombok.Getter;

/**
 * @author liujue
 * @version 1.0
 * @description 选课学习资格
 * @since 2024/7/27
 */
@Getter
public enum CourseLearningEligibility {

    ELIGIBLE("702001", "正常学习"),

    NOT_SELECTED_OR_UNPAID("702002", "没有选课或选课后没有支付"),

    EXPIRED_RENEWAL_REQUIRED("702003", "已过期需要申请续期或重新支付");

    private final String code;
    private final String desc;

    CourseLearningEligibility(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseLearningEligibility fromCode(String code) {
        for (CourseLearningEligibility eligibility : CourseLearningEligibility.values()) {
            if (eligibility.code.equals(code)) {
                return eligibility;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

