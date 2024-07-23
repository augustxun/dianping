package com.hmdp.rabbitmq;

import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.hmdp.config.RabbitMQConfig.SECKILL_QUEUE;

@Service
@Slf4j
public class MQReceiver {
    @Resource
    private IVoucherOrderService voucherOrderService;
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @RabbitListener(queues = SECKILL_QUEUE)
    public void listenSeckillMessage(String msg) {
        System.out.println("消费者接收到seckill.queue的消息：" + msg);
    }
}
