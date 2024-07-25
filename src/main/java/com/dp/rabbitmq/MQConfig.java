package com.dp.rabbitmq;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_KEY = "seckill.#";
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(SECKILL_EXCHANGE);
    }
    @Bean
    public Queue topicQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    // 绑定交换机和队列
    @Bean
    public Binding topicBinding() {
        return BindingBuilder
                .bind(topicQueue())
                .to(topicExchange())
                .with(SECKILL_KEY);
    }
}
