package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.service.CheckCodeService;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 数字字母验证码生成器
 *
 * @author Mr.M
 */
@Component("NumberLetterCheckCodeGenerator")
public class NumberLetterCheckCodeGenerator implements CheckCodeService.CheckCodeGenerator {

    Random random = new Random();

    @Override
    public String generate(int length) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(36);
            stringBuffer.append(str.charAt(number));
        }
        return stringBuffer.toString();
    }
}
