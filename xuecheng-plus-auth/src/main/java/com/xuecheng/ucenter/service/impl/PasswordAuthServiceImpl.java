package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 账号密码验证方式
 *
 * @author liujue
 */
@Slf4j
@Service("password_auth")
public class PasswordAuthServiceImpl implements AuthService {
    @Resource
    private XcUserMapper xcUserMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private CheckCodeClient checkCodeClient;

    /**
     * 用户认证
     *
     * @param authParamsDto 用户认证请求类,包含认证参数
     * @return 用户信息
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 1. 校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey)) {
            throw new RuntimeException("验证码为空");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }
        // 2. 校验用户账号密码
        String username = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }
        // 用户表单的密码
        String passwordForm = authParamsDto.getPassword();
        // 数据库中的用户密码
        String passwordDb = xcUser.getPassword();
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        // 如果校验失败抛出异常
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt result = new XcUserExt();
        BeanUtils.copyProperties(xcUser, result);
        return result;
    }
}
