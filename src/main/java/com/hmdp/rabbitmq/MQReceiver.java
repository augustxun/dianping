package com.hmdp.rabbitmq;

import com.hmdp.config.MQConfig;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MQReceiver {
    @Resource
    private IVoucherOrderService voucherOrderService;
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void listenSeckillMessage(String msg) {

    }
}
