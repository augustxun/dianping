package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.model.entity.VoucherOrder;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);
    void createVoucherOrder(VoucherOrder voucherOrder);
}
