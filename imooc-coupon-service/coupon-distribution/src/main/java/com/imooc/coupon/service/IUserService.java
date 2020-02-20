package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * 用户服务的相关接口定义
 * 1、用户三类状态优惠券信息展示服务
 * 2、查看用户当前可用领取的优惠券模版 - coupon-template 微服务配合实现
 * 3、用户领取优惠券服务
 * 4、用户消费优惠券服务 - coupon-settlement 微服务配合实现
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 15:29
 */
public interface IUserService {

    /**
     * 根据用户ID和状态查询优惠券记录
     *
     * @param userId
     * @param status
     * @return
     * @throws CouponException
     */
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;

    /**
     * 根据用户 id 查找当前可以领取的优惠券模版
     *
     * @param userId
     * @return
     * @throws CouponException
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     * 用户领取优惠券
     *
     * @param request
     * @return
     * @throws CouponException
     */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /**
     * 结算（核销）优惠券
     *
     * @param info
     * @return
     * @throws CouponException
     */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
