package com.dp.shop.model.controller;


import com.dp.common.Result;
import com.dp.service.IFollowService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *

 */
@RestController
@RequestMapping("follow")
@Api(tags = "关注管理")
public class FollowController {
    @Autowired
    private IFollowService followService;

    /**
     * 关注博主
     * @param followUserId
     * @param isFollow
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    /**
     * 取消关注
     * @param followUserId
     * @return
     */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    /**
     * 查询共同关注
     * @param userId
     * @return
     */
    @GetMapping("common/{id}")
    public Result followCommons(@PathVariable("id") Long userId) {
        return followService.followCommons(userId);
    }

}
