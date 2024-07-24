package com.dp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Test
    public void testSendMessage2TopicQueue() {
        String exchangeName = "seckill.exchange";
        String message = "hello, this is 1st order";
        // 发送消息
        rabbitTemplate.convertAndSend(exchangeName, "seckill.1",message);
    }
}
