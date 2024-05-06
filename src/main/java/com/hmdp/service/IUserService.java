package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.model.dto.LoginFormDTO;
import com.hmdp.common.Result;
import com.hmdp.model.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
