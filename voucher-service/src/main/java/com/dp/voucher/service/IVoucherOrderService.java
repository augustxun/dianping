package com.dp.voucher.service;

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

    Result seckillVoucher(Long voucherId);
    Result seckillVoucher1(Long voucherId);

    Result createVoucherOrder(Long voucherId, Long userId);
}
