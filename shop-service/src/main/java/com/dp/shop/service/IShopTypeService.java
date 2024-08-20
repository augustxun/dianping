package com.dp.shop.service;

import com.dp.common.Result;
import com.dp.model.entity.ShopType;
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
