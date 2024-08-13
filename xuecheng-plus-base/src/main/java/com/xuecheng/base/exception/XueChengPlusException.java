

package com.xuecheng.base.exception;


import lombok.Getter;

/**
 * @author liujue
 * @description 学成在线项目异常类
 */
@Getter
public class XueChengPlusException extends RuntimeException {

    private final String errMessage;

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }
}
