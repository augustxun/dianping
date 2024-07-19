package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.constant.dto.LoginFormDTO;
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
    /**
     * 发送验证码并验证
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 登陆和注册功能
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    /**
     * 用户签到
     * @return
     */
    Result sign();

    /**
     * 用户签到统计
     * @return
     */
    Result signCount();
}
