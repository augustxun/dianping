package com.dp.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_ROUTINGKEY = "seckill.#";

    /**
     * 声明队列
     * @return
     */
    @Bean
    public Queue topicQueue1() {
        return new Queue(SECKILL_QUEUE);
    }

    /**
     * 声明交换机
     * @return
     */
    @Bean
    public Exchange topicExchange() {
        return new TopicExchange(SECKILL_EXCHANGE);
    }

    /**
     * 绑定交换机和队列
     * @return
     */
    @Bean
    public Binding topicBinding() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(SECKILL_ROUTINGKEY).noargs();
    }
}
