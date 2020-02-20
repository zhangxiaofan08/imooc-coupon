package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;

import java.util.List;

/**
 * redis 相关操作服务接口定义
 * 1、用户的三个状态的优惠券
 * 2、优惠券模版生成的优惠券码 Cache 操作
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 15:03
 */
public interface IRedisService {

    /**
     * 根据 suerId 和 状态 找到缓存的优惠券列表数据
     *
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     */
    List<Coupon> getCachedCoupons(Long userId, Integer status);

    /**
     * 保存空的优惠券列表到缓存中
     *
     * @param userId
     * @param statusList
     */
    void saveEmptyCouponListToCache(Long userId, List<Integer> statusList);

    /**
     * 尝试从cache中获取优惠券码
     *
     * @param templateId 优惠券模版主键
     * @return 优惠券码
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     * 将优惠券保存到cache中
     *
     * @param userId
     * @param couponList
     * @param status
     * @return 保存成功的个数
     * @throws CouponException
     */
    Integer addCouponToCache(Long userId, List<Coupon> couponList, Integer status) throws CouponException;

}
