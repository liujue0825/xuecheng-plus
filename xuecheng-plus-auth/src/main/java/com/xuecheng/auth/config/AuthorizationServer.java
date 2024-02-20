package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import javax.annotation.Resource;

/**
 * 授权服务器配置类
 *
 * @author liujue
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Resource(name = "authorizationServerTokenServicesCustom")
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 客户端详情服务配置
     *
     * @param clients 用来配置客户端详情服务
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 使用 in-memory 存储
        clients.inMemory()
                // client_id
                .withClient("XcWebApp")
                // 客户端密钥, 进行 BCrypt 格式转换
                .secret(new BCryptPasswordEncoder().encode("XcWebApp"))
                // 资源列表
                .resourceIds("xuecheng-plus")
                // 该 client 允许的授权类型 authorization_code,password,refresh_token,implicit,client_credentials
                .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                // 允许的授权范围
                .scopes("all")
                // false跳转到授权页面
                .autoApprove(false)
                // 客户端接收授权码的重定向地址
                .redirectUris("http://localhost/");
    }

    /**
     * 令牌端点的访问配置
     *
     * @param endpoints 用来配置令牌（token）的访问端点和令牌服务（token services）
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager)// 认证管理器
                .tokenServices(authorizationServerTokenServices)// 令牌管理服务
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    /**
     * 令牌端点的安全配置
     *
     * @param security 用来配置令牌端点的安全约束
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.tokenKeyAccess("permitAll()")                    // oauth/token_key是公开
                .checkTokenAccess("permitAll()")                  // oauth/check_token公开
                .allowFormAuthenticationForClients();               // 表单认证（申请令牌）
    }

}
