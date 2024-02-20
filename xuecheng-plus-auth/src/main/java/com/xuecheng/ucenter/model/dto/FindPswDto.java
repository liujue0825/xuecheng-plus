package com.xuecheng.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 找回密码请求参数类
 *
 * @author liujue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindPswDto {

    String cellphone;

    String email;

    String checkcodekey;

    String checkcode;

    String password;

    String confirmpwd;
}
