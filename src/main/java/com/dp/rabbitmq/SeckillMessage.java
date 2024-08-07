package com.dp.rabbitmq;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消息体
 */
@Accessors(chain = true)
@Data
public class SeckillMessage {
    private Long id;
    private Long userId;
    private Long voucherId;
}
