package com.dp.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MQSender {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendSeckillMsg(SeckillMessage seckillMessage) {
        String msg = JSONUtil.toJsonStr(seckillMessage);
        log.debug("消费者放入消息:" + msg);
        String key = "seckill.msg";
        rabbitTemplate.convertAndSend(MQConfig.SECKILL_EXCHANGE, key, msg);
    }
}
