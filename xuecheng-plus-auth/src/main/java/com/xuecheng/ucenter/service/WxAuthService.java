package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author liujue
 */
public interface WxAuthService {

    /**
     * 微信授权
     * @param code
     * @return
     */
    XcUser wxAuth(String code);
}
