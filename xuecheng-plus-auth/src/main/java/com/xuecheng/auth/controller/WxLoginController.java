package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信登录相关业务接口
 *
 * @author liujue
 */
@Slf4j
@RestController
public class WxLoginController {
    @Autowired
    private WxAuthServiceImpl wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        XcUser xcUser = wxAuthService.wxAuth(code);
        if (xcUser == null) {
            return "redirect:http://localhost/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://localhost/sign.html?username=" + username + "&authType=wx";
    }
}
