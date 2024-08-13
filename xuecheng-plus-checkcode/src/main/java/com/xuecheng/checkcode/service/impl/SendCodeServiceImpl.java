package com.xuecheng.checkcode.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.checkcode.service.SendCodeService;
import com.xuecheng.checkcode.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.concurrent.TimeUnit;

/**
 * @author liujue
 */
@Slf4j
@Service
public class SendCodeServiceImpl implements SendCodeService {
    public static final Long CODE_TTL = 120L;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 向目标邮箱发送验证码
     *
     * @param email 目标邮箱
     * @param code  我们发送的验证码
     */
    @Override
    public void sendEmail(String email, String code) {
        // 1. 向目标邮箱发送验证码
        try {
            MailUtil.sendTestMail(email, code);
        } catch (MessagingException e) {
            log.debug("邮件发送失败：{}", e.getMessage());
            XueChengPlusException.cast("发送验证码失败，请稍后再试");
        }
        // 2. 将验证码缓存到 Redis 中
        redisTemplate.opsForValue().set(email, code, CODE_TTL, TimeUnit.SECONDS);
    }
}
