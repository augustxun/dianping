package com.dp.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.dp.model.entity.VoucherOrder;
import com.dp.redis.RedisService;
import com.dp.service.ISeckillVoucherService;
import com.dp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.dp.constant.RedisConstants.SECKILL_STOCK_KEY;


@Service
@Slf4j
public class MQReceiver {
    @Resource
    private IVoucherOrderService voucherOrderService;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisService redisService;

    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void listenSeckillMsg(String msg) {
        System.out.println("消费者接收到 seckill.queue 的消息：" + msg);
        VoucherOrder voucherOrder = JSONUtil.toBean(msg, VoucherOrder.class);
        Long voucherId = voucherOrder.getVoucherId();
        // 1. 一人一单
        Long userId = voucherOrder.getUserId(); //  因为是异步下单，所以要从子线程中去取UserId
        Integer count = voucherOrderService
                .query()
                .eq("user_id", userId)
                .eq("voucher_id", voucherOrder.getVoucherId())
                .count();
        if (count > 0) {
            // 用户已经下单了
            log.error("用户已经购买过一次了");
        }
        boolean success = seckillVoucherService
                .update()
                .setSql("stock = stock - 1")
                .gt("stock", 0) // 乐观锁
                .eq("voucher_id", voucherId)
                .update();
        if (!success) {
            // 扣减失败，说明库存不足，将 redis 数据置为 0
            String stockKey = SECKILL_STOCK_KEY + voucherId;
            redisService.stringRedisTemplate.opsForValue().set(stockKey, "0");
            log.error("库存不足！");
        }
        // 7. 保存订单
        voucherOrderService.save(voucherOrder);
    }
}
