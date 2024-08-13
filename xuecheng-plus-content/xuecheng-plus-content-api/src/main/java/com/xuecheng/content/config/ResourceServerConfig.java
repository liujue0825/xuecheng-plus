package com.xuecheng.content.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 资源服务器配置类
 *
 * @author liujue
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    public static final String RESOURCE_ID = "xuecheng-plus";

    @Autowired
    TokenStore tokenStore;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(RESOURCE_ID)
                .tokenStore(tokenStore)
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 禁用 CSRF 保护
        http.csrf().disable()
                // 配置对请求的授权策略
                .authorizeRequests()
                // 指定 "/r/" 和 "/course/" 这两个路径需要进行身份认证才能访问。
                // .antMatchers("/r/**", "/course/**")
                // .authenticated()
                // 允许所有其他请求（除了上面指定的路径之外）都可以被访问，不需要进行身份认证。
                .anyRequest()
                .permitAll();
    }
}
