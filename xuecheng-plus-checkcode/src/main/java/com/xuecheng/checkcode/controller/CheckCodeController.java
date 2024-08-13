package com.xuecheng.checkcode.controller;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.CheckCodeService;
import com.xuecheng.checkcode.service.SendCodeService;
import com.xuecheng.checkcode.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码服务接口
 *
 * @author liujue
 */
@RestController
public class CheckCodeController {

    private final CheckCodeService picCheckCodeService;

    private final SendCodeService sendCodeService;

    @Autowired
    public CheckCodeController(CheckCodeService picCheckCodeService, SendCodeService sendCodeService) {
        this.picCheckCodeService = picCheckCodeService;
        this.sendCodeService = sendCodeService;
    }

    /**
     * 生成验证码图片接口
     */
    @PostMapping(value = "/pic")
    public CheckCodeResultDto generatePicCheckCode(CheckCodeParamsDto checkCodeParamsDto) {
        return picCheckCodeService.generate(checkCodeParamsDto);
    }

    /**
     * 验证码校验接口
     */
    @PostMapping(value = "/verify")
    public Boolean verify(String key, String code) {
        return picCheckCodeService.verify(key, code);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/phone")
    public void sendEmail(@RequestParam("param1") String email) {
        String code = MailUtil.achieveCode();
        sendCodeService.sendEmail(email, code);
    }
}
