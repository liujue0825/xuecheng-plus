package com.xuecheng.ucenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 验证码服务接口
 *
 * @author liujue
 */
@FeignClient(value = "checkcode")
public interface CheckCodeClient {

    /**
     * 验证码校验接口
     */
    @PostMapping(value = "/checkcode/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);
}
