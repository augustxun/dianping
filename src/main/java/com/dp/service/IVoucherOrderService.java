package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.common.Result;
import com.dp.model.entity.VoucherOrder;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IVoucherOrderService extends IService<VoucherOrder> {
    /**
     * 实现秒杀下单
     * @param voucherId
     * @return
     */
    Result seckillVoucher(Long voucherId);
}
