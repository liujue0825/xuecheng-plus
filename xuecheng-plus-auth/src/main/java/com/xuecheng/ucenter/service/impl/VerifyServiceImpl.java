package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPswDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author liujue
 */
@Slf4j
@Service
public class VerifyServiceImpl implements VerifyService {
    @Resource
    private StringRedisTemplate redisTemplate;
    @Resource
    private XcUserMapper xcUserMapper;
    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    /**
     * 找回密码
     * 1. 校验验证码，不一致则抛异常
     * 2. 判断两次密码是否一致，不一致则抛异常
     * 3. 根据邮箱查询用户
     * 4. 如果找到用户，更新其密码
     *
     * @param findPswDto 找回密码请求参数类
     */
    @Override
    public void findPassword(FindPswDto findPswDto) {
        String email = findPswDto.getEmail();
        String checkcode = findPswDto.getCheckcode();
        Boolean verify = verifyCode(email, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, email);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            throw new RuntimeException("用户不存在");
        }
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUserMapper.updateById(xcUser);
    }

    /**
     * 用户注册
     * 1. 校验验证码，不一致，抛异常
     * 2. 校验两次密码是否一致，不一致，抛异常
     * 3. 校验用户是否存在，已存在，抛异常
     * 4. 向用户表、用户关系角色表添加数据，角色为学生
     *
     * @param registerDto 用户注册请求参数类
     */
    @Transactional
    @Override
    public void register(RegisterDto registerDto) {
        String uuid = UUID.randomUUID().toString();
        String email = registerDto.getEmail();
        String checkcode = registerDto.getCheckcode();
        Boolean verify = verifyCode(email, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, email);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser != null) {
            throw new RuntimeException("用户已存在");
        }
        xcUser = new XcUser();
        BeanUtils.copyProperties(registerDto, xcUser);
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUser.setId(uuid);
        // 学生类型
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setName(registerDto.getNickname());
        xcUser.setCreateTime(LocalDateTime.now());
        int row1 = xcUserMapper.insert(xcUser);
        if (row1 <= 0) {
            throw new RuntimeException("新增用户信息失败");
        }
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(uuid);
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        int row2 = xcUserRoleMapper.insert(xcUserRole);
        if (row2 <= 0) {
            throw new RuntimeException("新增用户角色信息失败");
        }
    }

    /**
     * 校验验证码
     *
     * @param email     邮箱
     * @param checkcode 验证码
     * @return 成功/失败
     */
    private Boolean verifyCode(String email, String checkcode) {
        // 1. 从redis中获取缓存的验证码
        String codeInRedis = redisTemplate.opsForValue().get(email);
        // 2. 判断是否与用户输入的一致
        if (checkcode.equalsIgnoreCase(codeInRedis)) {
            redisTemplate.delete(email);
            return true;
        }
        return false;
    }

}
