package com.imooc.coupon.dao;

import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * coupon dao 定义
 *
 * @AUTHOR zhangxf
 * @CREATE 2020-02-14 14:54
 */
public interface CouponDao extends JpaRepository<Coupon, Integer> {

    /**
     * 根据 userId 和 状态 查询优惠券的记录
     * where userId = ... and status = ...
     * @param userId
     * @param status
     * @return
     */
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
