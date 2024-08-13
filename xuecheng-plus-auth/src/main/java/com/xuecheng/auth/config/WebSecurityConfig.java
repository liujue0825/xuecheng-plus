package com.xuecheng.auth.config;

import com.xuecheng.ucenter.service.impl.DaoAuthenticationProviderCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置类
 *
 * @author liujue
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }

    /**
     * 配置认证管理 Bean
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置密码加密方式
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 将用户输入的密码编码为 BCrypt 格式与数据库中的密码进行比对
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全拦截机制
     *
     * @param http http 请求
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 通过 authorizeRequests() 方法来配置请求授权规则。
        http.authorizeRequests()
                // 使用 antMatchers() 方法指定需要进行访问控制的 URL 路径模式。在这里，/r/** 表示所有以 /r/ 开头的 URL 都需要进行授权访问。
                .antMatchers("/r/**")
                // 使用 authenticated() 方法指定需要进行身份验证的请求。
                .authenticated()
                // 使用 anyRequest() 方法配置除了 /r/** 以外的所有请求都不需要进行身份验证。
                .anyRequest()
                // 使用 permitAll() 方法表示任何用户都可以访问不需要进行身份验证的 URL。
                .permitAll()
                // 使用 and() 方法开启新一轮的配置
                .and()
                // 使用 formLogin() 方法配置登录页表单认证，其中 successForwardUrl() 方法指定登录成功后的跳转页面。
                .formLogin()
                .successForwardUrl("/login-success");
        // 使用 logout() 方法配置退出登录，其中 logoutUrl() 方法指定退出登录的 URL。
        http.logout().logoutUrl("/logout");
    }
}
