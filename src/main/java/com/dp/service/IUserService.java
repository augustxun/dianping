package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.common.Result;
import com.dp.constant.dto.LoginFormDTO;
import com.dp.model.entity.User;

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
     * @return
     */
    Result sendCode(String phone);

    /**
     * 登陆和注册功能
     * @param loginForm
     * @return
     */
    Result login(LoginFormDTO loginForm);

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
