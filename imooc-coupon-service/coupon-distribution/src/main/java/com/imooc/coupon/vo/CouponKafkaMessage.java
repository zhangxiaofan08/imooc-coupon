package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 优惠券Kafka消息对象定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-19 10:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponKafkaMessage {
    /**
     * 优惠券状态
     */
    private Integer status;

    /**
     * Coupon主键
     */
    private List<Integer> idList;
}
