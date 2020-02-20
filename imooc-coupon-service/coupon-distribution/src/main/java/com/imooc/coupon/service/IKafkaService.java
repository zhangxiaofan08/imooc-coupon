package com.imooc.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * kafka 相关的服务接口定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 15:26
 */
public interface IKafkaService {
    /**
     * 消费优惠券 kafka 消息
     *
     * @param record
     */
    void cousumerCouponKafkaMessage(ConsumerRecord<?, ?> record);
}
