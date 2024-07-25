package com.dp;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.dp.model.entity.VoucherOrder;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    public void testJsonUtil() {
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setUserId(1010L).setVoucherId(10L).setId(11283971284612L);
        String jsonStr = JSONUtil.toJsonStr(voucherOrder);
        System.out.println(jsonStr);
        VoucherOrder voucherOrder1 = JSONUtil.toBean(jsonStr, VoucherOrder.class);
        System.out.println(voucherOrder1.toString());
    }
    @Test
    public void testRandom() {
        Long userId = Long.valueOf(RandomUtil.randomNumbers(6));
        System.out.println(userId);
    }
}
