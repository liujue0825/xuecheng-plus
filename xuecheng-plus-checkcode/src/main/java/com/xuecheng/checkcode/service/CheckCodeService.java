package com.xuecheng.checkcode.service;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
/**
 * 验证码业务接口
 *
 * @author Mr.M
 */
public interface CheckCodeService {

    /**
     * 生成验证码
     *
     * @param checkCodeParamsDto 生成验证码参数类
     * @return 验证码结果类
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     * 校验验证码
     *
     * @param key  密钥
     * @param code 验证码
     * @return 成功/失败
     */
    boolean verify(String key, String code);

    /**
     * 验证码生成器
     */
    interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @return 验证码
         */
        String generate(int length);
    }

    /**
     * 密钥生成器
     */
    interface KeyGenerator {

        /**
         * 密钥生成
         *
         * @return 密钥
         */
        String generate(String prefix);
    }


    /**
     * 验证码存储接口
     */
    interface CheckCodeStore {

        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
