package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPswDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;

/**
 * @author liujue
 * @version 1.0
 * @description: TODO
 * @date 2024/2/20 13:40
 */
public interface VerifyService {

    /**
     * 找回密码
     *
     * @param findPswDto 找回密码请求参数类
     */
    void findPassword(FindPswDto findPswDto);

    /**
     * 用户注册
     * @param registerDto 用户注册请求参数类
     */
    void register(RegisterDto registerDto);
}
