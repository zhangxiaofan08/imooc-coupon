package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;

/**
 * 异步服务接口定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-12 16:56
 */
public interface IAsyncService {
    /**
     * 根据模版异步的创建优惠券码
     * @param template
     */
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
