package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.model.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IFollowService extends IService<Follow> {
    /**
     * 关注博主
     * @param followUserId
     * @param isFollow
     * @return
     */
    Result follow(Long followUserId, Boolean isFollow);

    /**
     * 取消关注
     * @param followUserId
     * @return
     */
    Result isFollow(Long followUserId);

    /**
     * 共同关注
     * @param id
     * @return
     */
    Result followCommons(Long id);
}
