package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 微信扫码验证方式
 *
 * @author liujue
 */
@Slf4j
@Service("wx_auth")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    /**
     * 应用唯一标识
     */
    @Value("${weixin.appid}")
    private String appid;

    /**
     * 应用密钥AppSecret
     */
    @Value("${weixin.secret")
    private String secret;

    @Autowired
    private RestTemplate restTemplate;
    @Resource
    XcUserMapper xcUserMapper;
    @Resource
    XcUserRoleMapper xcUserRoleMapper;
    @Resource
    private WxAuthServiceImpl proxy;

    @Override
    public XcUser wxAuth(String code) {
        // 1. 从微信接口获取令牌
        Map<String, String> accessTokenMap = getAccessToken(code);
        String accessToken = accessTokenMap.get("access_token");
        // 2. 从微信接口获取用户信息
        String openid = accessTokenMap.get("openid");
        Map<String, String> userInfoMap = getUserInfo(accessToken, openid);
        // 3. 将用户信息保存到数据库中
        // 非事务方法调用事务方法，要使用代理对象调用
        return proxy.addWxUser(userInfoMap);
    }

    /**
     * 从微信接口获取令牌
     *
     * @param code code
     * @return 接口调用凭证
     */
    private Map<String, String> getAccessToken(String code) {
        // 1. 请求路径模板，参数用%s占位符
        String urlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 2. 填充占位符：appid，secret，code
        String url = String.format(urlTemplate, appid, secret, code);
        // 3. 远程调用URL，POST方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 4. 获取相应结果，响应结果为json格式
        String result = exchange.getBody();
        // 5. 转为map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    /**
     * 从微信接口获取用户信息
     *
     * @param accessToken 接口调用凭证
     * @param openid      授权用户唯一标识
     * @return 用户信息
     */
    private Map<String, String> getUserInfo(String accessToken, String openid) {
        // 1. 请求路径模板，参数用%s占位符
        String urlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        // 2. 填充占位符，access_token和openid
        String url = String.format(urlTemplate, accessToken, openid);
        // 3. 远程调用URL，GET方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // 4. 获取相应结果，响应结果为json格式
        String result = exchange.getBody();
        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // 5. 转为map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    /**
     * 将用户信息保存到数据库中,如果不存在则新增
     *
     * @param userInfoMap userInfoMap
     * @return 用户信息
     */
    @Transactional
    public XcUser addWxUser(Map<String, String> userInfoMap) {
        // 1. 根据用户 unionID 信息查询用户信息
        String unionid = userInfoMap.get("unionid");
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        // 2. 存在返回，不存在则新增
        if (xcUser != null) {
            return xcUser;
        }
        // 3. 新增用户，设置需要的值
        xcUser = new XcUser();
        String uuid = UUID.randomUUID().toString();
        xcUser.setId(uuid);
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(userInfoMap.get("nickname"));
        xcUser.setUserpic(userInfoMap.get("headimgurl"));
        xcUser.setName(userInfoMap.get("nickname"));
        // 学生类型
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        // 4. 保存到数据库
        xcUserMapper.insert(xcUser);
        // 5. 添加用户信息到用户角色表
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(uuid);
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }

    /**
     * 用户认证
     * 微信扫码不需要校验密码和验证码,只需要查找数据库看有没有该用户即可
     *
     * @param authParamsDto 用户认证请求类,包含认证参数
     * @return 用户信息
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }
        XcUserExt result = new XcUserExt();
        BeanUtils.copyProperties(xcUser, result);
        return result;
    }
}
