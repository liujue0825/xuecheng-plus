package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liujue
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    ApplicationContext applicationContext;
    @Resource
    XcMenuMapper xcMenuMapper;

    /**
     * 根据用户账号获取用户信息,根据不同认证类型采用对应的认证方式
     *
     * @param account 用户输入的登录账号
     * @return 用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = new AuthParamsDto();
        try {
            authParamsDto = JSON.parseObject(account, AuthParamsDto.class);
        } catch (Exception e) {
            log.error("认证请求数据格式不对：{}", account);
            throw new RuntimeException("认证请求数据格式不对");
        }
        String authType = authParamsDto.getAuthType();
        // 根据认证类型通过 beanName 的方式获取对应实现类
        AuthService authService = applicationContext.getBean(authType + "_auth", AuthService.class);
        // 认证逻辑
        XcUserExt xcUser = authService.execute(authParamsDto);
        return getUserPrincipal(xcUser);
    }

    /**
     * 如果查到了用户拿到正确的密码，最终封装成一个 UserDetails 对象给 Spring Security 框架返回，由框架进行密码比对
     *
     * @param user 用户信息
     * @return UserDetails 对象
     */
    public UserDetails getUserPrincipal(XcUserExt user) {
        String userId = user.getId();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        List<String> permissions = new ArrayList<>();
        if (CollectionUtils.isEmpty(xcMenus)) {
            permissions.add("test");
        } else {
            xcMenus.forEach(xcMenu -> permissions.add(xcMenu.getCode()));
        }
        user.setPermissions(permissions);
        String[] authorities = permissions.toArray(new String[0]);
        String password = user.getPassword();
        user.setPassword(null);
        String userJsonString = JSON.toJSONString(user);
        return User.withUsername(userJsonString).password(password).authorities(authorities).build();
    }
}
