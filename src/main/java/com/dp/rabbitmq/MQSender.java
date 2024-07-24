package com.dp.rabbitmq;

import com.dp.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MQSender {
    private static final String ROUTINGKEY = "seckill.message";
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀信息
     *
     * @param msg
     */
    public void sendSeckillMessage(String msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE, ROUTINGKEY, msg);
    }
}
