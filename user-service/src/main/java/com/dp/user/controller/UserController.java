package com.dp.user.controller;


import cn.hutool.core.bean.BeanUtil;
import com.dp.common.domain.Result;
import com.dp.user.context.UserHolder;
import com.dp.user.model.dto.UserDTO;
import com.dp.user.model.entity.User;
import com.dp.user.model.entity.UserInfo;
import com.dp.user.model.vo.UserVO;
import com.dp.user.service.IUserInfoService;
import com.dp.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *

 */
@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    @ApiOperation(value = "发送验证码")
    public Result sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param userDTO，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO){
        // 实现登录功能
        return userService.login(userDTO);
    }

    /**
     * 登出功能
     * @return
     */
    @PostMapping("/logout")
    public Result logout(){
        return Result.ok("退出成功");
    }

    /**
     * 获取当前登录的用户并返回
     * @return
     */
    @GetMapping("/me")
    public Result me(){
        UserVO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * 根据 id 查询用户
     * @param userId
     * @return
     */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        // 返回
        return Result.ok(userVO);
    }

    /**
     * 用户签到
     * @return
     */
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    /**
     * 签到统计
     * @return
     */
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}
