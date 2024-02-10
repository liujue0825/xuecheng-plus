package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应用户的统一类型
 * @author liujue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestErrorResponse implements Serializable {
    private String errMessage;

}