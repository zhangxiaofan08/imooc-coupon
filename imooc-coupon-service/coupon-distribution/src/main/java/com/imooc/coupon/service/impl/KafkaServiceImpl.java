package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.service.IKafkaService;
import com.imooc.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Kafka相关的服务接口实现
 * 核心思想：是将Cache中的Coupon的状态变化同步到DB中
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 09:59
 */
@Slf4j
@Service
public class KafkaServiceImpl implements IKafkaService {

    private final CouponDao couponDao;

    @Autowired
    public KafkaServiceImpl(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    /**
     * 消费优惠券 kafka 消息
     *
     * @param record
     */
    @Override
    @KafkaListener(topics = {Constant.TOPIC}, groupId = "imooc-coupon-1")
    public void cousumerCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMsg = Optional.ofNullable(record.value());
        if (kafkaMsg.isPresent()) {
            Object msg = kafkaMsg.get();
            CouponKafkaMessage couponInfo = JSON.parseObject(msg.toString(), CouponKafkaMessage.class);

            log.info("Receive CouponKafkaMessage: {}", kafkaMsg.toString());

            CouponStatus status = CouponStatus.of(couponInfo.getStatus());
            switch (status) {
                case USABLE:
                    break;
                case USED:
                    processUsedCoupons(couponInfo);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo);
                    break;
            }

        }
    }

    /**
     * 处理已使用的优惠券
     *
     * @param kafkaMessage
     */
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage) {
        // TODO: 2020-02-19 给用户发送短信提醒
        processCouponsByStatus(kafkaMessage, CouponStatus.USED);
    }

    /**
     * 处理过期的用户优惠券
     *
     * @param kafkaMessage
     */
    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage) {
        // TODO: 2020-02-19 给用户发送推送提醒
        processCouponsByStatus(kafkaMessage, CouponStatus.EXPIRED);
    }

    /**
     * 根据状态处理优惠券信息
     *
     * @param kafkaMessage
     * @param couponStatus
     */
    private void processCouponsByStatus(CouponKafkaMessage kafkaMessage, CouponStatus couponStatus) {
        List<Coupon> couponList = couponDao.findAllById(kafkaMessage.getIdList());
        if (CollectionUtils.isEmpty(couponList) || couponList.size() != kafkaMessage.getIdList().size()) {
            log.error("Cannot Find Right Coupon Info: {}", JSON.toJSONString(kafkaMessage));
            // TODO: 2020-02-19 发送错误邮件
            return;
        }

        couponList.forEach(coupon -> coupon.setStatus(couponStatus));
        log.info("CouponKafkaMessage Op Coupon Count: {}", couponDao.saveAll(couponList).size());
    }
}
