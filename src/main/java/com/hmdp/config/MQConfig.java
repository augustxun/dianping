package com.hmdp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;

public class MQConfig {
    public static final String SECKILL_QUEUE = "seckillQueue";
    public static final String SECKILL_EXCHANGE = "seckillExchange";
    public static final String ROUTINGKEY = "seckill.#";
    @Bean
    public Queue queue(){
        return new Queue(SECKILL_QUEUE);
    }
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(SECKILL_EXCHANGE);
    }
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with(ROUTINGKEY);
    }
}
