package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindPswDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.VerifyService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 登录业务相关接口
 *
 * @author liujue
 */
@Slf4j
@RestController
public class LoginController {
    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    VerifyService verifyService;

    @RequestMapping("/login-success")
    public String loginSuccess() {
        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public XcUser getUser(@PathVariable("id") String id) {
        return xcUserMapper.selectById(id);
    }

    @RequestMapping("/r/r1")
    @PreAuthorize("hasAnyAuthority('p1')")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    @PreAuthorize("hasAnyAuthority('p2')")
    public String r2() {
        return "访问r2资源";
    }

    /**
     * 找回密码
     */
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPswDto findPswDto) {
        verifyService.findPassword(findPswDto);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public void register(@RequestBody RegisterDto registerDto) {
        verifyService.register(registerDto);
    }
}
