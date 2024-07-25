package com.dp.controller;


import com.dp.common.Result;
import com.dp.service.IVoucherOrderService;
import com.google.common.util.concurrent.RateLimiter;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *

 */
@RestController
@RequestMapping("/voucher-order")
@Api(tags = "优惠券订单管理")
public class VoucherOrderController {
    @Resource
    private IVoucherOrderService voucherOrderService;

    // 基于令牌桶算法限流
     private RateLimiter rateLimiter = RateLimiter.create(10000);

    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            return Result.fail("访问高峰期，请稍等！");
        }
        return voucherOrderService.seckillVoucher1(voucherId);
    }
}
