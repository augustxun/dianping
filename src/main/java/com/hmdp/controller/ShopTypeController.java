package com.hmdp.controller;


import com.hmdp.common.Result;
import com.hmdp.service.IShopTypeService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *

 */
@RestController
@RequestMapping("/shop-type")
@Api(tags = "商户种类管理")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.getTypeList();
    }
}
