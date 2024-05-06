package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.model.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IShopTypeService extends IService<ShopType> {
    Result getTypeList();
}
