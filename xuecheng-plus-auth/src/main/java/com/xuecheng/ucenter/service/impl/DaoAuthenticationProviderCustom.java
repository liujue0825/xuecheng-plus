package com.xuecheng.ucenter.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @author liujue
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {
    // 由于DaoAuthenticationProvider调用UserDetailsService，所以这里需要注入一个
    @Autowired
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService){
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // 屏蔽密码对比，因为不是所有的认证方式都需要校验密码
        // 取消继承即可
    }
}
