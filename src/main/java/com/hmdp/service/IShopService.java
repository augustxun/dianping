package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.model.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);
    Result update(Shop shop);
}
