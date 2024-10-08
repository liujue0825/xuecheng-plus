package com.xuecheng.config;

import lombok.Data;

import java.io.Serializable;

/**
 * 错误响应参数包装
 *
 * @author liujue
 */
@Data
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }
}
