package com.dp.rabbitmq;

import cn.hutool.core.util.StrUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MQSender {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendSeckillMsg(SeckillMessage seckillMessage) {
        String msg = StrUtil.toString(seckillMessage);
        String key = "seckill.msg";
        rabbitTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, key, msg);
    }
}
