package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * 认证相关业务
 *
 * @author liujue
 */
public interface AuthService {
    /**
     * 用户认证
     *
     * @param authParamsDto 用户认证请求类,包含认证参数
     * @return 用户信息
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
