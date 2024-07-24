package com.dp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.common.Result;
import com.dp.mapper.VoucherOrderMapper;
import com.dp.model.entity.VoucherOrder;
import com.dp.rabbitmq.MQSender;
import com.dp.rabbitmq.SeckillMessage;
import com.dp.service.IVoucherOrderService;
import com.dp.redis.RedisService;
import com.dp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    // 本地缓存，判断该商品是否被处理过了
    private final HashMap<Long, Boolean> localOverMap = new HashMap<>();
    @Resource
    private RedisService redisService;
    @Resource
    private RedissonClient redissonClient;
    // 消息发送方
    @Resource
    private MQSender mqSender;
    private IVoucherOrderService proxy;


    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        // 1. 获取用户
        Long userId = voucherOrder.getUserId();
        // 2. 创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 3. 获取锁
        boolean isLock = lock.tryLock();
        if (!isLock) {
            // 获取锁失败，返回错误或重试
            log.error("不允许重复下单");
        }
        try {
            //            proxy.createVoucherOrder(voucherOrder); // 在接口中声明后，此时事务可以生效
        } finally {
            lock.unlock();
        }
    }

    /**
     * 三级缓冲
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 一级缓冲：本地缓存检查
        boolean over = localOverMap.get(voucherId);
        if (over) {
            return Result.fail("优惠券已秒杀完毕");
        }
        // 1. 获取当前下单用户的 Id
        Long userId = UserHolder.getUser().getId();
        // 二级缓冲：执行 lua 脚本（库存预减）, 根据返回值判定用户是否具备下单资格
        Long result = redisService.stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString());
        // 4. 判断结果是否为0，结果为 0 才可以下单
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 2. 生成订单id
        long orderId = redisService.nextId("order");
        // 创建秒杀消息并放入队列
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setOrderId(orderId).setUserId(userId).setVoucherId(voucherId);
        mqSender.sendSeckillMsg(seckillMessage);
        // 5. 返回订单 Id
        return Result.ok(orderId);
    }
}
